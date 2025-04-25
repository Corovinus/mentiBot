package org.example.service.parsing;

public class JsonExtractor {

    /**
     * Извлекает JSON-массив questions из строки rawNextF
     *
     * @param rawNextF строка, содержащая фрагменты JSON, объединённые из window.__next_f
     * @return JSON-массив вопросов, включая скобки [ ... ]
     * @throws IllegalStateException если не удалось найти или закрыть массив
     */
    public String extractQuestionsArray(String rawNextF) {
        int p = rawNextF.indexOf("\"questions\":");
        if (p < 0) {
            throw new IllegalStateException("'questions' not found in raw data");
        }
        int start = rawNextF.indexOf('[', p);
        if (start < 0) {
            throw new IllegalStateException("Opening '[' for questions array not found");
        }
        int depth = 0;
        for (int i = start; i < rawNextF.length(); i++) {
            char c = rawNextF.charAt(i);
            if (c == '[') {
                depth++;
            } else if (c == ']') {
                depth--;
                if (depth == 0) {
                    return rawNextF.substring(start, i + 1);
                }
            }
        }
        throw new IllegalStateException("Closing ']' for questions array not found");
    }
}