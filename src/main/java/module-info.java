module com.plantpal.PlantPal {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires de.jensd.fx.glyphs.fontawesome;

    requires org.controlsfx.controls;
    requires java.sql;
    requires org.xerial.sqlitejdbc;
    requires com.mailjet.api;
    requires org.json;
    requires com.google.gson;
    requires java.desktop;
    requires com.zaxxer.hikari;
    requires org.slf4j;
    requires org.slf4j.simple;

    opens com.plantpal.app to javafx.fxml;
    exports com.plantpal.app;
    exports com.plantpal.model;
    opens com.plantpal.model to javafx.fxml;
    exports com.plantpal.logic;
    opens com.plantpal.logic to com.google.gson, javafx.fxml;
}
