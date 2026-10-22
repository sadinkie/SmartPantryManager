package com.example.smartpantrymanager.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * One place for the app's saved settings (backed by SharedPreferences).
 */
public final class AppPreferences {

    private static final String FILE_NAME = "smart_pantry_prefs";

    public static final String KEY_SHOW_ALMOST_THERE = "show_almost_there";
    public static final String KEY_HIGHLIGHT_EXPIRING = "highlight_expiring";

    // The strict list is the default experience; the bonus section is opt-in.
    private static final boolean DEFAULT_SHOW_ALMOST_THERE = false;
    private static final boolean DEFAULT_HIGHLIGHT_EXPIRING = true;

    private AppPreferences() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    }

    public static boolean isShowAlmostThere(Context context) {
        return prefs(context).getBoolean(KEY_SHOW_ALMOST_THERE, DEFAULT_SHOW_ALMOST_THERE);
    }

    public static void setShowAlmostThere(Context context, boolean value) {
        prefs(context).edit().putBoolean(KEY_SHOW_ALMOST_THERE, value).apply();
    }

    public static boolean isHighlightExpiring(Context context) {
        return prefs(context).getBoolean(KEY_HIGHLIGHT_EXPIRING, DEFAULT_HIGHLIGHT_EXPIRING);
    }

    public static void setHighlightExpiring(Context context, boolean value) {
        prefs(context).edit().putBoolean(KEY_HIGHLIGHT_EXPIRING, value).apply();
    }
}