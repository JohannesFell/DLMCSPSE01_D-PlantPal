package com.plantpal.app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

// Klasse, die sich um das Berechnen und Einfügen der nächsten Pflegeaufgaben kümmert
public class PflegeTaskUpdater {

    private final Connection connection;

    // Konstruktor
    public PflegeTaskUpdater(Connection connection) {
        this.connection = connection;
    }

    /**
     * Berechnet und fügt die nächsten Pflegeaufgaben für die Pflanze in die Datenbank ein.
     * Aktualisiert auch das PlantProfile mit den nächsten Daten für Gießen und Düngen.
     *
     * @param plantId Die ID der Pflanze, für die die Aufgaben berechnet werden sollen
     * @param lastWatered Das Datum, an dem die Pflanze zuletzt gegossen wurde
     * @param lastFertilized Das Datum, an dem die Pflanze zuletzt gedüngt wurde
     * @param wateringInterval Das Gießintervall (in Tagen)
     * @param fertilizingInterval Das Düngintervall (in Tagen)
     * @throws SQLException Wenn es Probleme mit der Datenbank gibt
     */
    public void calculateAndInsertNextCareTasks(int plantId, LocalDate lastWatered, LocalDate lastFertilized,
                                                int wateringInterval, int fertilizingInterval) throws SQLException {

        // Berechne die nächsten Daten basierend auf den Intervallen
        LocalDate nextWateringDate = calculateNextDate(lastWatered, wateringInterval);
        LocalDate nextFertilizingDate = calculateNextDate(lastFertilized, fertilizingInterval);

        // Füge die Aufgaben für Gießen und Düngen in die Tabelle "CareTask" ein, falls sie noch nicht existieren
        if (!careTaskExists(plantId, "Gießen", nextWateringDate)) {
            insertCareTask(plantId, "Gießen", nextWateringDate);
        }
        if (!careTaskExists(plantId, "Düngen", nextFertilizingDate)) {
            insertCareTask(plantId, "Düngen", nextFertilizingDate);
        }
    }

    /**
     * Berechnet das nächste Datum, an dem die Aufgabe durchgeführt werden muss.
     *
     * @param lastDate Das Datum, an dem die Aufgabe zuletzt durchgeführt wurde
     * @param interval Das Intervall (in Tagen) bis zur nächsten Durchführung
     * @return Das nächste Datum
     */
    private LocalDate calculateNextDate(LocalDate lastDate, int interval) {
        if (lastDate == null || interval <= 0) {
            // Falls kein gültiges Datum oder Intervall vorhanden ist, gib das heutige Datum zurück
            return LocalDate.now();
        }
        return lastDate.plus(interval, ChronoUnit.DAYS);
    }

    /**
     * Fügt eine neue Pflegeaufgabe in die Tabelle "CareTask" ein.
     *
     * @param plantId Die ID der Pflanze
     * @param taskType Der Typ der Aufgabe (z.B. Gießen oder Düngen)
     * @param dueDate Das Fälligkeitsdatum der Aufgabe
     * @throws SQLException Wenn es Probleme mit der Datenbank gibt
     */
    private void insertCareTask(int plantId, String taskType, LocalDate dueDate) throws SQLException {
        String insertSql = "INSERT INTO CareTask (plant_id, task_type, due_date, completed) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(insertSql)) {
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
     * @param plantId Die ID der Pflanze
     * @param taskType Der Typ der Aufgabe (z.B. Gießen oder Düngen)
     * @param dueDate Das Fälligkeitsdatum der Aufgabe
     * @return true, wenn die Aufgabe bereits existiert, false sonst
     * @throws SQLException Wenn es Probleme mit der Datenbank gibt
     */
    private boolean careTaskExists(int plantId, String taskType, LocalDate dueDate) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM CareTask WHERE plant_id = ? AND task_type = ? AND due_date = ?";
        try (PreparedStatement stmt = connection.prepareStatement(checkSql)) {
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
