package com.plantpal.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Die Klasse {@code SQLiteDB} ist für die Verwaltung der SQLite-Datenbankverbindungen zuständig.
 * Sie verwendet HikariCP für das Connection Pooling und aktiviert den WAL-Modus
 * (Write-Ahead Logging) zur Verbesserung der Performance bei parallelen Lese- und Schreiboperationen.
 * Sie stellt auch Methoden zur Erstellung von Tabellen und Standardeinträgen zur Verfügung.
 */
public class SQLiteDB {

    private static HikariDataSource dataSource;

    static {
        // HikariCP Konfiguration
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:plantpal.db");  // Pfad zur SQLite-Datenbank
        config.setMaximumPoolSize(5);  // Maximal 5 Verbindungen
        config.setConnectionTimeout(30000);  // Timeout in Millisekunden
        config.setIdleTimeout(600000);  // Idle-Verbindungen werden nach 10 Minuten geschlossen
        config.setMaxLifetime(1800000);  // Verbindungen werden nach 30 Minuten neu erstellt
        config.setLeakDetectionThreshold(2000);  // Leak detection: 2 Sekunden

        dataSource = new HikariDataSource(config);

        // Datenbankverbindung öffnen und PRAGMA-Befehle ausführen
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            stmt = conn.createStatement();

            // WAL-Modus aktivieren
            stmt.execute("PRAGMA journal_mode=WAL;");

            // Überprüfen, ob der WAL-Modus erfolgreich aktiviert wurde
            rs = stmt.executeQuery("PRAGMA journal_mode;");
            if (rs.next()) {
                System.out.println("Journal Mode: " + rs.getString(1));  // Sollte 'wal' ausgeben
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Schließe Ressourcen korrekt
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * Gibt eine Datenbankverbindung aus dem HikariCP-Pool zurück.
     *
     * @return eine {@code Connection} zur SQLite-Datenbank
     * @throws SQLException wenn keine Verbindung hergestellt werden kann
     */
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Schließt den HikariDataSource-Pool beim Beenden der Anwendung.
     */
    public static void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    /**
     * Erstellt die benötigten Tabellen, falls sie nicht bereits existieren.
     * Diese Methode wird einmalig beim Start der Anwendung aufgerufen.
     */
    public static void createTables() {
        String plantProfileTable = "CREATE TABLE IF NOT EXISTS PlantProfile (" +
                "plant_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "plant_name TEXT NOT NULL," +
                "botanical_plant_name TEXT," +
                "purchase_date DATE," +
                "location TEXT," +
                "watering_interval INTEGER NOT NULL," +
                "fertilizing_interval INTEGER," +
                "last_watered DATE," +
                "last_fertilized DATE," +
                "image_path TEXT" +
                ");";

        String careTaskTable = "CREATE TABLE IF NOT EXISTS CareTask (" +
                "task_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "plant_id INTEGER," +
                "task_type TEXT NOT NULL," +
                "due_date DATE NOT NULL," +
                "completed BOOLEAN NOT NULL DEFAULT false," +
                "note TEXT," +
                "FOREIGN KEY(plant_id) REFERENCES PlantProfile(plant_id)" +
                ");";

        String careTaskHistoryTable = "CREATE TABLE IF NOT EXISTS CareTaskHistory (" +
                "history_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "task_id INTEGER NOT NULL," +
                "plant_id INTEGER NOT NULL," +
                "task_type TEXT NOT NULL," +
                "completion_date DATE NOT NULL," +
                "note TEXT," +
                "FOREIGN KEY(task_id) REFERENCES CareTask(task_id)," +
                "FOREIGN KEY(plant_id) REFERENCES PlantProfile(plant_id)" +
                ");";

        String photoLogTable = "CREATE TABLE IF NOT EXISTS PhotoLog (" +
                "photo_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "plant_id INTEGER," +
                "photo_path TEXT NOT NULL," +
                "date_taken DATETIME NOT NULL," +
                "FOREIGN KEY(plant_id) REFERENCES PlantProfile(plant_id)" +
                ");";

        String settingsTable = "CREATE TABLE IF NOT EXISTS Settings (" +
                "settings_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT NOT NULL," +
                "email_address_sender TEXT NOT NULL DEFAULT ''," +          // E-Mail-Adresse des Absenders
                "days_before_reminder_app INTEGER NOT NULL DEFAULT 1," +    // Standarderinnerung App 1 Tag vorher
                "app_notification BOOLEAN NOT NULL DEFAULT 1," +            // Standardmäßig App-Benachrichtigung
                "email_notification BOOLEAN NOT NULL DEFAULT 0," +          // Standardmäßig keine E-Mail-Benachrichtigung
                "notification_email TEXT NOT NULL DEFAULT ''," +            // Empfänger E-Mail-Adresse
                "api_key TEXT NOT NULL DEFAULT ''," +                       // Verschlüsselter API-Schlüssel
                "private_api_key TEXT NOT NULL DEFAULT ''" +                // Verschlüsselter privater API-Schlüssel
                ");";

        Connection connection = null;
        Statement stmt = null;
        try {
            connection = getConnection();
            stmt = connection.createStatement();
            // Erstellen der Tabellen
            stmt.execute(plantProfileTable);
            stmt.execute(careTaskTable);
            stmt.execute(careTaskHistoryTable);
            stmt.execute(photoLogTable);
            stmt.execute(settingsTable);
            System.out.println("Alle Tabellen wurden erfolgreich erstellt.");

            // Erstellen von Standardeinträgen in der Settings-Tabelle
            createDefaultEntriesSettings(stmt);
            System.out.println("Standardeinträge wurden erfolgreich eingefügt.");
        } catch (SQLException e) {
            System.out.println("Fehler beim Erstellen der Tabellen.");
            e.printStackTrace();
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Erstellt Standardeinträge in der Settings-Tabelle, falls noch keine existieren.
     *
     * @param stmt das Statement, das für die Datenbankabfragen verwendet wird
     */
    private static void createDefaultEntriesSettings(Statement stmt) {
        ResultSet rs = null;
        try {
            String checkIfExistsSql = "SELECT settings_id FROM Settings WHERE settings_id = 1";
            rs = stmt.executeQuery(checkIfExistsSql);

            if (!rs.next()) {
                String insertDefaultSettings = "INSERT INTO Settings (" +
                        "username, email_address_sender, notification_email, days_before_reminder_app, " +
                        "app_notification, email_notification, api_key, private_api_key" +
                        ") VALUES (" +
                        "'Pflanzenfreund', '', '', 1, 1, 0, '', ''" +
                        ");";
                stmt.executeUpdate(insertDefaultSettings);
                System.out.println("Standarddatensatz in Settings eingefügt.");
            } else {
                System.out.println("Eintrag in Settings ist bereits vorhanden. Kein Einfügen erforderlich.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
