package com.example.smartpantrymanager.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantrymanager.R;
import com.example.smartpantrymanager.model.Ingredient;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Binds the pantry's Ingredient list to the rows of a RecyclerView.
 */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.ViewHolder> {

    /**
     * Lets the screen react when a row is tapped (used for editing in Step 7).
     */
    public interface OnItemClickListener {
        void onItemClick(Ingredient ingredient);
    }

    private static final long DAY_MS = 24L * 60 * 60 * 1000;
    private static final int EXPIRING_SOON_DAYS = 3;

    private final List<Ingredient> items = new ArrayList<>();
    private final OnItemClickListener listener;
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
    private final DecimalFormat quantityFormat = new DecimalFormat("0.##");
    private boolean highlightExpiring = true; // wired to a setting in Step 10

    public PantryAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    // ----- data operations used by the screen -----

    public void setItems(List<Ingredient> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    /**
     * Removes a row with an animation and returns the removed item.
     */
    public Ingredient removeAt(int position) {
        Ingredient removed = items.remove(position);
        notifyItemRemoved(position);
        return removed;
    }

    public void setHighlightExpiring(boolean highlight) {
        this.highlightExpiring = highlight;
        notifyDataSetChanged();
    }

    // ----- RecyclerView.Adapter contract -----

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pantry, parent, false);
        return new ViewHolder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Ingredient item = items.get(position);
        Context context = holder.itemView.getContext();

        holder.name.setText(item.getName());
        holder.quantity.setText(quantityFormat.format(item.getQuantity())
                + " " + displayUnit(item.getUnit()));
        bindExpiry(holder.expiry, item, context);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // ----- helpers -----

    private void bindExpiry(TextView view, Ingredient item, Context context) {
        if (!item.hasExpiryDate()) {
            view.setText(R.string.expiry_none);
            view.setTextColor(ContextCompat.getColor(context, R.color.text_muted));
            return;
        }

        int days = (int) daysUntil(item.getExpiryDate());
        String date = dateFormat.format(item.getExpiryDate());

        if (days < 0) {
            view.setText(context.getString(R.string.expiry_expired, date));
            view.setTextColor(ContextCompat.getColor(context, R.color.error_red));
        } else if (highlightExpiring && days <= EXPIRING_SOON_DAYS) {
            view.setText(days == 0
                    ? context.getString(R.string.expiry_today)
                    : context.getResources().getQuantityString(R.plurals.expiry_in_days, days, days));
            view.setTextColor(ContextCompat.getColor(context, R.color.accent_orange));
        } else {
            view.setText(context.getString(R.string.expiry_on, date));
            view.setTextColor(ContextCompat.getColor(context, R.color.text_muted));
        }
    }

    /**
     * Whole calendar days from today until the given date (negative = past).
     */
    private static long daysUntil(long expiryMillis) {
        return Math.round((startOfDay(expiryMillis) - startOfDay(System.currentTimeMillis()))
                / (double) DAY_MS);
    }

    private static long startOfDay(long millis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    private static String displayUnit(String unit) {
        return "piece".equals(unit) ? "pcs" : unit;
    }

    /**
     * Holds references to a row's views so findViewById runs once per row, not per bind.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView expiry;
        final TextView quantity;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tv_name);
            expiry = itemView.findViewById(R.id.tv_expiry);
            quantity = itemView.findViewById(R.id.tv_quantity);
        }
    }
}