package com.plantpal.app;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class BestaetigungsDialogController implements Initializable {

    @FXML
    private TextArea text_confirmation;

    @FXML
    private Button confirm;

    @FXML
    private Label exit;

    private boolean confirmed = false;

    /**
     * Setzt den Text für die Bestätigung.
     *
     * @param message Die Nachricht, die im Textfeld angezeigt wird.
     */
    @FXML
    public void setConfirmationText(String message) {
        text_confirmation.setText(message);
    }

    /**
     * Wird aufgerufen, wenn der Benutzer auf "Bestätigen" klickt.
     */
    @FXML
    private void handleConfirm() {
        confirmed = true;
        closeWindow();
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    private void closeWindow() {
        Stage stage = (Stage) confirm.getScene().getWindow();
        stage.close();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Listener für das Exit-Label
        exit.setOnMouseClicked(event -> {
            // Schließt das aktuelle Fenster
            Stage stage = (Stage) exit.getScene().getWindow();
            stage.close();
        });

    }
}
