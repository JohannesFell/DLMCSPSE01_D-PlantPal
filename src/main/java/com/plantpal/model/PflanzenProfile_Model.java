package com.plantpal.model;

import java.time.LocalDate;

public class PflanzenProfile_Model {

    private int plant_id;
    private String plant_name;
    private String botanical_plant_name;
    private LocalDate purchase_date;
    private String location;
    private int watering_interval;
    private int fertilizing_interval;
    private LocalDate last_watered;
    private LocalDate last_fertilized;
    private String image_path;

    public PflanzenProfile_Model(int plant_id, String plant_name, String botanical_plant_name, LocalDate purchase_date,
                                 String location, int watering_interval, int fertilizing_interval,
                                 LocalDate last_watered, LocalDate last_fertilized, String image_path) {
        this.plant_id = plant_id;
        this.plant_name = plant_name;
        this.botanical_plant_name = botanical_plant_name;
        this.purchase_date = purchase_date;
        this.location = location;
        this.watering_interval = watering_interval;
        this.fertilizing_interval = fertilizing_interval;
        this.last_watered = last_watered;
        this.last_fertilized = last_fertilized;
        this.image_path = image_path;
    }

    // Getter & Setter Methoden

    public int getPlant_id() {
        return plant_id;
    }

    public void setPlant_id(int plant_id) {
        this.plant_id = plant_id;
    }

    public String getPlant_name() {
        return plant_name;
    }

    public void setPlant_name(String plant_name) {
        this.plant_name = plant_name;
    }

    public String getBotanical_plant_name() {
        return botanical_plant_name;
    }

    public void setBotanical_plant_name(String botanical_plant_name) {
        this.botanical_plant_name = botanical_plant_name;
    }

    public LocalDate getPurchase_date() {
        return purchase_date;
    }

    public void setPurchase_date(LocalDate purchase_date) {
        this.purchase_date = purchase_date;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getWatering_interval() {
        return watering_interval;
    }

    public void setWatering_interval(int watering_interval) {
        this.watering_interval = watering_interval;
    }

    public int getFertilizing_interval() {
        return fertilizing_interval;
    }

    public void setFertilizing_interval(int fertilizing_interval) {
        this.fertilizing_interval = fertilizing_interval;
    }

    public LocalDate getLast_watered() {
        return last_watered;
    }

    public void setLast_watered(LocalDate last_watered) {
        this.last_watered = last_watered;
    }

    public LocalDate getLast_fertilized() {
        return last_fertilized;
    }

    public void setLast_fertilized(LocalDate last_fertilized) {
        this.last_fertilized = last_fertilized;
    }

    public String getImage_path() {
        return image_path;
    }

    public void setImage_path(String image_path) {
        this.image_path = image_path;
    }
}
