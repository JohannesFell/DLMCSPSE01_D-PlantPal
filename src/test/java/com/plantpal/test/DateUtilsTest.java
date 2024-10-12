package com.plantpal.test;

import com.plantpal.utils.DateUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testklasse für DateUtils.
 * Diese Klasse enthält Tests für die Methoden parseDate(), formatDate() und appendTimestampToFileName().
 */
public class DateUtilsTest {

    // Liste für die Testergebnisse
    private static final List<String[]> testResults = new ArrayList<>();

    /**
     * Test für parseDate() mit gültigen Datumsformaten.
     */
    @Test
    public void testParseDateWithValidFormats() {
        String testName = "testParseDateWithValidFormats";
        String input = "11.10.2024";
        LocalDate expectedDate = LocalDate.of(2024, 10, 11);
        LocalDate result = DateUtils.parseDate(input);

        try {
            assertEquals(expectedDate, result);
            testResults.add(new String[]{testName, input, result != null ? result.toString() : "null", "Bestanden"});
        } catch (AssertionError e) {
            testResults.add(new String[]{testName, input, result != null ? result.toString() : "null", "Nicht Bestanden"});
        }
    }

    /**
     * Test für parseDate() mit ungültigen und leeren Datumsformaten.
     */
    @Test
    public void testParseDateWithInvalidFormats() {
        String testName = "testParseDateWithInvalidFormats";
        String input = "2024-10-11"; // Ungültiges Format
        LocalDate result = DateUtils.parseDate(input);

        try {
            assertNull(result);
            testResults.add(new String[]{testName, input, "null", "Bestanden"});
        } catch (AssertionError e) {
            testResults.add(new String[]{testName, input, result != null ? result.toString() : "null", "Nicht Bestanden"});
        }
    }

    /**
     * Test für formatDate() mit gültigen Eingaben.
     */
    @Test
    public void testFormatDateWithValidDate() {
        String testName = "testFormatDateWithValidDate";
        LocalDate validDate = LocalDate.of(2024, 10, 11);
        String expectedFormattedDate = "11.10.2024";
        String result = DateUtils.formatDate(validDate);

        try {
            assertEquals(expectedFormattedDate, result);
            testResults.add(new String[]{testName, validDate.toString(), result, "Bestanden"});
        } catch (AssertionError e) {
            testResults.add(new String[]{testName, validDate.toString(), result, "Nicht Bestanden"});
        }
    }


    /**
     * Test für formatDate() mit einem Unix-Zeitstempel (Millisekunden) als Eingabe.
     * Der Unix-Zeitstempel wird in ein LocalDate konvertiert und dann formatiert.
     */
    @Test
    public void testFormatDateWithUnixTimestamp() {
        String testName = "testFormatDateWithUnixTimestamp";

        // Unix-Timestamp (1727820000000 Millisekunden entspricht dem 02.10.2024)
        long timestamp = 1727820000000L;

        // Konvertiere den Unix-Timestamp in LocalDate
        LocalDate dateFromTimestamp = Instant.ofEpochMilli(timestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        // Das erwartete formatierte Datum
        String expectedFormattedDate = "02.10.2024";
        String result = DateUtils.formatDate(dateFromTimestamp);

        try {
            // Überprüfen, ob das Datum korrekt formatiert wurde
            assertEquals(expectedFormattedDate, result);
            testResults.add(new String[]{testName, Long.toString(timestamp), result, "Bestanden"});
        } catch (AssertionError e) {
            testResults.add(new String[]{testName, Long.toString(timestamp), result, "Nicht Bestanden"});
        }
    }

    /**
     * Test für formatDate() mit null als Eingabe.
     */
    @Test
    public void testFormatDateWithNull() {
        String testName = "testFormatDateWithNull";
        String result = DateUtils.formatDate(null);

        try {
            assertEquals("", result);
            testResults.add(new String[]{testName, "null", result, "Bestanden"});
        } catch (AssertionError e) {
            testResults.add(new String[]{testName, "null", result, "Nicht Bestanden"});
        }
    }

    /**
     * Test für appendTimestampToFileName(), um sicherzustellen, dass der Zeitstempel korrekt angehängt wird.
     */
    @Test
    public void testAppendTimestampToFileName() {
        String testName = "testAppendTimestampToFileName";
        String inputFileName = "testfile.txt";
        String result = DateUtils.appendTimestampToFileName(inputFileName);

        try {
            assertTrue(result.matches("testfile_\\d{8}_\\d{6}\\.txt"));
            testResults.add(new String[]{testName, inputFileName, result, "Bestanden"});
        } catch (AssertionError e) {
            testResults.add(new String[]{testName, inputFileName, result, "Nicht Bestanden"});
        }
    }

    /**
     * Gibt eine Übersicht der Testergebnisse in Tabellenform aus.
     * Diese Methode wird nach allen Tests ausgeführt und zeigt an,
     * welche Tests bestanden wurden und welche nicht.
     */
    @AfterAll
    public static void printTestSummary() {
        System.out.println("\nTestergebnisse:");
        System.out.printf("%-40s %-20s %-50s %-15s\n", "Testname", "Eingabe", "Ausgabe", "Ergebnis");
        System.out.println("----------------------------------------------------------------------------------------------------------");
        for (String[] result : testResults) {
            System.out.printf("%-40s %-20s %-50s %-15s\n", result[0], result[1], result[2], result[3]);
        }
        System.out.println("----------------------------------------------------------------------------------------------------------");
    }
}
