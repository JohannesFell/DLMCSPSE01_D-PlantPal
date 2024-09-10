package com.plantpal.app;

import com.plantpal.database.SQLiteDB;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;


public class PflanzenPflegeController implements Initializable {

    @FXML
    private TableView<PflanzenPflege_Model> current_tasks;
    @FXML
    private TableView<PflanzenPflegeHistory_Model> history;
    @FXML
    private TableColumn<PflanzenPflege_Model, String> pflege_name_col, pflege_standort_col, pflege_aufgabe_col, pflege_aktion_col;
    @FXML
    private TableColumn<PflanzenPflege_Model, LocalDate> pflege_faellig_am_col;
    @FXML
    private TableColumn<PflanzenPflegeHistory_Model, String> pflege_hist_name_col, pflege_hist_notizen, pflege_hist_aktion_col, pflege_hist_datum_col;
    @FXML
    private ComboBox<String> filterAktion, filterPflanze, filterStandort;
    @FXML
    private FontAwesomeIconView history_reload, filter_reload;

    private ObservableList<PflanzenPflege_Model> taskList;
    private ObservableList<PflanzenPflegeHistory_Model> historyList;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private double x,y = 0;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        taskList = FXCollections.observableArrayList();
        historyList = FXCollections.observableArrayList();

        // Filter-Optionen laden
        loadFilterOptions();
        // Hinzufügen der Listener für die Filter-ComboBoxen
        setupFilterListeners();

        // Tabellen-Konfiguration
        setupTableColumns();
        setupHistoryTableColumns();

        // Datenbankverbindung holen
        try (Connection conn = SQLiteDB.getConnection()) {
            // Pflegeaufgaben automatisch berechnen und in die Datenbank einfügen
            PflegeTaskUpdater updater = new PflegeTaskUpdater(conn);

            // Für jede Pflanze in der Datenbank werden die Daten aus der Tabelle "PlantProfile" abgerufen.
            // Diese Informationen werden genutzt, um die nächsten Pflegeaufgaben zu berechnen
            // (basierend auf dem letzten Gießen/Düngen und den Intervallen) und  in die Tabelle "CareTask" einzufügen.
            String sql = "SELECT plant_id, last_watered, last_fertilized, watering_interval, fertilizing_interval FROM PlantProfile";
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    int plantId = rs.getInt("plant_id");
                    LocalDate lastWatered = rs.getDate("last_watered") != null ? rs.getDate("last_watered").toLocalDate() : null;
                    LocalDate lastFertilized = rs.getDate("last_fertilized") != null ? rs.getDate("last_fertilized").toLocalDate() : null;
                    int wateringInterval = rs.getInt("watering_interval");
                    int fertilizingInterval = rs.getInt("fertilizing_interval");

                    // Berechnung und Einfügen der nächsten Pflegeaufgaben
                    updater.calculateAndInsertNextCareTasks(plantId, lastWatered, lastFertilized, wateringInterval, fertilizingInterval);
                }
            }

            // Pflegeaufgaben aus der Datenbank laden
            loadTaskData();
            loadHistoryData();

            // Listener für die Auswahl in current_tasks hinzufügen
            current_tasks.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    // Wenn eine Pflanze ausgewählt wird, zeige nur die Historie dieser Pflanze
                    loadHistoryDataForPlant(newValue.getPlant_id());
                }
            });

            // Event-Handler für history_reload Icon (FontAwesome)
            history_reload.setOnMouseClicked(event -> {
                // Lädt alle Datensätze in der History-TableView, wenn das Icon geklickt wird
                loadHistoryData();
            });

            // Event-Handler für filter_reload Icon (FontAwesome)
            filter_reload.setOnMouseClicked(event -> resetFilters());

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Fehler bei der Initialisierung.");
        }
    }

    /**
     * Konfiguriert die Spalten der TableView `current_tasks`, die die aktuellen Pflegeaufgaben darstellt.
     * Jede Spalte wird mit den entsprechenden Attributen des Modells `PflanzenPflege_Model` verbunden,
     * einschließlich einer benutzerdefinierten Spalte für Aktionen, die das Markieren von Aufgaben als "Erledigt" ermöglicht.
     *
     * Diese Methode richtet auch die Formatierung für das Fälligkeitsdatum ein und fügt Aktionen in die Aktion-Spalte ein.
     */
    private void setupTableColumns() {
        // Spalten für aktuelle Pflegeaufgaben konfigurieren
        pflege_name_col.setCellValueFactory(new PropertyValueFactory<>("plant_name"));
        pflege_standort_col.setCellValueFactory(new PropertyValueFactory<>("location"));
        pflege_aufgabe_col.setCellValueFactory(new PropertyValueFactory<>("task_type"));
        pflege_faellig_am_col.setCellValueFactory(new PropertyValueFactory<>("due_date"));

        // Aktion-Spalte mit Buttons befüllen
        pflege_aktion_col.setCellFactory(new Callback<>() {
            @Override
            public TableCell<PflanzenPflege_Model, String> call(final TableColumn<PflanzenPflege_Model, String> param) {
                return new TableCell<>() {
                    private final Button btn = new Button("Erledigen");

                    {
                        btn.setOnAction(event -> {
                            PflanzenPflege_Model task = getTableView().getItems().get(getIndex());
                            markTaskAsCompleted(task.getTask_id(), task.getPlant_id(), task.getTask_type(), task.getNote());
                        });
                        // CSS-Klasse hinzufügen
                        btn.getStyleClass().add("action_buttons");
                    }

                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(btn);
                            setStyle("-fx-alignment: CENTER;");
                        }
                    }
                };
            }
        });

        // Datumsformatierung auf die Spalten anwenden
        applyDateFormatting(pflege_faellig_am_col);
    }

    /**
     * Konfiguriert die Spalten der TableView `history`, die die Pflegehistorie anzeigt.
     * Jede Spalte wird mit den entsprechenden Attributen des Modells `PflanzenPflegeHistory_Model` verbunden.
     *
     * In der Notizen-Spalte wird ein Button hinzugefügt, mit dem Notizen angezeigt oder hinzugefügt werden können,
     * abhängig davon, ob bereits eine Notiz für den Historieneintrag vorhanden ist.
     */
    private void setupHistoryTableColumns() {

        pflege_hist_name_col.setCellValueFactory(new PropertyValueFactory<>("plant_name"));
        pflege_hist_aktion_col.setCellValueFactory(new PropertyValueFactory<>("task_type"));
        pflege_hist_datum_col.setCellValueFactory(new PropertyValueFactory<>("completion_date"));

        // Notizen-Spalte: Button hinzufügen
        pflege_hist_notizen.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Notizen");

            {
                // CSS-Klasse hinzufügen
                btn.getStyleClass().add("action_buttons");
                btn.setOnAction(event -> {
                    PflanzenPflegeHistory_Model historyItem = getTableView().getItems().get(getIndex());

                    // Editor öffnen, um Notizen anzuzeigen oder hinzuzufügen
                    openNoteEditor(historyItem);
                });

            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    PflanzenPflegeHistory_Model historyItem = getTableRow().getItem();
                    if (historyItem.getNote() == null || historyItem.getNote().isEmpty()) {
                        btn.setText("Notiz hinzufügen");
                        setStyle("-fx-alignment: CENTER;");
                    } else {
                        btn.setText("Notiz anzeigen");
                        setStyle("-fx-alignment: CENTER;");
                    }
                    setGraphic(btn);
                }
            }
        });
    }

    /**
     * Lädt die Pflegeaufgaben aus der Datenbank, die noch nicht als erledigt markiert wurden (completed = false),
     * und weist die geladenen Daten der TableView `current_tasks` zu.
     *
     * Diese Methode lädt die Aufgaben aus der Tabelle `CareTask` und verknüpft sie mit Informationen aus der
     * Tabelle `PlantProfile`. Die Pflegeaufgaben werden in die ObservableList `taskList` eingefügt und in der TableView angezeigt.
     */
    private void loadTaskData() {
        String sql = "SELECT CareTask.task_id, CareTask.task_type, CareTask.due_date, CareTask.completed, CareTask.note, " +
                "PlantProfile.plant_id, PlantProfile.plant_name, PlantProfile.location, " +
                "PlantProfile.watering_interval, PlantProfile.fertilizing_interval " +
                "FROM CareTask " +
                "LEFT JOIN PlantProfile ON CareTask.plant_id = PlantProfile.plant_id " +
                "WHERE CareTask.completed = false";

        try (Connection conn = SQLiteDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            taskList.clear(); // Liste vor dem Neuladen leeren

            while (rs.next()) {
                        PflanzenPflege_Model task = new PflanzenPflege_Model(
                        rs.getInt("task_id"),
                        rs.getInt("plant_id"),
                        rs.getString("task_type"),
                        rs.getDate("due_date").toLocalDate(),
                        rs.getBoolean("completed"),
                        rs.getString("note"),
                        rs.getString("plant_name"),
                        rs.getString("location"),
                        rs.getInt("watering_interval"),
                        rs.getInt("fertilizing_interval"),
                        "actions"
                );

                taskList.add(task);
            }
            // Die Liste der TableView zuweisen
            current_tasks.setItems(taskList);

        } catch (Exception e) {
            System.out.println("Daten für Pflegeaufgaben konnten nicht geladen werden.");
            e.printStackTrace();
        }
    }

    /**
     * Lädt die Pflegehistorie aus der Datenbank und weist sie der TableView zu.
     */
    private void loadHistoryData() {
        String sql = "SELECT CareTaskHistory.history_id, CareTaskHistory.task_id, CareTaskHistory.plant_id, CareTaskHistory.task_type, " +
                "CareTaskHistory.completion_date, CareTaskHistory.note, " +
                "PlantProfile.plant_name, PlantProfile.location " +
                "FROM CareTaskHistory " +
                "LEFT JOIN PlantProfile ON CareTaskHistory.plant_id = PlantProfile.plant_id "+
                "ORDER BY history_id DESC";

        try (Connection conn = SQLiteDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            historyList.clear();  // Liste leeren, bevor neue Daten geladen werden

            while (rs.next()) {

                PflanzenPflegeHistory_Model historyEntry = new PflanzenPflegeHistory_Model(
                        rs.getInt("history_id"),
                        rs.getInt("task_id"),
                        rs.getInt("plant_id"),
                        rs.getString("task_type"),
                        rs.getDate("completion_date").toLocalDate(),
                        rs.getString("note"),
                        rs.getString("plant_name")
                );

                historyList.add(historyEntry);
            }
            // Die Liste der TableView zuweisen
            history.setItems(historyList);

        } catch (SQLException e) {
            System.out.println("Fehler beim Laden der Historie.");
            e.printStackTrace();
        }
    }



    /**
     * Wendet eine benutzerdefinierte Datumsformatierung auf die angegebenen Spalten an.
     * Die Methode konvertiert die LocalDate-Werte in Strings, um sie in der TableView anzuzeigen.
     *
     * @param columns   Die Spalten, auf die die Formatierung angewendet werden soll.
     */
    @SafeVarargs
    private void applyDateFormatting(TableColumn<PflanzenPflege_Model, LocalDate>... columns) {
        for (TableColumn<PflanzenPflege_Model, LocalDate> column : columns) {
            column.setCellFactory(col -> new TextFieldTableCell<>(new StringConverter<>() {
                @Override
                public String toString(LocalDate date) {
                    return date != null ? date.format(PflanzenPflegeController.DATE_FORMATTER) : "";
                }

                @Override
                public LocalDate fromString(String string) {
                    return string != null && !string.isEmpty() ? LocalDate.parse(string, PflanzenPflegeController.DATE_FORMATTER) : null;
                }
            }));
        }
    }

    /**
     * Markiert eine Pflegeaufgabe als erledigt, indem das Feld `completed` in der `CareTask`-Tabelle auf `true` gesetzt wird,
     * und fügt einen Eintrag in die Tabelle `CareTaskHistory` ein, um die Historie der erledigten Aufgaben zu dokumentieren.
     * Zudem wird je nach `task_type` das entsprechende Feld (`last_watered` oder `last_fertilized`) in der `PlantProfile`-Tabelle aktualisiert.
     *
     * @param taskId   Die ID der Aufgabe, die abgeschlossen wird.
     * @param plantId  Die ID der Pflanze, deren Pflegeprofil aktualisiert wird.
     * @param taskType Der Typ der Aufgabe, entweder "Gießen" oder "Düngen".
     * @param note     Eine optionale Notiz zur Aufgabe (z.B. Beobachtungen bei der Pflege).
     */
    private void markTaskAsCompleted(int taskId, int plantId, String taskType, String note) {
        // SQL-Befehl zum Setzen von `completed = true` in der `CareTask`-Tabelle
        String updateTaskSql = "UPDATE CareTask SET completed = true WHERE task_id = ?";

        // SQL-Befehl zur Aktualisierung von `last_watered` oder `last_fertilized` in der `PlantProfile`-Tabelle
        String updatePlantProfileSql = null;
        LocalDate completionDate = LocalDate.now(); // Das aktuelle Datum als Abschlussdatum

        // SQL-Befehl zum Einfügen eines neuen Eintrags in die `CareTaskHistory`-Tabelle
        String insertHistorySql = "INSERT INTO CareTaskHistory (task_id, plant_id, task_type, completion_date, note) VALUES (?, ?, ?, ?, ?)";

        // Bestimme, welches Feld in `PlantProfile` aktualisiert werden soll, abhängig vom `task_type`
        if ("Gießen".equals(taskType)) {
            updatePlantProfileSql = "UPDATE PlantProfile SET last_watered = ? WHERE plant_id = ?";
        } else if ("Düngen".equals(taskType)) {
            updatePlantProfileSql = "UPDATE PlantProfile SET last_fertilized = ? WHERE plant_id = ?";
        }

        // Verbindung zur Datenbank aufbauen
        try (Connection conn = SQLiteDB.getConnection()) {
            // Transaktion starten, um sicherzustellen, dass beide Updates entweder vollständig durchgeführt werden
            // oder im Falle eines Fehlers gar nicht
            conn.setAutoCommit(false);

            try {
                // Pflegeaufgabe als abgeschlossen markieren
                try (PreparedStatement updateTaskStmt = conn.prepareStatement(updateTaskSql)) {
                    updateTaskStmt.setInt(1, taskId);
                    updateTaskStmt.executeUpdate();
                }

                // Historieneintrag hinzufügen
                try (PreparedStatement insertHistoryStmt = conn.prepareStatement(insertHistorySql)) {
                    insertHistoryStmt.setInt(1, taskId);
                    insertHistoryStmt.setInt(2, plantId);
                    insertHistoryStmt.setString(3, taskType);
                    insertHistoryStmt.setDate(4, Date.valueOf(completionDate));
                    insertHistoryStmt.setString(5, note);
                    insertHistoryStmt.executeUpdate();
                }

                // Aktualisiere das Pflanzenprofil (last_watered oder last_fertilized)
                if (updatePlantProfileSql != null) {
                    try (PreparedStatement updatePlantStmt = conn.prepareStatement(updatePlantProfileSql)) {
                        updatePlantStmt.setDate(1, Date.valueOf(completionDate));
                        updatePlantStmt.setInt(2, plantId);
                        updatePlantStmt.executeUpdate();
                    }
                }

                // Commit der Transaktion, wenn alles erfolgreich war
                conn.commit();
                System.out.println("Aufgabe erfolgreich als erledigt markiert, Profil und Historie aktualisiert.");

            } catch (SQLException e) {
                // Falls ein Fehler auftritt, die Transaktion zurückrollen
                conn.rollback();
                System.out.println("Fehler beim Abschließen der Aufgabe. Transaktion wird zurückgesetzt.");
                e.printStackTrace();
            } finally {
                // Auto-Commit wieder aktivieren
                conn.setAutoCommit(true);
            }

            // Aktualisiere die TableViews, um die Änderungen anzuzeigen
            loadTaskData();  // Aktualisiere die Liste der offenen Aufgaben
            loadHistoryData();  // Aktualisiere die Historie

        } catch (SQLException e) {
            System.out.println("Fehler beim Markieren der Aufgabe als erledigt.");
            e.printStackTrace();
        }
    }

    /**
     * Lädt die Historie für eine bestimmte Pflanze und zeigt sie in der TableView an.
     * @param plantId Die ID der Pflanze, deren Historie angezeigt werden soll.
     */
    private void loadHistoryDataForPlant(int plantId) {
        String sql ="SELECT CareTaskHistory.history_id, CareTaskHistory.task_id, CareTaskHistory.plant_id, CareTaskHistory.task_type, " +
                "CareTaskHistory.completion_date, CareTaskHistory.note, " +
                "PlantProfile.plant_name, PlantProfile.location, PlantProfile.watering_interval, PlantProfile.fertilizing_interval, " +
                "PlantProfile.last_watered, PlantProfile.last_fertilized " +
                "FROM CareTaskHistory " +
                "LEFT JOIN PlantProfile ON CareTaskHistory.plant_id = PlantProfile.plant_id " +
                "WHERE CareTaskHistory.plant_id = ?";

        try (Connection conn = SQLiteDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, plantId);
            ResultSet rs = stmt.executeQuery();

            historyList.clear(); // Liste leeren, bevor neue Daten hinzugefügt werden

            while (rs.next()) {
                PflanzenPflegeHistory_Model historyEntry = new PflanzenPflegeHistory_Model(
                        rs.getInt("history_id"),
                        rs.getInt("task_id"),
                        rs.getInt("plant_id"),
                        rs.getString("task_type"),
                        rs.getDate("completion_date").toLocalDate(),
                        rs.getString("note"),
                        rs.getString("plant_name")
                );

                historyList.add(historyEntry);
            }

            // Weisen Sie die Liste der history-TableView zu
            history.setItems(historyList);

        } catch (SQLException e) {
            System.out.println("Fehler beim Laden der Historie für die Pflanze mit ID: " + plantId);
            e.printStackTrace();
        }
    }

    /**
     * Öffnet den Notizen-Editor, um die Notiz eines ausgewählten Eintrags in der Historie anzuzeigen oder zu bearbeiten.
     *
     * Diese Methode lädt die FXML-Datei des Notizeneditors, übergibt das ausgewählte `PflanzenPflegeHistory_Model`
     * an den Editor und zeigt den Editor in einem neuen Fenster an.
     *
     * @param historyItem Das Modell `PflanzenPflegeHistory_Model`, das den ausgewählten Eintrag in der Historie darstellt.
     */
    private void openNoteEditor(PflanzenPflegeHistory_Model historyItem) {
        try {
            // FXML-Datei laden
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/NotizenEditor.fxml"));
            Parent root = loader.load();

            // Controller des Notizeditors abrufen
            NotizenEditorController editorController = loader.getController();
            editorController.setHistoryItem(historyItem);  // Das ausgewählte History-Item an den Editor übergeben

            // Dialog anzeigen
            Stage stage = new Stage();
            stage.setTitle("Notiz bearbeiten");
            stage.setScene(new Scene(root));

            // Titelleiste entfernen
            stage.initStyle(StageStyle.UNDECORATED);

            root.setOnMousePressed((MouseEvent event) ->{
                x = event.getSceneX();
                y = event.getSceneY();
            });

            root.setOnMouseDragged((MouseEvent event) ->{
                stage.setX(event.getScreenX() - x);
                stage.setY(event.getScreenY() - y);

                stage.setOpacity(.8);
            });
            root.setOnMouseReleased((MouseEvent event) -> stage.setOpacity(1));

            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * Lädt die verfügbaren Optionen für die Filter-ComboBoxen (Pflanze, Standort und Aktion) aus der Datenbank
     * und fügt sie den jeweiligen ComboBoxen hinzu.
     */
    private void loadFilterOptions() {
        // Pflanzen-Optionen laden
        String plantSql = "SELECT DISTINCT plant_name FROM PlantProfile ORDER BY plant_name ASC";
        String locationSql = "SELECT DISTINCT location FROM PlantProfile ORDER BY location ASC";
        String actionSql = "SELECT task_type FROM (" +
                "SELECT DISTINCT task_type FROM CareTask " +
                "UNION " +
                "SELECT 'Name geändert' AS task_type " +
                "UNION " +
                "SELECT 'Standort geändert' AS task_type " +
                "UNION " +
                "SELECT 'Gießintervall geändert' AS task_type " +
                "UNION " +
                "SELECT 'Düngeintervall geändert' AS task_type) AS all_types " +
                "ORDER BY CASE " +
                "WHEN task_type = 'Gießen' THEN 1 " +
                "WHEN task_type = 'Düngen' THEN 2 " +
                "WHEN task_type = 'Name geändert' THEN 3 " +
                "WHEN task_type = 'Standort geändert' THEN 4 " +
                "WHEN task_type = 'Gießintervall geändert' THEN 5 " +
                "WHEN task_type = 'Düngeintervall geändert' THEN 6 " +
                "ELSE 7 END";

        try (Connection conn = SQLiteDB.getConnection()) {
            // Pflanzen laden
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(plantSql)) {
                ObservableList<String> plantOptions = FXCollections.observableArrayList();
                while (rs.next()) {
                    plantOptions.add(rs.getString("plant_name"));
                }
                // ComboBox für Pflanzen befüllen
                filterPflanze.setItems(plantOptions);
            }

            // Standorte laden
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(locationSql)) {
                ObservableList<String> locationOptions = FXCollections.observableArrayList();
                while (rs.next()) {
                    locationOptions.add(rs.getString("location"));
                }
                // ComboBox für Standorte befüllen
                filterStandort.setItems(locationOptions);
            }

            // Aktionen laden (Gießen, Düngen etc.)
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(actionSql)) {
                ObservableList<String> actionOptions = FXCollections.observableArrayList();
                while (rs.next()) {
                    actionOptions.add(rs.getString("task_type"));
                }
                // ComboBox für Aktionen befüllen
                filterAktion.setItems(actionOptions);
            }
        } catch (SQLException e) {
            System.out.println("Fehler beim Laden der Filteroptionen.");
            e.printStackTrace();
        }
    }

    /**
     * Setzt die Listener für die Filter-ComboBoxen. Wenn eine Auswahl in einer ComboBox getroffen wird,
     * werden die TableViews (CareTask und History) entsprechend den ausgewählten Filtern aktualisiert.
     */
    private void setupFilterListeners() {
        filterAktion.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        filterPflanze.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        filterStandort.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
    }

    /**
     * Diese Methode wendet die ausgewählten Filter (Pflanze, Standort, Aktion) auf die beiden TableViews an.
     * Sie filtert die Daten basierend auf den ausgewählten Optionen und zeigt nur die gefilterten Einträge an.
     */
    private void applyFilters() {
        String selectedPlant = filterPflanze.getValue();
        String selectedLocation = filterStandort.getValue();
        String selectedAction = filterAktion.getValue();

        // Task-TableView filtern
        filterTaskTableView(selectedPlant, selectedLocation, selectedAction);

        // History-TableView filtern
        filterHistoryTableView(selectedPlant, selectedLocation, selectedAction);
    }

    /**
     * Filtert die currentTask-TableView basierend auf den ausgewählten Pflanze, Standort und Aktion.
     *
     * @param plantName   Der Name der Pflanze (kann null sein).
     * @param location    Der Standort der Pflanze (kann null sein).
     * @param taskType    Der Typ der Aufgabe (z.B. Gießen, Düngen) (kann null sein).
     */
    private void filterTaskTableView(String plantName, String location, String taskType) {
        taskList.clear();

        // Baue das SQL-Statement dynamisch zusammen
        StringBuilder sql = new StringBuilder("SELECT CareTask.task_id, CareTask.task_type, CareTask.due_date, CareTask.completed, CareTask.note, " +
                "PlantProfile.plant_id, PlantProfile.plant_name, PlantProfile.location, PlantProfile.watering_interval, PlantProfile.fertilizing_interval " +
                "FROM CareTask LEFT JOIN PlantProfile ON CareTask.plant_id = PlantProfile.plant_id WHERE CareTask.completed = false");

        // Dynamische Filterung basierend auf den ausgewählten Optionen
        if (plantName != null && !plantName.isEmpty()) {
            sql.append(" AND PlantProfile.plant_name = '").append(plantName).append("'");
        }
        if (location != null && !location.isEmpty()) {
            sql.append(" AND PlantProfile.location = '").append(location).append("'");
        }
        if (taskType != null && !taskType.isEmpty()) {
            sql.append(" AND CareTask.task_type = '").append(taskType).append("'");
        }

        try (Connection conn = SQLiteDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql.toString())) {

            // Stelle sicher, dass die Liste vor dem Hinzufügen geleert wird
            taskList.clear();

            while (rs.next()) {
                PflanzenPflege_Model task = new PflanzenPflege_Model(
                        rs.getInt("task_id"),
                        rs.getInt("plant_id"),
                        rs.getString("task_type"),
                        rs.getDate("due_date").toLocalDate(),
                        rs.getBoolean("completed"),
                        rs.getString("note"),
                        rs.getString("plant_name"),
                        rs.getString("location"),
                        rs.getInt("watering_interval"),
                        rs.getInt("fertilizing_interval"),
                        "actions"
                );
                taskList.add(task);
            }

            // Setze die aktualisierte Liste in die TableView
            current_tasks.setItems(taskList);

        } catch (SQLException e) {
            System.out.println("Fehler beim Filtern der Pflegeaufgaben: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Filtert die CareTaskHistory-TableView basierend auf den ausgewählten Pflanze, Standort und Aktion.
     *
     * @param plantName   Der Name der Pflanze (kann null sein).
     * @param location    Der Standort der Pflanze (kann null sein).
     * @param taskType    Der Typ der Aufgabe (z.B. Gießen, Düngen) (kann null sein).
     */
    private void filterHistoryTableView(String plantName, String location, String taskType) {
        historyList.clear();
        StringBuilder sql = new StringBuilder("SELECT CareTaskHistory.history_id, CareTaskHistory.task_id, CareTaskHistory.plant_id, CareTaskHistory.task_type, " +
                "CareTaskHistory.completion_date, CareTaskHistory.note, PlantProfile.plant_name, PlantProfile.location " +
                "FROM CareTaskHistory LEFT JOIN PlantProfile ON CareTaskHistory.plant_id = PlantProfile.plant_id WHERE 1=1");

        // Dynamische Filterung basierend auf den ausgewählten Optionen
        if (plantName != null) {
            sql.append(" AND PlantProfile.plant_name = '").append(plantName).append("'");
        }
        if (location != null) {
            sql.append(" AND PlantProfile.location = '").append(location).append("'");
        }
        if (taskType != null) {
            if (taskType.equals("Name geändert") || taskType.equals("Standort geändert") ||
                    taskType.equals("Gießintervall geändert") || taskType.equals("Düngeintervall geändert")) {
                // Falls der taskType eine Profiländerung betrifft, entsprechend filtern
                sql.append(" AND CareTaskHistory.task_type = '").append(taskType).append("'");
            } else {
                sql.append(" AND CareTask.task_type = '").append(taskType).append("'");
            }
        }

        // Sortierung nach history_id DESC, um die neuesten Einträge oben anzuzeigen
        sql.append(" ORDER BY CareTaskHistory.history_id DESC");

        try (Connection conn = SQLiteDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql.toString())) {

            while (rs.next()) {
                PflanzenPflegeHistory_Model historyEntry = new PflanzenPflegeHistory_Model(
                        rs.getInt("history_id"),
                        rs.getInt("task_id"),
                        rs.getInt("plant_id"),
                        rs.getString("task_type"),
                        rs.getDate("completion_date").toLocalDate(),
                        rs.getString("note"),
                        rs.getString("plant_name")
                );
                historyList.add(historyEntry);
            }

            history.setItems(historyList);

        } catch (SQLException e) {
            System.out.println("Fehler beim Filtern der Pflegehistorie.");
            e.printStackTrace();
        }
    }

    /**
     * Methode zum Zurücksetzen aller Filter (Pflanze, Standort, Aktion).
     * Setzt die Auswahl in den Filter-ComboBoxen zurück und lädt alle Daten neu.
     */
    private void resetFilters() {
        // Setze alle ComboBoxen zurück
        filterAktion.getSelectionModel().clearSelection();
        filterPflanze.getSelectionModel().clearSelection();
        filterStandort.getSelectionModel().clearSelection();

        // Lade die vollständigen Daten ohne Filter
        loadTaskData();
        loadHistoryData();
    }
}
