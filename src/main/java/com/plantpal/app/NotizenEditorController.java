package com.plantpal.app;

import com.plantpal.logic.NotizenService;
import com.plantpal.model.PflanzenPflegeHistory_Model;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

/**
 * Controller für den Notizeneditor.
 *
 * Dieser Controller ermöglicht es dem Benutzer, Notizen zu einem bestimmten Historieneintrag hinzuzufügen
 * und zu bearbeiten. Die Notizen werden in der Datenbank gespeichert und können jederzeit geändert werden.
 *
 * Zugehörige FXML-Datei: NotizenEditor.fxml
 */

public class NotizenEditorController implements Initializable {

    public Button saveButton;
    @FXML
    private Label exit;

    @FXML
    private TextArea noteTextArea;

    @FXML
    private AnchorPane rootPane;

    private double x, y = 0;
    private PflanzenPflegeHistory_Model historyItem;
    private final NotizenService notizenService = new NotizenService();

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

    /**
     * Setzt das Historien-Item, das bearbeitet werden soll, und füllt den Notizbereich.
     *
     * @param historyItem Das Historien-Element, das in den Editor geladen werden soll.
     */
    public void setHistoryItem(PflanzenPflegeHistory_Model historyItem) {
        this.historyItem = historyItem;
        if (historyItem != null) {
            // Bestehende Notiz in das Textfeld laden
            noteTextArea.setText(historyItem.getNote());
        }
    }

     /**
     * Speichert die Notiz und schließt den Editor.
     */
     @FXML
     public void saveNote() throws SQLException {
         if (historyItem != null) {
             // Notiz in der Datenbank speichern
             historyItem.setNote(noteTextArea.getText());
             notizenService.updateNoteInHistory(historyItem);

             // Fenster schließen
             Stage stage = (Stage) noteTextArea.getScene().getWindow();
             stage.close();
         }
     }

}
