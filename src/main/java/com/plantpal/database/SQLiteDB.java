package com.plantpal.database;

import java.sql.*;
import java.util.logging.Logger;

/**
 * Die Klasse {@code SQLiteDB} bietet eine zentrale Verwaltung für den Datenbankzugriff auf eine SQLite-Datenbank.
 * Sie stellt eine Methode zur Verfügung, um eine Verbindung zur SQLite-Datenbank herzustellen und zu verwalten.
 *
 * Diese Klasse implementiert das Singleton-Designmuster, um sicherzustellen, dass immer nur eine Instanz der
 * Datenbankverbindung existiert. Sie wird in der gesamten Anwendung genutzt, um auf die Datenbank zuzugreifen.
 *
 * Die Verbindung wird für alle Lese- und Schreiboperationen in der Datenbank verwendet, die von verschiedenen
 * Repositories aufgerufen werden. Diese Repositories nutzen die von dieser Klasse bereitgestellte Verbindung, um
 * SQL-Abfragen und Updates durchzuführen.
 */
public class SQLiteDB {

    private static final String URL = "jdbc:sqlite:plantpal.db";
    private static Connection connection = null;
    private static final Logger logger = Logger.getLogger(SQLiteDB.class.getName());

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                connection = DriverManager.getConnection(URL);
                logger.info("Connected to database");
                createTables();  // Tabellen erstellen
            } catch (SQLException e) {
                logger.info("Connection to SQLite has failed.");
                logger.info(e.toString());
            }
        }
        return connection;
    }

    public void closeConnection() {
        try {
            if (connection != null) {
                logger.info("Connection closed");
                connection.close();
            }
        } catch (SQLException e) {
            logger.info(e.toString());
        }
    }

    private static void createTables() {

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

        try (Statement stmt = connection.createStatement()) {
            // Erstellen der Tabellen
            stmt.execute(plantProfileTable);
            stmt.execute(careTaskTable);
            stmt.execute(careTaskHistoryTable);
            stmt.execute(photoLogTable);
            stmt.execute(settingsTable);
            System.out.println("All tables have been created successfully.");
            createDefaultEntriesSettings(stmt);  // Übergebe das bereits existierende Statement
            System.out.println("All entries have been inserted successfully.");
        } catch (Exception e) {
            System.out.println("Failed to create tables.");
            e.printStackTrace();
        }
    }

    private static void createDefaultEntriesSettings(Statement stmt) {
        try {
            String checkIfExistsSql = "SELECT settings_id FROM Settings WHERE settings_id = 1";
            ResultSet rs = stmt.executeQuery(checkIfExistsSql);

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
        }
    }
}
