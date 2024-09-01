package com.plantpal.app;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

public class PflanzenProfileController {
    @FXML
    private TextField botanical_name;

    @FXML
    private Button btn_add;

    @FXML
    private Button btn_clear;

    @FXML
    private Button btn_delete;

    @FXML
    private Button btn_update;

    @FXML
    private ImageView image;

    @FXML
    private Button image_import;

    @FXML
    private Button image_show;

    @FXML
    private ComboBox<?> intervall_duengen;

    @FXML
    private ComboBox<?> intervall_giessen;

    @FXML
    private TextField kaufdatum;

    @FXML
    private TextField last_duengen;

    @FXML
    private TextField last_giessen;

    @FXML
    private TextField name;

    @FXML
    private TextField search;

    @FXML
    private TextField standort;
}
