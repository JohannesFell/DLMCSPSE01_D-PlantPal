package com.plantpal.database;

import com.plantpal.model.PflanzenProfile_Model;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Das {@code PlantProfileRepository} ist für den Zugriff und die Verwaltung der Pflanzenprofile in der SQLite-Datenbank
 * zuständig. Es stellt Methoden zum Laden, Hinzufügen, Aktualisieren und Löschen von Pflanzenprofilen zur Verfügung.
 *
 * Diese Klasse implementiert CRUD-Operationen für die "PlantProfile"-Tabelle und bietet zusätzliche Funktionen
 * wie das Abrufen von Pflanzennamen und Standorten, um Filter- oder Auswahlmöglichkeiten zu unterstützen.
 *
 * Die Klasse verwendet SQL-Anweisungen für den direkten Datenbankzugriff und bietet eine Schnittstelle zur Verwaltung der
 * Pflanzenprofildaten, die für die Pflegeaufgaben und die Anzeige in der Anwendung erforderlich sind.
 */
public class PlantProfileRepository {

    /**
     * Holt alle Pflanzenprofile aus der Datenbank.
     * @return Liste von PflanzenProfile_Model.
     */
    public List<PflanzenProfile_Model> getAllPlantProfiles() {
        List<PflanzenProfile_Model> plantList = new ArrayList<>();
        String sql = "SELECT * FROM PlantProfile";

        try (Connection conn = SQLiteDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                PflanzenProfile_Model plant = new PflanzenProfile_Model(
                        rs.getInt("plant_id"),
                        rs.getString("plant_name"),
                        rs.getString("botanical_plant_name"),
                        rs.getDate("purchase_date").toLocalDate(),
                        rs.getString("location"),
                        rs.getInt("watering_interval"),
                        rs.getInt("fertilizing_interval"),
                        rs.getDate("last_watered").toLocalDate(),
                        rs.getDate("last_fertilized").toLocalDate(),
                        rs.getString("image_path")
                );
                plantList.add(plant);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return plantList;
    }

    /**
     * Holt das Pflanzenprofil basierend auf der plant_id aus der Datenbank.
     *
     * @param plantId Die ID der Pflanze.
     * @return Das Pflanzenprofil oder null, wenn es nicht gefunden wurde.
     */
    public PflanzenProfile_Model getPlantProfileById(int plantId) {
        String sql = "SELECT * FROM PlantProfile WHERE plant_id = ?";
        PflanzenProfile_Model plantProfile = null;

        try (Connection conn = SQLiteDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, plantId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                plantProfile = new PflanzenProfile_Model(
                        rs.getInt("plant_id"),
                        rs.getString("plant_name"),
                        rs.getString("botanical_plant_name"),
                        rs.getDate("purchase_date").toLocalDate(),
                        rs.getString("location"),
                        rs.getInt("watering_interval"),
                        rs.getInt("fertilizing_interval"),
                        rs.getDate("last_watered").toLocalDate(),
                        rs.getDate("last_fertilized").toLocalDate(),
                        rs.getString("image_path")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return plantProfile;
    }

    /**
     * Fügt ein neues Pflanzenprofil in die Datenbank ein.
     * @param plant Das Pflanzenprofil, das eingefügt werden soll.
     */
    public synchronized void addPlantProfile(PflanzenProfile_Model plant) {
        String sql = "INSERT INTO PlantProfile (plant_name, botanical_plant_name, purchase_date, location, " +
                "watering_interval, fertilizing_interval, last_watered, last_fertilized, image_path) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = SQLiteDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, plant.getPlant_name());
            pstmt.setString(2, plant.getBotanical_plant_name());
            pstmt.setDate(3, Date.valueOf(plant.getPurchase_date()));
            pstmt.setString(4, plant.getLocation());
            pstmt.setInt(5, plant.getWatering_interval());
            pstmt.setInt(6, plant.getFertilizing_interval());
            pstmt.setDate(7, Date.valueOf(plant.getLast_watered()));
            pstmt.setDate(8, Date.valueOf(plant.getLast_fertilized()));
            pstmt.setString(9, plant.getImage_path());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Aktualisiert das Pflanzenprofil in der Datenbank.
     *
     * @param plant Das Pflanzenprofil, das aktualisiert werden soll.
     */
    public synchronized void updatePlantProfile(PflanzenProfile_Model plant) {
        String sql = "UPDATE PlantProfile SET plant_name = ?, botanical_plant_name = ?, purchase_date = ?, location = ?, " +
                "watering_interval = ?, fertilizing_interval = ?, last_watered = ?, last_fertilized = ?, image_path = ? " +
                "WHERE plant_id = ?";

        try (Connection conn = SQLiteDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, plant.getPlant_name());
            pstmt.setString(2, plant.getBotanical_plant_name());
            pstmt.setDate(3, Date.valueOf(plant.getPurchase_date()));
            pstmt.setString(4, plant.getLocation());
            pstmt.setInt(5, plant.getWatering_interval());
            pstmt.setInt(6, plant.getFertilizing_interval());
            pstmt.setDate(7, Date.valueOf(plant.getLast_watered()));
            pstmt.setDate(8, Date.valueOf(plant.getLast_fertilized()));
            pstmt.setString(9, plant.getImage_path());
            pstmt.setInt(10, plant.getPlant_id());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Löscht ein Pflanzenprofil aus der Datenbank.
     * @param plantId Die ID des Pflanzenprofils, das gelöscht werden soll.
     */
    public synchronized void deletePlantProfile(int plantId) {
        String sql = "DELETE FROM PlantProfile WHERE plant_id = ?";

        try (Connection conn = SQLiteDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, plantId);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Lädt alle distinct Pflanzennamen aus der PlantProfile-Tabelle.
     * @return Liste der distinct Pflanzennamen.
     */
    public List<String> getDistinctPlantNames() {
        String sql = "SELECT DISTINCT plant_name FROM PlantProfile ORDER BY plant_name ASC";
        List<String> plantNames = new ArrayList<>();

        try (Connection conn = SQLiteDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                plantNames.add(rs.getString("plant_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return plantNames;
    }

    /**
     * Lädt alle distinct Standorte aus der PlantProfile-Tabelle.
     * @return Liste der distinct Standorte.
     */
    public List<String> getDistinctLocations() {
        String sql = "SELECT DISTINCT location FROM PlantProfile ORDER BY location ASC";
        List<String> locations = new ArrayList<>();

        try (Connection conn = SQLiteDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                locations.add(rs.getString("location"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return locations;
    }
}
