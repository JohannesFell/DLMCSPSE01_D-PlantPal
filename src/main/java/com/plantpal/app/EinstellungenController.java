package com.plantpal.app;

import com.plantpal.database.SQLiteDB;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class EinstellungenController implements Initializable {

    @FXML
    private TextField txt_mail_reciever, txt_mail_sender, txt_host, txt_port, txt_mail_username, txt_tsl_ssl, txt_account_name;
    @FXML
    private PasswordField txt_mail_password;
    @FXML
    private ComboBox<Integer> cb_days_before_reminder;
    @FXML
    private CheckBox chk_mail_notification, chk_app_notification;
    @FXML
    private Button btn_save;

    /**
     * Initialisiert den Controller nach dem Laden der FXML-Datei.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Lade die Einstellungen aus der Datenbank
        Einstellungen_Model settings = loadSettingsFromDatabase();

        // Befülle die UI-Felder mit den geladenen Einstellungen
        populateSettingsFields(settings);

        // ComboBox mit Werten für die Tage vor der Benachrichtigung befüllen
        for (int i = 0; i <= 7; i++) {
            cb_days_before_reminder.getItems().add(i);
        }
        cb_days_before_reminder.setValue(1);
    }

    private Einstellungen_Model loadSettingsFromDatabase() {
        // Vorerst ist nur ein Benutzerkonto vorgesehen, daher kann hart auf settings_id = 1 selektiert werden
        String sql = "SELECT * FROM Settings WHERE settings_id = 1";

        try (Connection conn = SQLiteDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Einstellungen_Model(
                        rs.getString("username"),
                        rs.getString("email_address"),
                        rs.getString("smtp_host"),
                        rs.getInt("smtp_port"),
                        rs.getString("smtp_username"),
                        rs.getString("smtp_password"),  // Ensure you handle this securely
                        rs.getBoolean("use_tls"),
                        rs.getInt("days_before_reminder"),
                        rs.getBoolean("app_notification"),
                        rs.getBoolean("email_notification"),
                        rs.getString("notification_email")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Befüllt die Eingabefelder im Einstellungsformular mit den geladenen Werten aus der Datenbank.
     *
     * @param settings Die Einstellungen, die aus der Datenbank geladen wurden. Wenn null, werden Standardwerte verwendet.
     */
    private void populateSettingsFields(Einstellungen_Model settings) {
        if (settings != null) {
            // Benutzername
            txt_account_name.setText(settings.getUsername());

            // Mail-Empfänger und Versand-E-Mail
            txt_mail_reciever.setText(settings.getNotificationEmail());
            txt_mail_sender.setText(settings.getEmailAddress());

            // Host und Port (vorbelegt für Gmail)
            txt_host.setText(settings.getSmtpHost());
            txt_port.setText(String.valueOf(settings.getSmtpPort()));

            // SMTP-Benutzername und Passwort
            txt_mail_username.setText(settings.getSmtpUsername());
            txt_mail_password.setText(settings.getSmtpPassword()); //TODO Verschlüsselung

            // TLS / SSL Informationen
            txt_tsl_ssl.setText(settings.isUseTls() ? "TLS" : "SSL");

            // Benachrichtigungseinstellungen
            cb_days_before_reminder.setValue(settings.getDaysBeforeReminder());
            chk_app_notification.setSelected(settings.isAppNotification());
            chk_mail_notification.setSelected(settings.isEmailNotification());
        } else {
            // Falls keine Einstellungen geladen werden konnten, Standardwerte setzen
            txt_account_name.setText("Pflanzenfreund");
            txt_host.setText("smtp.gmail.com");
            txt_port.setText("587");
            txt_tsl_ssl.setText("TLS");

            // Setze die Default-Werte für die Felder
            cb_days_before_reminder.setValue(3);
            chk_app_notification.setSelected(false);
            chk_mail_notification.setSelected(false);
        }
    }

    /**
     * Speichert die geänderten Einstellungen in die Datenbank.
     * Es wird immer ein Update ausgeführt, da nur ein Benutzer existiert.
     *
     * @param settings Die geänderten Einstellungen als Einstellungen_Model.
     */
    private void saveSettingsToDatabase(Einstellungen_Model settings) {
        // Verwende die settings_id für das WHERE-Kriterium, um den vorhandenen Datensatz zu aktualisieren
        String updateSql = "UPDATE Settings SET username = ?, email_address = ?, smtp_host = ?, smtp_port = ?, smtp_username = ?, " +
                "smtp_password = ?, use_tls = ?, days_before_reminder = ?, app_notification = ?, email_notification = ?, " +
                "notification_email = ? WHERE settings_id = 1";  // Verwendet immer die ID 1

        try (Connection conn = SQLiteDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(updateSql)) {

            // Setze die Parameter für das Update
            pstmt.setString(1, settings.getUsername());
            pstmt.setString(2, settings.getEmailAddress());
            pstmt.setString(3, settings.getSmtpHost());
            pstmt.setInt(4, settings.getSmtpPort());
            pstmt.setString(5, settings.getSmtpUsername());
            pstmt.setString(6, settings.getSmtpPassword());
            pstmt.setBoolean(7, settings.isUseTls());
            pstmt.setInt(8, settings.getDaysBeforeReminder());
            pstmt.setBoolean(9, settings.isAppNotification());
            pstmt.setBoolean(10, settings.isEmailNotification());
            pstmt.setString(11, settings.getNotificationEmail());

            // Führe das Update aus
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    /**
     * Speichert die Einstellungen, wenn der Benutzer auf den "Speichern"-Button klickt.
     */
    @FXML
    public void saveSettings() {
        // Fasse die Benutzereingaben zusammen
        Einstellungen_Model settings = new Einstellungen_Model(
                txt_account_name.getText(),
                txt_mail_sender.getText(),
                txt_host.getText(),
                Integer.parseInt(txt_port.getText()),
                txt_mail_username.getText(),
                txt_mail_password.getText(),  // TODO: Sicher speichern
                txt_tsl_ssl.getText().equals("TLS"),
                cb_days_before_reminder.getValue(),
                chk_app_notification.isSelected(),
                chk_mail_notification.isSelected(),
                txt_mail_reciever.getText()
        );

        // Speichere die Einstellungen in der Datenbank
        saveSettingsToDatabase(settings);
    }
}
