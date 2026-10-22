package com.example.smartpantrymanager.logic;

import com.example.smartpantrymanager.model.Ingredient;
import com.example.smartpantrymanager.model.Recipe;
import com.example.smartpantrymanager.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * THE STRICT-MATCHING RULE.
 * A recipe is "suggested" only if EVERY ingredient it needs is in the pantry
 * in at least the required quantity. One missing or insufficient ingredient
 * excludes the whole recipe.
 */
public final class RecipeMatcher {

    private static final double EPSILON = 1e-9; // guards against double rounding

    private RecipeMatcher() { }

    /** Recipes the user can cook right now (zero missing ingredients). */
    public static List<Recipe> findStrictMatches(List<Recipe> recipes, List<Ingredient> pantry) {
        Map<String, Double> totals = buildPantryTotals(pantry);
        List<Recipe> matches = new ArrayList<>();
        for (Recipe recipe : recipes) {
            if (canMake(recipe, totals)) {
                matches.add(recipe);
            }
        }
        return matches;
    }

    /** OPTIONAL BONUS: recipes missing exactly ONE ingredient (shown separately). */
    public static List<Recipe> findAlmostThere(List<Recipe> recipes, List<Ingredient> pantry) {
        Map<String, Double> totals = buildPantryTotals(pantry);
        List<Recipe> almost = new ArrayList<>();
        for (Recipe recipe : recipes) {
            if (!recipe.getIngredients().isEmpty()
                    && getMissingIngredients(recipe, totals).size() == 1) {
                almost.add(recipe);
            }
        }
        return almost;
    }

    /** True only when nothing is missing. A recipe with no ingredients never qualifies. */
    public static boolean canMake(Recipe recipe, Map<String, Double> pantryTotals) {
        return !recipe.getIngredients().isEmpty()
                && getMissingIngredients(recipe, pantryTotals).isEmpty();
    }

    /** The recipe ingredients the pantry cannot fully cover. */
    public static List<RecipeIngredient> getMissingIngredients(
            Recipe recipe, Map<String, Double> pantryTotals) {
        List<RecipeIngredient> missing = new ArrayList<>();
        for (RecipeIngredient need : recipe.getIngredients()) {
            Double have = pantryTotals.get(keyFor(need.getName(), need.getUnit()));
            double required = UnitConverter.toBaseUnits(need.getQuantity(), need.getUnit());
            if (have == null || have + EPSILON < required) {
                missing.add(need);
            }
        }
        return missing;
    }

    public static List<RecipeIngredient> getMissingIngredients(
            Recipe recipe, List<Ingredient> pantry) {
        return getMissingIngredients(recipe, buildPantryTotals(pantry));
    }

    /**
     * Adds up the pantry per (normalized name + unit category), in base units.
     * Two "flour" entries (200 g + 150 g) become one total of 350 g.
     */
    public static Map<String, Double> buildPantryTotals(List<Ingredient> pantry) {
        Map<String, Double> totals = new HashMap<>();
        for (Ingredient item : pantry) {
            String key = keyFor(item.getName(), item.getUnit());
            double base = UnitConverter.toBaseUnits(item.getQuantity(), item.getUnit());
            Double existing = totals.get(key);
            totals.put(key, (existing == null ? 0.0 : existing) + base);
        }
        return totals;
    }

    private static String keyFor(String name, String unit) {
        return NameNormalizer.normalize(name) + "|" + UnitConverter.getCategory(unit).name();
    }
}