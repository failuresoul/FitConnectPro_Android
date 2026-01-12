package com.gym.fitconnectpro.models;

public class WeightLog {
    private int id;
    private int memberId;
    private double weight;
    private String logDate;
    private String notes;

    public WeightLog() {}

    public WeightLog(int memberId, double weight, String logDate) {
        this.memberId = memberId;
        this.weight = weight;
        this.logDate = logDate;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getMemberId() { return memberId; }
    public void setMemberId(int memberId) { this.memberId = memberId; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public String getLogDate() { return logDate; }
    public void setLogDate(String logDate) { this.logDate = logDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
