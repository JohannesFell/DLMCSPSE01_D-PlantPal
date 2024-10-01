package com.plantpal.database;

import com.plantpal.model.Einstellungen_Model;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Das {@code SettingsRepository} ist für den Zugriff und die Verwaltung der Anwendungseinstellungen in der SQLite-Datenbank verantwortlich.
 * Es stellt Methoden bereit, um Einstellungen zu laden, zu speichern und zu aktualisieren, die das Verhalten der Anwendung steuern.
 *
 * Diese Klasse implementiert CRUD-Operationen für die "Settings"-Tabelle und bietet Funktionen, um Konfigurationsdaten
 * wie Benutzereinstellungen, Anwendungspräferenzen und andere konfigurierbare Parameter zu verwalten.
 *
 * Die Klasse verwendet SQL-Anweisungen, um den direkten Zugriff auf die SQLite-Datenbank zu ermöglichen und
 * sicherzustellen, dass Einstellungen persistent gespeichert werden und bei Bedarf abgerufen werden können.
 */
public class SettingsRepository {

    // Singleton-Instanz der Klasse
    private static SettingsRepository instance;

    private SettingsRepository() {
    }

    public static synchronized SettingsRepository getInstance() {
        if (instance == null) {
            instance = new SettingsRepository();
        }
        return instance;
    }

    /**
     * Lädt die Einstellungen aus der Datenbank.
     *
     * @return Ein Einstellungen_Model, das die Einstellungen enthält, oder null, falls keine gefunden werden.
     */
    public Einstellungen_Model getSettings() {
        String sql = "SELECT * FROM Settings WHERE settings_id = 1";
        try (Connection conn = SQLiteDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                return new Einstellungen_Model(
                        rs.getString("username"),
                        rs.getString("email_address_sender"),
                        rs.getBoolean("app_notification"),
                        rs.getBoolean("email_notification"),
                        rs.getInt("days_before_reminder_app"),
                        rs.getString("notification_email"),
                        rs.getString("api_key"),
                        rs.getString("private_api_key")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Aktualisiert die Einstellungen in der Datenbank.
     *
     * @param settings Ein Einstellungen_Model, das die neuen Einstellungen enthält
     */
    public synchronized void updateSettings(Einstellungen_Model settings) {
        String sql = "UPDATE Settings SET username = ?, email_address_sender = ?, app_notification = ?, " +
                "email_notification = ?, days_before_reminder_app = ?, notification_email = ?, api_key = ?, private_api_key = ? WHERE settings_id = 1";

        try (Connection conn = SQLiteDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, settings.getUsername());
            pstmt.setString(2, settings.getEmailAddressSender());
            pstmt.setBoolean(3, settings.isAppNotification());
            pstmt.setBoolean(4, settings.isEmailNotification());
            pstmt.setInt(5, settings.getDaysBeforeReminderApp());
            pstmt.setString(6, settings.getNotificationEmail());
            pstmt.setString(7, settings.getApiKey());
            pstmt.setString(8, settings.getPrivateApiKey());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
