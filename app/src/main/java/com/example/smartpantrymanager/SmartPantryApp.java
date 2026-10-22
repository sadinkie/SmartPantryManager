package com.example.smartpantrymanager;

import android.app.Application;

import com.example.smartpantrymanager.database.DatabaseHelper;
import com.example.smartpantrymanager.database.RecipeSeeder;

/**
 * Runs once per app process, before any Activity is created.
 */
public class SmartPantryApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Pre-load the recipe collection on first run (no-op afterwards).
        RecipeSeeder.seedIfEmpty(DatabaseHelper.getInstance(this));
    }
}