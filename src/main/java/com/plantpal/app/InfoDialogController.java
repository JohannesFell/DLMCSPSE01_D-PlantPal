package com.plantpal.app;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Der {@code InfoDialogController} steuert das modale Info-Dialogfenster.
 * Es zeigt eine HTML-formatierte Information im WebView und bietet eine Schließen-Option.
 */
public class InfoDialogController implements Initializable {

    @FXML
    private WebView info_text;

    @FXML
    private Button confirm;

    @FXML
    private AnchorPane rootPane;

    private double xOffset = 0;
    private double yOffset = 0;

    /**
     * Setzt den HTML-Inhalt für das WebView.
     *
     * @param htmlContent Der anzuzeigende HTML-Inhalt.
     */
    @FXML
    public void setHtmlContent(String htmlContent) {
        info_text.getEngine().loadContent(htmlContent);
    }

    /**
     * Wird aufgerufen, wenn der Benutzer auf "OK" klickt.
     */
    @FXML
    private void handleConfirm() {
        closeWindow();
    }

    /**
     * Schließt das Fenster.
     */
    private void closeWindow() {
        Stage stage = (Stage) confirm.getScene().getWindow();
        stage.close();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // Fenster verschiebbar machen
        rootPane.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        rootPane.setOnMouseDragged(event -> {
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });
    }

    public void applyCss(String cssFilePath) {
        info_text.getEngine().setUserStyleSheetLocation(cssFilePath);
    }

    /**
     * Diese Methode setzt einen Event-Handler, der dafür sorgt, dass Links im HTML-Inhalt des WebView
     * in einem externen Browser geöffnet werden. Sie fügt JavaScript hinzu, um alle Links im Dokument
     * abzufangen und bei einem Klick das Standard-Browserfenster zu öffnen.
     */
    public void setLinkHandler() {
        WebEngine webEngine = info_text.getEngine();

        // Event-Handler zum Abfangen von Link-Klicks
        webEngine.setOnAlert((WebEvent<String> event) -> {
            try {
                // Öffne den Link im Standard-Browser
                Desktop.getDesktop().browse(URI.create(event.getData()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // Füge ein Skript hinzu, um alle Links abzufangen
        webEngine.documentProperty().addListener((observable, oldDoc, newDoc) -> {
            if (newDoc != null) {
                webEngine.executeScript("""
                document.querySelectorAll('a').forEach(a => {
                    a.addEventListener('click', function(event) {
                        event.preventDefault();
                        alert(this.href);  // Ruft das alert-Ereignis mit der URL auf
                    });
                });
            """);
            }
        });
    }

}
