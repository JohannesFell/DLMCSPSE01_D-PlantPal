package com.plantpal.test;

import com.plantpal.database.CareTaskHistoryRepository;
import com.plantpal.database.PlantProfileRepository;
import com.plantpal.logic.PflanzenProfileService;
import com.plantpal.model.PflanzenProfile_Model;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testklasse für den {@code PflanzenProfileService}.
 *
 * Diese Klasse testet die grundlegenden CRUD-Operationen des PflanzenProfileService,
 * einschließlich Hinzufügen, Aktualisieren und Löschen von Pflanzenprofilen.
 *
 * Sie stellt sicher, dass die Geschäftslogik korrekt ausgeführt wird und die Datenbank
 * konsistent bleibt.
 */
public class PflanzenProfileServiceTest {

    private static PflanzenProfileService pflanzenProfileService;
    private static PlantProfileRepository plantProfileRepository;
    private static int testPlantId;
    private static final List<String[]> testResults = new ArrayList<>();

    /**
     * Initialisiert die benötigten Objekte vor allen Tests.
     *
     * Erstellt Instanzen des {@code PflanzenProfileService} und des {@code PlantProfileRepository}.
     */
    @BeforeAll
    public static void setUp() {
        plantProfileRepository = new PlantProfileRepository();
        CareTaskHistoryRepository careTaskHistoryRepository = new CareTaskHistoryRepository();
        pflanzenProfileService = new PflanzenProfileService(plantProfileRepository, careTaskHistoryRepository);
    }

    /**
     * Fügt vor jedem Test eine Testpflanze hinzu.
     *
     * Diese Testpflanze wird in den Tests verwendet, um die verschiedenen Operationen
     * (Hinzufügen, Aktualisieren, Löschen) durchzuführen.
     */
    @BeforeEach
    public void addTestPlant() {
        PflanzenProfile_Model testPlant = new PflanzenProfile_Model(
                0, "JUnit Testpflanze", "Testus JUnitus", LocalDate.now(), "Testgarten", 7, 14, LocalDate.now(), LocalDate.now(), ""
        );
        pflanzenProfileService.addPlantProfile(testPlant);

        // Hole die ID der neu hinzugefügten Pflanze
        List<PflanzenProfile_Model> allPlants = pflanzenProfileService.getAllPlantProfiles();
        testPlantId = allPlants.stream()
                .filter(plant -> "JUnit Testpflanze".equals(plant.getPlant_name()))
                .findFirst()
                .map(PflanzenProfile_Model::getPlant_id)
                .orElse(-1);

        assertNotEquals(-1, testPlantId, "Testpflanze wurde nicht korrekt hinzugefügt.");
    }

    /**
     * Testet das Hinzufügen eines Pflanzenprofils.
     *
     * Überprüft, ob das Pflanzenprofil korrekt in der Datenbank gespeichert wurde.
     */
    @Test
    public void testAddPlantProfile() {
        try {
            PflanzenProfile_Model addedPlant = plantProfileRepository.getPlantProfileById(testPlantId);
            assertNotNull(addedPlant, "Das Pflanzenprofil sollte nicht null sein.");
            assertEquals("JUnit Testpflanze", addedPlant.getPlant_name(), "Der Pflanzenname sollte übereinstimmen.");
            testResults.add(new String[]{"testAddPlantProfile", "Bestanden"});
        } catch (AssertionError e) {
            testResults.add(new String[]{"testAddPlantProfile", "Nicht bestanden"});
            throw e;
        }
    }

    /**
     * Testet das Aktualisieren eines Pflanzenprofils.
     *
     * Ändert den Namen und andere Eigenschaften der Testpflanze und überprüft,
     * ob die Änderungen in der Datenbank korrekt gespeichert wurden.
     */
    @Test
    public void testUpdatePlantProfile() {
        try {
            PflanzenProfile_Model updatedPlant = new PflanzenProfile_Model(
                    testPlantId, "Aktualisierte JUnit Testpflanze", "Testus JUnitus Updated", LocalDate.now(), "Neuer Testgarten", 5, 10, LocalDate.now(), LocalDate.now(), ""
            );
            assertDoesNotThrow(() -> pflanzenProfileService.updatePlantProfile(updatedPlant));

            PflanzenProfile_Model retrievedPlant = plantProfileRepository.getPlantProfileById(testPlantId);
            assertNotNull(retrievedPlant, "Das aktualisierte Pflanzenprofil sollte nicht null sein.");
            assertEquals("Aktualisierte JUnit Testpflanze", retrievedPlant.getPlant_name(), "Der aktualisierte Pflanzenname sollte übereinstimmen.");
            testResults.add(new String[]{"testUpdatePlantProfile", "Bestanden"});
        } catch (AssertionError e) {
            testResults.add(new String[]{"testUpdatePlantProfile", "Nicht bestanden"});
            throw e;
        }
    }

    /**
     * Testet das Löschen eines Pflanzenprofils.
     *
     * Überprüft, ob die Testpflanze erfolgreich aus der Datenbank gelöscht wurde.
     */
    @Test
    public void testDeletePlantProfile() {
        try {
            pflanzenProfileService.deletePlantProfile(testPlantId);

            PflanzenProfile_Model deletedPlant = plantProfileRepository.getPlantProfileById(testPlantId);
            assertNull(deletedPlant, "Das Pflanzenprofil sollte gelöscht worden sein und null zurückgeben.");
            testResults.add(new String[]{"testDeletePlantProfile", "Bestanden"});
        } catch (AssertionError e) {
            testResults.add(new String[]{"testDeletePlantProfile", "Nicht bestanden"});
            throw e;
        }
    }

    /**
     * Bereinigt die Datenbank nach jedem Test.
     *
     * Löscht die Testpflanze, falls sie noch existiert, um sicherzustellen, dass
     * die Tests unabhängig voneinander ausgeführt werden können.
     */
    @AfterEach
    public void cleanUp() {
        try {
            plantProfileRepository.deletePlantByName("JUnit Testpflanze");
            plantProfileRepository.deletePlantByName("Aktualisierte JUnit Testpflanze");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Gibt eine Übersicht der Testergebnisse in Tabellenform aus.
     *
     * Diese Methode wird nach allen Tests ausgeführt und zeigt an,
     * welche Tests bestanden wurden und welche nicht.
     */
    @AfterAll
    public static void printTestSummary() {
        System.out.println("\nTestergebnisse:");
        System.out.printf("%-30s %-15s\n", "Testname", "Ergebnis");
        System.out.println("---------------------------------------------");
        for (String[] result : testResults) {
            System.out.printf("%-30s %-15s\n", result[0], result[1]);
        }
        System.out.println("---------------------------------------------");
    }
}
