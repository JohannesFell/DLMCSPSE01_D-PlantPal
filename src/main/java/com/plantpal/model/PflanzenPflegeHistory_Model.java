package com.plantpal.model;

import java.time.LocalDate;

public class PflanzenPflegeHistory_Model {
    private int history_id;
    private int task_id;
    private int plant_id;
    private String task_type;
    private LocalDate completion_date;
    private String note;
    private String plant_name;
    private String location;
    private int watering_interval;
    private int fertilizing_interval;
    private LocalDate last_watered;
    private LocalDate last_fertilized;

    public PflanzenPflegeHistory_Model(int history_id, int task_id, int plant_id, String task_type,
                                       LocalDate completion_date, String note, String plant_name) {
        this.history_id = history_id;
        this.task_id = task_id;
        this.plant_id = plant_id;
        this.task_type = task_type;
        this.completion_date = completion_date;
        this.note = note;
        this.plant_name = plant_name;
    }

    public int getHistory_id() {
        return history_id;
    }

    public void setHistory_id(int history_id) {
        this.history_id = history_id;
    }

    public int getTask_id() {
        return task_id;
    }

    public void setTask_id(int task_id) {
        this.task_id = task_id;
    }

    public int getPlant_id() {
        return plant_id;
    }

    public void setPlant_id(int plant_id) {
        this.plant_id = plant_id;
    }

    public String getTask_type() {
        return task_type;
    }

    public void setTask_type(String task_type) {
        this.task_type = task_type;
    }

    public LocalDate getCompletion_date() {
        return completion_date;
    }

    public void setCompletion_date(LocalDate completion_date) {
        this.completion_date = completion_date;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getPlant_name() {
        return plant_name;
    }

    public void setPlant_name(String plant_name) {
        this.plant_name = plant_name;
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
}
