package com.plantpal.logic;

import com.plantpal.model.PflanzenProfile_Model;
import com.plantpal.database.PhotoLogRepository;
import com.plantpal.utils.DateUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.sql.SQLException;

/**
 * Service-Klasse für das Verarbeiten und Speichern von Bildern.
 */
public class ImageService {

    private final PhotoLogRepository photoLogRepository;

    public ImageService() {
        this.photoLogRepository = new PhotoLogRepository();
    }

    /**
     * Speichert das ausgewählte Bild im Upload-Verzeichnis und in der Datenbank.
     *
     * @param selectedFile Die ausgewählte Bilddatei.
     * @param selectedPlant Das ausgewählte Pflanzenprofil.
     * @throws IOException   Wenn ein Fehler beim Kopieren der Datei auftritt.
     * @throws SQLException  Wenn ein Fehler beim Speichern in der Datenbank auftritt.
     */
    public void importImage(File selectedFile, PflanzenProfile_Model selectedPlant) throws IOException, SQLException {
        if (selectedFile == null || selectedPlant == null) {
            throw new IllegalArgumentException("Bilddatei oder Pflanzenprofil ist ungültig.");
        }

        // Zielordner im Ressourcenverzeichnis
        Path destinationDir = Path.of("src/main/resources/images/uploads/");
        if (!Files.exists(destinationDir)) {
            Files.createDirectories(destinationDir);
        }

        // Neuer Dateiname mit Zeitstempel
        String newFileName = DateUtils.appendTimestampToFileName(selectedFile.getName());
        Path destinationFile = destinationDir.resolve(newFileName);

        // Datei kopieren
        Files.copy(selectedFile.toPath(), destinationFile, StandardCopyOption.REPLACE_EXISTING);

        // Bildpfad in der Datenbank speichern
        photoLogRepository.savePhoto(selectedPlant.getPlant_id(), destinationFile.toString(), LocalDateTime.now());
    }
}