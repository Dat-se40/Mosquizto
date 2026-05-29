package com.example.mosquizto.Activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.mosquizto.Fragments.SettingsFragment;
import com.example.mosquizto.R;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings_container, new SettingsFragment())
                    .commit();
        }
    }
}