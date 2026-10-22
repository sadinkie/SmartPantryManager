package com.example.smartpantrymanager.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.smartpantrymanager.R;
import com.example.smartpantrymanager.database.DatabaseHelper;
import com.example.smartpantrymanager.logic.RecipeMatcher;
import com.example.smartpantrymanager.model.Ingredient;
import com.example.smartpantrymanager.model.Recipe;
import com.example.smartpantrymanager.model.RecipeIngredient;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Shows one recipe in full: description, ingredient list (with a tick or cross
 * showing what the pantry covers) and the numbered method.
 * Launched with an Intent carrying EXTRA_RECIPE_ID.
 */
public class RecipeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE_ID = "extra_recipe_id";
    private static final int NO_ID = -1;

    private MaterialToolbar toolbar;
    private TextView tvDescription, tvMeta, tvStatus;
    private MaterialCardView cardStatus;
    private LinearLayout llIngredients, llSteps;

    private final DecimalFormat quantityFormat = new DecimalFormat("0.##");
    private final ExecutorService io = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        toolbar = findViewById(R.id.toolbar);
        tvDescription = findViewById(R.id.tv_description);
        tvMeta = findViewById(R.id.tv_meta);
        tvStatus = findViewById(R.id.tv_status);
        cardStatus = findViewById(R.id.card_status);
        llIngredients = findViewById(R.id.ll_ingredients);
        llSteps = findViewById(R.id.ll_steps);

        toolbar.setNavigationOnClickListener(v -> finish());

        // Read the Intent extra sent by the Suggestions screen
        int recipeId = getIntent().getIntExtra(EXTRA_RECIPE_ID, NO_ID);
        if (recipeId == NO_ID) {
            showNotFoundAndClose();
            return;
        }
        loadRecipe(recipeId);
    }

    private void loadRecipe(int recipeId) {
        DatabaseHelper db = DatabaseHelper.getInstance(this);
        io.execute(() -> {
            Recipe recipe = db.getRecipeById(recipeId);
            List<Ingredient> pantry = db.getAllIngredients();

            // Reuse the SAME matching logic: which recipe ingredients are not covered?
            Set<Integer> missingIds = new HashSet<>();
            if (recipe != null) {
                for (RecipeIngredient missing : RecipeMatcher.getMissingIngredients(recipe, pantry)) {
                    missingIds.add(missing.getId());
                }
            }

            runOnUiThread(() -> {
                if (recipe == null) {
                    showNotFoundAndClose();
                } else {
                    bind(recipe, missingIds);
                }
            });
        });
    }

    private void bind(Recipe recipe, Set<Integer> missingIds) {
        toolbar.setTitle(recipe.getName());
        tvDescription.setText(recipe.getDescription());

        int count = recipe.getIngredients().size();
        String ingredientText = getResources()
                .getQuantityString(R.plurals.ingredient_count, count, count);
        tvMeta.setText(getString(R.string.recipe_meta, recipe.getPrepTimeMinutes(), ingredientText));

        bindStatusBanner(missingIds.size());
        bindIngredients(recipe, missingIds);
        bindSteps(recipe.getSteps());
    }

    private void bindStatusBanner(int missingCount) {
        if (missingCount == 0) {
            cardStatus.setCardBackgroundColor(ContextCompat.getColor(this, R.color.green_container));
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.on_green_container));
            tvStatus.setText(R.string.detail_ready);
        } else {
            cardStatus.setCardBackgroundColor(ContextCompat.getColor(this, R.color.orange_container));
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.accent_orange));
            tvStatus.setText(getResources()
                    .getQuantityString(R.plurals.missing_count, missingCount, missingCount));
        }
    }

    private void bindIngredients(Recipe recipe, Set<Integer> missingIds) {
        LayoutInflater inflater = LayoutInflater.from(this);
        llIngredients.removeAllViews();

        for (RecipeIngredient ri : recipe.getIngredients()) {
            View row = inflater.inflate(R.layout.item_detail_ingredient, llIngredients, false);
            ImageView icon = row.findViewById(R.id.iv_status);
            TextView text = row.findViewById(R.id.tv_ingredient);

            boolean missing = missingIds.contains(ri.getId());
            icon.setImageResource(missing ? R.drawable.ic_close : R.drawable.ic_check);
            icon.setColorFilter(ContextCompat.getColor(this,
                    missing ? R.color.error_red : R.color.pantry_green));
            icon.setContentDescription(getString(missing
                    ? R.string.desc_missing_ingredient : R.string.desc_have_ingredient));
            text.setText(describe(ri));

            llIngredients.addView(row);
        }
    }

    private void bindSteps(String steps) {
        LayoutInflater inflater = LayoutInflater.from(this);
        llSteps.removeAllViews();
        if (steps == null) return;

        int number = 1;
        for (String step : steps.split("\n")) {
            String trimmed = step.trim();
            if (trimmed.isEmpty()) continue;

            View row = inflater.inflate(R.layout.item_detail_step, llSteps, false);
            ((TextView) row.findViewById(R.id.tv_step_number)).setText(String.valueOf(number));
            ((TextView) row.findViewById(R.id.tv_step_text)).setText(trimmed);
            llSteps.addView(row);
            number++;
        }
    }

    /** "200 g pasta", "3 × egg". */
    private String describe(RecipeIngredient ri) {
        String qty = quantityFormat.format(ri.getQuantity());
        if ("piece".equals(ri.getUnit())) {
            return qty + " × " + ri.getName();
        }
        return qty + " " + ri.getUnit() + " " + ri.getName();
    }

    private void showNotFoundAndClose() {
        Toast.makeText(this, R.string.msg_recipe_not_found, Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        io.shutdown();
    }
}