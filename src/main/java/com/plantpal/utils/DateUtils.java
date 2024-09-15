package com.plantpal.utils;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Dienstprogrammklasse zur Formatierung von Datumswerten in einem bestimmten Format.
 */
public class DateUtils {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

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
}
