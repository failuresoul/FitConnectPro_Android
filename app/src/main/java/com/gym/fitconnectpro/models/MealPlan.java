package com.gym.fitconnectpro.models;

import java.util.ArrayList;
import java.util.List;

public class MealPlan {
    private int id;
    private int trainerId;
    private int memberId;
    private String planDate;
    private String mealType; // Breakfast, Lunch, Dinner, Snack
    private String instructions;
    private String createdAt;
    private List<MealPlanFood> foods;

    public MealPlan() {
        this.foods = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getPlanDate() {
        return planDate;
    }

    public void setPlanDate(String planDate) {
        this.planDate = planDate;
    }

    public String getMealType() {
        return mealType;
    }

    public void setMealType(String mealType) {
        this.mealType = mealType;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public List<MealPlanFood> getFoods() {
        return foods;
    }

    public void setFoods(List<MealPlanFood> foods) {
        this.foods = foods;
    }

    public void addFood(MealPlanFood food) {
        this.foods.add(food);
    }
}
