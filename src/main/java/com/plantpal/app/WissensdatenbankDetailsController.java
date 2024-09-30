package com.plantpal.app;

import com.plantpal.logic.WissensdatenbankService.KnowledgeBaseEntry;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class WissensdatenbankDetailsController implements Initializable {

    @FXML
    private WebView txt_symptoms;
    @FXML
    private WebView txt_treatment;
    @FXML
    private Hyperlink hyperlink;
    @FXML
    private Label exit, lbl_name;
    @FXML
    private AnchorPane rootPane;

    private double x, y = 0;


    // Methode zum Setzen der Details in der Detailansicht
    public void setDetails(KnowledgeBaseEntry entry) {
        lbl_name.setText(entry.getName());

        String cssFilePath = Objects.requireNonNull(getClass().getResource("/css/webview_style.css")).toExternalForm();

        String symptomsHtml = "<html><head><link rel='stylesheet' type='text/css' href='" + cssFilePath + "'></head><body>"
                + "<p>" + entry.getIdentification() + "</p></body></html>";
        txt_symptoms.getEngine().loadContent(symptomsHtml);

        String treatmentHtml = "<html><head><link rel='stylesheet' type='text/css' href='" + cssFilePath + "'></head><body>"
                + "<p>" + entry.getControl() + "</p></body></html>";
        txt_treatment.getEngine().loadContent(treatmentHtml);

        // Setze den Link für weitere Informationen
        hyperlink.setText(entry.getAdditional_info());
        hyperlink.setOnAction(event -> openLink(entry.getAdditional_info()));
    }

    // Methode zum Öffnen des Links im Browser
    private void openLink(String url) {
        try {
            java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Event-Handler zum Schließen des Fensters
        exit.setOnMouseClicked(e -> {
            Stage stage = (Stage) exit.getScene().getWindow();
            stage.close();
        });

        // Logik für das Verschieben des Fensters
        rootPane.setOnMousePressed((MouseEvent event) -> {
            x = event.getSceneX();
            y = event.getSceneY();
        });

        rootPane.setOnMouseDragged((MouseEvent event) -> {
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setX(event.getScreenX() - x);
            stage.setY(event.getScreenY() - y);
            stage.setOpacity(0.8);  // Fenster wird beim Ziehen transparent
        });

        rootPane.setOnMouseReleased((MouseEvent event) -> {
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setOpacity(1);  // Fenster wird wieder undurchsichtig
        });
    }
}
