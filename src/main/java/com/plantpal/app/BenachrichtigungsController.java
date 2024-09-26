package com.plantpal.app;

import com.plantpal.logic.EmailService;
import com.plantpal.logic.PflegeAufgabenService;
import com.plantpal.model.Einstellungen_Model;
import com.plantpal.utils.NotificationUtils;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BenachrichtigungsController {

    @FXML
    private GridPane gridPane;

    @FXML
    private TextField txt_pflanzenname, txt_standort, txt_aufgabe, txt_faellig_am;

    @FXML
    private Button btn_snd_mail;

    @FXML
    private Label notificationLabel;

    private int taskId;
    private int plantId;
    private String taskType;
    private PflegeAufgabenService pflegeAufgabenService;
    private EmailService emailService;
    private Einstellungen_Model einstellungen;
    private MainScreenController mainScreenController;

    /**
     * Initialisiert den Controller und den Button-Handler für den E-Mail-Versand.
     */
    @FXML
    public void initialize() {

    }

    /**
     * Setzt die Benachrichtigungsdaten in die UI und speichert die benötigten Referenzen.
     *
     * @param plantName Name der Pflanze.
     * @param location Standort der Pflanze.
     * @param taskType Typ der Aufgabe.
     * @param dueDate Fälligkeitsdatum der Aufgabe.
     * @param taskId ID der Aufgabe.
     * @param plantId ID der Pflanze.
     * @param pflegeAufgabenService Service zur Pflege der Aufgaben.
     * @param gridPane Referenz zum GridPane.
     * @param mainScreenController Referenz zum MainScreenController.
     */
    public void setNotificationData(String plantName, String location, String taskType, String dueDate, int taskId, int plantId,
                                    PflegeAufgabenService pflegeAufgabenService, GridPane gridPane, MainScreenController mainScreenController) {
        this.taskId = taskId;
        this.taskType = taskType;
        this.plantId = plantId;
        this.pflegeAufgabenService = pflegeAufgabenService;
        this.gridPane = gridPane;
        this.mainScreenController = mainScreenController;

        // Setze die Felder in der UI direkt
        txt_pflanzenname.setText(plantName);
        txt_standort.setText(location);
        txt_aufgabe.setText(taskType);
        txt_faellig_am.setText(dueDate);
    }

    /**
     * Aktion beim Klicken auf den "E-Mail-Benachrichtigung senden"-Button.
     * Sendet alle Benachrichtigungen per E-Mail.
     */
    @FXML
    private void sendEmailNotification() {

        try {
            // Generiere den Betreff und den E-Mail-Inhalt basierend auf den angezeigten Kacheln
            String subject = "PlantPal - Fällige Aufgaben";
            String emailContent = generateEmailContentFromGridPane(); // HTML-Inhalt

            if (emailContent.isEmpty()) {
                NotificationUtils.showNotification(notificationLabel, "Es gibt keine fälligen Aufgaben zu versenden.");
                return;
            }

            // Verwende den EmailService, um die E-Mail zu senden
            emailService.sendMail(einstellungen.getApiKey(),
                    einstellungen.getPrivateApiKey(),
                    einstellungen.getEmailAddressSender(),
                    einstellungen.getNotificationEmail(),
                    subject,
                    emailContent);

            NotificationUtils.showNotification(notificationLabel, "E-Mail erfolgreich versendet!");

        } catch (Exception e) {
            NotificationUtils.showNotification(notificationLabel, "Fehler beim Senden der E-Mail: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Generiert den Inhalt der E-Mail basierend auf den Kacheln im GridPane, mit HTML-Formatierung.
     *
     * @return Der generierte E-Mail-Inhalt als HTML-String.
     */
    private String generateEmailContentFromGridPane() {
        StringBuilder emailBody = new StringBuilder("<h1>Fällige Aufgaben</h1>");
        List<Node> notificationTiles = gridPane.getChildren();

        if (notificationTiles.isEmpty()) {
            return "<p>Es gibt keine fälligen Aufgaben.</p>";
        }

        // HTML für Tabelle erstellen
        emailBody.append("<table style='width:100%; border-collapse: collapse;'>")
                .append("<tr style='border-bottom: 2px solid #ccc; text-align: left;'>")
                .append("<th>Pflanze</th><th>Aufgabe</th><th>Standort</th><th>Fällig am</th></tr>");

        // Iteriere über die Kacheln und hole die Task-Informationen
        for (Node node : notificationTiles) {
            if (node instanceof Parent) {
                TextField pflanzenNameField = (TextField) node.lookup("#txt_pflanzenname");
                TextField aufgabeField = (TextField) node.lookup("#txt_aufgabe");
                TextField standortField = (TextField) node.lookup("#txt_standort");
                TextField faelligAmField = (TextField) node.lookup("#txt_faellig_am");

                // Fügt eine neue Tabellenzeile für jede Aufgabe hinzu
                emailBody.append("<tr style='border-bottom: 1px solid #ccc;'>")
                        .append("<td>").append(pflanzenNameField.getText()).append("</td>")
                        .append("<td>").append(aufgabeField.getText()).append("</td>")
                        .append("<td>").append(standortField.getText()).append("</td>")
                        .append("<td>").append(faelligAmField.getText()).append("</td>")
                        .append("</tr>");
            }
        }

        // Schließe die Tabelle
        emailBody.append("</table>");
        return emailBody.toString();
    }

    /**
     * Markiert die Aufgabe als erledigt und schließt die Benachrichtigung.
     */
    @FXML
    private void erledigt() {
        String note = "";
        LocalDate completionDate = LocalDate.now();

        // Die Aufgabe wird als erledigt markiert
        pflegeAufgabenService.markTaskAsCompleted(taskId, plantId, taskType, note, completionDate);

        // Schließt die Benachrichtigung
        closeNotification();
    }

    /**
     * Entfernt die Kachel und aktualisiert das Badge.
     */
    private void closeNotification() {
        // Entferne die Kachel aus dem GridPane
        Node thisTile = txt_pflanzenname.getParent();
        gridPane.getChildren().remove(thisTile);

        // Neu Anordnen der verbleibenden Kacheln
        rearrangeGridPane();

        // Aktualisiere die Benachrichtigungsanzahl im MainScreenController
        if (mainScreenController != null) {
            mainScreenController.updateNotificationButton();
        }
    }

    /**
     * Ordnet das GridPane neu, um Lücken nach dem Entfernen einer Kachel zu vermeiden.
     */
    private void rearrangeGridPane() {
        int row = 0;
        int column = 0;

        // Hole die verbleibenden Kacheln (Nodes) aus dem GridPane
        List<Node> remainingTiles = new ArrayList<>(gridPane.getChildren());

        // Leere das GridPane
        gridPane.getChildren().clear();

        // Füge die verbleibenden Kacheln neu hinzu
        for (Node tile : remainingTiles) {
            gridPane.add(tile, column, row);
            column++;
            if (column == 3) {
                column = 0;
                row++;
            }
        }
    }

    /**
     * Aktualisiert die Sichtbarkeit des Mail-Sende-Buttons.
     *
     * @param isVisible true, wenn der Button sichtbar sein soll, sonst false.
     */
    public void toggleSendMailButton(boolean isVisible) {

        btn_snd_mail.setVisible(isVisible);
    }

    /**
     * Gibt das GridPane zurück, das für die Anzeige der Benachrichtigungskacheln verwendet wird.
     *
     * @return Das GridPane für Benachrichtigungen.
     */
    public GridPane getGridPane() {
        return gridPane;
    }

    // Setter für den PflegeAufgabenService, EmailService und die Einstellungen

    public void setPflegeAufgabenService(PflegeAufgabenService pflegeAufgabenService) {
        this.pflegeAufgabenService = pflegeAufgabenService;
    }

    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }

    public void setEinstellungen(Einstellungen_Model einstellungen) {
        this.einstellungen = einstellungen;
    }
}
