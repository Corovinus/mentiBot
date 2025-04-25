package org.example.ui;

import org.example.model.Question;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

public class MentiFrame {
    private final JFrame frame;
    private final JTextField codeField;
    private final JButton fetchBtn;
    private final JButton copyBtn;
    private final JPanel contentPanel;
    private final JLabel statusLabel;

    private List<Question> currentQuestions;

    public MentiFrame(Consumer<String> onFetch, Consumer<List<Question>> onCopy) {
        frame = new JFrame("Menti Bot");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(700, 600);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Enter code:"));
        codeField = new JTextField(10);
        top.add(codeField);

        fetchBtn = new JButton("Fetch");
        fetchBtn.addActionListener(e -> onFetch.accept(codeField.getText().trim()));
        top.add(fetchBtn);

        copyBtn = new JButton("Copy All");
        copyBtn.setEnabled(false);
        copyBtn.addActionListener(e -> onCopy.accept(currentQuestions));
        top.add(copyBtn);

        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        JScrollPane scroll = new JScrollPane(contentPanel);

        statusLabel = new JLabel(" ");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        frame.add(top, BorderLayout.NORTH);
        frame.add(scroll, BorderLayout.CENTER);
        frame.add(statusLabel, BorderLayout.SOUTH);
    }

    public void show() {
        frame.setVisible(true);
    }

    public void showLoading() {
        contentPanel.removeAll();
        contentPanel.add(new JLabel("Loading..."));
        refresh();
        setButtons(false);
    }

    public void displayQuestions(List<Question> questions) {
        this.currentQuestions = questions;
        contentPanel.removeAll();
        if (questions.isEmpty()) {
            contentPanel.add(new JLabel("No questions found."));
        } else {
            for (Question q : questions) {
                contentPanel.add(new QuestionPanel(q));
                contentPanel.add(Box.createRigidArea(new Dimension(0,10)));
            }
        }
        refresh();
        copyBtn.setEnabled(!questions.isEmpty());
        fetchBtn.setEnabled(true);
    }

    public void showError(String msg) {
        contentPanel.removeAll();
        contentPanel.add(new JLabel("Error: " + msg));
        refresh();
        setButtons(true);
    }

    public void showCopiedNotification() {
        JOptionPane.showMessageDialog(frame,
                "Questions copied!", "Copied",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public void updateStatus(String msg) {
        statusLabel.setText(msg);
    }

    public void clearStatus() {
        statusLabel.setText(" ");
    }

    private void setButtons(boolean enabled) {
        fetchBtn.setEnabled(enabled);
        copyBtn.setEnabled(enabled && currentQuestions!=null && !currentQuestions.isEmpty());
    }

    private void refresh() {
        contentPanel.revalidate();
        contentPanel.repaint();
    }
}
