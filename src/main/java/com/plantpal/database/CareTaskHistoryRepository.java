package com.plantpal.database;

import com.plantpal.model.PflanzenPflegeHistory_Model;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Das {@code CareTaskHistoryRepository} ist für den Zugriff und die Verwaltung der Historie von Pflegeaufgaben
 * in der SQLite-Datenbank verantwortlich. Es stellt Methoden zur Verfügung, um Änderungen an Pflegeaufgaben in der
 * Historie zu speichern, einschließlich der Speicherung von Notizen zu diesen Änderungen.
 *
 * Diese Klasse implementiert CRUD-Operationen für die "CareTaskHistory"-Tabelle und ermöglicht das Speichern
 * von abgeschlossenen Aufgaben und deren zugehörigen Details, wie z.B. alte und neue Werte bei Änderungen.
 *
 * Die Klasse verwendet SQL-Anweisungen für den direkten Datenbankzugriff und ermöglicht das Nachverfolgen der Pflegeaufgaben
 * über die Zeit hinweg.
 */
public class CareTaskHistoryRepository {

    /**
     * Fügt einen neuen Eintrag in die Pflegeaufgaben-Historie hinzu.
     *
     * @param plantId        Die ID der Pflanze.
     * @param taskType       Die Art der Aufgabe (z.B. "Name geändert", "Gießintervall geändert").
     * @param oldValue       Der alte Wert.
     * @param newValue       Der neue Wert.
     * @param completionDate Das Datum, an dem die Änderung vorgenommen wurde.
     */
    public void insertIntoHistoryWithNote(int plantId, int taskId, String taskType, String oldValue, String newValue, LocalDate completionDate) throws SQLException {
        // Wenn der alte und neue Wert gleich sind, wird keine Historie geschrieben.
        if (oldValue.equals(newValue)) {
            return;
        }

        // Erstelle die Notiz mit altem und neuem Wert.
        String note = "Alter Wert: " + oldValue + " || Neuer Wert: " + newValue;

        String sql = "INSERT INTO CareTaskHistory (plant_id, task_id, task_type, completion_date, note) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = SQLiteDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, plantId);
            stmt.setInt(2, taskId);  // Die ID der spezifischen Aufgabe
            stmt.setString(3, taskType);
            stmt.setDate(4, java.sql.Date.valueOf(completionDate));
            stmt.setString(5, note);

            stmt.executeUpdate();
        }
    }

    /**
     * Lädt die Pflegehistorie aus der `CareTaskHistory`-Tabelle.
     *
     * @return Eine Liste von `PflanzenPflegeHistory_Model` mit allen Historieneinträgen.
     */
    public List<PflanzenPflegeHistory_Model> loadHistory() {
        List<PflanzenPflegeHistory_Model> historyList = new ArrayList<>();
        String sql = "SELECT CareTaskHistory.history_id, CareTaskHistory.task_id, CareTaskHistory.plant_id, CareTaskHistory.task_type, " +
                "CareTaskHistory.completion_date, CareTaskHistory.note, " +
                "PlantProfile.plant_name " +
                "FROM CareTaskHistory " +
                "LEFT JOIN PlantProfile ON CareTaskHistory.plant_id = PlantProfile.plant_id " +
                "ORDER BY CareTaskHistory.history_id DESC";

        try (Connection conn = SQLiteDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                PflanzenPflegeHistory_Model historyEntry = new PflanzenPflegeHistory_Model(
                        rs.getInt("history_id"),
                        rs.getInt("task_id"),
                        rs.getInt("plant_id"),
                        rs.getString("task_type"),
                        rs.getDate("completion_date").toLocalDate(),
                        rs.getString("note"),
                        rs.getString("plant_name")
                );
                historyList.add(historyEntry);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return historyList;
    }

    /**
     * Lädt die Pflegehistorie für eine bestimmte Pflanze.
     *
     * @param plantId Die ID der Pflanze, deren Historie geladen werden soll.
     * @return Eine Liste von `PflanzenPflegeHistory_Model` für die ausgewählte Pflanze.
     */
    public List<PflanzenPflegeHistory_Model> loadHistoryForPlant(int plantId) {
        List<PflanzenPflegeHistory_Model> historyList = new ArrayList<>();
        String sql = "SELECT CareTaskHistory.history_id, CareTaskHistory.task_id, CareTaskHistory.plant_id, CareTaskHistory.task_type, " +
                "CareTaskHistory.completion_date, CareTaskHistory.note, " +
                "PlantProfile.plant_name " +
                "FROM CareTaskHistory " +
                "LEFT JOIN PlantProfile ON CareTaskHistory.plant_id = PlantProfile.plant_id " +
                "WHERE CareTaskHistory.plant_id = ? "+
                "ORDER BY CareTaskHistory.history_id DESC";

        try (Connection conn = SQLiteDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, plantId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                PflanzenPflegeHistory_Model historyEntry = new PflanzenPflegeHistory_Model(
                        rs.getInt("history_id"),
                        rs.getInt("task_id"),
                        rs.getInt("plant_id"),
                        rs.getString("task_type"),
                        rs.getDate("completion_date").toLocalDate(),
                        rs.getString("note"),
                        rs.getString("plant_name")
                );
                historyList.add(historyEntry);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return historyList;
    }

    /**
     * Filtert die Historie der Pflegeaufgaben (CareTaskHistory) basierend auf Pflanze, Standort und Aufgabe.
     *
     * @param plantName  Der Name der Pflanze (kann null sein).
     * @param location   Der Standort der Pflanze (kann null sein).
     * @param taskType   Der Typ der Aufgabe (kann null sein).
     * @return Eine Liste der gefilterten Historie-Einträge.
     */
    public List<PflanzenPflegeHistory_Model> filterCareTaskHistory(String plantName, String location, String taskType) {
        List<PflanzenPflegeHistory_Model> history = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT CareTaskHistory.history_id, CareTaskHistory.task_id, CareTaskHistory.plant_id, CareTaskHistory.task_type, " +
                "CareTaskHistory.completion_date, CareTaskHistory.note, PlantProfile.plant_name, PlantProfile.location " +
                "FROM CareTaskHistory LEFT JOIN PlantProfile ON CareTaskHistory.plant_id = PlantProfile.plant_id WHERE 1=1");

        // Dynamische Filterung basierend auf den ausgewählten Optionen
        if (plantName != null && !plantName.isEmpty()) {
            sql.append(" AND PlantProfile.plant_name = '").append(plantName).append("'");
        }
        if (location != null && !location.isEmpty()) {
            sql.append(" AND PlantProfile.location = '").append(location).append("'");
        }
        if (taskType != null && !taskType.isEmpty()) {
            sql.append(" AND CareTaskHistory.task_type = '").append(taskType).append("'");
        }

        // Sortierung
        sql.append(" ORDER BY CareTaskHistory.history_id DESC");

        try (Connection conn = SQLiteDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql.toString())) {

            while (rs.next()) {
                PflanzenPflegeHistory_Model historyEntry = new PflanzenPflegeHistory_Model(
                        rs.getInt("history_id"),
                        rs.getInt("task_id"),
                        rs.getInt("plant_id"),
                        rs.getString("task_type"),
                        rs.getDate("completion_date").toLocalDate(),
                        rs.getString("note"),
                        rs.getString("plant_name")
                );
                history.add(historyEntry);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return history;
    }

    /**
     * Aktualisiert die Notiz eines bestimmten Eintrags in der `CareTaskHistory`-Tabelle.
     *
     * @param historyId Die ID des History-Eintrags.
     * @param note      Die aktualisierte Notiz.
     * @throws SQLException Wenn ein Fehler bei der Datenbankoperation auftritt.
     */
    public void updateNoteInDatabase(int historyId, String note) throws SQLException {
        String sql = "UPDATE CareTaskHistory SET note = ? WHERE history_id = ?";

        try (Connection conn = SQLiteDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, note);
            stmt.setInt(2, historyId);

            stmt.executeUpdate();
        }
    }
}
