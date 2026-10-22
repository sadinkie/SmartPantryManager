package com.example.smartpantrymanager.ui;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartpantrymanager.R;
import com.example.smartpantrymanager.database.DatabaseHelper;
import com.example.smartpantrymanager.logic.UnitConverter;
import com.example.smartpantrymanager.model.Ingredient;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * One screen for both ADDING a new ingredient and EDITING an existing one.
 * If the launching Intent carries EXTRA_INGREDIENT_ID the screen is in edit mode.
 */
public class AddEditIngredientActivity extends AppCompatActivity {

    /**
     * Intent extra holding the id of the ingredient to edit (absent = add mode).
     */
    public static final String EXTRA_INGREDIENT_ID = "extra_ingredient_id";

    private static final int NO_ID = -1;
    private static final int MAX_NAME_LENGTH = 40;
    private static final double MAX_QUANTITY = 100000;
    private static final String KEY_EXPIRY = "state_expiry";
    private static final String KEY_UNIT = "state_unit";

    private TextInputLayout tilName, tilQuantity, tilUnit;
    private TextInputEditText etName, etQuantity, etExpiry;
    private AutoCompleteTextView actvUnit;
    private Button btnSave, btnClearDate;

    private DatabaseHelper db;
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    private int ingredientId = NO_ID; // NO_ID means "add mode"
    private long expiryMillis = 0;    // 0 means "no expiry date"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_ingredient);

        db = DatabaseHelper.getInstance(this);
        bindViews();

        // Read the Intent: did the caller pass an ingredient id?
        ingredientId = getIntent().getIntExtra(EXTRA_INGREDIENT_ID, NO_ID);
        boolean editMode = ingredientId != NO_ID;

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(editMode ? R.string.edit_ingredient : R.string.add_ingredient);
        toolbar.setNavigationOnClickListener(v -> finish());
        btnSave.setText(editMode ? R.string.btn_save_changes : R.string.btn_add_to_pantry);

        // Unit dropdown, restored after rotation if needed
        String startUnit = UnitConverter.SUPPORTED_UNITS[0];
        if (savedInstanceState != null) {
            expiryMillis = savedInstanceState.getLong(KEY_EXPIRY, 0);
            startUnit = savedInstanceState.getString(KEY_UNIT, startUnit);
        }
        actvUnit.setSaveEnabled(false); // we restore it ourselves (see KEY_UNIT)
        actvUnit.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, UnitConverter.SUPPORTED_UNITS));
        actvUnit.setText(startUnit, false); // false = don't filter the list

        etExpiry.setOnClickListener(v -> showDatePicker());
        btnClearDate.setOnClickListener(v -> {
            expiryMillis = 0;
            updateExpiryUi();
        });
        btnSave.setOnClickListener(v -> onSaveClicked());

        // Only load from the database on a fresh start; after rotation the
        // text fields restore themselves and keep what the user already typed.
        if (editMode && savedInstanceState == null) {
            loadIngredient();
        }
        updateExpiryUi();
    }

    private void bindViews() {
        tilName = findViewById(R.id.til_name);
        tilQuantity = findViewById(R.id.til_quantity);
        tilUnit = findViewById(R.id.til_unit);
        etName = findViewById(R.id.et_name);
        etQuantity = findViewById(R.id.et_quantity);
        etExpiry = findViewById(R.id.et_expiry);
        actvUnit = findViewById(R.id.actv_unit);
        btnSave = findViewById(R.id.btn_save);
        btnClearDate = findViewById(R.id.btn_clear_date);
    }

    // ---------- READ (edit mode) ----------

    private void loadIngredient() {
        io.execute(() -> {
            Ingredient item = db.getIngredientById(ingredientId);
            runOnUiThread(() -> {
                if (item == null) {
                    Toast.makeText(this, R.string.msg_not_found, Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                etName.setText(item.getName());
                double q = item.getQuantity();
                etQuantity.setText(q == Math.rint(q) ? String.valueOf((long) q) : String.valueOf(q));
                actvUnit.setText(UnitConverter.canonicalUnit(item.getUnit()), false);
                expiryMillis = item.getExpiryDate();
                updateExpiryUi();
            });
        });
    }

    // ---------- CREATE / UPDATE ----------

    private void onSaveClicked() {
        if (!validate()) {
            return; // errors are already shown on the fields
        }

        String name = capitalizeFirst(text(etName).replaceAll("\\s+", " "));
        double quantity = Double.parseDouble(text(etQuantity).replace(',', '.'));
        String unit = text(actvUnit);
        long expiry = expiryMillis;
        boolean isNew = (ingredientId == NO_ID);

        btnSave.setEnabled(false); // prevent double taps creating duplicates
        io.execute(() -> {
            if (isNew) {
                db.addIngredient(new Ingredient(name, quantity, unit, expiry));
            } else {
                db.updateIngredient(new Ingredient(ingredientId, name, quantity, unit, expiry));
            }
            runOnUiThread(() -> {
                Toast.makeText(this,
                        getString(isNew ? R.string.msg_added : R.string.msg_updated, name),
                        Toast.LENGTH_SHORT).show();
                finish(); // back to the list, which reloads in onResume
            });
        });
    }

    // ---------- INPUT VALIDATION ----------

    /**
     * Checks every field, showing an error under each invalid one.
     */
    private boolean validate() {
        boolean valid = true;

        // Name
        String name = text(etName);
        if (name.isEmpty()) {
            valid = fail(tilName, R.string.err_name_required);
        } else if (name.length() > MAX_NAME_LENGTH) {
            valid = fail(tilName, R.string.err_name_long);
        } else if (!name.matches(".*\\p{L}.*")) {
            valid = fail(tilName, R.string.err_name_letters);
        } else {
            tilName.setError(null);
        }

        // Quantity (accepts "4.5" and "4,5")
        String qtyRaw = text(etQuantity);
        if (qtyRaw.isEmpty()) {
            valid = fail(tilQuantity, R.string.err_qty_required);
        } else {
            try {
                double q = Double.parseDouble(qtyRaw.replace(',', '.'));
                if (!(q > 0)) {                      // also rejects NaN
                    valid = fail(tilQuantity, R.string.err_qty_positive);
                } else if (q > MAX_QUANTITY) {       // also rejects Infinity
                    valid = fail(tilQuantity, R.string.err_qty_large);
                } else {
                    tilQuantity.setError(null);
                }
            } catch (NumberFormatException e) {
                valid = fail(tilQuantity, R.string.err_qty_invalid);
            }
        }

        // Unit must be one of the supported units
        String unit = text(actvUnit);
        boolean unitOk = false;
        for (String supported : UnitConverter.SUPPORTED_UNITS) {
            if (supported.equals(unit)) {
                unitOk = true;
                break;
            }
        }
        if (unitOk) {
            tilUnit.setError(null);
        } else {
            valid = fail(tilUnit, R.string.err_unit_invalid);
        }

        return valid;
    }

    private boolean fail(TextInputLayout layout, int messageRes) {
        layout.setError(getString(messageRes));
        return false;
    }

    // ---------- EXPIRY DATE ----------

    private void showDatePicker() {
        long selection = expiryMillis > 0
                ? localMidnightToUtc(expiryMillis)
                : MaterialDatePicker.todayInUtcMilliseconds();

        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.select_expiry_title)
                .setSelection(selection)
                .build();
        picker.addOnPositiveButtonClickListener(utcMillis -> {
            expiryMillis = utcToLocalMidnight(utcMillis);
            updateExpiryUi();
        });
        picker.show(getSupportFragmentManager(), "expiry_picker");
    }

    private void updateExpiryUi() {
        boolean hasDate = expiryMillis > 0;
        etExpiry.setText(hasDate ? dateFormat.format(expiryMillis) : "");
        btnClearDate.setVisibility(hasDate ? android.view.View.VISIBLE : android.view.View.GONE);
    }

    /**
     * The date picker works in UTC; we store LOCAL midnight so the date never shifts.
     */
    private static long utcToLocalMidnight(long utcMillis) {
        Calendar utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        utc.setTimeInMillis(utcMillis);
        Calendar local = Calendar.getInstance();
        local.clear();
        local.set(utc.get(Calendar.YEAR), utc.get(Calendar.MONTH), utc.get(Calendar.DAY_OF_MONTH));
        return local.getTimeInMillis();
    }

    private static long localMidnightToUtc(long localMillis) {
        Calendar local = Calendar.getInstance();
        local.setTimeInMillis(localMillis);
        Calendar utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        utc.clear();
        utc.set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH));
        return utc.getTimeInMillis();
    }

    // ---------- helpers ----------

    private static String text(TextView view) {
        return view.getText() == null ? "" : view.getText().toString().trim();
    }

    private static String capitalizeFirst(String s) {
        return s.isEmpty() ? s : s.substring(0, 1).toUpperCase(Locale.getDefault()) + s.substring(1);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(KEY_EXPIRY, expiryMillis);
        outState.putString(KEY_UNIT, text(actvUnit));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        io.shutdown();
    }
}