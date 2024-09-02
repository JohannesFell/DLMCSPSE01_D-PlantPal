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

    opens com.plantpal.app to javafx.fxml;
    exports com.plantpal.app;
}
