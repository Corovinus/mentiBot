package org.example;

import org.example.model.Question;

import javax.swing.*;
import java.awt.*;
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
                    return MentimeterService.fetchQuestions(deckId);
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
}