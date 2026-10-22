package com.example.smartpantrymanager.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.smartpantrymanager.model.Ingredient;
import com.example.smartpantrymanager.model.Recipe;
import com.example.smartpantrymanager.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class RecipeMatcherTest {

    // ---------- helpers ----------
    private static RecipeIngredient need(String name, double qty, String unit) {
        return new RecipeIngredient(name, qty, unit);
    }

    private static Ingredient have(String name, double qty, String unit) {
        return new Ingredient(name, qty, unit, 0);
    }

    private static Recipe recipe(String name, RecipeIngredient... ingredients) {
        Recipe r = new Recipe(name, "", 10, "");
        r.setIngredients(Arrays.asList(ingredients));
        return r;
    }

    /** A recipe needing exactly 5 ingredients. */
    private static Recipe stirFry() {
        return recipe("Stir-Fry",
                need("chicken", 300, "g"),
                need("onion", 1, "piece"),
                need("carrot", 2, "piece"),
                need("soy sauce", 2, "tbsp"),
                need("oil", 1, "tbsp"));
    }

    private static List<Recipe> only(Recipe r) {
        return Collections.singletonList(r);
    }

    // ---------- tests ----------

    @Test
    public void fourOfFiveIngredients_isExcluded() {
        List<Ingredient> pantry = Arrays.asList(
                have("chicken", 500, "g"), have("onion", 2, "piece"),
                have("carrot", 3, "piece"), have("soy sauce", 5, "tbsp"));
        assertTrue(RecipeMatcher.findStrictMatches(only(stirFry()), pantry).isEmpty());
    }

    @Test
    public void allFiveIngredients_isIncluded() {
        List<Ingredient> pantry = Arrays.asList(
                have("chicken", 500, "g"), have("onion", 2, "piece"),
                have("carrot", 3, "piece"), have("soy sauce", 5, "tbsp"),
                have("oil", 4, "tbsp"));
        assertEquals(1, RecipeMatcher.findStrictMatches(only(stirFry()), pantry).size());
    }

    @Test
    public void insufficientQuantity_isExcluded() {
        Recipe r = recipe("Pancakes", need("flour", 150, "g"));
        assertTrue(RecipeMatcher.findStrictMatches(only(r),
                Collections.singletonList(have("flour", 100, "g"))).isEmpty());
    }

    @Test
    public void exactQuantity_isIncluded() {
        Recipe r = recipe("Pancakes", need("flour", 150, "g"));
        assertEquals(1, RecipeMatcher.findStrictMatches(only(r),
                Collections.singletonList(have("flour", 150, "g"))).size());
    }

    @Test
    public void gramsVersusKilograms_areConverted() {
        Recipe r = recipe("Pasta Dish", need("pasta", 200, "g"));
        assertEquals(1, RecipeMatcher.findStrictMatches(only(r),
                Collections.singletonList(have("pasta", 0.5, "kg"))).size());
    }

    @Test
    public void tablespoonsVersusMillilitres_areConverted() {
        Recipe r = recipe("Dressing", need("oil", 2, "tbsp")); // 30 ml
        assertEquals(1, RecipeMatcher.findStrictMatches(only(r),
                Collections.singletonList(have("oil", 100, "ml"))).size());
    }

    @Test
    public void pluralAndCaseDifferences_stillMatch() {
        Recipe r = recipe("Tomato Soup", need("tomato", 4, "piece"));
        assertEquals(1, RecipeMatcher.findStrictMatches(only(r),
                Collections.singletonList(have("  Tomatoes ", 6, "piece"))).size());
    }

    @Test
    public void incompatibleUnits_areNotMatched() {
        Recipe r = recipe("Chicken Rice", need("chicken", 300, "g"));
        assertTrue(RecipeMatcher.findStrictMatches(only(r),
                Collections.singletonList(have("chicken", 2, "piece"))).isEmpty());
    }

    @Test
    public void duplicatePantryEntries_areCombined() {
        Recipe r = recipe("Bread", need("flour", 300, "g"));
        List<Ingredient> pantry = Arrays.asList(have("flour", 200, "g"), have("Flour", 150, "g"));
        assertEquals(1, RecipeMatcher.findStrictMatches(only(r), pantry).size());
    }

    @Test
    public void emptyPantry_returnsNoRecipes() {
        assertTrue(RecipeMatcher.findStrictMatches(only(stirFry()),
                new ArrayList<Ingredient>()).isEmpty());
    }

    @Test
    public void recipeWithNoIngredients_isNeverSuggested() {
        assertTrue(RecipeMatcher.findStrictMatches(only(recipe("Empty")),
                Collections.singletonList(have("egg", 3, "piece"))).isEmpty());
    }

    @Test
    public void almostThere_listsRecipeMissingExactlyOne() {
        List<Ingredient> pantry = Arrays.asList(
                have("chicken", 500, "g"), have("onion", 2, "piece"),
                have("carrot", 3, "piece"), have("soy sauce", 5, "tbsp"));
        assertEquals(1, RecipeMatcher.findAlmostThere(only(stirFry()), pantry).size());
        assertFalse(RecipeMatcher.findStrictMatches(only(stirFry()), pantry).size() > 0);
    }
}