package com.plantpal.app;

import com.plantpal.database.SQLiteDB;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class PflanzenProfileController implements Initializable {

    @FXML
    private TextField botanical_name, kaufdatum, last_duengen, last_giessen, name, search, standort;

    @FXML private Label notificationLabel;

    @FXML
    private Button btn_add, btn_clear, btn_delete, btn_update, image_import, image_show;

    @FXML
    private ImageView image;

    @FXML
    private ComboBox<Integer> intervall_duengen, intervall_giessen;

    @FXML
    private TableView<PflanzenProfile_Model> pflanzenProfil_tableView;

    @FXML
    private TableColumn<PflanzenProfile_Model, String> profile_bot_name_col, profile_name_col, profile_standort_col;

    @FXML
    private TableColumn<PflanzenProfile_Model, Integer> profile_int_duengen_col, profile_int_giessen_col;

    @FXML
    private TableColumn<PflanzenProfile_Model, LocalDate> profile_kaufdatum, profile_last_duengen_col,
            profile_last_giessen_col;

    private ObservableList<PflanzenProfile_Model> plantData;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private FilteredList<PflanzenProfile_Model> filteredData;

    /**
     * Initialisiert den Controller.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        plantData = FXCollections.observableArrayList();
        // FilteredList mit der ObservableList initialisieren
        filteredData = new FilteredList<>(plantData, p -> true);

        // Einrichten der TableView-Spalten
        setupTableColumns();

        // Daten aus der Datenbank laden und in die TableView einfügen
        loadPlantData();

        // Logik für die Suchfunktion anwenden
        applySearchFilter();

        // Formular leeren, wenn die Seite das erste Mal geladen wird
        clearFormFields();

        // Listener für die Zeilenauswahl in der TableView hinzufügen
        pflanzenProfil_tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                populateFormFields(newSelection);
            }
        });

        // Die TableView mit den gefilterten Daten verbinden
        pflanzenProfil_tableView.setItems(filteredData);
    }

    /**
     * Lädt die Pflanzenprofildaten aus der Datenbank und aktualisiert die TableView
     */
    private void loadPlantData() {
        plantData.clear();
        String sql = "SELECT * FROM PlantProfile";
        try (Connection conn = SQLiteDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                PflanzenProfile_Model plant = new PflanzenProfile_Model(
                        rs.getInt("plant_id"),
                        rs.getString("plant_name"),
                        rs.getString("botanical_plant_name"),
                        rs.getDate("purchase_date").toLocalDate(),
                        rs.getString("location"),
                        rs.getInt("watering_interval"),
                        rs.getInt("fertilizing_interval"),
                        rs.getDate("last_watered").toLocalDate(),
                        rs.getDate("last_fertilized").toLocalDate(),
                        rs.getString("image_path")
                );
                plantData.add(plant);
            }
            pflanzenProfil_tableView.setItems(plantData);
        } catch (Exception e) {
            System.out.println("Daten konnten nicht geladen werden.");
            e.printStackTrace();
        }
    }

    /**
     * Fügt ein neues Pflanzenprofil in die Datenbank ein.
     */
    @FXML
    private void addPlant() {
        String sql = "INSERT INTO PlantProfile (plant_name, botanical_plant_name, purchase_date, location, " +
                "watering_interval, fertilizing_interval, last_watered, last_fertilized, image_path) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = SQLiteDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name.getText());
            pstmt.setString(2, botanical_name.getText());
            // Kaufdatum im Format dd.MM.yyyy
            pstmt.setDate(3, java.sql.Date.valueOf(LocalDate.parse(kaufdatum.getText(), DATE_FORMATTER)));
            pstmt.setString(4, standort.getText());
            pstmt.setInt(5, getComboBoxValue(intervall_giessen));
            pstmt.setInt(6, getComboBoxValue(intervall_duengen));
            // last_watered auf das heutige Datum setzen
            pstmt.setDate(7, java.sql.Date.valueOf(LocalDate.now()));
            // last_fertilized auf das heutige Datum setzen
            pstmt.setDate(8, java.sql.Date.valueOf(LocalDate.now()));
            // Bildpfad vorerst leer
            pstmt.setString(9, "");

            pstmt.executeUpdate();
            loadPlantData();
            pflanzenProfil_tableView.refresh();
            clearFormFields();  // Formular nach dem Hinzufügen leeren
            showNotification("Pflanze erfolgreich hinzugefügt!");

        } catch (SQLException e) {
            System.out.println("Fehler beim Hinzufügen der Pflanze.");
            e.printStackTrace();
        }
    }

    /**
     * Löscht ein ausgewähltes Pflanzenprofil aus der Datenbank.
     */
    @FXML
    private void deletePlant() {
        PflanzenProfile_Model selectedPlant = pflanzenProfil_tableView.getSelectionModel().getSelectedItem();
        // Prüfe, ob eine Pflanze ausgewählt wurde
        if (selectedPlant != null) {
            String sql = "DELETE FROM PlantProfile WHERE plant_id = ?";
            try (Connection conn = SQLiteDB.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                // Setze die plant_id des ausgewählten Eintrags
                pstmt.setInt(1, selectedPlant.getPlant_id());
                // Führe das Löschen durch
                pstmt.executeUpdate();
                // Aktualisiere die TableView, um die gelöschte Pflanze zu entfernen
                plantData.remove(selectedPlant);
                pflanzenProfil_tableView.refresh();
                // Leere das Formular, nachdem der Eintrag gelöscht wurde
                clearFormFields();
                showNotification("Pflanze erfolgreich gelöscht!");
            } catch (SQLException e) {
                System.out.println("Fehler beim Löschen der Pflanze.");
                e.printStackTrace();
            }
        }
    }

    /**
     * Aktualisiert das ausgewählte Pflanzenprofil in der Datenbank.
     */
    @FXML
    private void updatePlant() {
        // Hole die ausgewählte Pflanze aus der TableView
        PflanzenProfile_Model selectedPlant = pflanzenProfil_tableView.getSelectionModel().getSelectedItem();
        // Prüfe, ob eine Pflanze ausgewählt wurde
        if (selectedPlant != null) {
            String sql = "UPDATE PlantProfile SET plant_name = ?, botanical_plant_name = ?, purchase_date = ?, " +
                    "location = ?, watering_interval = ?, fertilizing_interval = ? WHERE plant_id = ?";
            try (Connection conn = SQLiteDB.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                // Setze die aktualisierten Werte in die SQL-Abfrage
                pstmt.setString(1, name.getText());
                pstmt.setString(2, botanical_name.getText());
                pstmt.setDate(3, java.sql.Date.valueOf(LocalDate.parse(kaufdatum.getText(), DATE_FORMATTER)));
                pstmt.setString(4, standort.getText());
                pstmt.setInt(5, getComboBoxValue(intervall_giessen));
                pstmt.setInt(6, getComboBoxValue(intervall_duengen));
                pstmt.setInt(7, selectedPlant.getPlant_id());

                // Führe das Update aus
                pstmt.executeUpdate();
                loadPlantData();
                pflanzenProfil_tableView.refresh();
                clearFormFields();  // Formular nach dem Update leeren
                showNotification("Pflanze erfolgreich aktualisiert!");
            } catch (SQLException e) {
                System.out.println("Fehler beim Aktualisieren der Pflanze.");
                e.printStackTrace();
            }
        }
    }

    /**
     * Filtert die Pflanzenprofildaten basierend auf der Sucheingabe.
     * Die Suche filtert nach Name, botanischer Name und Standort.
     */
    private void applySearchFilter() {
        // Hinzufügen eines Listeners für das TextField "search"
        search.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(plant -> {
                // Wenn das Suchfeld leer ist, zeige alle Daten an
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                // Verarbeite die Eingabe als Suchtext, ignoriere Groß-/Kleinschreibung
                String lowerCaseFilter = newValue.toLowerCase();
                return plant.getPlant_name().toLowerCase().contains(lowerCaseFilter) ||
                        plant.getBotanical_plant_name().toLowerCase().contains(lowerCaseFilter) ||
                        plant.getLocation().toLowerCase().contains(lowerCaseFilter);
            });
            pflanzenProfil_tableView.refresh();  // Stellt sicher, dass die gefilterten Daten angezeigt werden
        });
    }

    /**
     * Leert das Formular für neue Einträge.
     */
    @FXML
    private void clearFormFields() {
        // Alle Textfelder leeren
        name.clear();
        botanical_name.clear();
        kaufdatum.clear();
        standort.clear();

        // Komboboxen auf Standardwert setzen
        intervall_giessen.getSelectionModel().clearSelection();
        intervall_duengen.getSelectionModel().clearSelection();
        last_giessen.clear();
        last_duengen.clear();

        // Bildvorschau/Pfad zurücksetzen (optional, falls verwendet)
        pflanzenProfil_tableView.getSelectionModel().clearSelection();
    }

    /**
     * Logik zum Ausfüllen der Formularfelder basierend auf der ausgewählten Zeile in der TableView.
     */
    private void populateFormFields(PflanzenProfile_Model plant) {
        name.setText(plant.getPlant_name());
        botanical_name.setText(plant.getBotanical_plant_name());
        kaufdatum.setText(plant.getPurchase_date() != null ? plant.getPurchase_date().format(DATE_FORMATTER) : "");
        standort.setText(plant.getLocation());
        intervall_giessen.setValue(plant.getWatering_interval());
        intervall_duengen.setValue(plant.getFertilizing_interval());
        last_giessen.setText(plant.getLast_watered() != null ? plant.getLast_watered().format(DATE_FORMATTER) : "");
        last_duengen.setText(plant.getLast_fertilized() != null ? plant.getLast_fertilized().format(DATE_FORMATTER) : "");
    }



    /**
     * Einrichten der TableView-Spalten.
     */
    private void setupTableColumns() {
        profile_name_col.setCellValueFactory(new PropertyValueFactory<>("plant_name"));
        profile_bot_name_col.setCellValueFactory(new PropertyValueFactory<>("botanical_plant_name"));
        profile_kaufdatum.setCellValueFactory(new PropertyValueFactory<>("purchase_date"));
        profile_standort_col.setCellValueFactory(new PropertyValueFactory<>("location"));
        profile_int_giessen_col.setCellValueFactory(new PropertyValueFactory<>("watering_interval"));
        profile_int_duengen_col.setCellValueFactory(new PropertyValueFactory<>("fertilizing_interval"));
        profile_last_giessen_col.setCellValueFactory(new PropertyValueFactory<>("last_watered"));
        profile_last_duengen_col.setCellValueFactory(new PropertyValueFactory<>("last_fertilized"));

        // Wendet die Datumsformatierung auf die Spalten an
        applyDateFormatting(DATE_FORMATTER, profile_kaufdatum, profile_last_giessen_col, profile_last_duengen_col);
    }

    /**
     * Wendet die Datumsformatierung auf die gegebenen Spalten an.
     */
    @SafeVarargs
    private void applyDateFormatting(DateTimeFormatter formatter, TableColumn<PflanzenProfile_Model, LocalDate>... columns) {
        for (TableColumn<PflanzenProfile_Model, LocalDate> column : columns) {
            column.setCellFactory(col -> new TextFieldTableCell<>(new StringConverter<>() {
                @Override
                public String toString(LocalDate date) {
                    return date != null ? date.format(formatter) : "";
                }

                @Override
                public LocalDate fromString(String string) {
                    return string != null && !string.isEmpty() ? LocalDate.parse(string, formatter) : null;
                }
            }));
        }
    }

    /**
     * Hilfsfunktion, um den Wert einer ComboBox abzurufen.
     */
    private int getComboBoxValue(ComboBox<Integer> comboBox) {
        return comboBox.getSelectionModel().getSelectedItem() != null ? comboBox.getSelectionModel().getSelectedItem() : 0;
    }

    /**
     * Zeigt eine Benachrichtigung mit einer Einblendanimation an.
     * @param message Die Nachricht, die angezeigt werden soll.
     */
    private void showNotification(String message) {
        notificationLabel.setText(message);
        notificationLabel.setVisible(true);

        // Animation: Einblenden von oben nach unten
        TranslateTransition slideIn = new TranslateTransition(Duration.seconds(0.5), notificationLabel);
        slideIn.setFromY(-60); // Startet außerhalb des Bildschirms
        slideIn.setToY(0); // Endet an der gewünschten Position
        slideIn.setOnFinished(event -> {
            // Nach einer kurzen Pause wieder ausblenden
            TranslateTransition slideOut = new TranslateTransition(Duration.seconds(0.5), notificationLabel);
            slideOut.setDelay(Duration.seconds(2)); // 2 Sekunden warten
            slideOut.setFromY(0);
            slideOut.setToY(-60); // Gleitet wieder nach oben heraus
            slideOut.setOnFinished(e -> notificationLabel.setVisible(false));
            slideOut.play();
        });
        slideIn.play();
    }
}
