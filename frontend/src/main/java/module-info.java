module com.mediflow.ui {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;
    requires java.net.http;

    opens com.mediflow.ui to javafx.fxml;
    exports com.mediflow.ui;

    // --- LES DEUX LIGNES À AJOUTER ---
    opens com.mediflow.ui.controller to javafx.fxml;
    exports com.mediflow.ui.controller;
}
