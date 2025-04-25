package org.example.service.parsing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.model.Question;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Преобразует JSON-массив вопросов в список объектов Question,
 * добавляя префикс "Вопрос N:" для нумерации.
 */
public class QuestionMapper {
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * @param questionsJson JSON-массив вопросов, например "[{...},{...},...]"
     * @return список валидных вопросов с нумерацией
     * @throws IOException если не удалось распарсить JSON
     * @throws IllegalStateException если корневой узел не массив
     */
    public List<Question> map(String questionsJson) throws IOException {
        // Оборачиваем в объект для доступа к узлу "questions"
        String wrapper = "{\"questions\":" + questionsJson + "}";
        JsonNode root = objectMapper.readTree(wrapper);
        JsonNode arr = root.path("questions");
        if (!arr.isArray()) {
            throw new IllegalStateException("Expected 'questions' to be an array");
        }

        List<Question> result = new ArrayList<>();
        for (int i = 0; i < arr.size(); i++) {
            JsonNode qn = arr.get(i);
            String rawText = qn.path("question").asText().trim();
            if (rawText.isEmpty()) {
                continue;
            }

            JsonNode choicesNode = qn.path("choices");
            if (!choicesNode.isArray() || choicesNode.isEmpty()) {
                continue;
            }

            List<String> choices = new ArrayList<>();
            for (JsonNode choice : choicesNode) {
                String label = choice.path("label").asText().trim();
                if (!label.isEmpty()) {
                    choices.add(label);
                }
            }
            if (choices.isEmpty()) {
                continue;
            }

            String imageUrl = qn.path("question_image_url").asText().trim();
            if (imageUrl.isEmpty()) {
                imageUrl = null;
            }

            // Добавляем префикс с номером вопроса
            String numberedText = String.format("Вопрос %d: %s", i + 1, rawText);
            result.add(new Question(numberedText, choices, imageUrl));
        }
        return result;
    }
}