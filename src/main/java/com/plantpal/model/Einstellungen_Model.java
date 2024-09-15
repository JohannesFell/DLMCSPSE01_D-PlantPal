package com.plantpal.model;

public class Einstellungen_Model {

    private String username;
    private String emailAddress;
    private String smtpHost;
    private int smtpPort;
    private String smtpUsername;
    private String smtpPassword;
    private boolean useTls;
    private int daysBeforeReminder;
    private boolean appNotification;
    private boolean emailNotification;
    private String notificationEmail;

    // Constructor
    public Einstellungen_Model(String username, String emailAddress, String smtpHost, int smtpPort, String smtpUsername,
                         String smtpPassword, boolean useTls, int daysBeforeReminder, boolean appNotification,
                         boolean emailNotification, String notificationEmail) {
        this.username = username;
        this.emailAddress = emailAddress;
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
        this.smtpUsername = smtpUsername;
        this.smtpPassword = smtpPassword;
        this.useTls = useTls;
        this.daysBeforeReminder = daysBeforeReminder;
        this.appNotification = appNotification;
        this.emailNotification = emailNotification;
        this.notificationEmail = notificationEmail;
    }

    // Getters and Setters for each field
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getSmtpHost() {
        return smtpHost;
    }

    public void setSmtpHost(String smtpHost) {
        this.smtpHost = smtpHost;
    }

    public int getSmtpPort() {
        return smtpPort;
    }

    public void setSmtpPort(int smtpPort) {
        this.smtpPort = smtpPort;
    }

    public String getSmtpUsername() {
        return smtpUsername;
    }

    public void setSmtpUsername(String smtpUsername) {
        this.smtpUsername = smtpUsername;
    }

    public String getSmtpPassword() {
        return smtpPassword;
    }

    public void setSmtpPassword(String smtpPassword) {
        this.smtpPassword = smtpPassword;
    }

    public boolean isUseTls() {
        return useTls;
    }

    public void setUseTls(boolean useTls) {
        this.useTls = useTls;
    }

    public int getDaysBeforeReminder() {
        return daysBeforeReminder;
    }

    public void setDaysBeforeReminder(int daysBeforeReminder) {
        this.daysBeforeReminder = daysBeforeReminder;
    }

    public boolean isAppNotification() {
        return appNotification;
    }

    public void setAppNotification(boolean appNotification) {
        this.appNotification = appNotification;
    }

    public boolean isEmailNotification() {
        return emailNotification;
    }

    public void setEmailNotification(boolean emailNotification) {
        this.emailNotification = emailNotification;
    }

    public String getNotificationEmail() {
        return notificationEmail;
    }

    public void setNotificationEmail(String notificationEmail) {
        this.notificationEmail = notificationEmail;
    }
}

