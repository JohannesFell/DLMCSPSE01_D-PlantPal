module com.plantpal.PlantPal {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires de.jensd.fx.glyphs.fontawesome;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires java.sql;
    requires org.xerial.sqlitejdbc;
    requires unirest.java;
    requires com.mailjet.api;
    requires org.json;

    opens com.plantpal.app to javafx.fxml;
    exports com.plantpal.app;
    exports com.plantpal.model;
    opens com.plantpal.model to javafx.fxml;
    exports com.plantpal.logic;
    opens com.plantpal.logic to javafx.fxml;
}
