package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.time.Duration;

public class ParticipationKeyFetcher {
    public static String fetchParticipationKey(String slideDeckId) throws IOException {
        String url = "https://www.menti.com/core/audience/slide-deck/"
                + slideDeckId + "/participation-key";
        OkHttpClient client = new OkHttpClient.Builder()
                .callTimeout(Duration.ofSeconds(10))
                .build();
        Request req = new Request.Builder()
                .url(url)
                .header("User-Agent","Mozilla/5.0")
                .build();
        try (Response resp = client.newCall(req).execute()) {
            if (!resp.isSuccessful()) {
                throw new IOException("HTTP " + resp.code() + " fetching participation-key");
            }
            String json = resp.body().string();
            JsonNode node = new ObjectMapper().readTree(json);
            return node.path("participation_key").asText();
        }
    }
}
