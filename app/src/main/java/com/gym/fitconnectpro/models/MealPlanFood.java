package com.gym.fitconnectpro.models;

public class MealPlanFood {
    private int id;
    private int mealPlanId;
    private int foodId;
    private double quantity; // e.g. 1.5 servings or 150g depending on logic, let's assume multiplier of serving unit for simplicity or raw amount
    private Food food; // Helper object for UI

    public MealPlanFood() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMealPlanId() {
        return mealPlanId;
    }

    public void setMealPlanId(int mealPlanId) {
        this.mealPlanId = mealPlanId;
    }

    public int getFoodId() {
        return foodId;
    }

    public void setFoodId(int foodId) {
        this.foodId = foodId;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public Food getFood() {
        return food;
    }

    public void setFood(Food food) {
        this.food = food;
    }
}
