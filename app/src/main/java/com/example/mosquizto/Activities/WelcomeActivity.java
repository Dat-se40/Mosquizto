package com.example.mosquizto.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.mosquizto.R;
import com.example.mosquizto.ViewModels.WelcomeViewModel;

public class WelcomeActivity extends AppCompatActivity {

    private WelcomeViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        viewModel = new ViewModelProvider(this).get(WelcomeViewModel.class);

        Button btnGoogle      = findViewById(R.id.btnGoogle);
        Button btnSignUpEmail = findViewById(R.id.btnSignUpEmail);
        TextView tvLogin      = findViewById(R.id.tvLogin);

        btnGoogle.setOnClickListener(v -> viewModel.onGoogleClicked());
        btnSignUpEmail.setOnClickListener(v -> viewModel.onEmailClicked());
        tvLogin.setOnClickListener(v -> viewModel.onLoginClicked());

        viewModel.navigateTo.observe(this, destination -> {
            if (destination == null) return;

            Intent intent;
            switch (destination) {
                case "register":
                    intent = new Intent(this, CreateAccountActivity.class);
                    startActivity(intent);
                    break;
                case "login":
                    intent = new Intent(this, Login.class);
                    startActivity(intent);
                    break;
            }

            viewModel.onNavigationDone();
        });
    }
}