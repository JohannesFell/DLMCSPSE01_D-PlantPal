package com.plantpal.utils;

import javafx.animation.TranslateTransition;
import javafx.scene.control.Label;
import javafx.util.Duration;

/**
 * Diese Klasse enthält verschiedene Hilfsmethoden, die in der gesamten Anwendung verwendet werden können.
 */
public class NotificationUtils {

    /**
     * Zeigt eine Benachrichtigung mit einer Einblendanimation an.
     *
     * @param notificationLabel Das Label, in dem die Benachrichtigung angezeigt wird.
     * @param message           Die Nachricht, die angezeigt werden soll.
     */
    public static void showNotification(Label notificationLabel, String message) {
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
}
