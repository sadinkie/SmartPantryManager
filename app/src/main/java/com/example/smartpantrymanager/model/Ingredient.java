package com.example.smartpantrymanager.model;

/**
 * One item in the user's pantry.
 * expiryDate is epoch milliseconds; 0 means "no expiry date set".
 */
public class Ingredient {

    private int id;
    private String name;
    private double quantity;
    private String unit;
    private long expiryDate;

    // Used when creating a NEW item (the database assigns the id).
    public Ingredient(String name, double quantity, String unit, long expiryDate) {
        this(0, name, quantity, unit, expiryDate);
    }

    // Used when reading an EXISTING item back from the database.
    public Ingredient(int id, String name, double quantity, String unit, long expiryDate) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.expiryDate = expiryDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public long getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(long expiryDate) {
        this.expiryDate = expiryDate;
    }

    public boolean hasExpiryDate() {
        return expiryDate > 0;
    }
}