package com.plantpal.app;

import com.plantpal.utils.DateUtils;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.util.List;

public class SlideshowController {

    @FXML
    private FontAwesomeIconView arrow_left;

    @FXML
    private FontAwesomeIconView arrow_right;

    @FXML
    private ImageView slideshowImage;

    @FXML
    private ImageView exit;

    @FXML
    private Label dateLabel;

    private List<Image> plantPhotos;
    private List<String> photoDates;
    private int currentIndex = 0;

    /**
     * Initialisiert die Slideshow mit den übergebenen Bilddaten und Datumswerten.
     *
     * @param plantPhotos Liste der Bilder.
     * @param photoDates  Liste der zugehörigen Datumswerte.
     */
    public void initializeSlideshow(List<Image> plantPhotos, List<String> photoDates) {
        this.plantPhotos = plantPhotos;
        this.photoDates = photoDates;

        if (plantPhotos.isEmpty()) {
            // Wenn keine Bilder vorhanden sind, eine Meldung anzeigen
            dateLabel.setText("Keine Bilder vorhanden.");
            arrow_left.setVisible(false);
            arrow_right.setVisible(false);
            slideshowImage.setImage(null);
        }

        // Setze das erste Bild
        if (!plantPhotos.isEmpty()) {
            showImage(currentIndex);
        }

        // Listener für die Navigation
        arrow_left.setOnMouseClicked(event -> previousImage());
        arrow_right.setOnMouseClicked(event -> nextImage());

        // Listener für das Schließen
        exit.setOnMouseClicked(event -> handleExit());
    }

    /**
     * Zeigt das aktuelle Bild in der Slideshow an und aktualisiert das Datumslabel.
     */
    private void showImage(int index) {
        slideshowImage.setImage(plantPhotos.get(index));
        dateLabel.setText(DateUtils.formatTimestamp(LocalDateTime.parse(photoDates.get(index))));
    }

    /**
     * Zeigt das nächste Bild in der Slideshow an.
     */
    private void nextImage() {
        // Erhöht den Index, um das nächste Bild anzuzeigen.
        // Wenn das Ende der Liste erreicht ist, wird der Index auf 0 gesetzt,
        // sodass die Slideshow wieder von vorne beginnt
        currentIndex = (currentIndex + 1) % plantPhotos.size();
        showImage(currentIndex);
    }

    /**
     * Zeigt das vorherige Bild in der Slideshow an.
     */
    private void previousImage() {
        // Erhöht den Index, um das nächste Bild anzuzeigen.
        // Wenn das Ende der Liste erreicht ist, wird der Index auf 0 gesetzt,
        // sodass die Slideshow wieder von vorne beginnt
        currentIndex = (currentIndex - 1 + plantPhotos.size()) % plantPhotos.size();
        showImage(currentIndex);
    }

    /**
     * Schließt das Fenster der Slideshow.
     */
    @FXML
    private void handleExit() {
        Stage stage = (Stage) exit.getScene().getWindow();
        stage.close();
    }
}
