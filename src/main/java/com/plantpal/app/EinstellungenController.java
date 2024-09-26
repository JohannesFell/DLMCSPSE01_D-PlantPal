package com.plantpal.app;

import com.plantpal.logic.EinstellungenManager;
import com.plantpal.model.Einstellungen_Model;
import com.plantpal.utils.NotificationUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller für die Verwaltung der Anwendungseinstellungen.
 * Dieser Controller bietet die Benutzeroberfläche, um verschiedene Einstellungen der Anwendung zu ändern.
 */
public class EinstellungenController implements Initializable {

    @FXML
    private TextField txt_mail_reciever, txt_mail_sender, txt_account_name;

    @FXML
    private PasswordField pw_api_key, pw_private_api_key;

    @FXML
    private ComboBox<Integer> cb_days_before_reminder_app;

    @FXML
    private CheckBox chk_mail_notification, chk_app_notification;

    @FXML
    private Label notificationLabel;

    @FXML
    private Button btn_save;

    private final EinstellungenManager einstellungenManager = new EinstellungenManager();
    private MainScreenController mainScreenController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeComboBox();
        loadSettings();

        // Wenn die App-Benachrichtigung geändert wird
        chk_app_notification.setOnAction(event -> toggleAppNotificationSettings(chk_app_notification.isSelected()));

        // Toggle der Mail-Einstellungen je nach Benachrichtigungsstatus
        chk_mail_notification.setOnAction(event -> toggleMailSettings(chk_mail_notification.isSelected()));

        // Initiale Überprüfung für UI-Status
        toggleAppNotificationSettings(chk_app_notification.isSelected());
        toggleMailSettings(chk_mail_notification.isSelected());
    }

    /**
     * Initialisiert die ComboBox für die Auswahl der Tage vor der Erinnerung.
     */
    private void initializeComboBox() {
        ObservableList<Integer> reminderOptions = FXCollections.observableArrayList(1, 2, 3, 5, 7, 14, 30);
        cb_days_before_reminder_app.setItems(reminderOptions);
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
        txt_mail_sender.setText(settings.getEmailAddressSender());
        pw_api_key.setText(settings.getApiKey());
        pw_private_api_key.setText(settings.getPrivateApiKey());
        cb_days_before_reminder_app.setValue(settings.getDaysBeforeReminderApp());
        chk_app_notification.setSelected(settings.isAppNotification());
        chk_mail_notification.setSelected(settings.isEmailNotification());
    }

    /**
     * Speichert die Benutzereinstellungen in der Datenbank, wenn sie gültig sind.
     */
    @FXML
    public void saveSettings() {
        // Validierung der Mail-Einstellungen, falls die Mail-Benachrichtigung aktiviert ist
        if (chk_mail_notification.isSelected()) {
            if (txt_mail_sender.getText().isEmpty() || txt_mail_reciever.getText().isEmpty() ||
                    pw_api_key.getText().isEmpty() || pw_private_api_key.getText().isEmpty()) {
                NotificationUtils.showNotification(notificationLabel, "Bitte füllen Sie alle E-Mail- und API-Felder aus.");
                return;
            }
        }

        // Speichere die geänderten Einstellungen in der Datenbank
        Einstellungen_Model settings = new Einstellungen_Model(
                txt_account_name.getText(),
                txt_mail_sender.getText(),
                chk_app_notification.isSelected(),
                chk_mail_notification.isSelected(),
                cb_days_before_reminder_app.getValue(),
                txt_mail_reciever.getText(),
                pw_api_key.getText(),
                pw_private_api_key.getText()
        );

        einstellungenManager.saveSettings(settings);
        NotificationUtils.showNotification(notificationLabel, "Einstellungen erfolgreich gespeichert.");

        // Aktualisiere ggfs die Benachrichtigungsanzeige in der UI
        updateNotificationVisibilityAfterSettingsSaved();

        // Steuern die Sichtbarkeit des Buttons zum Senden von Mails in der Benachrichtigungsansicht und
        // aktualisiert den Badge
        if (mainScreenController != null) {
            mainScreenController.updateSendMailButtonVisibility(chk_mail_notification.isSelected());
            mainScreenController.updateNotificationButton();
        }
    }

    /**
     * Diese Methode wird aufgerufen, nachdem die Einstellungen gespeichert wurden,
     * um die Sichtbarkeit von Buttons und Labels basierend auf den gespeicherten Einstellungen zu aktualisieren.
     */
    private void updateNotificationVisibilityAfterSettingsSaved() {
        // App-Benachrichtigung: Deaktiviere/aktiviere die Tage-Auswahl
        cb_days_before_reminder_app.setDisable(!chk_app_notification.isSelected());
        chk_mail_notification.setDisable(!chk_app_notification.isSelected());

        // Aktualisiere den Benachrichtigungs-Button und das Label im MainScreenController
        if (mainScreenController != null) {
            boolean appNotificationEnabled = chk_app_notification.isSelected();

            mainScreenController.updateNotificationVisibility(appNotificationEnabled);
        }

    }

    private void toggleAppNotificationSettings(boolean isAppNotificationEnabled) {
        // ComboBox ausgrauen und deaktivieren, wenn App-Benachrichtigungen deaktiviert sind
        cb_days_before_reminder_app.setDisable(!isAppNotificationEnabled);
        cb_days_before_reminder_app.setOpacity(isAppNotificationEnabled ? 1.0 : 0.5);

        // Mail-Benachrichtigungscheckbox deaktivieren, wenn App-Benachrichtigungen aus sind
        chk_mail_notification.setDisable(!isAppNotificationEnabled);

        // Benachrichtigungsbutton und Zähler im MainScreenController ausblenden
        if (mainScreenController != null) {
            mainScreenController.updateNotificationVisibility(isAppNotificationEnabled);
        }

        // Falls App-Benachrichtigungen deaktiviert sind, stelle sicher, dass auch die Mail-Benachrichtigungen deaktiviert werden
        if (!isAppNotificationEnabled) {
            chk_mail_notification.setSelected(false);
            toggleMailSettings(false);
        }
    }

    /**
     * Aktiviert oder deaktiviert die Mail-Einstellungen, basierend auf dem Status der Checkbox.
     *
     * @param isEnabled true, wenn Mail-Benachrichtigungen aktiviert sind, sonst false.
     */
    private void toggleMailSettings(boolean isEnabled) {
        txt_mail_sender.setDisable(!isEnabled);
        txt_mail_reciever.setDisable(!isEnabled);
        pw_api_key.setDisable(!isEnabled);
        pw_private_api_key.setDisable(!isEnabled);
        txt_mail_sender.setOpacity(isEnabled ? 1.0 : 0.5);
    }

    /**
     * Setzt den MainScreenController, um später das Badge mit der Anzahl der Benachrichtigungen zu aktualisieren.
     *
     * @param mainScreenController Der Hauptbildschirm-Controller.
     */
    public void setMainScreenController(MainScreenController mainScreenController) {
        this.mainScreenController = mainScreenController;
    }
}
