package com.plantpal.app;

import com.plantpal.database.CareTaskHistoryRepository;
import com.plantpal.database.CareTaskRepository;
import com.plantpal.database.PlantProfileRepository;
import com.plantpal.logic.BenachrichtigungsService;
import com.plantpal.logic.EinstellungenManager;
import com.plantpal.logic.EmailService;
import com.plantpal.logic.PflegeAufgabenService;
import com.plantpal.model.Einstellungen_Model;
import com.plantpal.model.PflanzenPflege_Model;
import com.plantpal.utils.DateUtils;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * Controller für die Hauptansicht der Anwendung.
 *
 * Der `MainScreenController` verwaltet die allgemeine Navigation der Anwendung und die
 * Verknüpfung der verschiedenen Bildschirme, wie Pflanzenprofile und Pflegeaufgaben.
 *
 * Funktionen:
 * - Steuert die Navigation zu den verschiedenen Ansichten
 * - Verwalten des Seitenmenüs und der Schaltflächen für die Navigation
 * - Initialisierung der Hauptansicht und Laden der Komponenten
 *
 * Zugehörige FXML-Datei: MainScreen.fxml
 */
public class MainScreenController implements Initializable {

    @FXML
    private Button btn_einstellungen;

    @FXML
    private Button btn_pflanzen;

    @FXML
    private Button btn_pflege;

    @FXML
    private Button btn_wissensdatenbank;

    @FXML
    private StackPane contentArea;

    @FXML
    private Label exit;

    @FXML
    private HBox root_mainscreen;

    @FXML
    private AnchorPane sidebar_anchorpane;

    @FXML
    private Pane sidebar_inner_pane;

    @FXML
    private FontAwesomeIconView btn_notifications;

    @FXML
    private Label lbl_notification_count;

    @FXML
    private Label notificationLabel;

    private BenachrichtigungsService benachrichtigungsService;
    private PflegeAufgabenService pflegeAufgabenService;
    private BenachrichtigungsController benachrichtigungsController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        CareTaskRepository careTaskRepository = new CareTaskRepository();
        // PflegeAufgabenService initialisieren
        pflegeAufgabenService = new PflegeAufgabenService(careTaskRepository, new CareTaskHistoryRepository(), new PlantProfileRepository());

        // Initialisiere den BenachrichtigungsService
        benachrichtigungsService = new BenachrichtigungsService(careTaskRepository);

        // Führe die Pflegeaufgaben-Update-Funktion aus
        try {
            pflegeAufgabenService.updateAllCareTasks();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Setze den Exit-Button
        exit.setOnMouseClicked(e -> System.exit(0));


        // Überprüfe die In-App-Benachrichtigungseinstellungen
        checkInAppNotificationSettings();

        // Event-Handler für das Klicken auf das Benachrichtigungs-Icon
        btn_notifications.setOnMouseClicked(event -> showNotifications());
    }

    /**
     * Aktualisiert den Benachrichtigungs-Button basierend auf den anstehenden Benachrichtigungen.
     */
    public void updateNotificationButton() {
        // Lade die aktuellen Einstellungen
        EinstellungenManager einstellungenManager = new EinstellungenManager();
        Einstellungen_Model settings = einstellungenManager.loadSettings();
        int daysBeforeReminderApp = settings.getDaysBeforeReminderApp();

        // Anzahl der anstehenden Benachrichtigungen ermitteln
        int notificationCount = benachrichtigungsService.checkAndNotifyUpcomingTasks(daysBeforeReminderApp);

        // Aktualisiere den Badge-Zähler
        if (notificationCount > 0) {
            lbl_notification_count.setText(String.valueOf(notificationCount));
        }
    }

    /**
     * Aktualisiert die Sichtbarkeit von Benachrichtigungs-Button und Label basierend auf dem Status.
     *
     * @param isVisible true, wenn die Benachrichtigungen sichtbar sein sollen, sonst false.
     */
    public void updateNotificationVisibility(boolean isVisible) {
        btn_notifications.setVisible(isVisible);
        lbl_notification_count.setVisible(isVisible);
    }

    /**
     * Überprüft die In-App-Benachrichtigungseinstellungen und zeigt oder versteckt den Benachrichtigungs-Button.
     */
    private void checkInAppNotificationSettings() {
        EinstellungenManager einstellungenManager = new EinstellungenManager();
        Einstellungen_Model settings = einstellungenManager.loadSettings();

        // Wenn In-App-Benachrichtigungen deaktiviert sind, blende Button und Label aus
        if (!settings.isAppNotification()) {
            btn_notifications.setVisible(false);
            lbl_notification_count.setVisible(false);
        } else {
            // Wenn aktiviert, aktualisiere die Benachrichtigungsanzahl
            btn_notifications.setVisible(true);
            updateNotificationButton();
        }
    }

    /**
     * Öffnet das Benachrichtigungsfenster mit allen anstehenden Benachrichtigungen.
     */
    @FXML
    public void showNotifications() {
        try {
            // Lade die aktuellen Einstellungen
            EinstellungenManager einstellungenManager = new EinstellungenManager();
            Einstellungen_Model settings = einstellungenManager.loadSettings();
            int daysBeforeReminderApp = settings.getDaysBeforeReminderApp();

            // Lade das Hauptbenachrichtigungsfenster (mit dem GridPane)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Benachrichtigungen.fxml"));
            Parent root = loader.load();
            benachrichtigungsController = loader.getController();

            // Setze die Einstellungen und Services im BenachrichtigungsController
            benachrichtigungsController.setEinstellungen(settings);
            benachrichtigungsController.setPflegeAufgabenService(pflegeAufgabenService);
            benachrichtigungsController.setEmailService(new EmailService());

            // Überprüfe, ob der E-Mail-Sende-Button sichtbar sein soll, basierend auf den Mail-Benachrichtigungseinstellungen
            boolean isMailNotificationEnabled = settings.isEmailNotification();
            benachrichtigungsController.toggleSendMailButton(isMailNotificationEnabled);

            // Füge die dynamischen Kacheln hinzu
            List<PflanzenPflege_Model> pendingTasks = benachrichtigungsService.getTasksDueIn(daysBeforeReminderApp);

            // Sortiere die Aufgaben nach dem Fälligkeitsdatum (aufsteigend)
            pendingTasks.sort(Comparator.comparing(PflanzenPflege_Model::getDue_date));

            GridPane gridPane = benachrichtigungsController.getGridPane();

            int row = 0;
            int column = 0;
            for (PflanzenPflege_Model task : pendingTasks) {
                FXMLLoader tileLoader = new FXMLLoader(getClass().getResource("/fxml/BenachrichtigungsKachel.fxml"));
                Parent tile = tileLoader.load();
                BenachrichtigungsController tileController = tileLoader.getController();

                // Übergebe die erforderlichen Daten an die Kachel (einschließlich gridPane und mainScreenController)
                tileController.setNotificationData(
                        task.getPlant_name(),
                        task.getLocation(),
                        task.getTask_type(),
                        DateUtils.formatDate(task.getDue_date()),
                        task.getTask_id(),
                        task.getPlant_id(),
                        pflegeAufgabenService,
                        gridPane,
                        this
                );

                // Füge die Kachel zum GridPane hinzu
                gridPane.add(tile, column, row);

                // Zeilen und Spalten anpassen, um die Kacheln gleichmäßig anzuordnen
                column++;
                if (column == 3) { // Wenn 3 Kacheln in einer Reihe, gehe zur nächsten Zeile
                    column = 0;
                    row++;
                }
            }

            // Setze das root in die Hauptansicht
            contentArea.getChildren().clear();
            contentArea.getChildren().add(root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Aktualisiert die Sichtbarkeit des Mail-Sende-Buttons in der Benachrichtigungsansicht.
     *
     * @param isVisible true, wenn der Button sichtbar sein soll, sonst false.
     */
    public void updateSendMailButtonVisibility(boolean isVisible) {
        if (benachrichtigungsController != null) {
            benachrichtigungsController.toggleSendMailButton(isVisible);
        } else {
            System.out.println("BenachrichtigungsController ist nicht initialisiert.");
        }
    }

    private void loadView(String fxmlPath) throws IOException {
        Parent fxml = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxmlPath)));
        contentArea.getChildren().clear();
        contentArea.getChildren().add(fxml);
    }

    public void pflanzenprofile() throws IOException {
        loadView("/fxml/PflanzenProfile.fxml");
    }

    public void pflanzenpflege() throws IOException {
        loadView("/fxml/PflanzenPflege.fxml");
    }

    public void wissensdatenbank() throws IOException {
        loadView("/fxml/Wissensdatenbank.fxml");
    }

    public void einstellungen() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Einstellungen.fxml"));
        Parent settingsView = loader.load();
        EinstellungenController controller = loader.getController();
        controller.setMainScreenController(this);
        contentArea.getChildren().clear();
        contentArea.getChildren().add(settingsView);
    }
}
