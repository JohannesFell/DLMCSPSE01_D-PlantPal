package com.plantpal.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
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

        try (Statement stmt = connection.createStatement()) {
            // Erstellen der Tabellen
            stmt.execute(plantProfileTable);
            stmt.execute(careTaskTable);
            stmt.execute(careTaskHistoryTable);
            stmt.execute(reminderTable);
            stmt.execute(knowledgeBaseTable);
            stmt.execute(photoLogTable);
            System.out.println("All tables have been created successfully.");
        } catch (Exception e) {
            System.out.println("Failed to create tables.");
            e.printStackTrace();
        }
    }
}
