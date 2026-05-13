package com.mediflow.ui.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mediflow.entity.Ticket;
import com.mediflow.ui.util.SessionContext;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DoctorDashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private TableView<Ticket> queueTable;

    private ObservableList<Ticket> ticketList = FXCollections.observableArrayList();
    private final HttpClient client = HttpClient.newHttpClient();
    private ScheduledExecutorService scheduler;
    
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
        SessionContext session = SessionContext.getInstance();
        if (session.getFullName() != null) {
            welcomeLabel.setText("Dr. " + session.getFullName() + " - Tableau de Bord");
        }

        queueTable.setItems(ticketList);
        loadQueue();

        // Auto-refresh every 10 seconds
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::loadQueue, 10, 10, TimeUnit.SECONDS);
    }

    @FXML
    private void refreshQueue() {
        loadQueue();
    }

    private void loadQueue() {
        Long doctorId = SessionContext.getInstance().getDoctorId();
        if (doctorId == null) {
            System.err.println("Erreur: Aucun doctorId trouvé dans la session !");
            return;
        }

        String API_URL = "http://localhost:8080/api/tickets/doctor/" + doctorId + "/queue";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .GET()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(responseBody -> {
                    Platform.runLater(() -> {
                        try {
                            Type listType = new TypeToken<List<Ticket>>(){}.getType();
                            List<Ticket> tickets = gson.fromJson(responseBody, listType);
                            ticketList.setAll(tickets);
                        } catch (Exception e) {
                            System.err.println("Erreur parsing: " + e.getMessage());
                        }
                    });
                })
                .exceptionally(e -> {
                    System.err.println("Erreur réseau : " + e.getMessage());
                    return null;
                });
    }

    @FXML
    private void startConsultation() {
        Ticket selected = queueTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Veuillez sélectionner un patient.");
            return;
        }

        if ("IN_CONSULTATION".equals(selected.getStatus())) {
            showAlert(Alert.AlertType.INFORMATION, "Ce patient est déjà en consultation.");
            return;
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/tickets/" + selected.getId() + "/start"))
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(res -> Platform.runLater(this::loadQueue));
    }

    @FXML
    private void completeConsultation() {
        Ticket selected = queueTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Veuillez sélectionner un patient.");
            return;
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/tickets/" + selected.getId() + "/complete"))
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(res -> Platform.runLater(this::loadQueue));
    }

    @FXML
    private void logout() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
        SessionContext.getInstance().clear();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mediflow/ui/LoginView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) queueTable.getScene().getWindow();
            stage.setScene(new Scene(root, 400, 500));
            stage.setTitle("MediFlow AI - Connexion");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setContentText(message);
        alert.show();
    }
}
