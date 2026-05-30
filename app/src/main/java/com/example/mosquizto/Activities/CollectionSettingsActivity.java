package com.example.mosquizto.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mosquizto.R;

public class CollectionSettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_settings);

        RadioGroup rgVisibility = findViewById(R.id.rg_visibility);
        String initialVisibility = getIntent().getStringExtra("visibility");

        if ("PRIVATE".equals(initialVisibility)) {
            ((RadioButton) findViewById(R.id.rb_private)).setChecked(true);
        } else {
            ((RadioButton) findViewById(R.id.rb_public)).setChecked(true);
        }

        rgVisibility.setOnCheckedChangeListener((group, checkedId) -> {
            String selected = (checkedId == R.id.rb_private) ? "PRIVATE" : "PUBLIC";
            Intent resultIntent = new Intent();
            resultIntent.putExtra("visibility", selected);
            setResult(RESULT_OK, resultIntent);
        });
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }
}