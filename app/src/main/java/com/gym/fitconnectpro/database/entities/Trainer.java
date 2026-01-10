package com.gym.fitconnectpro.database.entities;

public class Trainer {
    private int trainerId;
    private int userId;
    private String fullName;
    private String email;
    private String phone;
    private String specialization;
    private int experienceYears;
    private String certification;
    private double salary;
    private String username;
    private String password;
    private String status;
    private String createdAt;

    public Trainer() {
    }

    public int getTrainerId() { return trainerId; }
    public void setTrainerId(int trainerId) { this.trainerId = trainerId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public int getExperienceYears() { return experienceYears; }
    public void setExperienceYears(int experienceYears) { this.experienceYears = experienceYears; }

    public String getCertification() { return certification; }
    public void setCertification(String certification) { this.certification = certification; }

    public double getSalary() { return salary; }
    public void setSalary(double salary) { this.salary = salary; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    // Compatibility methods for user's Activity code
    public void setSpecializations(String s) { this.specialization = s; }
    public void setCertifications(String s) { this.certification = s; }
    public void setMonthlySalary(double salary) { this.salary = salary; }
    public void setPasswordHash(String password) { this.password = password; }
    public void setAccountStatus(String status) { this.status = status; }
    
    // Fields present in Activity but not in Database (Ignored for now)
    public void setEducation(String education) { /* Not persisted */ }
    public void setMaxClients(int maxClients) { /* Not persisted */ }
}
