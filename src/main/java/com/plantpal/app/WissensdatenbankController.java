package com.plantpal.app;

import com.plantpal.logic.WissensdatenbankService;
import com.plantpal.logic.WissensdatenbankService.KnowledgeBaseEntry;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class WissensdatenbankController {

    @FXML
    private GridPane gridPane;

    @FXML
    private StackPane contentArea;

    private WissensdatenbankService wissensdatenbankService;

    @FXML
    public void initialize() {
        wissensdatenbankService = new WissensdatenbankService();
        loadKnowledgeBaseEntries();
    }

    // Laden der Einträge und Anzeige in der GridPane
    private void loadKnowledgeBaseEntries() {
        List<KnowledgeBaseEntry> entries = wissensdatenbankService.loadKnowledgeBase();
        if (entries != null) {
            int row = 0;
            int column = 0;

            for (KnowledgeBaseEntry entry : entries) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/WissensdatenbankKachel.fxml"));
                    Pane tile = loader.load();

                    // Setze den Namen und das Bild in die Kachel
                    ImageView imageView = (ImageView) tile.lookup("#image");
                    Label nameLabel = (Label) tile.lookup("#lbl_name");

                    nameLabel.setText(entry.getName());

                    // Hole den Controller der Kachel
                    WissensdatenbankKachelController tileController = loader.getController();

                    // Setze die Daten in der Kachel
                    tileController.setTileData(entry, this);

                    // Lade das Bild, falls vorhanden
                    if (entry.getImagePath() != null) {
                        String imagePath = "/" + entry.getImagePath();
                        InputStream imageStream = getClass().getResourceAsStream(imagePath);

                        if (imageStream != null) {
                            Image image = new Image(imageStream);
                            imageView.setImage(image);
                        } else {
                            System.out.println("Bild konnte nicht gefunden werden: " + imagePath);
                        }
                    }
                    // Tooltip für den Bildautor setzen, falls vorhanden
                    if (entry.getImageAuthor() != null) {
                        Tooltip tooltip = new Tooltip(entry.getImageAuthor());
                        Tooltip.install(tile, tooltip);
                    }

                    // Füge einen Klick-Listener hinzu, um die Detailansicht zu öffnen
                    tile.setOnMouseClicked(event -> openDetails(entry));

                    // Füge die Kachel zum GridPane hinzu
                    gridPane.add(tile, column, row);

                    // Layout: Füge die Kacheln in mehreren Spalten und Zeilen hinzu
                    column++;
                    if (column == 3) { // Anzahl der Spalten anpassen
                        column = 0;
                        row++;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Methode zum Öffnen der Detailansicht, wenn auf eine Kachel geklickt wird
    protected void openDetails(KnowledgeBaseEntry entry) {
        try {
            // Lade die FXML-Datei der Detailansicht
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/WissensdatenbankDetails.fxml"));
            Parent detailsView = loader.load();

            // Hole den Controller der Detailansicht
            WissensdatenbankDetailsController detailsController = loader.getController();

            // Setze die Details im Controller
            detailsController.setDetails(entry);

            // Erstelle ein neues Fenster (Stage) für die Detailansicht
            Stage detailStage = new Stage();
            detailStage.setScene(new Scene(detailsView));

            // Setze das aktuelle Fenster als Owner des neuen Fensters
            Stage mainStage = (Stage) gridPane.getScene().getWindow();
            detailStage.initOwner(mainStage);

            // Setze die Modality auf APPLICATION_MODAL, um das Hauptfenster auszublenden
            detailStage.initModality(Modality.APPLICATION_MODAL);

            detailStage.initStyle(StageStyle.UNDECORATED);

            // Ändere die Opacity des Hauptfensters, um es ausgegraut darzustellen
            mainStage.getScene().getRoot().setOpacity(0.7);

            // Zeige das neue Fenster an
            detailStage.showAndWait();

            // Setze die Opacity des Hauptfensters nach dem Schließen des Detailfensters zurück
            mainStage.getScene().getRoot().setOpacity(1.0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
