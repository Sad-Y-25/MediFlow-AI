package com.mediflow.ui.controller;

import com.mediflow.entity.Patient;
import com.mediflow.entity.Ticket;
import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class DashboardController {

    // --- COMPOSANTS DE LA FILE D'ATTENTE ---
    @FXML private TableView<Ticket> ticketTable;
    @FXML private TableColumn<Ticket, String> idCol;       // Modifié en String pour ticketNumber (ex: TCK-123)
    @FXML private TableColumn<Ticket, String> nameCol;
    @FXML private TableColumn<Ticket, String> urgencyCol;  // Modifié en String (LOW, MEDIUM, HIGH)
    @FXML private TableColumn<Ticket, Integer> scoreCol;   // Nouvelle colonne IA
    @FXML private TableColumn<Ticket, Integer> waitCol;    // Nouvelle colonne Temps Estimé
    @FXML private TableColumn<Ticket, Void> actionCol;

    // --- COMPOSANTS DES STATISTIQUES ---
    @FXML private Label totalStats;
    @FXML private Label urgentStats;
    @FXML private TextField searchField;

    // --- COMPOSANTS DU FORMULAIRE ---
    @FXML private TextField nameInput;
    @FXML private TextField reasonInput;
    @FXML private TextField ageInput;                // Nouveau champ Âge
    @FXML private CheckBox appointmentInput;         // Nouvelle case Rendez-vous
    @FXML private ComboBox<String> urgencyInput;
    @FXML private TextField emailInput;

    // --- VARIABLES GLOBALES ---
    private FilteredList<Ticket> filteredData;
    private ObservableList<Ticket> masterData = FXCollections.observableArrayList();
    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new Gson();
    private final String API_URL = "http://localhost:8080/api/tickets/queue";

    @FXML
    public void initialize() {
        // 1. Configuration des colonnes
        idCol.setCellValueFactory(new PropertyValueFactory<>("ticketNumber"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("patientName")); // Utilise le getter intelligent
        urgencyCol.setCellValueFactory(new PropertyValueFactory<>("urgencyLevel"));
        scoreCol.setCellValueFactory(new PropertyValueFactory<>("priorityScore"));
        waitCol.setCellValueFactory(new PropertyValueFactory<>("estimatedWaitingTime"));

        // 2. Chargement initial des données
        loadDataFromServer();

        // 3. Coloration intelligente des lignes (selon LOW, MEDIUM, HIGH)
        ticketTable.setRowFactory(tv -> new TableRow<Ticket>() {
            @Override
            protected void updateItem(Ticket item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.getUrgencyLevel() == null) {
                    setStyle("");
                } else {
                    switch (item.getUrgencyLevel().toUpperCase()) {
                        case "HIGH":
                            setStyle("-fx-background-color: #ffcccc;"); // Rouge clair
                            break;
                        case "MEDIUM":
                            setStyle("-fx-background-color: #fff4e6;"); // Orange clair
                            break;
                        default:
                            setStyle(""); // Blanc standard
                            break;
                    }
                }
            }
        });

        // 4. Configuration du Bouton d'Action "Terminer"
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Terminer");
            {
                btn.getStyleClass().add("button-terminate"); // Utilise le CSS
                btn.setOnAction(event -> {
                    Ticket ticket = getTableView().getItems().get(getIndex());
                    processCompletion(ticket.getId());
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        // 5. Analyse NLP en temps réel pour suggérer l'urgence
        reasonInput.textProperty().addListener((observable, oldValue, newValue) -> {
            String suggestedLevel = analyzeUrgency(newValue);
            urgencyInput.setValue(suggestedLevel);

            if ("HIGH".equals(suggestedLevel)) {
                reasonInput.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2;");
            } else {
                reasonInput.setStyle("");
            }
        });

        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button btnAbsent = new Button("Absent");
            private final HBox container = new HBox(10, btnAbsent); // Conteneur pour les boutons

            {
                btnAbsent.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
                btnAbsent.setOnAction(event -> {
                    Ticket ticket = getTableView().getItems().get(getIndex());
                    handleMarkAbsent(ticket.getId());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(container);
                }
            }
        });


    }

    private void handleMarkAbsent(Long id) {
        System.out.println("Tentative de marquage ABSENT pour l'ID : " + id);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/tickets/" + id + "/absent"))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        System.out.println("Succès : Le patient a été marqué absent.");
                        // On rafraîchit le tableau pour que le patient disparaisse (statut != WAITING)
                        Platform.runLater(this::loadDataFromServer);
                    } else {
                        System.err.println("Erreur Serveur : Code " + response.statusCode());
                    }
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
    }
    /**
     * Analyse sémantique basique pour définir l'urgence (NLP).
     */
    private String analyzeUrgency(String reason) {
        if (reason == null || reason.isEmpty()) return "LOW";
        String text = reason.toLowerCase();

        if (text.contains("poitrine") || text.contains("respirer") || text.contains("inconscient") || text.contains("sang")) {
            return "HIGH";
        }
        if (text.contains("douleur") || text.contains("fièvre") || text.contains("brulure") || text.contains("fracture")) {
            return "MEDIUM";
        }
        return "LOW";
    }

    /**
     * Termine la consultation d'un patient.
     */
    private void processCompletion(Long id) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/tickets/" + id + "/complete"))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        Platform.runLater(this::loadDataFromServer);
                    }
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
    }

    /**
     * Rafraîchit les données depuis le serveur.
     */
    @FXML
    private void loadDataFromServer() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .GET()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(json -> {
                    Ticket[] tickets = gson.fromJson(json, Ticket[].class);

                    Platform.runLater(() -> {
                        masterData.setAll(tickets);

                        if (filteredData == null) {
                            filteredData = new FilteredList<>(masterData, p -> true);
                            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                                filteredData.setPredicate(ticket -> {
                                    if (newValue == null || newValue.isEmpty()) return true;
                                    String filter = newValue.toLowerCase();

                                    // On cherche dans le nom OU dans le numéro de ticket
                                    return ticket.getPatientName().toLowerCase().contains(filter) ||
                                            ticket.getTicketNumber().toLowerCase().contains(filter);
                                });
                            });
                            ticketTable.setItems(filteredData);
                        }
                        updateStatistics(tickets);
                    });
                });
    }

    /**
     * Met à jour les statistiques en haut de l'écran.
     */
    private void updateStatistics(Ticket[] tickets) {
        if (tickets == null) return;
        int total = tickets.length;
        long urgents = java.util.Arrays.stream(tickets)
                .filter(t -> "HIGH".equalsIgnoreCase(t.getUrgencyLevel()))
                .count();

        totalStats.setText(String.valueOf(total));
        urgentStats.setText(String.valueOf(urgents));
    }

    /**
     * Création d'un ticket avec l'objet Patient imbriqué pour le moteur IA.
     */
    @FXML
    private void handleAddPatient() {
        if (nameInput.getText().isEmpty()) return;

        // 1. Création du Patient
        Patient patient = new Patient();
        patient.setFullName(nameInput.getText());
        patient.setEmail(emailInput.getText());
        try {
            patient.setAge(Integer.parseInt(ageInput.getText()));
        } catch (NumberFormatException e) {
            patient.setAge(30); // Âge par défaut si la case est vide ou mal formatée
        }

        // 2. Création du Ticket
        Ticket newTicket = new Ticket();
        newTicket.setPatient(patient);
        newTicket.setUrgencyLevel(urgencyInput.getValue() != null ? urgencyInput.getValue() : "LOW");
        newTicket.setHasAppointment(appointmentInput.isSelected());
        // Note : Le "motif" (reason) n'est plus envoyé en base selon le SRS,
        // il sert uniquement au NLP pour choisir l'urgence localement.

        // 3. Envoi au Backend
        String jsonBody = gson.toJson(newTicket);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/tickets/add"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    Platform.runLater(() -> {
                        if (response.statusCode() == 200 || response.statusCode() == 201) {
                            nameInput.clear();
                            reasonInput.clear();
                            if (ageInput != null) ageInput.clear();
                            if (appointmentInput != null) appointmentInput.setSelected(false);
                            if (urgencyInput != null) urgencyInput.setValue("LOW");
                            loadDataFromServer();
                        }
                    });
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });

        emailInput.clear();
    }

    /**
     * Ouvre la fenêtre d'historique des consultations.
     */
    @FXML
    private void showHistory() {
        Stage historyStage = new Stage();
        historyStage.setTitle("MediFlow AI - Historique des Consultations");

        TableView<Ticket> historyTable = new TableView<>();

        // 1. Colonne Ticket (Numéro unique)
        TableColumn<Ticket, String> idColH = new TableColumn<>("Ticket");
        idColH.setCellValueFactory(new PropertyValueFactory<>("ticketNumber"));

        // 2. Colonne Nom du Patient
        TableColumn<Ticket, String> nameColH = new TableColumn<>("Patient");
        nameColH.setCellValueFactory(new PropertyValueFactory<>("patientName"));

        // 3. Colonne Niveau d'Urgence
        TableColumn<Ticket, String> urgencyColH = new TableColumn<>("Urgence");
        urgencyColH.setCellValueFactory(new PropertyValueFactory<>("urgencyLevel"));

        // 4. Colonne Statut (Nouveau : Distingue les Absents des Consultés)
        TableColumn<Ticket, String> statusColH = new TableColumn<>("Statut");
        statusColH.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusColH.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    // Style dynamique : Rouge pour Absent, Vert pour Complété
                    if ("ABSENT".equals(item)) {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
                    }
                }
            }
        });

        // 5. Colonne Temps de passage (Calcul de la durée en minutes)
        TableColumn<Ticket, String> durationCol = new TableColumn<>("Temps de passage");
        durationCol.setCellValueFactory(cellData -> {
            Ticket t = cellData.getValue();
            if (t.getCreatedAt() != null && t.getCompletedAt() != null &&
                    !t.getCreatedAt().isEmpty() && !t.getCompletedAt().isEmpty()) {
                try {
                    java.time.LocalDateTime start = java.time.LocalDateTime.parse(t.getCreatedAt());
                    java.time.LocalDateTime end = java.time.LocalDateTime.parse(t.getCompletedAt());
                    long minutes = java.time.Duration.between(start, end).toMinutes();
                    return new SimpleStringProperty(minutes + " min");
                } catch (Exception e) {
                    return new SimpleStringProperty("-");
                }
            }
            return new SimpleStringProperty("-");
        });

        // Ajout de toutes les colonnes à la table
        historyTable.getColumns().addAll(idColH, nameColH, urgencyColH, statusColH, durationCol);

        // Ajustement automatique de la largeur des colonnes
        historyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Requête asynchrone vers ton endpoint Backend
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/tickets/history"))
                .GET()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(json -> {
                    Ticket[] history = gson.fromJson(json, Ticket[].class);
                    Platform.runLater(() -> historyTable.getItems().setAll(history));
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });

        // Mise en page de la fenêtre
        VBox layout = new VBox(15, new Label("Archives des Patients (Consultés & Absents)"), historyTable);
        layout.setStyle("-fx-padding: 25; -fx-background-color: #f4f7f6;");

        historyStage.setScene(new Scene(layout, 750, 500));
        historyStage.show();
    }
}