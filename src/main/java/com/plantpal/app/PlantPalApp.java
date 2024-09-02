package com.plantpal.app;

import com.plantpal.database.SQLiteDB;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.Objects;

public class PlantPalApp extends Application {
    
    private double x,y = 0;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/MainScreen.fxml")));

            root.setOnMousePressed((MouseEvent event) ->{
                x = event.getSceneX();
                y = event.getSceneY();
            });

            root.setOnMouseDragged((MouseEvent event) ->{
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


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
