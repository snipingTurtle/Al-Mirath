module com.example.al_mirath {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.example.al_mirath to javafx.fxml;
    opens com.example.al_mirath.controller to javafx.fxml;

    exports com.example.al_mirath;
    exports com.example.al_mirath.controller;
    exports com.example.al_mirath.model;
    exports com.example.al_mirath.service;
}