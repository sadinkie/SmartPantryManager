package com.example.smartpantrymanager;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.smartpantrymanager.ui.PantryFragment;
import com.example.smartpantrymanager.ui.SettingsFragment;
import com.example.smartpantrymanager.ui.SuggestionsFragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Home screen. Hosts a toolbar, a bottom navigation bar and a container
 * into which the Pantry, Suggestions or Settings Fragment is placed.
 */
public class MainActivity extends AppCompatActivity {

    private static final String KEY_SELECTED_TAB = "selected_tab";

    private MaterialToolbar toolbar;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        bottomNav = findViewById(R.id.bottom_nav);

        bottomNav.setOnItemSelectedListener(item -> {
            showTab(item.getItemId());
            return true;
        });

        // First launch: open the Pantry tab. After a rotation: restore the last tab.
        int startTab = (savedInstanceState != null)
                ? savedInstanceState.getInt(KEY_SELECTED_TAB, R.id.nav_pantry)
                : R.id.nav_pantry;
        bottomNav.setSelectedItemId(startTab);
    }

    /**
     * Swaps the Fragment in the container and updates the toolbar title.
     */
    private void showTab(int itemId) {
        Fragment fragment;
        int titleRes;

        if (itemId == R.id.nav_suggestions) {
            fragment = new SuggestionsFragment();
            titleRes = R.string.nav_suggestions;
        } else if (itemId == R.id.nav_settings) {
            fragment = new SettingsFragment();
            titleRes = R.string.nav_settings;
        } else {
            fragment = new PantryFragment();
            titleRes = R.string.nav_pantry;
        }

        toolbar.setTitle(titleRes);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_SELECTED_TAB, bottomNav.getSelectedItemId());
    }
}