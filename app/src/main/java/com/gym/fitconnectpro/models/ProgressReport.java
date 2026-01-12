package com.gym.fitconnectpro.models;

public class ProgressReport {
    private int id;
    private int trainerId;
    private int memberId;
    private String reportStartDate;
    private String reportEndDate;
    private String generatedDate;
    
    // Metrics Snapshot
    private double workoutCompletionRate;
    private int mealsLoggedCount;
    private double waterComplianceRate;
    private double weightChange; // + or - kg
    
    private String trainerFeedback;
    private String status; // 'SENT', 'DRAFT'

    public ProgressReport() {}

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getTrainerId() { return trainerId; }
    public void setTrainerId(int trainerId) { this.trainerId = trainerId; }

    public int getMemberId() { return memberId; }
    public void setMemberId(int memberId) { this.memberId = memberId; }

    public String getReportStartDate() { return reportStartDate; }
    public void setReportStartDate(String reportStartDate) { this.reportStartDate = reportStartDate; }

    public String getReportEndDate() { return reportEndDate; }
    public void setReportEndDate(String reportEndDate) { this.reportEndDate = reportEndDate; }

    public String getGeneratedDate() { return generatedDate; }
    public void setGeneratedDate(String generatedDate) { this.generatedDate = generatedDate; }

    public double getWorkoutCompletionRate() { return workoutCompletionRate; }
    public void setWorkoutCompletionRate(double workoutCompletionRate) { this.workoutCompletionRate = workoutCompletionRate; }

    public int getMealsLoggedCount() { return mealsLoggedCount; }
    public void setMealsLoggedCount(int mealsLoggedCount) { this.mealsLoggedCount = mealsLoggedCount; }

    public double getWaterComplianceRate() { return waterComplianceRate; }
    public void setWaterComplianceRate(double waterComplianceRate) { this.waterComplianceRate = waterComplianceRate; }

    public double getWeightChange() { return weightChange; }
    public void setWeightChange(double weightChange) { this.weightChange = weightChange; }

    public String getTrainerFeedback() { return trainerFeedback; }
    public void setTrainerFeedback(String trainerFeedback) { this.trainerFeedback = trainerFeedback; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
