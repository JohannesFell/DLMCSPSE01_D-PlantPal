package com.plantpal.logic;

import com.plantpal.database.CareTaskHistoryRepository;
import com.plantpal.database.CareTaskRepository;
import com.plantpal.database.PlantProfileRepository;
import com.plantpal.model.PflanzenPflegeHistory_Model;
import com.plantpal.model.PflanzenPflege_Model;

import java.util.List;

/**
 * Der {@code FilterService} stellt die Geschäftslogik für das Filtern von Pflanzenprofilen und Pflegeaufgaben bereit.
 *
 * Diese Klasse ermöglicht das Filtern von Aufgaben und Profilen basierend auf verschiedenen Kriterien wie
 * Pflanzennamen, Standort und Aufgabentyp. Sie greift auf die Datenbank zu, um gefilterte Listen von Aufgaben
 * oder Profilen zu erstellen, die den angegebenen Kriterien entsprechen.
 */
public class FilterService {

    private final CareTaskRepository careTaskRepository;
    private final CareTaskHistoryRepository careTaskHistoryRepository;
    private final PlantProfileRepository plantProfileRepository;

    public FilterService(CareTaskRepository careTaskRepository, CareTaskHistoryRepository careTaskHistoryRepository, PlantProfileRepository plantProfileRepository) {
        this.careTaskRepository = careTaskRepository;
        this.careTaskHistoryRepository = careTaskHistoryRepository;
        this.plantProfileRepository = plantProfileRepository;
    }

    // Methoden für Filterung von Aufgaben
    public List<PflanzenPflege_Model> filterTasks(String plantName, String location, String taskType) {
        return careTaskRepository.filterCareTasks(plantName, location, taskType);
    }

    // Methoden für Filterung von Historie
    public List<PflanzenPflegeHistory_Model> filterHistory(String plantName, String location, String taskType) {
        return careTaskHistoryRepository.filterCareTaskHistory(plantName, location, taskType);
    }
}
