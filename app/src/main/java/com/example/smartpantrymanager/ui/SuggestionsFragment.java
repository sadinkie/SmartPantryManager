package com.example.smartpantrymanager.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantrymanager.R;
import com.example.smartpantrymanager.adapter.RecipeAdapter;
import com.example.smartpantrymanager.database.DatabaseHelper;
import com.example.smartpantrymanager.logic.RecipeMatcher;
import com.example.smartpantrymanager.model.Ingredient;
import com.example.smartpantrymanager.model.Recipe;
import com.example.smartpantrymanager.model.RecipeIngredient;
import com.example.smartpantrymanager.util.AppPreferences;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Suggestions tab: runs the strict-matching rule against the current pantry
 * and lists ONLY the recipes the user can cook right now.
 */
public class SuggestionsFragment extends Fragment {

    private DatabaseHelper db;
    private RecipeAdapter adapter;
    private RecyclerView recyclerView;
    private View emptyState;
    private TextView tvEmptyTitle, tvEmptyHint;

    private final ExecutorService io = Executors.newSingleThreadExecutor();

    public SuggestionsFragment() {
        super(R.layout.fragment_suggestions);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = DatabaseHelper.getInstance(requireContext());
        recyclerView = view.findViewById(R.id.recycler_recipes);
        emptyState = view.findViewById(R.id.empty_state);
        tvEmptyTitle = view.findViewById(R.id.tv_empty_title);
        tvEmptyHint = view.findViewById(R.id.tv_empty_hint);
        Button goPantry = view.findViewById(R.id.btn_go_pantry);

        adapter = new RecipeAdapter(recipe ->
                // TEMPORARY: replaced in Step 9 with an Intent to the Recipe Detail screen
                Toast.makeText(requireContext(),
                        "Detail screen arrives in Step 9: " + recipe.getName(),
                        Toast.LENGTH_SHORT).show());
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        // Jump to the Pantry tab so the user can add ingredients
        goPantry.setOnClickListener(v -> {
            BottomNavigationView nav = requireActivity().findViewById(R.id.bottom_nav);
            nav.setSelectedItemId(R.id.nav_pantry);
        });
    }

    /** Re-run the matching every time the user comes back to this tab. */
    @Override
    public void onResume() {
        super.onResume();
        loadSuggestions();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        io.shutdown();
    }

    private void loadSuggestions() {
        Context appContext = requireContext().getApplicationContext();
        boolean showAlmostThere = AppPreferences.isShowAlmostThere(appContext);

        io.execute(() -> {
            List<Recipe> allRecipes = db.getAllRecipes();
            List<Ingredient> pantry = db.getAllIngredients();

            // THE STRICT RULE: only recipes with every ingredient covered.
            List<Recipe> strict = RecipeMatcher.findStrictMatches(allRecipes, pantry);

            List<RecipeAdapter.Row> rows = new ArrayList<>();
            if (!strict.isEmpty()) {
                rows.add(RecipeAdapter.Row.header(
                        appContext.getString(R.string.sugg_header_ready, strict.size())));
                for (Recipe recipe : strict) {
                    rows.add(RecipeAdapter.Row.recipe(recipe, null));
                }
            }

            // OPTIONAL BONUS: a clearly separate section, shown only if enabled.
            if (showAlmostThere) {
                List<Recipe> almost = RecipeMatcher.findAlmostThere(allRecipes, pantry);
                if (!almost.isEmpty()) {
                    int headerRes = strict.isEmpty()
                            ? R.string.sugg_header_almost_none
                            : R.string.sugg_header_almost;
                    rows.add(RecipeAdapter.Row.header(appContext.getString(headerRes, almost.size())));

                    Map<String, Double> totals = RecipeMatcher.buildPantryTotals(pantry);
                    for (Recipe recipe : almost) {
                        List<RecipeIngredient> missing =
                                RecipeMatcher.getMissingIngredients(recipe, totals);
                        String note = appContext.getString(
                                R.string.sugg_short_on, missing.get(0).getName());
                        rows.add(RecipeAdapter.Row.recipe(recipe, note));
                    }
                }
            }

            boolean pantryEmpty = pantry.isEmpty();
            postToUi(() -> render(rows, pantryEmpty));
        });
    }

    private void render(List<RecipeAdapter.Row> rows, boolean pantryEmpty) {
        adapter.setRows(rows);

        boolean empty = rows.isEmpty();
        emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);

        if (empty) {
            if (pantryEmpty) {
                tvEmptyTitle.setText(R.string.pantry_empty_title);
                tvEmptyHint.setText(R.string.sugg_empty_pantry_hint);
            } else {
                tvEmptyTitle.setText(R.string.sugg_empty_title);
                tvEmptyHint.setText(R.string.sugg_empty_hint);
            }
        }
    }

    private void postToUi(Runnable action) {
        Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(action);
        }
    }
}