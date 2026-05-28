package com.example.mosquizto.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mosquizto.R;

public class RSPassNoti extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView tvResetNoti;
    private Button btnGoBackToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_rspassnoti);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.imageButton_back).getRootView(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupListeners();
        displayEmail();
    }

    private void initViews() {
        btnBack = findViewById(R.id.imageButton_back);
        tvResetNoti = findViewById(R.id.textView_ResetNoti);
        btnGoBackToLogin = findViewById(R.id.btn_Gobacktologin);

        // Kích hoạt nút Go back to login
        btnGoBackToLogin.setEnabled(true);
        btnGoBackToLogin.setAlpha(1.0f);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnGoBackToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RSPassNoti.this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void displayEmail() {
        String email = getIntent().getStringExtra("user_email");if (email != null && !email.isEmpty()) {
            String formattedText = getString(R.string.textView_ResetNoti, email);
            tvResetNoti.setText(formattedText);
        }
    }
}
