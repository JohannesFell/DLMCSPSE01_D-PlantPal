package com.plantpal.logic;

import com.plantpal.database.CareTaskRepository;
import com.plantpal.model.PflanzenPflege_Model;

import java.time.LocalDate;
import java.util.List;

/**
 * Der BenachrichtigungsService verwaltet das Senden von In-App-Benachrichtigungen.
 * Er prüft fällige Aufgaben und aktualisiert den Badge-Zähler.
 */
public class BenachrichtigungsService {

    private final CareTaskRepository careTaskRepository;

    /**
     * Konstruktor für den BenachrichtigungsService.
     *
     * @param careTaskRepository Das Repository für Pflegeaufgaben, um auf die Aufgaben in der Datenbank zuzugreifen.
     */
    public BenachrichtigungsService(CareTaskRepository careTaskRepository) {
        this.careTaskRepository = careTaskRepository;
    }

    /**
     * Überprüft, ob Aufgaben innerhalb eines bestimmten Zeitraums fällig sind.
     * Diese Methode wird verwendet, um den Badge-Zähler zu aktualisieren.
     *
     * @param daysBefore Die Anzahl der Tage vor der Fälligkeit.
     * @return Anzahl der fälligen Aufgaben innerhalb des Zeitraums.
     */
    public int checkAndNotifyUpcomingTasks(int daysBefore) {
        LocalDate currentDate = LocalDate.now();
        List<PflanzenPflege_Model> tasks = careTaskRepository.loadOpenTasks();

        // Zähle die Aufgaben, die innerhalb der nächsten 'daysBefore' Tage fällig sind
        int taskCount = 0;
        for (PflanzenPflege_Model task : tasks) {
            if (!task.getDue_date().minusDays(daysBefore).isAfter(currentDate)) {
                taskCount++;
            }
        }
        return taskCount;
    }

    /**
     * Ruft alle Aufgaben ab, die innerhalb der nächsten 'daysBefore' Tage fällig sind.
     *
     * @param daysBefore Die Anzahl der Tage vor der Fälligkeit.
     * @return Liste von PflanzenPflege_Model, die fällige Aufgaben repräsentieren.
     */
    public List<PflanzenPflege_Model> getTasksDueIn(int daysBefore) {
        LocalDate currentDate = LocalDate.now();
        LocalDate dueDateThreshold = currentDate.plusDays(daysBefore);

        // Lade alle Aufgaben, die vor oder am 'dueDateThreshold' fällig sind
        return careTaskRepository.loadTasksDueBefore(dueDateThreshold);
    }
}
