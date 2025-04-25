package org.example.model;

import java.util.List;
import java.util.Objects;

public class Question {
    private final String text;
    private final List<String> choices;
    private final String imageUrl;

    public Question(String text, List<String> choices, String imageUrl) {
        this.text = Objects.requireNonNull(text);
        this.choices = Objects.requireNonNull(choices);
        this.imageUrl = imageUrl;
    }

    // Сохраним конструктор без картинки для обратной совместимости
    public Question(String text, List<String> choices) {
        this(text, choices, null);
    }

    public String getText() {
        return text;
    }

    public List<String> getChoices() {
        return choices;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
