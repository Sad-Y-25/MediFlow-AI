package com.mediflow.ui.controller;

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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class DashboardController {

    // Assure-toi que les déclarations ressemblent EXACTEMENT à ceci :
    @FXML private TableView<Ticket> ticketTable;
    @FXML private TableColumn<Ticket, Long> idCol;
    @FXML private TableColumn<Ticket, String> nameCol;
    @FXML private TableColumn<Ticket, Integer> urgencyCol;
    @FXML private TableColumn<Ticket, Void> actionCol; // La nouvelle colonne action
    @FXML private Label totalStats;
    @FXML private Label urgentStats;
    @FXML private TextField searchField;

    private FilteredList<Ticket> filteredData;
    private ObservableList<Ticket> masterData = FXCollections.observableArrayList();
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


        ticketTable.setRowFactory(tv -> new TableRow<Ticket>() {
            @Override
            protected void updateItem(Ticket item, boolean empty) {
                super.updateItem(item, empty);

                // On vérifie que la ligne n'est pas vide ET que l'item est bien un Ticket
                if (empty || item == null) {
                    setStyle("");
                } else {
                    // Version compatible Java 11/17
                    switch (item.getUrgencyLevel()) {
                        case 5:
                            setStyle("-fx-background-color: #ffcccc;"); // Rouge clair
                            break;
                        case 4:
                        case 3:
                            setStyle("-fx-background-color: #fff4e6;"); // Orange clair
                            break;
                        default:
                            setStyle(""); // Blanc standard
                            break;
                    }
                }
            }
        });

        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Terminer");

            {
                btn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
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

        // À ajouter dans initialize()
        reasonInput.textProperty().addListener((observable, oldValue, newValue) -> {
            int suggestedLevel = analyzeUrgency(newValue);

            // On met à jour le ComboBox automatiquement
            urgencyInput.setValue(suggestedLevel);

            // Petit bonus visuel : on colore le champ Motif si c'est critique
            if (suggestedLevel == 5) {
                reasonInput.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2;");
            } else {
                reasonInput.setStyle("");
            }
        });

        // Dans ton initialize(), là où tu crées le bouton :
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Terminer");

            {
                // On applique la classe CSS définie dans //style.css
                btn.getStyleClass().add("button-terminate");

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

    }
    private void processCompletion(Long id) {
        // 1. Préparation de la requête PUT vers le backend
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/tickets/" + id + "/complete"))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();

        // 2. Envoi asynchrone
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        Platform.runLater(() -> {
                            // 3. Rafraîchir la liste actuelle (le patient disparaît, car son statut n'est plus WAITING)
                            loadDataFromServer();
                            System.out.println("Ticket " + id + " marqué comme COMPLETED dans la DB.");
                        });
                    }
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
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

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(json -> {
                    Ticket[] tickets = gson.fromJson(json, Ticket[].class);

                    Platform.runLater(() -> {
                        // 1. On met à jour la liste principale
                        masterData.setAll(tickets);

                        // 2. On initialise le FilteredList (seulement la première fois).
                        if (filteredData == null) {
                            filteredData = new FilteredList<>(masterData, p -> true);

                            // 3. Liaison du champ de recherche (une seule fois suffit)
                            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                                filteredData.setPredicate(ticket -> {
                                    if (newValue == null || newValue.isEmpty()) return true;
                                    String lowerCaseFilter = newValue.toLowerCase();

                                    return ticket.getPatientName().toLowerCase().contains(lowerCaseFilter) ||
                                            ticket.getReason().toLowerCase().contains(lowerCaseFilter);
                                });
                            });

                            // 4. On lie le tableau au filtre au lieu de la liste brute
                            ticketTable.setItems(filteredData);
                        }

                        // 5. Mise à jour des statistiques (Total / Urgences)
                        updateStatistics(tickets);
                    });
                });
    }
    /**
     * Calcule et affiche les statistiques globales de la file d'attente.
     * @param tickets Le tableau des patients actuellement chargés.
     */
    private void updateStatistics(Ticket[] tickets) {
        if (tickets == null) return;

        // 1. Nombre total de patients dans la file "WAITING"
        int total = tickets.length;

        // 2. Nombre de cas critiques (Urgence = 5)
        // Utilisation des Streams pour un filtrage efficace
        long urgents = java.util.Arrays.stream(tickets)
                .filter(t -> t.getUrgencyLevel() == 5)
                .count();

        // 3. Mise à jour des labels FXML
        totalStats.setText(String.valueOf(total));
        urgentStats.setText(String.valueOf(urgents));

        // Petit log de contrôle pour votre console IntelliJ
        System.out.println("Mise à jour Stats : Total=" + total + " | Urgents=" + urgents);
    }

    @FXML private TextField nameInput;
    @FXML private TextField reasonInput;
    @FXML private ComboBox<Integer> urgencyInput;

    /**
     * Envoie un nouveau patient au serveur et rafraîchit le tableau.
     */
    @FXML
    private void handleAddPatient() {
        if (nameInput.getText().isEmpty()) return;

        // 1. Création de l'objet Ticket
        Ticket newTicket = new Ticket();
        newTicket.setPatientName(nameInput.getText());
        newTicket.setReason(reasonInput.getText());
        newTicket.setUrgencyLevel(urgencyInput.getValue());
        newTicket.setStatus("WAITING");

        // 2. Conversion en JSON via Gson
        String jsonBody = gson.toJson(newTicket);

        // 3. Envoi de la requête POST
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/tickets/add"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    Platform.runLater(() -> {
                        if (response.statusCode() == 200 || response.statusCode() == 201) {
                            // Nettoyage et rafraîchissement
                            nameInput.clear();
                            reasonInput.clear();
                            urgencyInput.setValue(1);
                            loadDataFromServer();
                        }
                    });
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
    }
    private int analyzeUrgency(String reason) {
        if (reason == null || reason.isEmpty()) return 1;
        String text = reason.toLowerCase();

        // Niveau 5 : Urgence Vitale
        if (text.contains("poitrine") || text.contains("respirer") || text.contains("inconscient") || text.contains("sang")) {
            return 5;
        }
        // Niveau 3-4 : Urgent
        if (text.contains("douleur") || text.contains("fièvre") || text.contains("brulure") || text.contains("fracture")) {
            return 4;
        }
        // Niveau 2 : Standard
        if (text.contains("rhume") || text.contains("toux") || text.contains("fatigue")) {
            return 2;
        }

        return 1; // Par défaut
    }
    @FXML

    private void showHistory() {
        Stage historyStage = new Stage();
        historyStage.setTitle("MediFlow AI - Historique des Consultations");

        // Configuration de la table d'historique
        TableView<Ticket> historyTable = new TableView<>();

        TableColumn<Ticket, Long> idColH = new TableColumn<>("ID");
        idColH.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Ticket, String> nameColH = new TableColumn<>("Patient");
        nameColH.setCellValueFactory(new PropertyValueFactory<>("patientName"));

        TableColumn<Ticket, Integer> urgencyColH = new TableColumn<>("Urgence");
        urgencyColH.setCellValueFactory(new PropertyValueFactory<>("urgencyLevel"));

        TableColumn<Ticket, String> durationCol = new TableColumn<>("Attente");
        durationCol.setCellValueFactory(cellData -> {
            Ticket t = cellData.getValue();

            // On vérifie que les deux dates ne sont pas nulles et ne sont pas vides
            if (t.getCreatedAt() != null && t.getCompletedAt() != null &&
                    !t.getCreatedAt().isEmpty() && !t.getCompletedAt().isEmpty()) {
                try {
                    // 1. Conversion des Strings en objets LocalDateTime
                    // Note : Spring envoie généralement le format ISO (ex: 2026-05-04T23:15:00)
                    java.time.LocalDateTime start = java.time.LocalDateTime.parse(t.getCreatedAt());
                    java.time.LocalDateTime end = java.time.LocalDateTime.parse(t.getCompletedAt());

                    // 2. Calcul de la durée
                    long minutes = java.time.Duration.between(start, end).toMinutes();
                    return new javafx.beans.property.SimpleStringProperty(minutes + " min");

                } catch (Exception e) {
                    System.err.println("Erreur de parsing des dates : " + e.getMessage());
                    return new javafx.beans.property.SimpleStringProperty("Err format");
                }
            }
            return new javafx.beans.property.SimpleStringProperty("-");
        });

        historyTable.getColumns().add(durationCol);

        historyTable.getColumns().addAll(idColH, nameColH, urgencyColH);

        // Chargement des données COMPLETED
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/tickets/history"))
                .GET()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(json -> {
                    Ticket[] history = gson.fromJson(json, Ticket[].class);
                    Platform.runLater(() -> historyTable.getItems().setAll(history));
                });

        VBox layout = new VBox(10, new Label("Patients déjà consultés"), historyTable);
        layout.setStyle("-fx-padding: 20;");
        historyStage.setScene(new Scene(layout, 500, 400));
        historyStage.show();


    }
}