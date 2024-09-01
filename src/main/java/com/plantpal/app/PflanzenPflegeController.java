package com.plantpal.app;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;

public class PflanzenPflegeController {

    @FXML
    private TableView<?> current_tasks;

    @FXML
    private TableView<?> history;

    @FXML
    private ListView<?> pflanzen;

    @FXML
    private ComboBox<?> standort;
}
