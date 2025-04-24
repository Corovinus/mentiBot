package org.example.model;

import java.util.List;

public class Question {
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