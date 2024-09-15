package com.plantpal.app;

import com.plantpal.logic.EinstellungenManager;
import com.plantpal.model.Einstellungen_Model;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller für die Verwaltung der Anwendungseinstellungen.
 *
 * Dieser Controller bietet die Benutzeroberfläche, um verschiedene Einstellungen der Anwendung
 * zu ändern.
 *
 * Zugehörige FXML-Datei: Einstellungen.fxml
 */
public class EinstellungenController implements Initializable {

    @FXML
    private TextField txt_mail_reciever, txt_mail_sender, txt_host, txt_port, txt_mail_username, txt_tsl_ssl, txt_account_name;
    @FXML
    private PasswordField txt_mail_password;
    @FXML
    private ComboBox<Integer> cb_days_before_reminder;
    @FXML
    private CheckBox chk_mail_notification, chk_app_notification;

    private final EinstellungenManager einstellungenManager = new EinstellungenManager();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeComboBox();
        loadSettings();
    }

    /**
     * Initialisiert die ComboBox für die Auswahl der Tage vor der Erinnerung.
     */
    private void initializeComboBox() {
        for (int i = 0; i <= 7; i++) {
            cb_days_before_reminder.getItems().add(i);
        }
    }

    /**
     * Lädt die Einstellungen aus der Geschäftslogik und füllt die Felder in der UI.
     */
    private void loadSettings() {
        Einstellungen_Model settings = einstellungenManager.loadSettings();
        if (settings != null) {
            populateSettingsFields(settings);
        }
    }

    /**
     * Befüllt die Eingabefelder in der UI mit den Werten aus dem Einstellungsmodell.
     *
     * @param settings Das Einstellungsmodell mit den Daten aus der Datenbank.
     */
    private void populateSettingsFields(Einstellungen_Model settings) {
        txt_account_name.setText(settings.getUsername());
        txt_mail_reciever.setText(settings.getNotificationEmail());
        txt_mail_sender.setText(settings.getEmailAddress());
        txt_host.setText(settings.getSmtpHost());
        txt_port.setText(String.valueOf(settings.getSmtpPort()));
        txt_mail_username.setText(settings.getSmtpUsername());
        txt_mail_password.setText(settings.getSmtpPassword());
        txt_tsl_ssl.setText(settings.isUseTls() ? "TLS" : "SSL");
        cb_days_before_reminder.setValue(settings.getDaysBeforeReminder());
        chk_app_notification.setSelected(settings.isAppNotification());
        chk_mail_notification.setSelected(settings.isEmailNotification());
    }

    /**
     * Speichert die Benutzereinstellungen in der Datenbank, wenn sie gültig sind.
     */
    @FXML
    public void saveSettings() {
        Einstellungen_Model settings = new Einstellungen_Model(
                txt_account_name.getText(),
                txt_mail_sender.getText(),
                txt_host.getText(),
                Integer.parseInt(txt_port.getText()),
                txt_mail_username.getText(),
                txt_mail_password.getText(),
                "TLS".equals(txt_tsl_ssl.getText()),
                cb_days_before_reminder.getValue(),
                chk_app_notification.isSelected(),
                chk_mail_notification.isSelected(),
                txt_mail_reciever.getText()
        );

        // Validierung über die Geschäftslogik
        if (einstellungenManager.validateSettings(settings)) {
            einstellungenManager.saveSettings(settings);
        }
    }
}
