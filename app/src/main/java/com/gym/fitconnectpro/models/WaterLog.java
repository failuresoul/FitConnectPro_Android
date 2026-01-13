package com.gym.fitconnectpro.models;

public class WaterLog {
    private int logId;
    private int memberId;
    private int amountMl;
    private String logTime;
    private String createdAt;

    public WaterLog() {
    }

    public WaterLog(int logId, int memberId, int amountMl, String logTime) {
        this.logId = logId;
        this.memberId = memberId;
        this.amountMl = amountMl;
        this.logTime = logTime;
    }

    // Getters and Setters
    public int getLogId() {
        return logId;
    }

    public void setLogId(int logId) {
        this.logId = logId;
    }

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    public int getAmountMl() {
        return amountMl;
    }

    public void setAmountMl(int amountMl) {
        this.amountMl = amountMl;
    }

    public String getLogTime() {
        return logTime;
    }

    public void setLogTime(String logTime) {
        this.logTime = logTime;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
