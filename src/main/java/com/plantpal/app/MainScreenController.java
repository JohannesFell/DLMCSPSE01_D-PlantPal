package com.plantpal.app;

import com.plantpal.database.CareTaskHistoryRepository;
import com.plantpal.database.CareTaskRepository;
import com.plantpal.database.PlantProfileRepository;
import com.plantpal.logic.PflegeAufgabenService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * Controller für die Hauptansicht der Anwendung.
 *
 * Der `MainScreenController` verwaltet die allgemeine Navigation der Anwendung und die
 * Verknüpfung der verschiedenen Bildschirme, wie Pflanzenprofile und Pflegeaufgaben.
 *
 * Funktionen:
 * - Steuert die Navigation zu den verschiedenen Ansichten
 * - Verwalten des Seitenmenüs und der Schaltflächen für die Navigation
 * - Initialisierung der Hauptansicht und Laden der Komponenten
 *
 * Zugehörige FXML-Datei: MainScreen.fxml
 */
public class MainScreenController implements Initializable {

    @FXML
    private Button btn_einstellungen;

    @FXML
    private Button btn_pflanzen;

    @FXML
    private Button btn_pflege;

    @FXML
    private Button btn_wissensdatenbank;

    @FXML
    private StackPane contentArea;

    @FXML
    private Label exit;

    @FXML
    private HBox root_mainscreen;

    @FXML
    private AnchorPane sidebar_anchorpane;

    @FXML
    private Pane sidebar_inner_pane;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialisiere den PflegeAufgabenService
        PflegeAufgabenService pflegeAufgabenService = new PflegeAufgabenService(new CareTaskRepository(), new CareTaskHistoryRepository(), new PlantProfileRepository());

        // Führe die Pflegeaufgaben-Update-Funktion aus
        try {
            pflegeAufgabenService.updateAllCareTasks();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Setze den Exit-Button
        exit.setOnMouseClicked(e -> System.exit(0));    }

    private void loadView(String fxmlPath) throws IOException {
        Parent fxml = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxmlPath)));
        contentArea.getChildren().clear();
        contentArea.getChildren().add(fxml);
    }

    public void pflanzenprofile(javafx.event.ActionEvent actionEvent) throws IOException {
        loadView("/fxml/PflanzenProfile.fxml");
    }

    public void pflanzenpflege(javafx.event.ActionEvent actionEvent) throws IOException {
        loadView("/fxml/PflanzenPflege.fxml");
    }

    public void wissensdatenbank(javafx.event.ActionEvent actionEvent) throws IOException {
        loadView("/fxml/Wissensdatenbank.fxml");
    }

    public void einstellungen(javafx.event.ActionEvent actionEvent) throws IOException {
        loadView("/fxml/Einstellungen.fxml");
    }
}
