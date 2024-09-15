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

    // Privater Konstruktor, um direkte Instanziierung zu verhindern
    private SettingsRepository() {
    }

    /**
     * Gibt die Singleton-Instanz von SettingsRepository zurück.
     * Falls keine Instanz existiert, wird sie erstellt.
     *
     * @return Instanz von SettingsRepository
     */
    public static SettingsRepository getInstance() {
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
                        rs.getString("email_address"),
                        rs.getString("smtp_host"),
                        rs.getInt("smtp_port"),
                        rs.getString("smtp_username"),
                        rs.getString("smtp_password"),  // Passwort (entschlüsseln falls erforderlich)
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
     * Aktualisiert die Einstellungen in der Datenbank.
     *
     * @param settings Ein Einstellungen_Model, das die neuen Einstellungen enthält
     */
    public void updateSettings(Einstellungen_Model settings) {
        String sql = "UPDATE Settings SET username = ?, email_address = ?, smtp_host = ?, smtp_port = ?, smtp_username = ?, " +
                "smtp_password = ?, use_tls = ?, days_before_reminder = ?, app_notification = ?, email_notification = ?, " +
                "notification_email = ? WHERE settings_id = 1";

        try (Connection conn = SQLiteDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Die Werte aus dem Einstellungen_Model werden in das PreparedStatement gesetzt
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

            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
