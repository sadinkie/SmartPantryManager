package com.example.smartpantrymanager.model;

/** One required ingredient line of a recipe, e.g. "200 g pasta". */
public class RecipeIngredient {

    private int id;
    private int recipeId;
    private String name;
    private double quantity;
    private String unit;

    // For building seed data (ids are assigned on insert).
    public RecipeIngredient(String name, double quantity, String unit) {
        this(0, 0, name, quantity, unit);
    }

    // For rows read from the database.
    public RecipeIngredient(int id, int recipeId, String name, double quantity, String unit) {
        this.id = id;
        this.recipeId = recipeId;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
    }

    public int getId() { return id; }
    public int getRecipeId() { return recipeId; }
    public String getName() { return name; }
    public double getQuantity() { return quantity; }
    public String getUnit() { return unit; }
}