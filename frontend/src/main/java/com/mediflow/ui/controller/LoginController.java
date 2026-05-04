package com.mediflow.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
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
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        // 1. Vérifier si les champs sont vides
        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setStyle("-fx-text-fill: #e74c3c;"); // Rouge
            errorLabel.setText("Veuillez remplir tous les champs.");
            return;
        }

        // 2. Simulation de connexion (En attendant le vrai Backend Spring Boot)
        if (email.equals("admin") && password.equals("admin")) {
            errorLabel.setStyle("-fx-text-fill: #2ecc71;"); // Vert
            errorLabel.setText("Connexion réussie ! Chargement...");
            
            // Plus tard, on mettra ici : App.setRoot("ReceptionDashboardView");
        } else {
            errorLabel.setStyle("-fx-text-fill: #e74c3c;"); // Rouge
            errorLabel.setText("Email ou mot de passe incorrect.");
        }
    }
}