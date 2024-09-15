package com.plantpal.app;

import com.plantpal.database.CareTaskHistoryRepository;
import com.plantpal.database.PlantProfileRepository;
import com.plantpal.logic.PflanzenProfileService;
import com.plantpal.model.PflanzenProfile_Model;
import com.plantpal.utils.DateUtils;
import com.plantpal.utils.NotificationUtils;
import javafx.animation.TranslateTransition;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.Duration;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

/**
 * Controller für das Pflanzenprofil-Management.
 *
 * Dieser Controller verwaltet die GUI-Interaktionen im Zusammenhang mit dem Pflanzenprofil.
 * Dazu gehören das Hinzufügen, Aktualisieren und Löschen von Pflanzenprofilen,
 * sowie das Verwalten von Formularfeldern und der Tabellenansicht.
 *
 * Es wird auch eine Such- und Filterfunktion bereitgestellt, um die Pflanzenprofile in der Tabelle
 * dynamisch zu filtern. Änderungen an den Profilen werden außerdem in die Historie geschrieben.
 *
 * Benachrichtigungen werden über eine ausblendbare Notification-Komponente angezeigt.
 *
 * Initialisierung und Datenlogik werden über den `PflanzenProfileService` gesteuert.
 *
 * Zugehörige FXML-Datei: PflanzenProfile.fxml
 */
public class PflanzenProfileController implements Initializable {

    @FXML
    private TextField botanical_name, kaufdatum, last_duengen, last_giessen, name, search, standort;

    @FXML
    private Label notificationLabel;

    @FXML
    private ComboBox<Integer> intervall_duengen, intervall_giessen;

    @FXML
    private TableView<PflanzenProfile_Model> pflanzenProfil_tableView;

    @FXML
    private TableColumn<PflanzenProfile_Model, String> profile_bot_name_col, profile_name_col, profile_standort_col;

    @FXML
    private TableColumn<PflanzenProfile_Model, Integer> profile_int_duengen_col, profile_int_giessen_col;

    @FXML
    private TableColumn<PflanzenProfile_Model, String> profile_kaufdatum, profile_last_duengen_col,
            profile_last_giessen_col;

    private ObservableList<PflanzenProfile_Model> plantData;
    private FilteredList<PflanzenProfile_Model> filteredData;

    private PflanzenProfileService pflanzenProfileService;

    /**
     * Initialisiert den Controller.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialisiere den Service
        PlantProfileRepository plantProfileRepository = new PlantProfileRepository();
        CareTaskHistoryRepository careTaskHistoryRepository = new CareTaskHistoryRepository();
        pflanzenProfileService = new PflanzenProfileService(plantProfileRepository, careTaskHistoryRepository);

        // ComboBox für Gieß- und Düngeintervall mit Werten 1-31 initialisieren
        ObservableList<Integer> intervalValues = FXCollections.observableArrayList();
        for (int i = 1; i <= 31; i++) {
            intervalValues.add(i);
        }

        intervall_giessen.setItems(intervalValues);
        intervall_duengen.setItems(intervalValues);

        plantData = FXCollections.observableArrayList();
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
     * Lädt die Pflanzenprofildaten aus der Datenbank und aktualisiert die TableView.
     */
    private void loadPlantData() {
        plantData.setAll(pflanzenProfileService.getAllPlantProfiles());
    }

    /**
     * Fügt ein neues Pflanzenprofil über die Geschäftslogik hinzu.
     */
    @FXML
    private void addPlant() {
        PflanzenProfile_Model newPlant = new PflanzenProfile_Model(
                0, name.getText(), botanical_name.getText(),
                DateUtils.parseDate(kaufdatum.getText()),
                standort.getText(), getComboBoxValue(intervall_giessen),
                getComboBoxValue(intervall_duengen), LocalDate.now(), LocalDate.now(), ""
        );

        // Pflichtfelder prüfen
        if (checkMandatoryFields()) {
            pflanzenProfileService.addPlantProfile(newPlant);
            loadPlantData();
            pflanzenProfil_tableView.refresh();
            clearFormFields();
            NotificationUtils.showNotification(notificationLabel, "Pflanze erfolgreich hinzugefügt!");
        }
    }

    /**
     * Löscht ein ausgewähltes Pflanzenprofil über die Geschäftslogik.
     */
    @FXML
    private void deletePlant() {
        PflanzenProfile_Model selectedPlant = pflanzenProfil_tableView.getSelectionModel().getSelectedItem();
        if (selectedPlant != null) {
            pflanzenProfileService.deletePlantProfile(selectedPlant.getPlant_id());
            loadPlantData();
            pflanzenProfil_tableView.refresh();
            clearFormFields();
            NotificationUtils.showNotification(notificationLabel, "Pflanze erfolgreich gelöscht!");
        }
    }

    /**
     * Aktualisiert das ausgewählte Pflanzenprofil und schreibt Änderungen in die Historie.
     */
    @FXML
    private void updatePlantProfile() {
        PflanzenProfile_Model selectedPlant = pflanzenProfil_tableView.getSelectionModel().getSelectedItem();
        if (selectedPlant != null) {
            try {
                // Verwende den DateTimeFormatter zum Parsen des Datums
                LocalDate parsedKaufdatum = DateUtils.parseDate(kaufdatum.getText());

                selectedPlant.setPlant_name(name.getText());
                selectedPlant.setBotanical_plant_name(botanical_name.getText());
                selectedPlant.setPurchase_date(parsedKaufdatum);
                selectedPlant.setLocation(standort.getText());
                selectedPlant.setWatering_interval(intervall_giessen.getValue());
                selectedPlant.setFertilizing_interval(intervall_duengen.getValue());

                // Aktualisiere das Pflanzenprofil und schreibe die Historie
                pflanzenProfileService.updatePlantProfile(selectedPlant);
                NotificationUtils.showNotification(notificationLabel, "Profil erfolgreich aktualisiert");

                // Tabelle aktualisieren
                loadPlantData();
                pflanzenProfil_tableView.refresh();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                NotificationUtils.showNotification(notificationLabel, "Fehler beim Aktualisieren des Profils");
            }
        }
    }

    /**
     * Filtert die Pflanzenprofildaten basierend auf der Sucheingabe.
     * Die Suche filtert nach Name, botanischer Name und Standort.
     */
    private void applySearchFilter() {
        search.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(plant -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                return plant.getPlant_name().toLowerCase().contains(lowerCaseFilter) ||
                        plant.getBotanical_plant_name().toLowerCase().contains(lowerCaseFilter) ||
                        plant.getLocation().toLowerCase().contains(lowerCaseFilter);
            });
            pflanzenProfil_tableView.refresh();
        });
    }

    /**
     * Leert das Formular für neue Einträge.
     */
    @FXML
    private void clearFormFields() {
        name.clear();
        botanical_name.clear();
        kaufdatum.clear();
        standort.clear();
        intervall_giessen.getSelectionModel().clearSelection();
        intervall_duengen.getSelectionModel().clearSelection();
        last_giessen.clear();
        last_duengen.clear();
        pflanzenProfil_tableView.getSelectionModel().clearSelection();
    }

    /**
     * Füllt die Formularfelder basierend auf der ausgewählten Zeile in der TableView.
     *
     * @param plant Das ausgewählte Pflanzenprofil.
     */
    private void populateFormFields(PflanzenProfile_Model plant) {
        name.setText(plant.getPlant_name());
        botanical_name.setText(plant.getBotanical_plant_name());
        kaufdatum.setText(DateUtils.formatDate(plant.getPurchase_date()));
        standort.setText(plant.getLocation());
        intervall_giessen.setValue(plant.getWatering_interval());
        intervall_duengen.setValue(plant.getFertilizing_interval());
        last_giessen.setText(DateUtils.formatDate(plant.getLast_watered()));
        last_duengen.setText(DateUtils.formatDate(plant.getLast_fertilized()));
    }

    /**
     * Initialisiert die Spalten der TableView.
     */
    private void setupTableColumns() {
        profile_name_col.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPlant_name()));
        profile_bot_name_col.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getBotanical_plant_name()));
        profile_standort_col.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLocation()));
        profile_int_giessen_col.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getWatering_interval()).asObject());
        profile_int_duengen_col.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getFertilizing_interval()).asObject());

        // Datumsspalten mit formatiertem Datum
        profile_kaufdatum.setCellValueFactory(data -> DateUtils.formatProperty(data.getValue().getPurchase_date()));
        profile_last_giessen_col.setCellValueFactory(data -> DateUtils.formatProperty(data.getValue().getLast_watered()));
        profile_last_duengen_col.setCellValueFactory(data -> DateUtils.formatProperty(data.getValue().getLast_fertilized()));

        // Hinzufügen des Suffixes für die Intervall-Spalten
        addSuffixToColumn(profile_int_giessen_col, profile_int_duengen_col);
    }

    /**
     * Ruft den Wert einer ComboBox ab.
     *
     * @param comboBox Die zu überprüfende ComboBox.
     * @return Der ausgewählte Wert oder 0, wenn kein Wert ausgewählt ist.
     */
    private int getComboBoxValue(ComboBox<Integer> comboBox) {
        return comboBox.getSelectionModel().getSelectedItem() != null ? comboBox.getSelectionModel().getSelectedItem() : 0;
    }

    /**
     * Zeigt eine Benachrichtigung mit einer Einblendanimation an.
     *
     * @param message Die Nachricht, die angezeigt werden soll.
     */
    private void showNotification(String message) {
        notificationLabel.setText(message);
        notificationLabel.setVisible(true);

        TranslateTransition slideIn = new TranslateTransition(Duration.seconds(0.5), notificationLabel);
        slideIn.setFromY(-60);
        slideIn.setToY(0);
        slideIn.setOnFinished(event -> {
            TranslateTransition slideOut = new TranslateTransition(Duration.seconds(0.5), notificationLabel);
            slideOut.setDelay(Duration.seconds(2));
            slideOut.setFromY(0);
            slideOut.setToY(-60);
            slideOut.setOnFinished(e -> notificationLabel.setVisible(false));
            slideOut.play();
        });
        slideIn.play();
    }

    /**
     * Überprüft, ob die Pflichtfelder für das Pflanzenprofil ausgefüllt sind.
     *
     * @return true, wenn alle Pflichtfelder ausgefüllt sind, sonst false.
     */
    private boolean checkMandatoryFields() {
        if (intervall_giessen.getValue() == null) {
            showNotification("Bitte ein Gieß-Intervall auswählen.");
            return false;
        }
        if (intervall_duengen.getValue() == null) {
            showNotification("Bitte ein Düng-Intervall auswählen.");
            return false;
        }
        return true;
    }

    /**
     * Fügt der Anzeige der Gieß- und Düngeintervalle das Suffix "Tage" hinzu.
     */
    @SafeVarargs
    private void addSuffixToColumn(TableColumn<PflanzenProfile_Model, Integer>... columns) {
        for (TableColumn<PflanzenProfile_Model, Integer> column : columns) {
            column.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(Integer item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item + " Tage");
                    }
                }
            });
        }
    }
}
