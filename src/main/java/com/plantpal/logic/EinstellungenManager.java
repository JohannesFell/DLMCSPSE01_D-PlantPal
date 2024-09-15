package com.plantpal.logic;

import com.plantpal.database.SettingsRepository;
import com.plantpal.model.Einstellungen_Model;

/**
 * Der {@code EinstellungenManager} verwaltet die Applikationseinstellungen und Konfigurationen.
 *
 * Diese Klasse ermöglicht das Laden, Speichern und Bearbeiten von Anwendungseinstellungen, die für die
 * Benutzerkonfiguration relevant sind.
 *
 * Der {@code EinstellungenManager} ist eine zentrale Komponente für die Personalisierung der Anwendung
 * und stellt sicher, dass Änderungen an den Einstellungen beim nächsten Start der Anwendung verfügbar sind.
 */
public class EinstellungenManager {

    private final SettingsRepository settingsRepository = SettingsRepository.getInstance();  // Singleton Pattern

    /**
     * Lädt die Einstellungen aus der Datenbank.
     *
     * @return Die Einstellungen aus der Datenbank.
     */
    public Einstellungen_Model loadSettings() {
        return settingsRepository.getSettings();
    }

    /**
     * Speichert die Einstellungen in der Datenbank.
     *
     * @param settings Die zu speichernden Einstellungen.
     */
    public void saveSettings(Einstellungen_Model settings) {
        settingsRepository.updateSettings(settings);
    }

    /**
     * Validiert die Benutzereingaben.
     *
     * @param settings Die Einstellungen, die validiert werden sollen.
     * @return true, wenn die Eingaben gültig sind, sonst false.
     */
    public boolean validateSettings(Einstellungen_Model settings) {
        if (settings.getEmailAddress().isEmpty()
                || settings.getSmtpUsername().isEmpty()
                || settings.getSmtpPassword().isEmpty()) {
            return false;
        }

        try {
            Integer.parseInt(String.valueOf(settings.getSmtpPort()));
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }
}
