// src/main/java/org/example/app/MentiController.java
package org.example.app;

import org.example.config.AppConfig;
import org.example.model.Question;
import org.example.service.MentimeterService;
import org.example.ui.MentiFrame;

import javax.swing.*;
import java.util.List;

public class MentiController {
    private final MentimeterService service = new MentimeterService();
    private final MentiFrame frame = new MentiFrame(this::onFetch, this::onCopy);

    public void init() {
        frame.show();
    }

    private void onFetch(String code) {
        frame.showLoading();
        frame.clearStatus();

        new SwingWorker<List<Question>, Void>() {
            @Override
            protected List<Question> doInBackground() throws Exception {
                // Передаём прогресс-коллбэк, который сам обновляет UI
                return service.fetchQuestions(code, msg ->
                        SwingUtilities.invokeLater(() ->
                                frame.updateStatus(msg)
                        )
                );
            }

            @Override
            protected void done() {
                try {
                    List<Question> qs = get();
                    frame.displayQuestions(qs);
                    if (!AppConfig.isMetricsEnabled()) {
                        frame.updateStatus("Completed.");
                    }
                } catch (Exception ex) {
                    frame.showError(ex.getMessage());
                    frame.updateStatus("Failed.");
                }
            }
        }.execute();
    }

    private void onCopy(List<Question> qs) {
        service.copyToClipboard(qs);
        frame.showCopiedNotification();
    }
}
