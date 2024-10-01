package com.plantpal.utils;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Dienstprogrammklasse zur Formatierung von Datumswerten in einem bestimmten Format.
 */
public class DateUtils {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    /**
     * Formatiert ein LocalDate in einen String im "dd.MM.yyyy"-Format und gibt es als ObservableValue zurück.
     *
     * @param date Das zu formatierende LocalDate. Wenn der Wert null ist, wird ein leerer String zurückgegeben.
     * @return Ein ObservableValue, das das formatierte Datum als String enthält.
     */
    public static ObservableValue<String> formatProperty(LocalDate date) {
        String formattedDate = (date != null) ? date.format(DATE_FORMATTER) : "";
        return new SimpleStringProperty(formattedDate);
    }

    /**
     * Formatiert ein LocalDate in einen String im "dd.MM.yyyy"-Format.
     *
     * @param date Das zu formatierende LocalDate. Wenn der Wert null ist, wird ein leerer String zurückgegeben.
     * @return Das formatierte Datum als String oder ein leerer String, wenn das Datum null ist.
     */
    public static String formatDate(LocalDate date) {
        return (date != null) ? date.format(DATE_FORMATTER) : "";
    }

    /**
     * Wandelt einen String in ein LocalDate um, basierend auf dem "dd.MM.yyyy"-Format.
     *
     * @param dateString Der zu parsende String, der ein Datum im "dd.MM.yyyy"-Format enthält.
     * @return Das geparste LocalDate oder null, wenn der String nicht gültig ist.
     */
    public static LocalDate parseDate(String dateString) {
        try {
            return (dateString != null && !dateString.isEmpty()) ? LocalDate.parse(dateString, DATE_FORMATTER) : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Methode, um einen Zeitstempel zu generieren und an einen Dateinamen anzuhängen
     *
     * @param fileName Dateiname
     * @return Den formatierten Dateinamen mit TimeStamp als suffix.
     */
    public static String appendTimestampToFileName(String fileName) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String timestamp = LocalDateTime.now().format(formatter);

        String fileExtension = fileName.substring(fileName.lastIndexOf("."));
        String baseName = fileName.substring(0, fileName.lastIndexOf("."));

        return baseName + "_" + timestamp + fileExtension;
    }

    /**
     * Formatiert einen LocalDateTime-Wert im Format "dd.MM.yyyy HH:mm:ss".
     *
     * @param dateTime Das zu formatierende LocalDateTime.
     * @return Das formatierte Datum und die Uhrzeit als String.
     */
    public static String formatTimestamp(LocalDateTime dateTime) {
        return (dateTime != null) ? dateTime.format(TIMESTAMP_FORMATTER) : "";
    }
}
