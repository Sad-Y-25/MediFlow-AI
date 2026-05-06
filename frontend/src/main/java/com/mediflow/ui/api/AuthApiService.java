package com.mediflow.ui.api;

import com.google.gson.Gson;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class AuthApiService {
    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new com.google.gson.GsonBuilder()
            .registerTypeAdapter(java.time.LocalDateTime.class, (com.google.gson.JsonDeserializer<java.time.LocalDateTime>) (json, typeOfT, context) -> {
                return java.time.LocalDateTime.parse(json.getAsString());
            })
            .registerTypeAdapter(java.time.LocalDateTime.class, (com.google.gson.JsonSerializer<java.time.LocalDateTime>) (src, typeOfSrc, context) -> {
                return new com.google.gson.JsonPrimitive(src.toString());
            })
            .create();
    private final String API_URL = "http://localhost:8080/api/auth/login";

    public String login(String email, String password) throws Exception {
        Map<String, String> data = Map.of("email", email, "password", password);
        String jsonBody = gson.toJson(data);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return response.body(); // Retourne les infos de l'utilisateur
        } else {
            throw new RuntimeException("Erreur de connexion : " + response.body());
        }
    }
}