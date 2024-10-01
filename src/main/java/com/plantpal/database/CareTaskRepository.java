package com.plantpal.database;

import com.plantpal.model.PflanzenPflege_Model;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Das {@code CareTaskRepository} ist für den Zugriff und die Verwaltung der Pflegeaufgaben in der SQLite-Datenbank zuständig.
 * Es stellt Methoden zum Laden, Hinzufügen und Aktualisieren von Pflegeaufgaben zur Verfügung, sowie zur Verwaltung
 * der entsprechenden Historie von Aufgaben.
 *
 * Diese Klasse implementiert CRUD-Operationen für die "CareTask"-Tabelle und bietet Funktionen wie das Markieren
 * von Aufgaben als erledigt, das Einfügen neuer Aufgaben und das Abrufen von offenen oder gefilterten Aufgaben.
 *
 * Die Klasse verwendet SQL-Anweisungen für den direkten Datenbankzugriff und kümmert sich um die Transaktionsverwaltung,
 * wenn mehrere Datenbankoperationen miteinander verknüpft sind.
 */
public class CareTaskRepository {

    /**
     * Lädt alle offenen Pflegeaufgaben (completed = false) aus der Datenbank.
     *
     * @return Eine Liste von PflanzenPflege_Model Objekten, die die Pflegeaufgaben darstellen.
     */
    public List<PflanzenPflege_Model> loadOpenTasks() {
        List<PflanzenPflege_Model> tasks = new ArrayList<>();
        String sql = "SELECT CareTask.task_id, CareTask.task_type, CareTask.due_date, CareTask.completed, CareTask.note, " +
                "PlantProfile.plant_id, PlantProfile.plant_name, PlantProfile.location, " +
                "PlantProfile.watering_interval, PlantProfile.fertilizing_interval " +
                "FROM CareTask " +
                "LEFT JOIN PlantProfile ON CareTask.plant_id = PlantProfile.plant_id " +
                "WHERE CareTask.completed = false " +
                "ORDER BY CareTask.due_date ASC";

        try (Connection conn = SQLiteDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                PflanzenPflege_Model task = new PflanzenPflege_Model(
                        rs.getInt("task_id"),
                        rs.getInt("plant_id"),
                        rs.getString("task_type"),
                        rs.getDate("due_date").toLocalDate(),
                        rs.getBoolean("completed"),
                        rs.getString("note"),
                        rs.getString("plant_name"),
                        rs.getString("location"),
                        rs.getInt("watering_interval"),
                        rs.getInt("fertilizing_interval"),
                        "actions"
                );
                tasks.add(task);
            }
        } catch (SQLException e) {
            System.out.println("Fehler beim Laden der Pflegeaufgaben: " + e.getMessage());
            e.printStackTrace();
        }

        return tasks;
    }

    /**
     * Lädt alle Pflegeaufgaben, die vor oder am aktuellen Datum fällig sind.
     *
     * @param dueDate Das Datum, vor dem die Aufgaben fällig sind.
     * @return Liste von PflanzenPflege_Model mit fälligen Aufgaben.
     */
    public List<PflanzenPflege_Model> loadTasksDueBefore(LocalDate dueDate) {
        List<PflanzenPflege_Model> tasks = new ArrayList<>();
        String sql = "SELECT CareTask.task_id, CareTask.task_type, CareTask.due_date, CareTask.completed, CareTask.note, " +
                "PlantProfile.plant_id, PlantProfile.plant_name, PlantProfile.location, " +
                "PlantProfile.watering_interval, PlantProfile.fertilizing_interval " +
                "FROM CareTask " +
                "LEFT JOIN PlantProfile ON CareTask.plant_id = PlantProfile.plant_id " +
                "WHERE CareTask.due_date <= ? AND CareTask.completed = false";

        try (Connection conn = SQLiteDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, java.sql.Date.valueOf(dueDate));
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                // Erstelle ein PflanzenPflege_Model mit allen erforderlichen Parametern
                PflanzenPflege_Model task = new PflanzenPflege_Model(
                        rs.getInt("task_id"),
                        rs.getInt("plant_id"),
                        rs.getString("task_type"),
                        rs.getDate("due_date").toLocalDate(),
                        rs.getBoolean("completed"),
                        rs.getString("note"),
                        rs.getString("plant_name"),
                        rs.getString("location"),
                        rs.getInt("watering_interval"),
                        rs.getInt("fertilizing_interval"),
                        "actions"
                );
                tasks.add(task);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tasks;
    }

    /**
     * Markiert eine Pflegeaufgabe als erledigt und aktualisiert die entsprechenden Felder in der Datenbank.
     *
     * @param taskId Die ID der Aufgabe.
     * @param plantId Die ID der Pflanze.
     * @param taskType Der Typ der Aufgabe (z.B. "Gießen" oder "Düngen").
     * @param note Eine optionale Notiz zur Aufgabe.
     * @param completionDate Das Datum, an dem die Aufgabe abgeschlossen wurde.
     */
    public synchronized void markTaskAsCompleted(int taskId, int plantId, String taskType, String note, LocalDate completionDate) {
        String updateTaskSql = "UPDATE CareTask SET completed = true WHERE task_id = ?";
        String updatePlantProfileSql = null;

        // Bestimme, ob last_watered oder last_fertilized aktualisiert wird.
        if ("Gießen".equals(taskType)) {
            updatePlantProfileSql = "UPDATE PlantProfile SET last_watered = ? WHERE plant_id = ?";
        } else if ("Düngen".equals(taskType)) {
            updatePlantProfileSql = "UPDATE PlantProfile SET last_fertilized = ? WHERE plant_id = ?";
        }

        String insertHistorySql = "INSERT INTO CareTaskHistory (task_id, plant_id, task_type, completion_date, note) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = SQLiteDB.getConnection()) {
            conn.setAutoCommit(false);  // Beginne die Transaktion

            try (PreparedStatement updateTaskStmt = conn.prepareStatement(updateTaskSql);
                 PreparedStatement insertHistoryStmt = conn.prepareStatement(insertHistorySql)) {

                // Pflegeaufgabe als erledigt markieren
                updateTaskStmt.setInt(1, taskId);
                updateTaskStmt.executeUpdate();

                // Historieneintrag hinzufügen
                insertHistoryStmt.setInt(1, taskId);
                insertHistoryStmt.setInt(2, plantId);
                insertHistoryStmt.setString(3, taskType);
                insertHistoryStmt.setDate(4, Date.valueOf(completionDate));
                insertHistoryStmt.setString(5, note);
                insertHistoryStmt.executeUpdate();

                // Pflanzenprofil aktualisieren, falls notwendig
                if (updatePlantProfileSql != null) {
                    try (PreparedStatement updatePlantStmt = conn.prepareStatement(updatePlantProfileSql)) {
                        updatePlantStmt.setDate(1, Date.valueOf(completionDate));
                        updatePlantStmt.setInt(2, plantId);
                        updatePlantStmt.executeUpdate();
                    }
                }

                conn.commit();  // Transaktion abschließen

            } catch (SQLException e) {
                conn.rollback();  // Im Fehlerfall die Transaktion zurückrollen
                throw e;
            }

        } catch (SQLException e) {
            System.out.println("Fehler beim Markieren der Aufgabe als erledigt: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Filtert Pflegeaufgaben (CareTask) basierend auf Pflanze, Standort und Aufgabe.
     *
     * @param plantName  Der Name der Pflanze (kann null sein).
     * @param location   Der Standort der Pflanze (kann null sein).
     * @param taskType   Der Typ der Aufgabe (kann null sein).
     * @return Eine Liste der gefilterten Pflegeaufgaben.
     */
    public List<PflanzenPflege_Model> filterCareTasks(String plantName, String location, String taskType) {
        List<PflanzenPflege_Model> tasks = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT CareTask.task_id, CareTask.task_type, CareTask.due_date, CareTask.completed, CareTask.note, " +
                "PlantProfile.plant_id, PlantProfile.plant_name, PlantProfile.location, PlantProfile.watering_interval, PlantProfile.fertilizing_interval " +
                "FROM CareTask LEFT JOIN PlantProfile ON CareTask.plant_id = PlantProfile.plant_id WHERE CareTask.completed = false");

        // Dynamische Filterung basierend auf den ausgewählten Optionen
        if (plantName != null && !plantName.isEmpty()) {
            sql.append(" AND PlantProfile.plant_name = '").append(plantName).append("'");
        }
        if (location != null && !location.isEmpty()) {
            sql.append(" AND PlantProfile.location = '").append(location).append("'");
        }
        if (taskType != null && !taskType.isEmpty()) {
            sql.append(" AND CareTask.task_type = '").append(taskType).append("'");
        }

        try (Connection conn = SQLiteDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql.toString())) {

            while (rs.next()) {
                PflanzenPflege_Model task = new PflanzenPflege_Model(
                        rs.getInt("task_id"),
                        rs.getInt("plant_id"),
                        rs.getString("task_type"),
                        rs.getDate("due_date").toLocalDate(),
                        rs.getBoolean("completed"),
                        rs.getString("note"),
                        rs.getString("plant_name"),
                        rs.getString("location"),
                        rs.getInt("watering_interval"),
                        rs.getInt("fertilizing_interval"),
                        "actions"
                );
                tasks.add(task);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tasks;
    }
    /**
     * Ruft eine Liste von einzigartigen Aufgabentypen aus der Datenbank ab und fügt manuell definierte
     * Aufgabentypen hinzu. Die Liste wird entsprechend einer festgelegten Reihenfolge sortiert.
     *
     * Die Sortierung erfolgt wie folgt:
     * 1. Gießen
     * 2. Düngen
     * 3. Name geändert
     * 4. Standort geändert
     * 5. Gießintervall geändert
     * 6. Düngeintervall geändert
     * 7. Andere Task-Typen alphabetisch
     *
     * @return Eine Liste der distinct Aufgabentypen, einschließlich benutzerdefinierter Werte,
     *         sortiert nach der vorgegebenen Reihenfolge.
     */
    public List<String> getDistinctTaskTypes() {
        String sql = "SELECT task_type FROM ( " +
                "SELECT DISTINCT task_type FROM CareTask " +
                "UNION SELECT 'Gießen' " +
                "UNION SELECT 'Düngen' " +
                "UNION SELECT 'Name geändert' " +
                "UNION SELECT 'Standort geändert' " +
                "UNION SELECT 'Gießintervall geändert' " +
                "UNION SELECT 'Düngeintervall geändert' " +
                ") " +
                "ORDER BY CASE " +
                "WHEN task_type = 'Gießen' THEN 1 " +
                "WHEN task_type = 'Düngen' THEN 2 " +
                "WHEN task_type = 'Name geändert' THEN 3 " +
                "WHEN task_type = 'Standort geändert' THEN 4 " +
                "WHEN task_type = 'Gießintervall geändert' THEN 5 " +
                "WHEN task_type = 'Düngeintervall geändert' THEN 6 " +
                "ELSE 7 END, task_type";

        List<String> taskTypes = new ArrayList<>();

        try (Connection conn = SQLiteDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                taskTypes.add(rs.getString("task_type"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return taskTypes;
    }



    /**
     * Fügt eine neue Pflegeaufgabe in die Tabelle "CareTask" ein.
     *
     * @param plantId  Die ID der Pflanze
     * @param taskType Der Typ der Aufgabe (z.B. Gießen oder Düngen)
     * @param dueDate  Das Fälligkeitsdatum der Aufgabe
     * @throws SQLException Wenn es Probleme mit der Datenbank gibt
     */
    public void insertCareTask(int plantId, String taskType, LocalDate dueDate) throws SQLException {
        String insertSql = "INSERT INTO CareTask (plant_id, task_type, due_date, completed) VALUES (?, ?, ?, ?)";
        try (Connection conn = SQLiteDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertSql)) {
            stmt.setInt(1, plantId);
            stmt.setString(2, taskType);
            stmt.setDate(3, java.sql.Date.valueOf(dueDate));
            stmt.setBoolean(4, false);  // Neue Aufgaben sind standardmäßig nicht abgeschlossen
            stmt.executeUpdate();
        }
    }

    /**
     * Überprüft, ob bereits eine Pflegeaufgabe für die gegebene Pflanze, den Aufgabentyp und das Fälligkeitsdatum existiert.
     *
     * @param plantId  Die ID der Pflanze
     * @param taskType Der Typ der Aufgabe (z.B. Gießen oder Düngen)
     * @param dueDate  Das Fälligkeitsdatum der Aufgabe
     * @return true, wenn die Aufgabe bereits existiert, false sonst
     * @throws SQLException Wenn es Probleme mit der Datenbank gibt
     */
    public boolean careTaskExists(int plantId, String taskType, LocalDate dueDate) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM CareTask WHERE plant_id = ? AND task_type = ? AND due_date = ?";
        try (Connection conn = SQLiteDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(checkSql)) {
            stmt.setInt(1, plantId);
            stmt.setString(2, taskType);
            stmt.setDate(3, java.sql.Date.valueOf(dueDate));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
}
