package com.example.smartpantrymanager.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantrymanager.R;
import com.example.smartpantrymanager.model.Recipe;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays recipes in a RecyclerView with TWO kinds of rows:
 * section headers ("Ready to cook (3)") and recipe cards.
 */
public class RecipeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }

    /** One row of the list: either a header or a recipe card. */
    public static class Row {
        private final boolean header;
        private final String title;
        private final Recipe recipe;
        private final String note;

        private Row(boolean header, String title, Recipe recipe, String note) {
            this.header = header;
            this.title = title;
            this.recipe = recipe;
            this.note = note;
        }

        public static Row header(String title) {
            return new Row(true, title, null, null);
        }

        /** note is optional extra text, e.g. "Short on: butter". */
        public static Row recipe(Recipe recipe, @Nullable String note) {
            return new Row(false, null, recipe, note);
        }
    }

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_RECIPE = 1;

    private final List<Row> rows = new ArrayList<>();
    private final OnRecipeClickListener listener;

    public RecipeAdapter(OnRecipeClickListener listener) {
        this.listener = listener;
    }

    public void setRows(List<Row> newRows) {
        rows.clear();
        rows.addAll(newRows);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return rows.get(position).header ? TYPE_HEADER : TYPE_RECIPE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            return new HeaderHolder(inflater.inflate(R.layout.item_section_header, parent, false));
        }
        return new RecipeHolder(inflater.inflate(R.layout.item_recipe, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Row row = rows.get(position);

        if (holder instanceof HeaderHolder) {
            ((HeaderHolder) holder).title.setText(row.title);
            return;
        }

        RecipeHolder h = (RecipeHolder) holder;
        Recipe recipe = row.recipe;
        Context context = h.itemView.getContext();

        h.name.setText(recipe.getName());
        h.description.setText(recipe.getDescription());

        int count = recipe.getIngredients().size();
        String ingredientText = context.getResources()
                .getQuantityString(R.plurals.ingredient_count, count, count);
        h.meta.setText(context.getString(R.string.recipe_meta,
                recipe.getPrepTimeMinutes(), ingredientText));

        if (row.note == null) {
            h.note.setVisibility(View.GONE);
        } else {
            h.note.setText(row.note);
            h.note.setVisibility(View.VISIBLE);
        }

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onRecipeClick(recipe);
        });
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    static class HeaderHolder extends RecyclerView.ViewHolder {
        final TextView title;

        HeaderHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tv_header);
        }
    }

    static class RecipeHolder extends RecyclerView.ViewHolder {
        final TextView name, description, meta, note;

        RecipeHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tv_recipe_name);
            description = itemView.findViewById(R.id.tv_recipe_desc);
            meta = itemView.findViewById(R.id.tv_recipe_meta);
            note = itemView.findViewById(R.id.tv_recipe_note);
        }
    }
}