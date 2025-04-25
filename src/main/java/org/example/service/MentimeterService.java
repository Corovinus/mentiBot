package org.example.service;

import org.example.config.AppConfig;
import org.example.model.Question;
import org.example.service.http.DriverFactory;
import org.example.service.key.ParticipationKeyService;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.example.service.http.PageLoader;
import org.example.service.parsing.JsonExtractor;
import org.example.service.parsing.QuestionMapper;
import java.util.function.Consumer;

/**
 * Сервис для опроса Mentimeter: получает participation_key, загружает страницу
 * headless-движком HtmlUnit, вырезает JSON и строит список Question.
 */
public class MentimeterService {
    private final ParticipationKeyService keySvc = new ParticipationKeyService();
    private final DriverFactory factory    = new DriverFactory();
    private final PageLoader loader        = new PageLoader(factory);
    private final JsonExtractor jsExt      = new JsonExtractor();
    private final QuestionMapper mapper    = new QuestionMapper();

    public List<Question> fetchQuestions(String deckId, Consumer<String> progress) throws Exception {
        long t0 = System.nanoTime();

        if (AppConfig.isMetricsEnabled()) {
            progress.accept("1) Fetching key...");
        }
        long t1Start = System.nanoTime();
        String key = keySvc.fetchKey(deckId);
        long t1End = System.nanoTime();
        if (AppConfig.isMetricsEnabled()) {
            progress.accept(String.format("    Key fetched in %.1fms", (t1End - t1Start) / 1e6));
        }

        if (AppConfig.isMetricsEnabled()) {
            progress.accept("2) Loading page & waiting for JS...");
        }
        long t2Start = System.nanoTime();
        String raw = loader.loadRawData(key);
        long t2End = System.nanoTime();
        if (AppConfig.isMetricsEnabled()) {
            progress.accept(String.format("    Page loaded in %.1fms", (t2End - t2Start) / 1e6));
        }

        if (AppConfig.isMetricsEnabled()) {
            progress.accept("3) Extracting questions JSON...");
        }
        long t3Start = System.nanoTime();
        String qsJson = jsExt.extractQuestionsArray(raw);
        long t3End = System.nanoTime();
        if (AppConfig.isMetricsEnabled()) {
            progress.accept(String.format("    JSON extracted in %.1fms", (t3End - t3Start) / 1e6));
        }

        if (AppConfig.isMetricsEnabled()) {
            progress.accept("4) Parsing & mapping JSON...");
        }
        long t4Start = System.nanoTime();
        List<Question> result = mapper.map(qsJson);
        long t4End = System.nanoTime();
        if (AppConfig.isMetricsEnabled()) {
            progress.accept(String.format("    Parsed & mapped in %.1fms", (t4End - t4Start) / 1e6));
            progress.accept(String.format("Total fetchQuestions: %.1fms", (t4End - t0) / 1e6));
        }

        return result;
    }



    /** Старый метод остаётся для совместимости */
    public List<Question> fetchQuestions(String deckId) throws Exception {
        return fetchQuestions(deckId, msg -> {});
    }

    public CompletableFuture<List<Question>> fetchQuestionsAsync(String deckId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return fetchQuestions(deckId);
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });
    }

    public void copyToClipboard(List<Question> questions) {
        StringBuilder sb = new StringBuilder();
        Toolkit.getDefaultToolkit()
                .getSystemClipboard()
                .setContents(new StringSelection(sb.toString()), null);
    }
}
