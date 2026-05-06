package com.mediflow.ui.controller;

import com.google.gson.Gson;
import com.mediflow.entity.Doctor;
import com.mediflow.ui.api.DoctorApiService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

public class DoctorsController {

    @FXML private TableView<Doctor> doctorsTable;
    @FXML private TableColumn<Doctor, Long> idCol;
    @FXML private TableColumn<Doctor, String> nameCol;
    @FXML private TableColumn<Doctor, String> emailCol;
    @FXML private TableColumn<Doctor, String> serviceCol;

    @FXML private TextField nameInput;
    @FXML private TextField emailInput;
    @FXML private PasswordField passwordInput;
    @FXML private ComboBox<String> serviceInput; // Simplified: just storing Service ID in map, using names to show

    private final DoctorApiService doctorApiService = new DoctorApiService();
    private ObservableList<Doctor> doctorsList = FXCollections.observableArrayList();
    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new com.google.gson.GsonBuilder()
            .registerTypeAdapter(java.time.LocalDateTime.class, (com.google.gson.JsonDeserializer<java.time.LocalDateTime>) (json, typeOfT, context) -> {
                return java.time.LocalDateTime.parse(json.getAsString());
            })
            .registerTypeAdapter(java.time.LocalDateTime.class, (com.google.gson.JsonSerializer<java.time.LocalDateTime>) (src, typeOfSrc, context) -> {
                return new com.google.gson.JsonPrimitive(src.toString());
            })
            .create();

    @FXML
    public void initialize() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        
        nameCol.setCellValueFactory(cellData -> {
            if (cellData.getValue().getUser() != null) {
                return new SimpleStringProperty(cellData.getValue().getUser().getFullName());
            }
            return new SimpleStringProperty("");
        });

        emailCol.setCellValueFactory(cellData -> {
            if (cellData.getValue().getUser() != null) {
                return new SimpleStringProperty(cellData.getValue().getUser().getEmail());
            }
            return new SimpleStringProperty("");
        });

        serviceCol.setCellValueFactory(cellData -> {
            if (cellData.getValue().getService() != null) {
                return new SimpleStringProperty(cellData.getValue().getService().getName());
            }
            return new SimpleStringProperty("");
        });

        // Initialize Service dropdown (Hardcoded for demo, normally fetched from API)
        serviceInput.setItems(FXCollections.observableArrayList(
                "1 - Médecine Générale",
                "2 - Cardiologie",
                "3 - Dentisterie"
        ));

        loadDoctors();
    }

    private void loadDoctors() {
        new Thread(() -> {
            try {
                List<Doctor> fetchedDoctors = doctorApiService.getAllDoctors();
                Platform.runLater(() -> {
                    doctorsList.setAll(fetchedDoctors);
                    doctorsTable.setItems(doctorsList);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void handleAddDoctor() {
        String name = nameInput.getText();
        String email = emailInput.getText();
        String password = passwordInput.getText();
        String selectedService = serviceInput.getValue();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || selectedService == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Veuillez remplir tous les champs.");
            alert.show();
            return;
        }

        Long serviceId = Long.parseLong(selectedService.split(" - ")[0]);

        Map<String, Object> data = Map.of(
                "fullName", name,
                "email", email,
                "password", password,
                "serviceId", serviceId
        );

        String jsonBody = gson.toJson(data);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/doctors/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    Platform.runLater(() -> {
                        if (response.statusCode() == 200 || response.statusCode() == 201) {
                            nameInput.clear();
                            emailInput.clear();
                            passwordInput.clear();
                            serviceInput.setValue(null);
                            loadDoctors();
                        } else {
                            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur: " + response.body());
                            alert.show();
                        }
                    });
                });
    }
}
