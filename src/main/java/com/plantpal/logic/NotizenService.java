package com.plantpal.logic;

import com.plantpal.database.CareTaskHistoryRepository;
import com.plantpal.model.PflanzenPflegeHistory_Model;

import java.sql.SQLException;

/**
 * Der {@code NotizenService} verwaltet die Geschäftslogik für das Hinzufügen und Aktualisieren von Notizen.
 *
 * Diese Klasse ist für das Speichern und Aktualisieren von Notizen für Pflegeaufgaben verantwortlich.
 * Sie interagiert mit dem {@code CareTaskHistoryRepository}, um Notizen in der Datenbank zu speichern und
 * bei Änderungen zu aktualisieren.
 *
 * Notizen enthalten zusätzliche Informationen wie alte und neue Werte von Änderungen an Pflanzenprofilen
 * oder Pflegeaufgaben.
 */
public class NotizenService {

    private final CareTaskHistoryRepository careTaskHistoryRepository;

    public NotizenService() {
        this.careTaskHistoryRepository = new CareTaskHistoryRepository();
    }

    /**
     * Aktualisiert die Notiz eines bestimmten Historieneintrags in der Datenbank.
     *
     * @param historyItem Das Historienelement, das aktualisiert werden soll.
     * @throws SQLException Falls beim Aktualisieren ein Fehler auftritt.
     */
    public void updateNoteInHistory(PflanzenPflegeHistory_Model historyItem) throws SQLException {
        if (historyItem != null) {
            careTaskHistoryRepository.updateNoteInDatabase(historyItem.getHistory_id(), historyItem.getNote());
        }
    }
}
