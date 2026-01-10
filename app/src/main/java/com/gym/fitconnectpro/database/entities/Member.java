package com.gym.fitconnectpro.database.entities;

public class Member {
    private int memberId;
    private String fullName;
    private String email;
    private String phone;
    private String dateOfBirth;
    private String gender;
    private double height;
    private double weight;
    private String membershipType;
    private String membershipStartDate;
    private String membershipEndDate;
    private int duration;
    private double membershipFee; // Add this field
    private String medicalNotes;
    private String emergencyContact;
    private String username;
    private String password;
    private String status; // Active, Suspended, Expired
    private String assignedTrainer;
    private String createdAt;

    // Constructor
    public Member() {
    }

    public Member(int memberId, String fullName, String email, String phone, String dateOfBirth,
                  String gender, double height, double weight, String membershipType,
                  String membershipStartDate, String membershipEndDate, int duration,
                  double membershipFee, String medicalNotes, String emergencyContact, String username, String password,
                  String status, String assignedTrainer, String createdAt) {
        this.memberId = memberId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.height = height;
        this.weight = weight;
        this.membershipType = membershipType;
        this.membershipStartDate = membershipStartDate;
        this.membershipEndDate = membershipEndDate;
        this.duration = duration;
        this.membershipFee = membershipFee;
        this.medicalNotes = medicalNotes;
        this.emergencyContact = emergencyContact;
        this.username = username;
        this.password = password;
        this.status = status;
        this.assignedTrainer = assignedTrainer;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getMemberId() { return memberId; }
    public void setMemberId(int memberId) { this.memberId = memberId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public String getMembershipType() { return membershipType; }
    public void setMembershipType(String membershipType) { this.membershipType = membershipType; }

    public String getMembershipStartDate() { return membershipStartDate; }
    public void setMembershipStartDate(String membershipStartDate) { this.membershipStartDate = membershipStartDate; }

    public String getMembershipEndDate() { return membershipEndDate; }
    public void setMembershipEndDate(String membershipEndDate) { this.membershipEndDate = membershipEndDate; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public double getMembershipFee() { return membershipFee; }
    public void setMembershipFee(double membershipFee) { this.membershipFee = membershipFee; }

    public String getMedicalNotes() { return medicalNotes; }
    public void setMedicalNotes(String medicalNotes) { this.medicalNotes = medicalNotes; }

    public String getEmergencyContact() { return emergencyContact; }
    public void setEmergencyContact(String emergencyContact) { this.emergencyContact = emergencyContact; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAssignedTrainer() { return assignedTrainer; }
    public void setAssignedTrainer(String assignedTrainer) { this.assignedTrainer = assignedTrainer; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
