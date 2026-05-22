package com.example.mosquizto.Activities;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mosquizto.R;
import com.example.mosquizto.Util.ThemeManager;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ImageButton btnBack = findViewById(R.id.btnBack);
        SwitchMaterial switchDarkMode = findViewById(R.id.switchDarkMode);

        btnBack.setOnClickListener(v -> finish());
        switchDarkMode.setChecked(ThemeManager.isDarkMode(this));
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) ->
                ThemeManager.setDarkMode(this, isChecked));
    }
}
