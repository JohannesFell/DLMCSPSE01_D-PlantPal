package com.plantpal.app;

import com.plantpal.database.SQLiteDB;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class NotizenEditorController implements Initializable {

    @FXML
    private Label exit;

    @FXML
    private TextArea noteTextArea;

    private PflanzenPflegeHistory_Model historyItem;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Schließen des Fensters bei Klick auf den Exit-Button
        exit.setOnMouseClicked(e -> {
            Stage stage = (Stage) exit.getScene().getWindow();
            stage.close();
        });
    }

    /**
     * Methode, um den Inhalt des Notizeditors mit der vorhandenen Notiz zu füllen.
     */
    public void setHistoryItem(PflanzenPflegeHistory_Model historyItem) {
        this.historyItem = historyItem;
        if (historyItem != null) {
            // Bestehende Notiz anzeigen
            noteTextArea.setText(historyItem.getNote());
        }
    }

    /**
     * Speichert die Notiz und schließt den Editor.
     */
    @FXML
    public void saveNote() {
        if (historyItem != null) {
            // Setzt die neue Notiz im Modell
            historyItem.setNote(noteTextArea.getText());

            // Speichert die Notiz in der Datenbank
            updateNoteInDatabase(historyItem);

            // Schließt das Fenster nach dem Speichern
            Stage stage = (Stage) noteTextArea.getScene().getWindow();
            stage.close();
        }
    }

    /**
     * Aktualisiert die Notiz in der Datenbank.
     */
    private void updateNoteInDatabase(PflanzenPflegeHistory_Model historyItem) {
        String sql = "UPDATE CareTaskHistory SET note = ? WHERE history_id = ?";

        try (Connection conn = SQLiteDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, historyItem.getNote());
            stmt.setInt(2, historyItem.getHistory_id());

            stmt.executeUpdate();
            System.out.println("Notiz erfolgreich gespeichert!");

        } catch (SQLException e) {
            System.out.println("Fehler beim Speichern der Notiz.");
            e.printStackTrace();
        }
    }
}

