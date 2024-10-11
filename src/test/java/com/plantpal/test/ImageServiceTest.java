package com.plantpal.test;

import com.plantpal.logic.ImageService;
import com.plantpal.database.PhotoLogRepository;
import com.plantpal.model.PflanzenProfile_Model;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testklasse für den ImageService.
 * Diese Klasse enthält Tests für das Importieren von Bildern sowie die Behandlung
 * ungültiger Dateiformate.
 */
public class ImageServiceTest {

    private ImageService imageService;
    private PhotoLogRepository photoLogRepository;
    private PflanzenProfile_Model testPlant;
    private String savedImagePath;
    private static final List<String[]> testResults = new ArrayList<>();

    /**
     * Vorbereitungs-Methode, die vor jedem Test ausgeführt wird.
     * Initialisiert die benötigten Objekte und erstellt ein Testpflanzenprofil.
     *
     */
    @BeforeEach
    public void setUp() {
        photoLogRepository = new PhotoLogRepository();
        imageService = new ImageService();

        // Erstellen eines Testpflanzenprofils
        testPlant = new PflanzenProfile_Model(0, "Testpflanze", "Botanischer Name",
                LocalDateTime.now().toLocalDate(),
                "Standort", 7, 14, LocalDateTime.now().toLocalDate(),
                LocalDateTime.now().toLocalDate(), "");
    }

    /**
     * Test für den erfolgreichen Import eines Bildes.
     * Überprüft, ob das Bild korrekt in den Zielordner kopiert wird und ob der
     * entsprechende Datenbankeintrag erstellt wurde.
     */
    @Test
    public void testImportImageSuccess() {
        String testName = "testImportImageSuccess";
        Path testImagePath = null;
        try {
            // Vorbereitung: Testbild erstellen
            testImagePath = Files.createTempFile("testImage", ".jpg");
            Files.write(testImagePath, new byte[10]); // Beispielinhalt schreiben

            // Bild importieren
            File selectedFile = testImagePath.toFile();
            imageService.importImage(selectedFile, testPlant);

            // Überprüfen, ob der Dateieintrag im Zielordner existiert
            Path destinationDir = Path.of("src/main/resources/images/uploads/");
            File[] files = destinationDir.toFile().listFiles();
            boolean fileExists = false;
            if (files != null && files.length > 0) {
                for (File file : files) {
                    if (file.getName().contains("testImage") && file.getName().endsWith(".jpg")) {
                        fileExists = true;
                        break;
                    }
                }
            }
            assertTrue(fileExists, "Das Bild sollte im Zielordner vorhanden sein");

            // Überprüfen, ob der Datenbankeintrag erstellt wurde
            savedImagePath = photoLogRepository.getLatestPhotoPath(testPlant.getPlant_id());
            assertTrue(savedImagePath != null && !savedImagePath.isEmpty(),
                    "Der Bildpfad sollte in der Datenbank gespeichert sein");

            // Wenn alle Überprüfungen bestanden wurden
            testResults.add(new String[]{testName, "Bestanden", testImagePath.toString()});

        } catch (Exception e) {
            testResults.add(new String[]{testName, "Nicht Bestanden", testImagePath != null ? testImagePath.toString() : "N/A"});
            e.printStackTrace();
        }
    }

    /**
     * Test für den Import eines Bildes mit ungültigem Dateiformat.
     * Überprüft, ob bei ungültigen Dateiformaten (keine PNG, JPG, JPEG) eine
     * IllegalArgumentException ausgelöst wird.
     */
    @Test
    public void testImportImageWithInvalidFormat() {
        String testName = "testImportImageWithInvalidFormat";
        Path testImagePath = null;
        try {
            // Vorbereitung: Testbild mit ungültigem Format erstellen
            testImagePath = Files.createTempFile("testImage", ".txt");
            Files.write(testImagePath, new byte[10]); // Beispielinhalt schreiben

            File selectedFile = testImagePath.toFile();

            // Erwartet, dass eine IllegalArgumentException ausgelöst wird
            assertThrows(IllegalArgumentException.class, () -> imageService.importImage(selectedFile, testPlant), "Es sollte eine Ausnahme für ungültige Dateiformate ausgelöst werden");

            // Wenn die Ausnahme korrekt ausgelöst wurde
            testResults.add(new String[]{testName, "Bestanden", testImagePath.toString()});

        } catch (Exception e) {
            testResults.add(new String[]{testName, "Nicht Bestanden", testImagePath != null ? testImagePath.toString() : "N/A"});
            e.printStackTrace();
        } finally {
            // Aufräumen
            if (testImagePath != null) {
                try {
                    Files.deleteIfExists(testImagePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Aufräum-Methode, die nach jedem Test ausgeführt wird.
     * Löscht das importierte Testbild und entfernt den entsprechenden
     * Datenbankeintrag, um die Ausgangsbedingungen wiederherzustellen.
     *
     * @throws Exception wenn ein Fehler beim Aufräumen auftritt.
     */
    @AfterEach
    public void tearDown() throws Exception {
        // Aufräumen: Testbild und Datenbankeintrag entfernen
        if (savedImagePath != null) {
            // Lösche die Datei
            File savedFile = new File(savedImagePath);
            if (savedFile.exists()) {
                // aus dem Dateisystem
                savedFile.delete();
            }

            // Lösche den Datensatz aus der Datenbank
            photoLogRepository.deletePhotoByPath(savedImagePath);
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
        System.out.printf("%-40s %-15s %-50s\n", "Testname", "Ergebnis", "Dateipfad");
        System.out.println("-------------------------------------------------------------------------------");
        for (String[] result : testResults) {
            System.out.printf("%-40s %-15s %-50s\n", result[0], result[1], result[2]);
        }
        System.out.println("-------------------------------------------------------------------------------");
    }
}
