package com.plantpal.app;

import com.plantpal.database.CareTaskHistoryRepository;
import com.plantpal.database.CareTaskRepository;
import com.plantpal.database.PlantProfileRepository;
import com.plantpal.logic.FilterService;
import com.plantpal.logic.PflanzenProfileService;
import com.plantpal.logic.PflegeAufgabenService;
import com.plantpal.model.PflanzenPflegeHistory_Model;
import com.plantpal.model.PflanzenPflege_Model;
import com.plantpal.utils.DateUtils;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller für die Verwaltung von Pflegeaufgaben und deren Historie.
 *
 * Dieser Controller verwaltet die GUI-Interaktionen im Zusammenhang mit Pflegeaufgaben und
 * Pflegehistorie. Aufgaben können gefiltert, als erledigt markiert und ihre Historie angezeigt werden.
 *
 * Der `PflegeAufgabenService` wird verwendet, um die Pflegeaufgaben zu laden und deren Status
 * zu aktualisieren.
 *
 * Zugehörige FXML-Datei: PflanzenPflege.fxml
 */
public class PflanzenPflegeController implements Initializable {

    @FXML
    private TableView<PflanzenPflege_Model> current_tasks;
    @FXML
    private TableView<PflanzenPflegeHistory_Model> history;

    @FXML
    private ComboBox<String> filterAktion, filterPflanze, filterStandort;
    @FXML
    private FontAwesomeIconView history_reload, filter_reload;

    @FXML
    private TableColumn<PflanzenPflege_Model, String> pflege_name_col, pflege_standort_col, pflege_aufgabe_col;
    @FXML
    private TableColumn<PflanzenPflege_Model, String> pflege_faellig_am_col;
    @FXML
    private TableColumn<PflanzenPflege_Model, String> pflege_aktion_col;  // Spalte für Buttons (Aktionen)

    @FXML
    private TableColumn<PflanzenPflegeHistory_Model, String> pflege_hist_name_col, pflege_hist_aktion_col;
    @FXML
    private TableColumn<PflanzenPflegeHistory_Model, String> pflege_hist_datum_col;
    @FXML
    private TableColumn<PflanzenPflegeHistory_Model, String> pflege_hist_notizen; // Spalte für Notizen-Button

    private ObservableList<PflanzenPflege_Model> taskList;
    private ObservableList<PflanzenPflegeHistory_Model> historyList;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private PflegeAufgabenService pflegeAufgabenService;
    private PflanzenProfileService pflanzenProfileService;
    private FilterService filterService;
    private double x, y;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // Repositories und Services initialisieren
        pflanzenProfileService = new PflanzenProfileService(new PlantProfileRepository(), new CareTaskHistoryRepository());
        pflegeAufgabenService = new PflegeAufgabenService(new CareTaskRepository(), new CareTaskHistoryRepository(), new PlantProfileRepository());
        filterService = new FilterService(new CareTaskRepository(), new CareTaskHistoryRepository(), new PlantProfileRepository());

        taskList = FXCollections.observableArrayList();
        historyList = FXCollections.observableArrayList();

        // Tabellen konfigurieren
        setupTableColumns();

        // Filteroptionen laden
        loadFilterOptions();

        // Filter-Listener setzen
        setupFilterListeners();

        // Daten laden
        loadTaskData();
        loadHistoryData();

        // Listener für die Auswahl in current_tasks
        current_tasks.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                loadHistoryDataForPlant(newValue.getPlant_id());
            }
        });

        // Event-Handler für history_reload Icon
        history_reload.setOnMouseClicked(event -> loadHistoryData());

        // Event-Handler für filter_reload Icon
        filter_reload.setOnMouseClicked(event -> resetFilters());
    }

    /**
     * Setzt die Spaltenkonfiguration für die Pflegeaufgaben- und Historientabellen.
     */
    private void setupTableColumns() {
        // Konfiguration der Spalten für die Aufgaben-Tabelle
        pflege_name_col.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPlant_name()));
        pflege_standort_col.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLocation()));
        pflege_aufgabe_col.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTask_type()));
        // LocalDate in String umgewandelt und formatieren
        pflege_faellig_am_col.setCellValueFactory(data -> DateUtils.formatProperty(data.getValue().getDue_date()));

        // Hinzufügen einer CellFactory für die Aktionsspalte, um Buttons anzuzeigen
        pflege_aktion_col.setCellFactory(createButtonCellFactoryForTasks());

        // Konfiguration der Spalten für die Historie-Tabelle
        pflege_hist_name_col.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPlant_name()));
        pflege_hist_aktion_col.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTask_type()));
        pflege_hist_datum_col.setCellValueFactory(data -> DateUtils.formatProperty(data.getValue().getCompletion_date()));

        // Hinzufügen einer CellFactory für die Notizenspalte
        pflege_hist_notizen.setCellFactory(createButtonCellFactoryForHistory());
    }

    /**
     * Erstellt eine CellFactory für die Aufgaben-TableView, um Buttons für Aktionen hinzuzufügen.
     */
    private Callback<TableColumn<PflanzenPflege_Model, String>, TableCell<PflanzenPflege_Model, String>> createButtonCellFactoryForTasks() {
        return param -> new TableCell<>() {
            private final Button completeButton = new Button("Erledigen");

            {
                completeButton.setOnAction(event -> {
                    PflanzenPflege_Model task = getTableView().getItems().get(getIndex());
                    try {
                        pflegeAufgabenService.markTaskAsCompleted(
                                task.getTask_id(),
                                task.getPlant_id(),
                                task.getTask_type(),
                                task.getNote(),
                                LocalDate.now()
                        );
                        loadTaskData();
                        loadHistoryData();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                completeButton.getStyleClass().add("action_buttons");
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(completeButton);
                    setStyle("-fx-alignment: CENTER;");
                }
            }
        };
    }

    /**
     * Erstellt eine CellFactory für die Historie-TableView, um Buttons für Notizen hinzuzufügen.
     */
    private Callback<TableColumn<PflanzenPflegeHistory_Model, String>, TableCell<PflanzenPflegeHistory_Model, String>> createButtonCellFactoryForHistory() {
        return param -> new TableCell<>() {
            private final Button noteButton = new Button("Notizen");

            {
                noteButton.setOnAction(event -> {
                    PflanzenPflegeHistory_Model historyItem = getTableView().getItems().get(getIndex());
                    openNoteEditor(historyItem);
                });
                noteButton.getStyleClass().add("action_buttons");
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    PflanzenPflegeHistory_Model historyItem = getTableRow().getItem();
                    noteButton.setText((historyItem != null && historyItem.getNote() != null && !historyItem.getNote().isEmpty()) ? "Notiz anzeigen" : "Notiz hinzufügen");
                    setGraphic(noteButton);
                    setStyle("-fx-alignment: CENTER;");
                }
            }
        };
    }

    /**
     * Lädt die verfügbaren Optionen für die Filter-ComboBoxen (Pflanze, Standort und Aktion) aus den Services.
     */
    private void loadFilterOptions() {
        List<String> plantOptions = pflanzenProfileService.getDistinctPlantNames();
        filterPflanze.setItems(FXCollections.observableArrayList(plantOptions));

        List<String> locationOptions = pflanzenProfileService.getDistinctLocations();
        filterStandort.setItems(FXCollections.observableArrayList(locationOptions));

        List<String> actionOptions = pflegeAufgabenService.getDistinctTaskTypes();
        filterAktion.setItems(FXCollections.observableArrayList(actionOptions));
    }

    /**
     * Lädt die aktuellen Aufgaben in die TableView.
     */
    private void loadTaskData() {
        List<PflanzenPflege_Model> tasks = pflegeAufgabenService.loadCurrentTasks();
        taskList.setAll(tasks);
        current_tasks.setItems(taskList);
    }

    /**
     * Lädt die Pflegehistorie in die TableView.
     */
    private void loadHistoryData() {
        List<PflanzenPflegeHistory_Model> historyEntries = pflegeAufgabenService.loadHistory();
        historyList.setAll(historyEntries);
        history.setItems(historyList);
    }

    /**
     * Setzt die Filter-Listener für die ComboBoxen.
     */
    private void setupFilterListeners() {
        filterAktion.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        filterPflanze.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        filterStandort.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
    }

    /**
     * Setzt alle Filter zurück und lädt die gesamten Daten neu.
     */
    private void resetFilters() {
        filterAktion.getSelectionModel().clearSelection();
        filterPflanze.getSelectionModel().clearSelection();
        filterStandort.getSelectionModel().clearSelection();
        loadTaskData();
        loadHistoryData();
    }

    /**
     * Lädt die Historie für eine bestimmte Pflanze.
     */
    private void loadHistoryDataForPlant(int plantId) {
        List<PflanzenPflegeHistory_Model> plantHistory = pflegeAufgabenService.loadHistoryForPlant(plantId);
        historyList.setAll(plantHistory);
        history.setItems(historyList);
    }

    /**
     * Filtert die Aufgaben und die Historie basierend auf den ausgewählten Werten der Filter-ComboBoxen.
     */
    private void applyFilters() {
        String selectedPlant = filterPflanze.getValue();
        String selectedLocation = filterStandort.getValue();
        String selectedAction = filterAktion.getValue();

        // Verwendet den FilterService, um die Aufgaben basierend auf den ausgewählten Werten zu filtern
        List<PflanzenPflege_Model> filteredTasks = filterService.filterTasks(selectedPlant, selectedLocation, selectedAction);
        taskList.setAll(filteredTasks);
        current_tasks.setItems(taskList);

        // Verwendet den FilterService, um die Historie basierend auf den ausgewählten Werten zu filtern
        List<PflanzenPflegeHistory_Model> filteredHistory = filterService.filterHistory(selectedPlant, selectedLocation, selectedAction);
        historyList.setAll(filteredHistory);
        history.setItems(historyList);
    }

    /**
     * Öffnet den Notizen-Editor für das ausgewählte Historien-Element.
     *
     * @param historyItem Das Historien-Element, für das der Notizen-Editor geöffnet werden soll.
     */
    private void openNoteEditor(PflanzenPflegeHistory_Model historyItem) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/NotizenEditor.fxml"));
            Parent root = loader.load();

            NotizenEditorController editorController = loader.getController();
            editorController.setHistoryItem(historyItem);

            // Erstelle ein neues Fenster für den Notiz-Editor
            Stage noteEditorStage = new Stage();
            noteEditorStage.setScene(new Scene(root));

            // Setze das aktuelle Fenster als Owner des neuen Fensters
            Stage mainStage = (Stage) current_tasks.getScene().getWindow();
            noteEditorStage.initOwner(mainStage);
            noteEditorStage.initModality(Modality.APPLICATION_MODAL);

            // Titelleiste entfernen
            noteEditorStage.initStyle(StageStyle.UNDECORATED);

            // Ändere die Opacity des Hauptfensters, um es ausgegraut darzustellen
            mainStage.getScene().getRoot().setOpacity(0.7);

            // Schließe das Fenster und lade die Historien-Daten neu
            noteEditorStage.setOnHiding(event -> {
                loadHistoryData();
                // Setze die Opacity des Hauptfensters nach dem Schließen des Notiz-Editors zurück
                mainStage.getScene().getRoot().setOpacity(1.0);
            });

            noteEditorStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
