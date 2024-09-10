package com.plantpal.app;

import java.time.LocalDate;

public class PflanzenPflege_Model {

    private int task_id;
    private String task_type;
    private LocalDate due_date;
    private String plant_name;
    private String location;
    private String actions;
    private  int plant_id;
    private boolean completed;
    private String note;
    private int watering_interval;
    private int fertilizing_interval;

    public PflanzenPflege_Model(int task_id, int plant_id, String task_type, LocalDate due_date, boolean completed,
                                String note, String plant_name, String location, int watering_interval,
                                int fertilizing_interval, String actions) {
        this.task_id = task_id;
        this.plant_id = plant_id;
        this.task_type = task_type;
        this.due_date = due_date;
        this.completed = completed;
        this.note = note;
        this.plant_name = plant_name;
        this.location = location;
        this.watering_interval = watering_interval;
        this.fertilizing_interval = fertilizing_interval;
        this.actions = actions;
    }

    public int getPlant_id() {
        return plant_id;
    }

    public void setPlant_id(int plant_id) {
        this.plant_id = plant_id;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
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

    public int getTask_id() {
        return task_id;
    }

    public void setTask_id(int task_id) {
        this.task_id = task_id;
    }

    public String getTask_type() {
        return task_type;
    }

    public void setTask_type(String task_type) {
        this.task_type = task_type;
    }

    public LocalDate getDue_date() {
        return due_date;
    }

    public void setDue_date(LocalDate due_date) {
        this.due_date = due_date;
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

    public String getActions() {
        return actions;
    }

    public void setActions(String actions) {
        this.actions = actions;
    }
}
