package com.example.smartpantrymanager.ui;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.smartpantrymanager.R;
import com.example.smartpantrymanager.util.AppPreferences;
import com.google.android.material.materialswitch.MaterialSwitch;

/** Settings tab: two saved on/off preferences plus an About card. */
public class SettingsFragment extends Fragment {

    public SettingsFragment() {
        super(R.layout.fragment_settings);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Context context = requireContext();

        MaterialSwitch switchAlmost = view.findViewById(R.id.switch_almost);
        MaterialSwitch switchHighlight = view.findViewById(R.id.switch_highlight);

        // 1) Show the saved value first...
        switchAlmost.setChecked(AppPreferences.isShowAlmostThere(context));
        switchHighlight.setChecked(AppPreferences.isHighlightExpiring(context));

        // 2) ...then start listening, so loading doesn't trigger a save.
        switchAlmost.setOnCheckedChangeListener((button, checked) ->
                AppPreferences.setShowAlmostThere(context, checked));
        switchHighlight.setOnCheckedChangeListener((button, checked) ->
                AppPreferences.setHighlightExpiring(context, checked));

        // Tapping anywhere on a row toggles its switch
        view.findViewById(R.id.row_almost).setOnClickListener(v -> switchAlmost.toggle());
        view.findViewById(R.id.row_highlight).setOnClickListener(v -> switchHighlight.toggle());

        TextView tvVersion = view.findViewById(R.id.tv_version);
        tvVersion.setText(getString(R.string.settings_version, readVersionName(context)));
    }

    private String readVersionName(Context context) {
        try {
            return context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "1.0";
        }
    }
}