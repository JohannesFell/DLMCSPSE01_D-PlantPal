package com.plantpal.model;

/**
 * Das Modell für die Anwendungseinstellungen.
 * Enthält die Konfigurationsdaten, die in der Settings-Tabelle gespeichert sind,
 * sowie die Getter und Setter für die Verwaltung dieser Daten.
 */
public class Einstellungen_Model {

    private String username;
    private String emailAddressSender;
    private String notificationEmail;
    private boolean appNotification;
    private boolean emailNotification;
    private int daysBeforeReminderApp;
    private String apiKey;
    private String privateApiKey;

    // Konstruktor
    public Einstellungen_Model(String username, String emailAddressSender, boolean appNotification,
                               boolean emailNotification, int daysBeforeReminderApp, String notificationEmail,
                               String apiKey, String privateApiKey) {
        this.username = username;
        this.emailAddressSender = emailAddressSender;
        this.appNotification = appNotification;
        this.emailNotification = emailNotification;
        this.daysBeforeReminderApp = daysBeforeReminderApp;
        this.notificationEmail = notificationEmail;
        this.apiKey = apiKey;
        this.privateApiKey = privateApiKey;
    }


    // Getter und Setter
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmailAddressSender() { return emailAddressSender; }
    public void setEmailAddressSender(String emailAddressSender) { this.emailAddressSender = emailAddressSender; }

    public boolean isAppNotification() { return appNotification; }
    public void setAppNotification(boolean appNotification) { this.appNotification = appNotification; }

    public boolean isEmailNotification() { return emailNotification; }
    public void setEmailNotification(boolean emailNotification) { this.emailNotification = emailNotification; }

    public int getDaysBeforeReminderApp() { return daysBeforeReminderApp; }
    public void setDaysBeforeReminderApp(int daysBeforeReminderApp) { this.daysBeforeReminderApp = daysBeforeReminderApp; }

    public String getNotificationEmail() { return notificationEmail; }
    public void setNotificationEmail(String notificationEmail) { this.notificationEmail = notificationEmail; }

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    public String getPrivateApiKey() { return privateApiKey; }
    public void setPrivateApiKey(String privateApiKey) { this.privateApiKey = privateApiKey; }
}
