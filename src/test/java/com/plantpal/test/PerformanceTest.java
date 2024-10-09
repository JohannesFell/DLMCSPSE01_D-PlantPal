package com.plantpal.test;

import com.plantpal.app.PlantPalApp;
import com.plantpal.database.PlantProfileRepository;
import com.plantpal.database.SQLiteDB;
import com.plantpal.logic.EinstellungenManager;
import com.plantpal.logic.PflanzenProfileService;
import com.plantpal.model.PflanzenProfile_Model;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Performancetests für die Anwendung.
 * Testet verschiedene Operationen auf ihre Ausführungszeit, einschließlich Startzeit,
 * Hinzufügen und Löschen von Pflanzen sowie das Laden von Daten.
 */
public class PerformanceTest {

    private static final List<String[]> results = new ArrayList<>();
    private static PlantProfileRepository plantProfileRepository;
    private static PflanzenProfileService pflanzenProfileService;

    /**
     * Initialisiert die Testumgebung, indem die Datenbankverbindung eingerichtet
     * und die benötigten Tabellen erstellt werden.
     *
     */
    @BeforeAll
    public static void setUp() {
        SQLiteDB.createTables();

        plantProfileRepository = new PlantProfileRepository();
        pflanzenProfileService = new PflanzenProfileService(plantProfileRepository, null);
    }

    /**
     * Gibt nach Abschluss aller Tests die Ergebnisse formatiert in der Konsole aus
     * und schließt die Datenbankverbindung.
     */
    @AfterAll
    public static void printResults() {
        System.out.println("\nPerformancetests Ergebnisse:");
        System.out.printf("%-40s %-20s %-20s %-15s\n", "Operation", "Zielwert (ms)", "Messwert (ms)", "Ergebnis");
        System.out.println("----------------------------------------------------------------------------------------");

        for (String[] result : results) {
            String status = Long.parseLong(result[2]) <= Long.parseLong(result[1]) ? "Bestanden" : "Nicht bestanden";
            System.out.printf("%-40s %-20s %-20s %-15s\n", result[0], result[1], result[2], status);
        }
        System.out.println("----------------------------------------------------------------------------------------");

        SQLiteDB.close();
    }


    /**
     * Löscht nach jedem Test eine eventuell vorhandene Testpflanze, um sicherzustellen,
     * dass keine Testdaten in der Datenbank verbleiben.
     *
     * @throws SQLException falls ein Fehler beim Löschen aus der Datenbank auftritt
     */
    @AfterEach
    public void cleanUp() throws SQLException {
        plantProfileRepository.deletePlantByName("Testpflanze");
    }

    /**
     * Testet, wie lange es dauert, eine neue Datenbankverbindung herzustellen.
     * Ziel ist es sicherzustellen, dass die Verbindungszeit innerhalb eines akzeptablen Bereichs bleibt.
     * Dieser Test hilft, die Effizienz des Verbindungs-Pooling zu überwachen.
     */
    @Test
    public void testDatabaseConnectionTime() {
        long startTime = System.currentTimeMillis();

        try (Connection connection = SQLiteDB.getConnection()) {
            assertNotNull(connection, "Datenbankverbindung konnte nicht hergestellt werden.");
        } catch (SQLException e) {
            fail("Fehler beim Herstellen der Datenbankverbindung: " + e.getMessage());
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        results.add(new String[]{"Datenbankverbindung herstellen", "100", String.valueOf(duration), duration <= 100 ? "Bestanden" : "Nicht bestanden"});
        assertTrue(duration <= 100, "Das Herstellen der Datenbankverbindung hat länger als 100 ms gedauert");
    }


    /**
     * Testet, ob die Anwendungsstartzeit (Simulation) innerhalb von 10 Sekunden bleibt.
     * Diese Simulation wird verwendet, da ein vollständiger Start der JavaFX-Anwendung nicht für Unit-Tests
     * geeignet ist. Der Test umfasst wesentliche Initialisierungen wie das Starten des Schedulers, das
     * Laden der Einstellungen und die Erstellung der Datenbanktabellen. Dadurch kann eine realistischere
     * Abschätzung der Startzeit vorgenommen werden, ohne die Abhängigkeiten der Benutzeroberfläche zu testen.
     */
    @Test
    public void testApplicationStartupPerformance() {
        long startTime = System.currentTimeMillis();

        PlantPalApp app = new PlantPalApp();

        // Simulierter Anwendungsstart
        app.startNotificationScheduler(); // Starten des Schedulers
        SQLiteDB.createTables(); // Erstellen der Datenbanktabellen
        EinstellungenManager einstellungenManager = new EinstellungenManager();
        einstellungenManager.loadSettings(); // Laden der Einstellungen

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        results.add(new String[]{"Anwendungsstart (simuliert)", "10000", String.valueOf(duration), (duration <= 10000 ? "Bestanden" : "Nicht bestanden")});
        assertTrue(duration <= 10000, "Die Startzeit der Anwendung hat mehr als 10 Sekunden gedauert");
    }

    /**
     * Testet, ob das Hinzufügen eines Pflanzenprofils innerhalb von 100 ms abgeschlossen wird.
     *
     */
    @Test
    public void testAddPlantPerformance() {
        long startTime = System.currentTimeMillis();

        PflanzenProfile_Model newPlant = new PflanzenProfile_Model(-1, "Testpflanze", "Botanischer Name",
                LocalDate.now(), "Standort", 7, 14, LocalDate.now(), LocalDate.now(), "");

        pflanzenProfileService.addPlantProfile(newPlant);

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        results.add(new String[]{"Hinzufügen einer Pflanze", "100", String.valueOf(duration)});
        assertTrue(duration <= 100, "Das Hinzufügen einer Pflanze hat länger als 100 ms gedauert");
    }

    /**
     * Testet, ob das Löschen eines Pflanzenprofils innerhalb von 100 ms abgeschlossen wird.
     *
     */
    @Test
    public void testDeletePlantPerformance() {
        PflanzenProfile_Model plantToDelete = new PflanzenProfile_Model(-1, "Testpflanze", "Botanischer Name",
                LocalDate.now(), "Standort", 7, 14, LocalDate.now(), LocalDate.now(), "");
        pflanzenProfileService.deletePlantProfile(plantToDelete.getPlant_id());

        long startTime = System.currentTimeMillis();

        pflanzenProfileService.deletePlantProfile(plantToDelete.getPlant_id());

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        results.add(new String[]{"Löschen einer Pflanze", "100", String.valueOf(duration)});
        assertTrue(duration <= 100, "Das Löschen einer Pflanze hat länger als 100 ms gedauert");
    }

    /**
     * Testet, ob das Laden aller Pflanzenprofile innerhalb von 100 ms abgeschlossen wird.
     *
     */
    @Test
    public void testLoadAllPlantProfilesPerformance() {
        long startTime = System.currentTimeMillis();

        plantProfileRepository.getAllPlantProfiles();

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        results.add(new String[]{"Laden aller Pflanzenprofile", "100", String.valueOf(duration)});
        assertTrue(duration <= 100, "Das Laden aller Pflanzenprofile hat länger als 100 ms gedauert");
    }
}
