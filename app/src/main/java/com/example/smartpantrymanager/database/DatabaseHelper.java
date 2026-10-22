package com.example.smartpantrymanager.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.smartpantrymanager.model.Ingredient;
import com.example.smartpantrymanager.model.Recipe;
import com.example.smartpantrymanager.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.List;

/**
 * Single access point to the on-device SQLite database.
 * Provides full CRUD for pantry ingredients and read/insert for recipes.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "smart_pantry.db";
    private static final int DB_VERSION = 1;

    // ingredients table
    private static final String T_ING = "ingredients";
    private static final String I_ID = "id";
    private static final String I_NAME = "name";
    private static final String I_QTY = "quantity";
    private static final String I_UNIT = "unit";
    private static final String I_EXPIRY = "expiry_date";

    // recipes table
    private static final String T_REC = "recipes";
    private static final String R_ID = "id";
    private static final String R_NAME = "name";
    private static final String R_DESC = "description";
    private static final String R_TIME = "prep_time";
    private static final String R_STEPS = "steps";

    // recipe_ingredients table
    private static final String T_RI = "recipe_ingredients";
    private static final String RI_ID = "id";
    private static final String RI_RECIPE_ID = "recipe_id";
    private static final String RI_NAME = "name";
    private static final String RI_QTY = "quantity";
    private static final String RI_UNIT = "unit";

    private static DatabaseHelper instance;

    /**
     * Singleton so the whole app shares one connection.
     */
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true); // needed for ON DELETE CASCADE
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + T_ING + " ("
                + I_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + I_NAME + " TEXT NOT NULL, "
                + I_QTY + " REAL NOT NULL, "
                + I_UNIT + " TEXT NOT NULL, "
                + I_EXPIRY + " INTEGER NOT NULL DEFAULT 0)");

        db.execSQL("CREATE TABLE " + T_REC + " ("
                + R_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + R_NAME + " TEXT NOT NULL, "
                + R_DESC + " TEXT, "
                + R_TIME + " INTEGER, "
                + R_STEPS + " TEXT)");

        db.execSQL("CREATE TABLE " + T_RI + " ("
                + RI_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + RI_RECIPE_ID + " INTEGER NOT NULL, "
                + RI_NAME + " TEXT NOT NULL, "
                + RI_QTY + " REAL NOT NULL, "
                + RI_UNIT + " TEXT NOT NULL, "
                + "FOREIGN KEY(" + RI_RECIPE_ID + ") REFERENCES " + T_REC
                + "(" + R_ID + ") ON DELETE CASCADE)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + T_RI);
        db.execSQL("DROP TABLE IF EXISTS " + T_REC);
        db.execSQL("DROP TABLE IF EXISTS " + T_ING);
        onCreate(db);
    }

    // =====================  PANTRY: CRUD  =====================

    /**
     * CREATE: returns the new row id, or -1 on failure.
     */
    public long addIngredient(Ingredient ingredient) {
        return getWritableDatabase().insert(T_ING, null, toValues(ingredient));
    }

    /**
     * READ: all pantry items, alphabetical.
     */
    public List<Ingredient> getAllIngredients() {
        List<Ingredient> list = new ArrayList<>();
        try (Cursor c = getReadableDatabase().query(
                T_ING, null, null, null, null, null, I_NAME + " COLLATE NOCASE ASC")) {
            while (c.moveToNext()) {
                list.add(cursorToIngredient(c));
            }
        }
        return list;
    }

    /**
     * READ: a single pantry item, or null if it doesn't exist.
     */
    public Ingredient getIngredientById(int id) {
        try (Cursor c = getReadableDatabase().query(
                T_ING, null, I_ID + "=?", new String[]{String.valueOf(id)},
                null, null, null)) {
            return c.moveToFirst() ? cursorToIngredient(c) : null;
        }
    }

    /**
     * UPDATE: returns the number of rows changed (1 on success).
     */
    public int updateIngredient(Ingredient ingredient) {
        return getWritableDatabase().update(
                T_ING, toValues(ingredient),
                I_ID + "=?", new String[]{String.valueOf(ingredient.getId())});
    }

    /**
     * DELETE: returns the number of rows removed (1 on success).
     */
    public int deleteIngredient(int id) {
        return getWritableDatabase().delete(
                T_ING, I_ID + "=?", new String[]{String.valueOf(id)});
    }

    private ContentValues toValues(Ingredient ing) {
        ContentValues v = new ContentValues();
        v.put(I_NAME, ing.getName());
        v.put(I_QTY, ing.getQuantity());
        v.put(I_UNIT, ing.getUnit());
        v.put(I_EXPIRY, ing.getExpiryDate());
        return v;
    }

    private Ingredient cursorToIngredient(Cursor c) {
        return new Ingredient(
                c.getInt(c.getColumnIndexOrThrow(I_ID)),
                c.getString(c.getColumnIndexOrThrow(I_NAME)),
                c.getDouble(c.getColumnIndexOrThrow(I_QTY)),
                c.getString(c.getColumnIndexOrThrow(I_UNIT)),
                c.getLong(c.getColumnIndexOrThrow(I_EXPIRY)));
    }

    // =====================  RECIPES  =====================

    /**
     * Inserts a recipe and all its ingredient rows in ONE transaction,
     * so we never end up with a recipe that is missing ingredients.
     */
    public void insertRecipe(Recipe recipe) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues rv = new ContentValues();
            rv.put(R_NAME, recipe.getName());
            rv.put(R_DESC, recipe.getDescription());
            rv.put(R_TIME, recipe.getPrepTimeMinutes());
            rv.put(R_STEPS, recipe.getSteps());
            long recipeId = db.insertOrThrow(T_REC, null, rv);

            for (RecipeIngredient ri : recipe.getIngredients()) {
                ContentValues iv = new ContentValues();
                iv.put(RI_RECIPE_ID, recipeId);
                iv.put(RI_NAME, ri.getName());
                iv.put(RI_QTY, ri.getQuantity());
                iv.put(RI_UNIT, ri.getUnit());
                db.insertOrThrow(T_RI, null, iv);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public int getRecipeCount() {
        try (Cursor c = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM " + T_REC, null)) {
            return c.moveToFirst() ? c.getInt(0) : 0;
        }
    }

    /**
     * READ: every recipe, each with its ingredient list filled in.
     */
    public List<Recipe> getAllRecipes() {
        List<Recipe> list = new ArrayList<>();
        try (Cursor c = getReadableDatabase().query(
                T_REC, null, null, null, null, null, R_NAME + " ASC")) {
            while (c.moveToNext()) {
                Recipe recipe = cursorToRecipe(c);
                recipe.setIngredients(getIngredientsForRecipe(recipe.getId()));
                list.add(recipe);
            }
        }
        return list;
    }

    /**
     * READ: one recipe with its ingredients, or null if not found.
     */
    public Recipe getRecipeById(int id) {
        try (Cursor c = getReadableDatabase().query(
                T_REC, null, R_ID + "=?", new String[]{String.valueOf(id)},
                null, null, null)) {
            if (!c.moveToFirst()) return null;
            Recipe recipe = cursorToRecipe(c);
            recipe.setIngredients(getIngredientsForRecipe(id));
            return recipe;
        }
    }

    private List<RecipeIngredient> getIngredientsForRecipe(int recipeId) {
        List<RecipeIngredient> list = new ArrayList<>();
        try (Cursor c = getReadableDatabase().query(
                T_RI, null, RI_RECIPE_ID + "=?", new String[]{String.valueOf(recipeId)},
                null, null, null)) {
            while (c.moveToNext()) {
                list.add(new RecipeIngredient(
                        c.getInt(c.getColumnIndexOrThrow(RI_ID)),
                        c.getInt(c.getColumnIndexOrThrow(RI_RECIPE_ID)),
                        c.getString(c.getColumnIndexOrThrow(RI_NAME)),
                        c.getDouble(c.getColumnIndexOrThrow(RI_QTY)),
                        c.getString(c.getColumnIndexOrThrow(RI_UNIT))));
            }
        }
        return list;
    }

    private Recipe cursorToRecipe(Cursor c) {
        return new Recipe(
                c.getInt(c.getColumnIndexOrThrow(R_ID)),
                c.getString(c.getColumnIndexOrThrow(R_NAME)),
                c.getString(c.getColumnIndexOrThrow(R_DESC)),
                c.getInt(c.getColumnIndexOrThrow(R_TIME)),
                c.getString(c.getColumnIndexOrThrow(R_STEPS)));
    }
}