package com.plantpal.logic;

import com.plantpal.database.CareTaskHistoryRepository;
import com.plantpal.database.PlantProfileRepository;
import com.plantpal.database.SQLiteDB;
import com.plantpal.model.PflanzenProfile_Model;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Der {@code PflanzenProfileService} verwaltet die Geschäftslogik für Pflanzenprofile.
 *
 * Diese Klasse ist für alle Operationen rund um Pflanzenprofile verantwortlich, einschließlich der
 * Erstellung, Aktualisierung, Löschung und des Abrufs von Pflanzenprofilen. Zudem werden Änderungen
 * an Pflanzenprofilen überwacht und in der Historie gespeichert.
 *
 * Es wird mit den entsprechenden Repository-Klassen zusammengearbeitet, um Datenbankoperationen durchzuführen.
 */
public class PflanzenProfileService {

    private final PlantProfileRepository plantProfileRepository;
    private final CareTaskHistoryRepository careTaskHistoryRepository;

    // Konstruktor
    public PflanzenProfileService(PlantProfileRepository plantProfileRepository, CareTaskHistoryRepository careTaskHistoryRepository) {
        this.plantProfileRepository = plantProfileRepository;
        this.careTaskHistoryRepository = careTaskHistoryRepository;
    }

    /**
     * Gibt eine Liste mit allen Pflanzennamen zurück, die in der Datenbank existieren.
     *
     * @return Liste von Pflanzennamen
     */
    public List<String> getDistinctPlantNames() {
        return plantProfileRepository.getDistinctPlantNames();
    }

    /**
     * Gibt eine Liste mit allen Standorten zurück, die in der Datenbank existieren.
     *
     * @return Liste von Standorten
     */
    public List<String> getDistinctLocations() {
        return plantProfileRepository.getDistinctLocations();
    }

    // Methode zum Abrufen aller Pflanzenprofile
    public List<PflanzenProfile_Model> getAllPlantProfiles() {
        return plantProfileRepository.getAllPlantProfiles();
    }

    // Methode zum Hinzufügen eines neuen Pflanzenprofils
    public void addPlantProfile(PflanzenProfile_Model pflanzenProfile) {
        plantProfileRepository.addPlantProfile(pflanzenProfile);
    }

    /**
     * Aktualisiert ein Pflanzenprofil und schreibt die Änderungen in die Historie.
     *
     * @param updatedPlant Das aktualisierte Pflanzenprofil.
     */
    public void updatePlantProfile(PflanzenProfile_Model updatedPlant) throws SQLException {
        // Verwende try-with-resources für die Verbindung, um sicherzustellen, dass sie ordnungsgemäß geschlossen wird
        try (Connection conn = SQLiteDB.getConnection()) {
            conn.setAutoCommit(false);  // Transaktion beginnen

            // Hole die aktuellen Daten aus der Datenbank
            PflanzenProfile_Model currentPlant = plantProfileRepository.getPlantProfileById(updatedPlant.getPlant_id());

            if (currentPlant != null) {
                // Überprüfe Änderungen und schreibe Historie mit alter und neuer Wert-Notiz
                if (!currentPlant.getPlant_name().equals(updatedPlant.getPlant_name())) {
                    int taskIdNameGeaendert = 1001;
                    careTaskHistoryRepository.insertIntoHistoryWithNote(
                            updatedPlant.getPlant_id(),
                            taskIdNameGeaendert, // taskId für "Name geändert"
                            "Name geändert",
                            currentPlant.getPlant_name(),
                            updatedPlant.getPlant_name(),
                            LocalDate.now()
                    );
                }

                if (!currentPlant.getLocation().equals(updatedPlant.getLocation())) {
                    int taskIdStandortGeaendert = 1002;
                    careTaskHistoryRepository.insertIntoHistoryWithNote(
                            updatedPlant.getPlant_id(),
                            taskIdStandortGeaendert, // taskId für "Standort geändert"
                            "Standort geändert",
                            currentPlant.getLocation(),
                            updatedPlant.getLocation(),
                            LocalDate.now()
                    );
                }

                if (currentPlant.getWatering_interval() != updatedPlant.getWatering_interval()) {
                    int taskIdGiessenGeaendert = 1003;
                    careTaskHistoryRepository.insertIntoHistoryWithNote(
                            updatedPlant.getPlant_id(),
                            taskIdGiessenGeaendert, // taskId für "Gießintervall geändert"
                            "Gießintervall geändert",
                            String.valueOf(currentPlant.getWatering_interval()),
                            String.valueOf(updatedPlant.getWatering_interval()),
                            LocalDate.now()
                    );
                }

                if (currentPlant.getFertilizing_interval() != updatedPlant.getFertilizing_interval()) {
                    int taskIdDuengenGeaendert = 1004;
                    careTaskHistoryRepository.insertIntoHistoryWithNote(
                            updatedPlant.getPlant_id(),
                            taskIdDuengenGeaendert, // taskId für "Düngeintervall geändert"
                            "Düngeintervall geändert",
                            String.valueOf(currentPlant.getFertilizing_interval()),
                            String.valueOf(updatedPlant.getFertilizing_interval()),
                            LocalDate.now()
                    );
                }

                // Aktualisiere das Pflanzenprofil in der Datenbank
                plantProfileRepository.updatePlantProfile(updatedPlant);

                // Transaktion erfolgreich, Commit durchführen
                conn.commit();
            }
        } catch (SQLException e) {
            // Falls eine Ausnahme auftritt, wird hier die Transaktion zurückgesetzt
            throw new SQLException("Fehler beim Aktualisieren des Pflanzenprofils: " + e.getMessage(), e);
        }
    }

    // Methode zum Löschen eines Pflanzenprofils
    public void deletePlantProfile(int plantId) {
        plantProfileRepository.deletePlantProfile(plantId);
    }
}
