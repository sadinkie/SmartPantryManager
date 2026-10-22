package com.example.smartpantrymanager.database;

import android.util.Log;

import com.example.smartpantrymanager.model.Recipe;
import com.example.smartpantrymanager.model.RecipeIngredient;

import java.util.Arrays;
import java.util.List;

/**
 * Pre-loads the recipe collection the first time the app runs.
 * Safe to call on every launch: it does nothing if recipes already exist.
 * <p>
 * Units used: g, kg, ml, l, tsp, tbsp, cup, piece
 */
public class RecipeSeeder {

    private static final String TAG = "RecipeSeeder";

    public static void seedIfEmpty(DatabaseHelper db) {
        if (db.getRecipeCount() > 0) {
            return; // already seeded
        }
        List<Recipe> recipes = buildRecipes();
        for (Recipe recipe : recipes) {
            db.insertRecipe(recipe);
        }
        Log.d(TAG, "Seeded " + recipes.size() + " recipes");
    }

    // ---- small helpers to keep the data below readable ----

    private static RecipeIngredient ing(String name, double qty, String unit) {
        return new RecipeIngredient(name, qty, unit);
    }

    private static Recipe recipe(String name, String description, int minutes,
                                 String steps, RecipeIngredient... ingredients) {
        Recipe r = new Recipe(name, description, minutes, steps);
        r.setIngredients(Arrays.asList(ingredients));
        return r;
    }

    private static List<Recipe> buildRecipes() {
        return Arrays.asList(

                recipe("Scrambled Eggs on Toast",
                        "A fast, filling breakfast.", 10,
                        "Toast the bread.\nMelt the butter in a pan on low heat.\n"
                                + "Whisk the eggs and stir gently in the pan until just set.\nServe on the toast.",
                        ing("egg", 3, "piece"), ing("bread", 2, "piece"), ing("butter", 10, "g")),

                recipe("Tomato Pasta",
                        "Simple pasta with a fresh tomato sauce.", 25,
                        "Boil the pasta until tender and drain.\nFry chopped onion and garlic in oil until soft.\n"
                                + "Add chopped tomatoes and simmer for 10 minutes.\nToss the pasta through the sauce.",
                        ing("pasta", 200, "g"), ing("tomato", 4, "piece"), ing("onion", 1, "piece"),
                        ing("garlic", 2, "piece"), ing("oil", 2, "tbsp")),

                recipe("Cheese Omelette",
                        "Fluffy eggs folded around melted cheese.", 10,
                        "Whisk the eggs with a pinch of salt.\nMelt the butter in a pan and pour in the eggs.\n"
                                + "Sprinkle over the grated cheese.\nFold in half and serve.",
                        ing("egg", 3, "piece"), ing("cheese", 50, "g"), ing("butter", 10, "g")),

                recipe("Egg Fried Rice",
                        "Turns leftover rice into a full meal.", 15,
                        "Heat the oil in a wide pan.\nFry the chopped onion for 2 minutes.\n"
                                + "Push aside, scramble the eggs, then mix everything together.\n"
                                + "Add the rice and soy sauce and stir-fry for 5 minutes.",
                        ing("rice", 200, "g"), ing("egg", 2, "piece"), ing("onion", 1, "piece"),
                        ing("soy sauce", 2, "tbsp"), ing("oil", 1, "tbsp")),

                recipe("Classic Pancakes",
                        "Light pancakes from basic staples.", 20,
                        "Whisk the flour, sugar, egg and milk into a smooth batter.\n"
                                + "Melt a little butter in a pan.\nPour in batter and cook until bubbles form, then flip.\n"
                                + "Repeat with the remaining batter.",
                        ing("flour", 150, "g"), ing("milk", 250, "ml"), ing("egg", 1, "piece"),
                        ing("butter", 20, "g"), ing("sugar", 1, "tbsp")),

                recipe("Banana Oat Porridge",
                        "Warm, creamy porridge sweetened with banana.", 10,
                        "Combine the oats and milk in a pot.\nCook on medium heat, stirring, for 5 minutes.\n"
                                + "Slice the banana on top and drizzle with honey.",
                        ing("oats", 80, "g"), ing("milk", 300, "ml"), ing("banana", 1, "piece"),
                        ing("honey", 1, "tbsp")),

                recipe("Garlic Butter Potatoes",
                        "Crispy roast potatoes with garlic butter.", 45,
                        "Heat the oven to 200°C.\nCut the potatoes into chunks and parboil for 8 minutes.\n"
                                + "Toss with melted butter, crushed garlic and salt.\nRoast for 30 minutes until golden.",
                        ing("potato", 500, "g"), ing("butter", 30, "g"), ing("garlic", 3, "piece"),
                        ing("salt", 1, "tsp")),

                recipe("Macaroni Cheese",
                        "Creamy comfort food.", 30,
                        "Boil the pasta and drain.\nMelt the butter, stir in the flour, then slowly whisk in the milk.\n"
                                + "Add the grated cheese and stir until smooth.\nCombine with the pasta.",
                        ing("pasta", 250, "g"), ing("cheese", 150, "g"), ing("milk", 200, "ml"),
                        ing("butter", 25, "g"), ing("flour", 2, "tbsp")),

                recipe("Chicken Rice Bowl",
                        "Seasoned chicken served over fluffy rice.", 35,
                        "Cook the rice according to the packet.\nFry onion and garlic in oil until soft.\n"
                                + "Add diced chicken and salt; cook through, about 8 minutes.\nServe over the rice.",
                        ing("chicken", 300, "g"), ing("rice", 200, "g"), ing("onion", 1, "piece"),
                        ing("garlic", 2, "piece"), ing("oil", 1, "tbsp"), ing("salt", 1, "tsp")),

                recipe("Hearty Vegetable Soup",
                        "A warming soup from root vegetables.", 35,
                        "Peel and chop the potatoes, carrots and onion.\nSimmer in water with the stock cube for 25 minutes.\n"
                                + "Mash lightly for a thicker texture.",
                        ing("potato", 2, "piece"), ing("carrot", 2, "piece"), ing("onion", 1, "piece"),
                        ing("stock cube", 1, "piece")),

                recipe("Egg Fried Noodles",
                        "Quick noodles with a soy glaze.", 15,
                        "Cook the noodles and drain.\nHeat oil and scramble the eggs.\n"
                                + "Add noodles, soy sauce and sliced spring onion.\nToss for 2 minutes.",
                        ing("noodles", 200, "g"), ing("egg", 2, "piece"), ing("soy sauce", 2, "tbsp"),
                        ing("oil", 1, "tbsp"), ing("spring onion", 2, "piece")),

                recipe("French Toast",
                        "Eggy bread, golden and sweet.", 15,
                        "Whisk the eggs, milk and sugar together.\nSoak each slice of bread briefly.\n"
                                + "Fry in butter until golden on both sides.",
                        ing("bread", 4, "piece"), ing("egg", 2, "piece"), ing("milk", 100, "ml"),
                        ing("butter", 15, "g"), ing("sugar", 1, "tsp")),

                recipe("Tuna Pasta",
                        "A store-cupboard lunch in minutes.", 15,
                        "Boil the pasta and drain.\nFlake in the tuna and add the mayonnaise and chopped onion.\n"
                                + "Mix well and serve warm or cold.",
                        ing("pasta", 200, "g"), ing("tuna", 1, "piece"), ing("mayonnaise", 2, "tbsp"),
                        ing("onion", 0.5, "piece")),

                recipe("Cheese Toastie",
                        "Crisp outside, melted inside.", 8,
                        "Butter one side of each slice of bread.\nPlace cheese between the unbuttered sides.\n"
                                + "Toast in a pan until golden on both sides.",
                        ing("bread", 2, "piece"), ing("cheese", 60, "g"), ing("butter", 10, "g")),

                recipe("Beans on Toast",
                        "The classic quick comfort meal.", 8,
                        "Toast the bread and butter it.\nHeat the baked beans in a small pot.\nSpoon over the toast.",
                        ing("baked beans", 1, "piece"), ing("bread", 2, "piece"), ing("butter", 10, "g")),

                recipe("Fresh Tomato Soup",
                        "Smooth, rich soup from fresh tomatoes.", 35,
                        "Melt butter and soften the onion and garlic.\nAdd chopped tomatoes and the stock cube with a cup of water.\n"
                                + "Simmer for 20 minutes, then blend until smooth.",
                        ing("tomato", 6, "piece"), ing("onion", 1, "piece"), ing("garlic", 2, "piece"),
                        ing("butter", 20, "g"), ing("stock cube", 1, "piece")),

                recipe("Creamy Mashed Potatoes",
                        "Smooth mash that goes with everything.", 30,
                        "Peel and boil the potatoes until soft.\nDrain well and return to the pot.\n"
                                + "Mash with the butter, milk and salt.",
                        ing("potato", 600, "g"), ing("butter", 40, "g"), ing("milk", 100, "ml"),
                        ing("salt", 1, "tsp")),

                recipe("Banana Oat Pancakes",
                        "Three ingredients, no flour.", 15,
                        "Mash the bananas in a bowl.\nStir in the eggs and oats.\n"
                                + "Cook spoonfuls in a pan until golden, flipping once.",
                        ing("banana", 2, "piece"), ing("egg", 2, "piece"), ing("oats", 50, "g")),

                recipe("Chicken Stir-Fry",
                        "Fast chicken and vegetable stir-fry.", 20,
                        "Slice the chicken, carrots and onion.\nHeat oil and fry the chicken for 6 minutes.\n"
                                + "Add vegetables and garlic and cook 4 more minutes.\nFinish with the soy sauce.",
                        ing("chicken", 300, "g"), ing("onion", 1, "piece"), ing("carrot", 2, "piece"),
                        ing("soy sauce", 2, "tbsp"), ing("oil", 1, "tbsp"), ing("garlic", 2, "piece")),

                recipe("Creamy Rice Pudding",
                        "A sweet dessert from pantry basics.", 40,
                        "Combine the rice, milk and sugar in a pot.\nSimmer gently, stirring often, for 30 minutes.\n"
                                + "Stir in the butter and serve warm.",
                        ing("rice", 100, "g"), ing("milk", 500, "ml"), ing("sugar", 3, "tbsp"),
                        ing("butter", 10, "g"))
        );
    }
}