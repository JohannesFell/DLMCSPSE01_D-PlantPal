package com.plantpal.app;

import com.plantpal.logic.WissensdatenbankService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

public class WissensdatenbankKachelController {

    @FXML
    private AnchorPane tilePane;

    @FXML
    private ImageView image;

    @FXML
    private Label lbl_name;

    /**
     * Setzt die Daten der Kachel (Bild und Name).
     */
    public void setTileData(WissensdatenbankService.KnowledgeBaseEntry entry, WissensdatenbankController parentController) {

        lbl_name.setText(entry.getName());
        if (entry.getImagePath() != null && getClass().getResourceAsStream("/" + entry.getImagePath()) != null) {
            image.setImage(new Image(getClass().getResourceAsStream("/" + entry.getImagePath())));
        }

        tilePane.setOnMouseClicked(event -> parentController.openDetails(entry));
    }
}
