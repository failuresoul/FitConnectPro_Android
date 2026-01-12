package com.gym.fitconnectpro.models;

public class DailyLog {
    private int id;
    private int memberId;
    private String logDate;
    private int waterIntakeMl;
    private int caloriesConsumed;
    private int workoutDurationMinutes;
    private boolean workoutCompleted;
    private int sleepHours;
    private String mood; // 'Good', 'Tired', etc.

    public DailyLog() {}

    // Getters and Setters definition
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getMemberId() { return memberId; }
    public void setMemberId(int memberId) { this.memberId = memberId; }

    public String getLogDate() { return logDate; }
    public void setLogDate(String logDate) { this.logDate = logDate; }

    public int getWaterIntakeMl() { return waterIntakeMl; }
    public void setWaterIntakeMl(int waterIntakeMl) { this.waterIntakeMl = waterIntakeMl; }

    public int getCaloriesConsumed() { return caloriesConsumed; }
    public void setCaloriesConsumed(int caloriesConsumed) { this.caloriesConsumed = caloriesConsumed; }

    public int getWorkoutDurationMinutes() { return workoutDurationMinutes; }
    public void setWorkoutDurationMinutes(int workoutDurationMinutes) { this.workoutDurationMinutes = workoutDurationMinutes; }

    public boolean isWorkoutCompleted() { return workoutCompleted; }
    public void setWorkoutCompleted(boolean workoutCompleted) { this.workoutCompleted = workoutCompleted; }

    public int getSleepHours() { return sleepHours; }
    public void setSleepHours(int sleepHours) { this.sleepHours = sleepHours; }

    public String getMood() { return mood; }
    public void setMood(String mood) { this.mood = mood; }
}
