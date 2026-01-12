package com.gym.fitconnectpro.models;

public class TrainerDailyGoal {
    private int trainerGoalId;
    private int trainerId;
    private int memberId;
    private String goalDate;
    private int workoutDuration;
    private int calorieTarget;
    private int waterIntakeMl;
    private int calorieLimit;
    private int proteinTarget;
    private int carbsTarget;
    private int fatsTarget;
    private String specialInstructions;
    private String createdDate;

    public TrainerDailyGoal() {
    }

    // Getters and Setters
    public int getTrainerGoalId() {
        return trainerGoalId;
    }

    public void setTrainerGoalId(int trainerGoalId) {
        this.trainerGoalId = trainerGoalId;
    }

    public int getTrainerId() {
        return trainerId;
    }

    public void setTrainerId(int trainerId) {
        this.trainerId = trainerId;
    }

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    public String getGoalDate() {
        return goalDate;
    }

    public void setGoalDate(String goalDate) {
        this.goalDate = goalDate;
    }

    public int getWorkoutDuration() {
        return workoutDuration;
    }

    public void setWorkoutDuration(int workoutDuration) {
        this.workoutDuration = workoutDuration;
    }

    public int getCalorieTarget() {
        return calorieTarget;
    }

    public void setCalorieTarget(int calorieTarget) {
        this.calorieTarget = calorieTarget;
    }

    public int getWaterIntakeMl() {
        return waterIntakeMl;
    }

    public void setWaterIntakeMl(int waterIntakeMl) {
        this.waterIntakeMl = waterIntakeMl;
    }

    public int getCalorieLimit() {
        return calorieLimit;
    }

    public void setCalorieLimit(int calorieLimit) {
        this.calorieLimit = calorieLimit;
    }

    public int getProteinTarget() {
        return proteinTarget;
    }

    public void setProteinTarget(int proteinTarget) {
        this.proteinTarget = proteinTarget;
    }

    public int getCarbsTarget() {
        return carbsTarget;
    }

    public void setCarbsTarget(int carbsTarget) {
        this.carbsTarget = carbsTarget;
    }

    public int getFatsTarget() {
        return fatsTarget;
    }

    public void setFatsTarget(int fatsTarget) {
        this.fatsTarget = fatsTarget;
    }

    public String getSpecialInstructions() {
        return specialInstructions;
    }

    public void setSpecialInstructions(String specialInstructions) {
        this.specialInstructions = specialInstructions;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }
}
