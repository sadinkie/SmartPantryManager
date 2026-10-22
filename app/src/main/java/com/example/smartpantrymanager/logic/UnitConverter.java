package com.example.smartpantrymanager.logic;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Converts quantities to a common base unit so they can be compared.
 * MASS   -> grams
 * VOLUME -> millilitres
 * COUNT  -> pieces
 * Mass and volume cannot be converted into each other without knowing the
 * ingredient's density, so they are kept as separate categories.
 */
public final class UnitConverter {

    public enum Category {MASS, VOLUME, COUNT}

    /**
     * The units offered in the app's spinner. Keep in sync with the seed data.
     */
    public static final String[] SUPPORTED_UNITS =
            {"g", "kg", "ml", "l", "tsp", "tbsp", "cup", "piece"};

    private static final Map<String, String> ALIASES = new HashMap<>();
    private static final Map<String, Double> FACTOR_TO_BASE = new HashMap<>();
    private static final Map<String, Category> CATEGORY = new HashMap<>();

    static {
        define("g", Category.MASS, 1.0, "gram", "grams", "gr");
        define("kg", Category.MASS, 1000.0, "kilogram", "kilograms", "kgs");
        define("ml", Category.VOLUME, 1.0, "millilitre", "millilitres",
                "milliliter", "milliliters", "mls");
        define("l", Category.VOLUME, 1000.0, "litre", "litres", "liter", "liters");
        define("tsp", Category.VOLUME, 5.0, "teaspoon", "teaspoons");
        define("tbsp", Category.VOLUME, 15.0, "tablespoon", "tablespoons", "tbs");
        define("cup", Category.VOLUME, 240.0, "cups");
        define("piece", Category.COUNT, 1.0, "pieces", "pc", "pcs", "unit",
                "units", "whole", "each");
    }

    private UnitConverter() {
    }

    private static void define(String canonical, Category category,
                               double factor, String... aliases) {
        FACTOR_TO_BASE.put(canonical, factor);
        CATEGORY.put(canonical, category);
        ALIASES.put(canonical, canonical);
        for (String alias : aliases) {
            ALIASES.put(alias, canonical);
        }
    }

    /**
     * "Grams", " KG " -> "g", "kg". Unknown units are treated as "piece".
     */
    public static String canonicalUnit(String unit) {
        if (unit == null) return "piece";
        String key = unit.toLowerCase(Locale.ENGLISH).trim().replace(".", "");
        String canonical = ALIASES.get(key);
        return canonical != null ? canonical : "piece";
    }

    public static Category getCategory(String unit) {
        return CATEGORY.get(canonicalUnit(unit));
    }

    /**
     * Quantity expressed in the base unit of its category (g, ml or piece).
     */
    public static double toBaseUnits(double quantity, String unit) {
        return quantity * FACTOR_TO_BASE.get(canonicalUnit(unit));
    }
}