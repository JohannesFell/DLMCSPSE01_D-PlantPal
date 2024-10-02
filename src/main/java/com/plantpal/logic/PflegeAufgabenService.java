package com.plantpal.logic;

import com.plantpal.database.CareTaskRepository;
import com.plantpal.database.CareTaskHistoryRepository;
import com.plantpal.database.PlantProfileRepository;
import com.plantpal.model.PflanzenPflegeHistory_Model;
import com.plantpal.model.PflanzenPflege_Model;
import com.plantpal.model.PflanzenProfile_Model;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Der {@code PflegeAufgabenService} verwaltet die Geschäftslogik für die Pflegeaufgaben von Pflanzen.
 *
 * Diese Klasse ist für das Laden, Erstellen und Markieren von Pflegeaufgaben als erledigt zuständig.
 * Sie interagiert mit den entsprechenden Repositorys, um die Datenbank zu aktualisieren und auf
 * Aufgaben zuzugreifen. Zudem unterstützt sie das Filtern von Aufgaben und die Pflegehistorie.
 *
 * Änderungen an Pflegeaufgaben wie Gießen, Düngen oder Standortwechsel werden hier verwaltet.
 */
public class PflegeAufgabenService {

    private final CareTaskRepository careTaskRepository;
    private final CareTaskHistoryRepository careTaskHistoryRepository;
    private final PlantProfileRepository plantProfileRepository;

    public PflegeAufgabenService(CareTaskRepository careTaskRepository, CareTaskHistoryRepository careTaskHistoryRepository, PlantProfileRepository plantProfileRepository) {
        this.careTaskRepository = careTaskRepository;
        this.careTaskHistoryRepository = careTaskHistoryRepository;
        this.plantProfileRepository = plantProfileRepository;
    }

    /**
     * Lädt alle aktuellen Pflegeaufgaben.
     *
     * @return Liste der Pflegeaufgaben.
     */
    public List<PflanzenPflege_Model> loadCurrentTasks() {
        return careTaskRepository.loadOpenTasks();
    }

    /**
     * Lädt alle Pflegehistorieeinträge.
     *
     * @return Liste der Pflegehistorie.
     */
    public List<PflanzenPflegeHistory_Model> loadHistory() {
        return careTaskHistoryRepository.loadHistory();
    }

    /**
     * Lädt die Pflegehistorie für eine bestimmte Pflanze.
     *
     * @param plantId Die ID der Pflanze.
     * @return Liste der Pflegehistorie für die Pflanze.
     */
    public List<PflanzenPflegeHistory_Model> loadHistoryForPlant(int plantId) {
        return careTaskHistoryRepository.loadHistoryForPlant(plantId);
    }

    /**
     * Gibt eine Liste aller distinct Aufgabentypen (Aktionen) zurück.
     *
     * @return Liste von Aufgabentypen.
     */
    public List<String> getDistinctTaskTypes() {
        return careTaskRepository.getDistinctTaskTypes();
    }

    /**
     * Markiert eine Pflegeaufgabe als erledigt.
     *
     * @param taskId      Die ID der Aufgabe.
     * @param plantId     Die ID der Pflanze.
     * @param taskType    Der Typ der Aufgabe.
     * @param note        Eine Notiz zur Aufgabe.
     * @param completionDate Das Abschlussdatum.
     */
    public synchronized void markTaskAsCompleted(int taskId, int plantId, String taskType, String note, LocalDate completionDate) {
        careTaskRepository.markTaskAsCompleted(taskId, plantId, taskType, note, completionDate);
    }

    /**
     * Löscht alle bestehenden Pflegeaufgaben und fügt neue Aufgaben basierend auf den Intervallen hinzu.
     *
     * @throws SQLException wenn ein Fehler beim Zugriff auf die Datenbank auftritt.
     */
    public synchronized void updateAllCareTasks() throws SQLException {
        List<PflanzenProfile_Model> plantProfiles = plantProfileRepository.getAllPlantProfiles();

        for (PflanzenProfile_Model plantProfile : plantProfiles) {
            // Lösche alle bestehenden Aufgaben für diese Pflanze
            careTaskRepository.deleteCareTasksForPlant(plantProfile.getPlant_id());

            // Berechne die neuen Fälligkeitsdaten
            LocalDate nextWatering = calculateNextDate(plantProfile.getLast_watered(), plantProfile.getWatering_interval());
            LocalDate nextFertilizing = calculateNextDate(plantProfile.getLast_fertilized(), plantProfile.getFertilizing_interval());

            // Füge die neuen Aufgaben ein
            careTaskRepository.insertCareTask(plantProfile.getPlant_id(), "Gießen", nextWatering);
            careTaskRepository.insertCareTask(plantProfile.getPlant_id(), "Düngen", nextFertilizing);
        }
    }

    /**
     * Berechnet das nächste Datum, an dem eine Pflegeaufgabe durchgeführt werden muss.
     *
     * @param lastDate Das Datum der letzten Pflege.
     * @param interval Das Intervall in Tagen.
     * @return Das nächste Datum der Pflege.
     */
    private LocalDate calculateNextDate(LocalDate lastDate, int interval) {
        if (lastDate == null || interval <= 0) {
            return LocalDate.now();
        }
        return lastDate.plusDays(interval);
    }
}
