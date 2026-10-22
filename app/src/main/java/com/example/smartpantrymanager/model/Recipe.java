package com.example.smartpantrymanager.model;

import java.util.ArrayList;
import java.util.List;

/** A recipe plus the list of ingredients it requires. */
public class Recipe {

    private int id;
    private String name;
    private String description;
    private int prepTimeMinutes;
    private String steps; // one step per line, separated by "\n"
    private List<RecipeIngredient> ingredients = new ArrayList<>();

    public Recipe(String name, String description, int prepTimeMinutes, String steps) {
        this(0, name, description, prepTimeMinutes, steps);
    }

    public Recipe(int id, String name, String description, int prepTimeMinutes, String steps) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.prepTimeMinutes = prepTimeMinutes;
        this.steps = steps;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getPrepTimeMinutes() { return prepTimeMinutes; }
    public String getSteps() { return steps; }

    public List<RecipeIngredient> getIngredients() { return ingredients; }
    public void setIngredients(List<RecipeIngredient> ingredients) { this.ingredients = ingredients; }
}