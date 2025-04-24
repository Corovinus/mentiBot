package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gargoylesoftware.htmlunit.DefaultCssErrorHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import javax.swing.*;
import java.awt.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Refactored Menti Bot application with structured service layer, robust error handling,
 * and console timing output for each stage of the fetch process.
 */
public class MentiBotApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(MentiBotApp::createAndShowGui);
    }

    private static void createAndShowGui() {
        JFrame frame = new JFrame("Menti Bot");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Enter code to join:"));
        JTextField idField = new JTextField(10);
        top.add(idField);
        JButton fetchBtn = new JButton("Fetch Questions");
        top.add(fetchBtn);

        JTextArea output = new JTextArea();
        output.setEditable(false);
        JScrollPane scroll = new JScrollPane(output);

        frame.add(top, BorderLayout.NORTH);
        frame.add(scroll, BorderLayout.CENTER);
        frame.setVisible(true);

        fetchBtn.addActionListener(e -> {
            String deckId = idField.getText().trim();
            if (deckId.isEmpty()) {
                JOptionPane.showMessageDialog(frame,
                        "Please enter a slide-deck ID.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            fetchBtn.setEnabled(false);
            output.setText("Loading…");

            new SwingWorker<List<Question>, Void>() {
                @Override
                protected List<Question> doInBackground() throws Exception {
                    return MentiService.fetchQuestions(deckId);
                }

                @Override
                protected void done() {
                    try {
                        List<Question> questions = get();
                        if (questions.isEmpty()) {
                            output.setText("No questions found.");
                        } else {
                            StringBuilder sb = new StringBuilder();
                            int idx = 1;
                            for (Question q : questions) {
                                sb.append("Вопрос ").append(idx++).append(": \"")
                                        .append(q.getText()).append("\"\n");
                                int i = 1;
                                for (String choice : q.getChoices()) {
                                    sb.append(i++).append(". ").append(choice).append("\n");
                                }
                                sb.append("\n");
                            }
                            output.setText(sb.toString());
                        }
                    } catch (Exception ex) {
                        output.setText("Error: " + ex.getMessage());
                        ex.printStackTrace();
                    } finally {
                        fetchBtn.setEnabled(true);
                    }
                }
            }.execute();
        });
    }

    static class MentiService {
        public static List<Question> fetchQuestions(String deckId) throws Exception {
            long t0 = System.nanoTime();
            System.out.println("Starting fetchQuestions for deckId: " + deckId);

            long startKey = System.nanoTime();
            String key = ParticipationKeyFetcher.fetchParticipationKey(deckId);
            long endKey = System.nanoTime();
            System.out.printf("Fetched participation key: %s (took %d ms)%n", key,
                    (endKey - startKey) / 1_000_000);

            long startDriver = System.nanoTime();
            HtmlUnitDriver driver = new HtmlUnitDriver();
            driver.setJavascriptEnabled(true);

            WebClient wc = driver.getWebClient();
            wc.getOptions().setCssEnabled(false);
            wc.getOptions().setThrowExceptionOnScriptError(false);
            wc.getOptions().setThrowExceptionOnFailingStatusCode(false);
            wc.setCssErrorHandler(new DefaultCssErrorHandler());
            java.util.logging.Logger jsLogger =
                    java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit.javascript");
            jsLogger.setLevel(java.util.logging.Level.OFF);
            long endDriver = System.nanoTime();
            System.out.printf("Initialized HtmlUnitDriver (took %d ms)%n",
                    (endDriver - startDriver) / 1_000_000);

            List<Question> questions = new ArrayList<>();
            try {
                long startLoad = System.nanoTime();
                driver.get("https://www.menti.com/" + key);
                new WebDriverWait(driver, Duration.ofSeconds(10))
                        .until((ExpectedCondition<Boolean>) d -> {
                            Object result = ((JavascriptExecutor) d).executeScript(
                                    "return window.__next_f && window.__next_f.length > 1;");
                            return Boolean.TRUE.equals(result);
                        });
                long endLoad = System.nanoTime();
                System.out.printf("Page loaded and JS data available (took %d ms)%n",
                        (endLoad - startLoad) / 1_000_000);

                long startExtract = System.nanoTime();
                Object rawAll = ((JavascriptExecutor) driver).executeScript(
                        "return window.__next_f"
                                + ".filter(x => x[0]===1)"
                                + ".map(x => x[1])"
                                + ".join('');"
                );
                long endExtract = System.nanoTime();
                System.out.printf("Extracted raw JSON (took %d ms)%n",
                        (endExtract - startExtract) / 1_000_000);

                if (!(rawAll instanceof String)) {
                    throw new IllegalStateException("Unexpected data format from script execution");
                }
                String all = (String) rawAll;
                String questionsJson = extractQuestionsJson(all);

                long startParse = System.nanoTime();
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree("{\"questions\":" + questionsJson + "}");
                JsonNode qs = root.path("questions");
                long endParse = System.nanoTime();
                System.out.printf("Parsed JSON into tree (took %d ms)%n",
                        (endParse - startParse) / 1_000_000);

                long startBuild = System.nanoTime();
                if (qs.isArray()) {
                    for (JsonNode qn : qs) {
                        String questionText = qn.path("question").asText().trim();
                        if (questionText.isEmpty()) continue;
                        JsonNode choicesNode = qn.path("choices");
                        if (!choicesNode.isArray() || choicesNode.isEmpty()) continue;

                        List<String> choices = new ArrayList<>();
                        for (JsonNode c : choicesNode) {
                            String label = c.path("label").asText().trim();
                            if (!label.isEmpty()) {
                                choices.add(label);
                            }
                        }
                        if (!choices.isEmpty()) {
                            questions.add(new Question(questionText, choices));
                        }
                    }
                }
                long endBuild = System.nanoTime();
                System.out.printf("Built question objects (took %d ms)%n",
                        (endBuild - startBuild) / 1_000_000);
            } catch (TimeoutException te) {
                throw new RuntimeException("Timed out waiting for page to load", te);
            } finally {
                driver.quit();
                long t1 = System.nanoTime();
                System.out.printf("Total fetchQuestions time: %d ms%n",
                        (t1 - t0) / 1_000_000);
            }

            return questions;
        }

        private static String extractQuestionsJson(String all) {
            int qpos = all.indexOf("\"questions\":");
            if (qpos < 0) {
                throw new IllegalStateException("Field 'questions' not found in JSON data");
            }
            int start = all.indexOf('[', qpos);
            if (start < 0) {
                throw new IllegalStateException("Opening '[' for questions array not found");
            }
            int depth = 0;
            for (int i = start; i < all.length(); i++) {
                char c = all.charAt(i);
                if (c == '[') depth++;
                else if (c == ']') {
                    depth--;
                    if (depth == 0) {
                        return all.substring(start, i + 1);
                    }
                }
            }
            throw new IllegalStateException("Closing ']' for questions array not found");
        }
    }

    /**
     * Simple model representing a question with its list of choices.
     */
    static class Question {
        private final String text;
        private final List<String> choices;

        public Question(String text, List<String> choices) {
            this.text = text;
            this.choices = choices;
        }

        public String getText() {
            return text;
        }

        public List<String> getChoices() {
            return choices;
        }
    }
}