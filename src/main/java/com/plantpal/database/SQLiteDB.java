package com.plantpal.database;

import java.sql.*;
import java.util.logging.Logger;

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

        String reminderTable = "CREATE TABLE IF NOT EXISTS Reminder (" +
                "reminder_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "task_id INTEGER," +
                "reminder_date DATETIME NOT NULL," +
                "sent BOOLEAN NOT NULL DEFAULT false," +
                "FOREIGN KEY(task_id) REFERENCES CareTask(task_id)" +
                ");";

        String knowledgeBaseTable = "CREATE TABLE IF NOT EXISTS KnowledgeBase (" +
                "knowledge_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL, " +
                "identification TEXT NOT NULL, " +
                "control TEXT NOT NULL," +
                "additional_info TEXT" +
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
                "email_address TEXT NOT NULL DEFAULT ''," +                         // E-Mail-Adresse für den Versand von Benachrichtigungen
                "smtp_host TEXT NOT NULL DEFAULT 'smtp.gmail.com'," +               // Gmail-Host vorbelegt
                "smtp_port INTEGER NOT NULL DEFAULT 587," +                         // Standard TLS-Port für Gmail
                "smtp_username TEXT NOT NULL DEFAULT ''," +                         // SMTP-Benutzername (meist die gleiche E-Mail)
                "smtp_password TEXT NOT NULL DEFAULT ''," +                         // Passwort (verschlüsselt speichern)
                "use_tls BOOLEAN NOT NULL DEFAULT 1," +                             // TLS für Gmail immer aktiv
                "days_before_reminder INTEGER NOT NULL DEFAULT 1," +                // Standarderinnerung 1 Tag vorher
                "app_notification BOOLEAN NOT NULL DEFAULT 0," +                    // Standardmäßig keine App-Benachrichtigung
                "email_notification BOOLEAN NOT NULL DEFAULT 0," +                  // Standardmäßig keine E-Mail-Benachrichtigung
                "notification_email TEXT NOT NULL DEFAULT ''" +                    // Standard-Notification-E-Mail
                ");";

        try (Statement stmt = connection.createStatement()) {
            // Erstellen der Tabellen
            stmt.execute(plantProfileTable);
            stmt.execute(careTaskTable);
            stmt.execute(careTaskHistoryTable);
            stmt.execute(reminderTable);
            stmt.execute(knowledgeBaseTable);
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
        // Verwende das übergebene Statement anstatt eine neue Verbindung zu öffnen
        try {
            // Überprüfen, ob bereits ein Eintrag in der Settings-Tabelle vorhanden ist (basierend auf der settings_id)
            String checkIfExistsSql = "SELECT settings_id FROM Settings WHERE settings_id = 1";
            ResultSet rs = stmt.executeQuery(checkIfExistsSql);

            if (!rs.next()) {
                // Wenn noch kein Eintrag vorhanden ist, füge den Standarddatensatz ein
                String insertDefaultSettings = "INSERT INTO Settings (" +
                        "username, email_address, smtp_username, smtp_password, smtp_host, smtp_port, " +
                        "use_tls, app_notification, email_notification" +
                        ") VALUES (" +
                        "'Pflanzenfreund', '', '', '', 'smtp.gmail.com', 587, 1, 0, 0" +
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
