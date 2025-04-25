package org.example.app;

import javax.swing.*;

public class MentiBotApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MentiController controller = new MentiController();
            controller.init();
        });
    }
}
