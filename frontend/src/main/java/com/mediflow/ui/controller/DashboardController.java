package com.mediflow.ui.controller;

import com.mediflow.entity.Ticket;
import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class DashboardController {

    @FXML private TableView<Ticket> ticketTable;
    @FXML private TableColumn<Ticket, Long> idCol;
    @FXML private TableColumn<Ticket, String> nameCol;
    @FXML private TableColumn<Ticket, Integer> urgencyCol;

    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new Gson();
    private final String API_URL = "http://localhost:8080/api/tickets/queue";

    /**
     * Cette méthode est appelée automatiquement par JavaFX après le chargement du FXML.
     */
    @FXML
    public void initialize() {
        // Configuration des colonnes : les noms entre guillemets doivent
        // correspondre EXACTEMENT aux noms des variables dans ta classe Ticket.java.
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        urgencyCol.setCellValueFactory(new PropertyValueFactory<>("urgencyLevel"));

        // Charger les données dès l'ouverture du tableau de bord
        loadDataFromServer();
    }

    /**
     * Appelle l'API Spring Boot et met à jour le tableau.
     */
    @FXML
    private void loadDataFromServer() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .GET()
                .build();

        // Envoi de la requête en mode asynchrone pour ne pas bloquer l'UI
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(json -> {
                    // Conversion du JSON reçu en tableau d'objets Ticket
                    Ticket[] tickets = gson.fromJson(json, Ticket[].class);

                    // Mise à jour de l'interface graphique sur le thread principal
                    Platform.runLater(() -> {
                        ticketTable.getItems().setAll(tickets);
                        System.out.println("Données reçues et affichées : " + tickets.length + " patients.");
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        System.err.println("Erreur de connexion au serveur : " + ex.getMessage());
                    });
                    return null;
                });
    }
}