package com.gym.fitconnectpro.database.entities;

public class Salary {
    private int salaryId;
    private int trainerId;
    private String trainerName; // For display convenience (joined query)
    private int month;
    private int year;
    private double baseSalary;
    private double bonus;
    private double deductions;
    private double netSalary;
    private String status; // PENDING, PAID
    private String paymentDate;
    private int processedByAdminId;

    public Salary() {
    }

    public Salary(int trainerId, int month, int year, double baseSalary, double bonus, double deductions, String status, int processedByAdminId) {
        this.trainerId = trainerId;
        this.month = month;
        this.year = year;
        this.baseSalary = baseSalary;
        this.bonus = bonus;
        this.deductions = deductions;
        this.status = status;
        this.processedByAdminId = processedByAdminId;
        calculateNetSalary();
    }

    public void calculateNetSalary() {
        this.netSalary = this.baseSalary + this.bonus - this.deductions;
    }

    public int getSalaryId() {
        return salaryId;
    }

    public void setSalaryId(int salaryId) {
        this.salaryId = salaryId;
    }

    public int getTrainerId() {
        return trainerId;
    }

    public void setTrainerId(int trainerId) {
        this.trainerId = trainerId;
    }

    public String getTrainerName() {
        return trainerName;
    }

    public void setTrainerName(String trainerName) {
        this.trainerName = trainerName;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public double getBaseSalary() {
        return baseSalary;
    }

    public void setBaseSalary(double baseSalary) {
        this.baseSalary = baseSalary;
        calculateNetSalary();
    }

    public double getBonus() {
        return bonus;
    }

    public void setBonus(double bonus) {
        this.bonus = bonus;
        calculateNetSalary();
    }

    public double getDeductions() {
        return deductions;
    }

    public void setDeductions(double deductions) {
        this.deductions = deductions;
        calculateNetSalary();
    }

    public double getNetSalary() {
        return netSalary;
    }

    public void setNetSalary(double netSalary) {
        this.netSalary = netSalary;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(String paymentDate) {
        this.paymentDate = paymentDate;
    }

    public int getProcessedByAdminId() {
        return processedByAdminId;
    }

    public void setProcessedByAdminId(int processedByAdminId) {
        this.processedByAdminId = processedByAdminId;
    }
}
