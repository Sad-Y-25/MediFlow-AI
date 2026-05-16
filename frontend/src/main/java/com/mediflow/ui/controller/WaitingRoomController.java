package com.mediflow.ui.controller;

import com.google.gson.Gson;
import com.mediflow.entity.Ticket;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WaitingRoomController {

    @FXML private TableView<Ticket> publicQueueTable;
    @FXML private TableColumn<Ticket, Integer> positionCol;
    @FXML private TableColumn<Ticket, String> ticketCol;
    @FXML private TableColumn<Ticket, String> patientCol;
    @FXML private TableColumn<Ticket, String> waitCol;
    @FXML private TableColumn<Ticket, String> doctorCol;

    private final ObservableList<Ticket> publicQueueList = FXCollections.observableArrayList();
    private final HttpClient client = HttpClient.newHttpClient();
    private ScheduledExecutorService autoRefreshScheduler;

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


        // 1. Liaison des colonnes de l'écran public
        positionCol.setCellValueFactory(new PropertyValueFactory<>("positionNumber"));
        ticketCol.setCellValueFactory(new PropertyValueFactory<>("ticketNumber"));

        // Liaison de la nouvelle colonne dépendante du médecin
        doctorCol.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getDoctorName() + " (" + cellData.getValue().getServiceName() + ")"
        ));
        // Formate le temps restant pour ajouter la mention "min" explicitement
        waitCol.setCellValueFactory(cellData -> {
            Integer time = cellData.getValue().getEstimatedWaitingTime();
            return new SimpleStringProperty(time == 0 ? "Votre tour ! 🏃‍♂️" : time + " min");
        });

        // 2. Protection de la vie privée : anonymisation du nom (Ex: J. DUPONT)
        patientCol.setCellValueFactory(cellData -> {
            String rawName = cellData.getValue().getPatientName();
            if (rawName == null || rawName.equals("Inconnu")) {
                return new SimpleStringProperty("Inconnu");
            }

            String[] parts = rawName.split(" ");
            if (parts.length >= 2) {
                String firstNameInitial = parts[0].substring(0, 1).toUpperCase();
                String lastName = parts[1].toUpperCase();
                return new SimpleStringProperty(firstNameInitial + ". " + lastName);
            }
            return new SimpleStringProperty(rawName.toUpperCase());
        });

        publicQueueTable.setItems(publicQueueList);

        // 3. Premier chargement immédiat des données
        fetchLiveQueue();

        // 4. Thread d'arrière-plan : Rafraîchissement automatique toutes les 10 secondes
        autoRefreshScheduler = Executors.newScheduledThreadPool(1);
        autoRefreshScheduler.scheduleAtFixedRate(this::fetchLiveQueue, 10, 10, TimeUnit.SECONDS);

        // Sécurité : couper proprement le thread si l'écran est fermé
        Platform.runLater(() -> {
            if (publicQueueTable.getScene() != null && publicQueueTable.getScene().getWindow() != null) {
                publicQueueTable.getScene().getWindow().setOnCloseRequest(event -> {
                    if (autoRefreshScheduler != null && !autoRefreshScheduler.isShutdown()) {
                        autoRefreshScheduler.shutdownNow();
                        System.out.println("Fermeture de l'écran public : Thread de rafraîchissement arrêté.");
                    }
                });
            }
        });
    }

    private void fetchLiveQueue() {
        String API_URL = "http://localhost:8080/api/tickets/queue";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .GET()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(responseBody -> Platform.runLater(() -> {
                    try {
                        Ticket[] tickets = gson.fromJson(responseBody, Ticket[].class);
                        publicQueueList.setAll(tickets);
                    } catch (Exception e) {
                        System.err.println("Erreur mise à jour écran public : " + e.getMessage());
                    }
                }))
                .exceptionally(ex -> {
                    System.err.println("Erreur de communication avec l'API de file d'attente : " + ex.getMessage());
                    return null;
                });
    }
}