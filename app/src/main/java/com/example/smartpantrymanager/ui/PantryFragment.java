package com.example.smartpantrymanager.ui;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantrymanager.R;
import com.example.smartpantrymanager.adapter.PantryAdapter;
import com.example.smartpantrymanager.database.DatabaseHelper;
import com.example.smartpantrymanager.model.Ingredient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Pantry tab: lists every ingredient and lets the user delete by swiping. */
public class PantryFragment extends Fragment {

    private DatabaseHelper db;
    private PantryAdapter adapter;
    private RecyclerView recyclerView;
    private View emptyState;
    private FloatingActionButton fab;

    // Database work runs off the main thread so the UI never freezes.
    private final ExecutorService io = Executors.newSingleThreadExecutor();

    public PantryFragment() {
        super(R.layout.fragment_pantry);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = DatabaseHelper.getInstance(requireContext());
        recyclerView = view.findViewById(R.id.recycler_pantry);
        emptyState = view.findViewById(R.id.empty_state);
        fab = view.findViewById(R.id.fab_add);

        adapter = new PantryAdapter(ingredient ->
                // TEMPORARY: replaced in Step 7 with an Intent to the Edit screen
                Toast.makeText(requireContext(),
                        "Edit screen arrives in Step 7: " + ingredient.getName(),
                        Toast.LENGTH_SHORT).show());

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
        attachSwipeToDelete();

        // TEMPORARY: replaced in Step 7 with an Intent to the Add screen
        fab.setOnClickListener(v -> addSampleItem());
    }

    /** Reload on every return to this screen so changes made elsewhere show up. */
    @Override
    public void onResume() {
        super.onResume();
        loadPantry();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        io.shutdown();
    }

    // ---------- READ ----------

    private void loadPantry() {
        io.execute(() -> {
            List<Ingredient> items = db.getAllIngredients();
            postToUi(() -> {
                adapter.setItems(items);
                updateEmptyState();
            });
        });
    }

    private void updateEmptyState() {
        boolean empty = adapter.getItemCount() == 0;
        emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void postToUi(Runnable action) {
        Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(action);
        }
    }

    // ---------- DELETE (swipe left or right, with Undo) ----------

    private void attachSwipeToDelete() {
        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            private final ColorDrawable redBackground = new ColorDrawable(
                    ContextCompat.getColor(requireContext(), R.color.error_red));

            @Override
            public boolean onMove(@NonNull RecyclerView rv,
                                  @NonNull RecyclerView.ViewHolder holder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false; // we don't support drag-to-reorder
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder holder, int direction) {
                deleteAt(holder.getBindingAdapterPosition());
            }

            @Override
            public void onChildDraw(@NonNull Canvas canvas, @NonNull RecyclerView rv,
                                    @NonNull RecyclerView.ViewHolder holder,
                                    float dX, float dY, int actionState,
                                    boolean isCurrentlyActive) {
                // Draw the red background first so the row slides over it.
                View row = holder.itemView;
                if (dX > 0) {
                    redBackground.setBounds(row.getLeft(), row.getTop(),
                            row.getLeft() + (int) dX, row.getBottom());
                } else if (dX < 0) {
                    redBackground.setBounds(row.getRight() + (int) dX, row.getTop(),
                            row.getRight(), row.getBottom());
                } else {
                    redBackground.setBounds(0, 0, 0, 0);
                }
                redBackground.draw(canvas);
                super.onChildDraw(canvas, rv, holder, dX, dY, actionState, isCurrentlyActive);
            }
        };
        new ItemTouchHelper(callback).attachToRecyclerView(recyclerView);
    }

    private void deleteAt(int position) {
        if (position == RecyclerView.NO_POSITION) return;

        Ingredient removed = adapter.removeAt(position); // update the screen immediately
        updateEmptyState();
        io.execute(() -> db.deleteIngredient(removed.getId())); // then the database

        Snackbar.make(requireView(),
                        getString(R.string.pantry_removed, removed.getName()),
                        Snackbar.LENGTH_LONG)
                .setAnchorView(fab)
                .setAction(R.string.undo, v -> io.execute(() -> {
                    db.addIngredient(new Ingredient(removed.getName(), removed.getQuantity(),
                            removed.getUnit(), removed.getExpiryDate()));
                    loadPantry();
                }))
                .show();
    }

    // ---------- TEMPORARY: sample data so the list can be tested ----------

    private void addSampleItem() {
        long day = 24L * 60 * 60 * 1000;
        long now = System.currentTimeMillis();
        Ingredient[] samples = {
                new Ingredient("Eggs", 6, "piece", now + 10 * day),
                new Ingredient("Milk", 500, "ml", now + 2 * day),
                new Ingredient("Flour", 1, "kg", 0),
                new Ingredient("Tomatoes", 4, "piece", now - day)
        };
        Ingredient sample = samples[adapter.getItemCount() % samples.length];
        io.execute(() -> {
            db.addIngredient(sample);
            loadPantry();
        });
    }
}