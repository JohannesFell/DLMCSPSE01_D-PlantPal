package com.plantpal.app;

import com.plantpal.database.SQLiteDB;
import com.plantpal.logic.EinstellungenManager;
import com.plantpal.model.Einstellungen_Model;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Hauptklasse der Anwendung `PlantPal`.
 *
 * Diese Klasse startet die Anwendung und lädt die Haupt-Benutzeroberfläche. Sie ist der Einstiegspunkt
 * der gesamten JavaFX-Anwendung und steuert die Initialisierung der grafischen Benutzeroberfläche.
 *
 * Funktionen:
 * - Initialisierung des Hauptfensters der Anwendung
 * - Laden der FXML-Dateien und Setzen der Hauptbühne
 * - Starten der Anwendung und Festlegen des grundlegenden Anwendungslayouts
 * - Überprüfung der Benachrichtigungen
 */
public class PlantPalApp extends Application {

    private double x,y = 0;
    private ScheduledExecutorService scheduler;
    private MainScreenController mainScreenController;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            // Lade die MainScreen.fxml und den zugehörigen Controller
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/fxml/MainScreen.fxml")));
            Parent root = loader.load();

            // Hol dir den MainScreenController aus dem FXMLLoader
            mainScreenController = loader.getController();  // Speichere den Controller

            // Ermögliche das Verschieben des Fensters per Drag
            root.setOnMousePressed((MouseEvent event) -> {
                x = event.getSceneX();
                y = event.getSceneY();
            });

            root.setOnMouseDragged((MouseEvent event) -> {
                primaryStage.setX(event.getScreenX() - x);
                primaryStage.setY(event.getScreenY() - y);
                primaryStage.setOpacity(.8);
            });

            root.setOnMouseReleased((MouseEvent event) -> primaryStage.setOpacity(1));

            primaryStage.initStyle(StageStyle.TRANSPARENT);
            primaryStage.setScene(new Scene(root, 1200, 800));
            primaryStage.show();

            // Öffne die Datenbank und erstelle die Tabellen
            SQLiteDB.getConnection();

            // Überprüfe die Benachrichtigungen beim Start
            checkNotifications();

            // Starte den Scheduler, um Benachrichtigungen täglich zu prüfen
            startNotificationScheduler();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Überprüft die Benachrichtigungen beim Start und aktualisiert den Badge.
     */
    private void checkNotifications() {
        // Lade die aktuellen Einstellungen aus der Datenbank
        EinstellungenManager einstellungenManager = new EinstellungenManager();
        Einstellungen_Model settings = einstellungenManager.loadSettings();

        // Prüfen, ob In-App-Benachrichtigungen aktiviert sind
        boolean isAppNotificationEnabled = settings.isAppNotification();

        if (isAppNotificationEnabled) {
            // Badge wird aktualisiert
            mainScreenController.updateNotificationButton();
        }
    }

    /**
     * Startet einen Scheduler, der täglich um Mitternacht die Benachrichtigungen überprüft.
     */
    private void startNotificationScheduler() {
        scheduler = Executors.newScheduledThreadPool(1);

        // Berechne die Zeit bis Mitternacht für die erste Ausführung
        LocalTime midnight = LocalTime.MIDNIGHT;
        LocalTime now = LocalTime.now();
        long initialDelay = Duration.between(now, midnight).toMinutes();

        // Führe die Benachrichtigungsprüfung täglich um Mitternacht durch
        scheduler.scheduleAtFixedRate(() -> {
            try {
                checkNotifications();  // Aktualisiere den Badge
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, initialDelay, 24 * 60, TimeUnit.MINUTES);  // 24 Stunden nach der initialen Verzögerung
    }

    @Override
    public void stop() {
        // Scheduler beenden, wenn die Anwendung geschlossen wird
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }
}
