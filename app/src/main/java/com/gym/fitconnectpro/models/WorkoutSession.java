package com.gym.fitconnectpro.models;

import java.util.ArrayList;
import java.util.List;

public class WorkoutSession {
    private int sessionId;
    private int memberId;
    private int planId;
    private String sessionDate;
    private int durationMinutes;
    private int caloriesBurned;
    private String notes;
    private List<WorkoutLog> exercises;

    public WorkoutSession() {
        this.exercises = new ArrayList<>();
    }

    public WorkoutSession(int sessionId, int memberId, String sessionDate, int durationMinutes, int caloriesBurned) {
        this.sessionId = sessionId;
        this.memberId = memberId;
        this.sessionDate = sessionDate;
        this.durationMinutes = durationMinutes;
        this.caloriesBurned = caloriesBurned;
        this.exercises = new ArrayList<>();
    }

    // Getters and Setters
    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    public int getPlanId() {
        return planId;
    }

    public void setPlanId(int planId) {
        this.planId = planId;
    }

    public String getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(String sessionDate) {
        this.sessionDate = sessionDate;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public int getCaloriesBurned() {
        return caloriesBurned;
    }

    public void setCaloriesBurned(int caloriesBurned) {
        this.caloriesBurned = caloriesBurned;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<WorkoutLog> getExercises() {
        return exercises;
    }

    public void setExercises(List<WorkoutLog> exercises) {
        this.exercises = exercises;
    }

    public void addExercise(WorkoutLog exercise) {
        this.exercises.add(exercise);
    }
}
