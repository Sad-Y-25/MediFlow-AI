package com.mediflow.ui.controller;

import com.mediflow.ui.api.AuthApiService;

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
        errorLabel.setStyle("-fx-text-fill: #2ecc71;");
        errorLabel.setText("Bienvenue ! Connexion réussie.");
        
        // C'est ici que tu redirigeras vers le Dashboard
        System.out.println("Réponse du serveur : " + response);
        
    } catch (Exception e) {
        errorLabel.setStyle("-fx-text-fill: #e74c3c;");
        errorLabel.setText("Identifiants incorrects ou serveur éteint.");
    }
}
}