package com.mediflow.ui.api;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mediflow.entity.Doctor;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class DoctorApiService {
    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new com.google.gson.GsonBuilder()
            .registerTypeAdapter(java.time.LocalDateTime.class, (com.google.gson.JsonDeserializer<java.time.LocalDateTime>) (json, typeOfT, context) -> {
                return java.time.LocalDateTime.parse(json.getAsString());
            })
            .registerTypeAdapter(java.time.LocalDateTime.class, (com.google.gson.JsonSerializer<java.time.LocalDateTime>) (src, typeOfSrc, context) -> {
                return new com.google.gson.JsonPrimitive(src.toString());
            })
            .create();
    private final String BASE_URL = "http://localhost:8080/api/doctors";

    public List<Doctor> getAllDoctors() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            Type listType = new TypeToken<List<Doctor>>(){}.getType();
            return gson.fromJson(response.body(), listType);
        } else {
            throw new RuntimeException("Error fetching doctors: " + response.body());
        }
    }

    public List<Doctor> getDoctorsByService(Long serviceId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/service/" + serviceId))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            Type listType = new TypeToken<List<Doctor>>(){}.getType();
            return gson.fromJson(response.body(), listType);
        } else {
            throw new RuntimeException("Error fetching doctors for service: " + response.body());
        }
    }
}
