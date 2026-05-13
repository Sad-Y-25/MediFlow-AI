package com.mediflow.ui.controller;

import com.mediflow.ui.api.AuthApiService;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
// import com.mediflow.ui.App; // Garde-le commenté pour l'instant

public class LoginController {

    // On relie les variables Java aux "fx:id" du fichier FXML
    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    // Cette méthode est déclenchée par le "onAction" du bouton
    @FXML
    private final AuthApiService authService = new AuthApiService();

@FXML
private void handleLogin() {
    String email = emailField.getText();
    String password = passwordField.getText();

    if (email.isEmpty() || password.isEmpty()) {
        errorLabel.setText("Veuillez remplir tous les champs.");
        return;
    }

    try {
        String response = authService.login(email, password);
        System.out.println("Réponse du serveur : " + response);

        // Parse JSON response
        com.google.gson.JsonObject jsonResponse = com.google.gson.JsonParser.parseString(response).getAsJsonObject();
        
        // Populate SessionContext
        com.mediflow.ui.util.SessionContext session = com.mediflow.ui.util.SessionContext.getInstance();
        session.setUserId(jsonResponse.has("id") && !jsonResponse.get("id").isJsonNull() ? jsonResponse.get("id").getAsLong() : null);
        session.setFullName(jsonResponse.has("fullName") && !jsonResponse.get("fullName").isJsonNull() ? jsonResponse.get("fullName").getAsString() : null);
        session.setRole(jsonResponse.has("role") && !jsonResponse.get("role").isJsonNull() ? jsonResponse.get("role").getAsString() : null);
        
        if (jsonResponse.has("doctorId") && !jsonResponse.get("doctorId").isJsonNull()) {
            session.setDoctorId(jsonResponse.get("doctorId").getAsLong());
        } else {
            session.setDoctorId(null);
        }

        errorLabel.setStyle("-fx-text-fill: #2ecc71;");
        errorLabel.setText("Bienvenue ! Connexion réussie.");

        Platform.runLater(() -> {
            try {
                String fxmlPath = "/com/mediflow/ui/Dashboard.fxml"; // Default to Receptionist
                if ("DOCTOR".equals(session.getRole())) {
                    fxmlPath = "/com/mediflow/ui/DoctorDashboard.fxml";
                }

                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                Parent root = loader.load();
                Stage stage = (Stage) emailField.getScene().getWindow();
                stage.setScene(new Scene(root, 800, 600));
                stage.setTitle("MediFlow AI - Tableau de Bord");
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    } catch (Exception e) {
        errorLabel.setStyle("-fx-text-fill: #e74c3c;");
        errorLabel.setText("Identifiants incorrects ou serveur éteint.");
    }
}
}