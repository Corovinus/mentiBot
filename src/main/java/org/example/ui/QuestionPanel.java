package org.example.ui;

import org.example.model.Question;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.InputStream;

public class QuestionPanel extends JPanel {
    private static final OkHttpClient http = new OkHttpClient();

    public QuestionPanel(Question q) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        JTextArea area = new JTextArea(formatText(q));
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setOpaque(false);
        add(area);

        if (q.getImageUrl() != null) {
            loadImage(q.getImageUrl()).ifPresent(pic -> {
                add(Box.createRigidArea(new Dimension(0,5)));
                add(new JLabel(new ImageIcon(pic)));
            });
        }
    }

    private String formatText(Question q) {
        StringBuilder sb = new StringBuilder(q.getText()).append("\n");
        for (int i = 0; i < q.getChoices().size(); i++) {
            sb.append(i+1).append(". ").append(q.getChoices().get(i)).append("\n");
        }
        return sb.toString();
    }

    private java.util.Optional<Image> loadImage(String url) {
        try (Response resp = http.newCall(new Request.Builder()
                .url(url).header("User-Agent","Mozilla/5.0").build()).execute();
             InputStream in = resp.body().byteStream()) {
            Image img = ImageIO.read(in);
            int maxW = 200;
            double scale = Math.min(1.0, (double)maxW / img.getWidth(null));
            return java.util.Optional.of(img.getScaledInstance(
                    (int)(img.getWidth(null)*scale),
                    (int)(img.getHeight(null)*scale),
                    Image.SCALE_SMOOTH));
        } catch (Exception e) {
            return java.util.Optional.empty();
        }
    }
}
