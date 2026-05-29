package com.example.mosquizto.Activities;

import static android.app.ProgressDialog.show;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.content.res.ColorStateList;
import android.graphics.Color;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.mosquizto.MainActivity;
import com.example.mosquizto.R;
import com.example.mosquizto.ViewModels.WelcomeViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class WelcomeActivity extends AppCompatActivity {

    private WelcomeViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        viewModel = new ViewModelProvider(this).get(WelcomeViewModel.class);

        //Button btnGoogle      = findViewById(R.id.btnGoogle);
        Button btnSignUpEmail = findViewById(R.id.btnSignUpEmail);
        TextView tvLogin      = findViewById(R.id.tvLogin);
        Button btnContinue = findViewById(R.id.btnContinue);

      //  btnGoogle.setOnClickListener(v -> viewModel.onGoogleClicked());
        btnSignUpEmail.setOnClickListener(v -> viewModel.onEmailClicked());
        tvLogin.setOnClickListener(v -> viewModel.onLoginClicked());
        btnContinue.setOnClickListener(v -> viewModel.onWelcomeBackClicked());

        viewModel.AfterCreateView();
        viewModel.UserName.observe(this, name ->
        {
            if(name != null && !name.isEmpty())
            {
                btnContinue.setVisibility(View.VISIBLE);
                btnContinue.setText("Welcome back " + name + " !");
                btnSignUpEmail.setAlpha(0.5f);

                btnSignUpEmail.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F0F2F8")));
                btnSignUpEmail.setTextColor(Color.parseColor("#282E3E"));
            }else {
                btnContinue.setVisibility(View.GONE);

                // Nút Sign Up nổi bật lên (Màu xanh, alpha 1.0)
                btnSignUpEmail.setEnabled(true);
                btnSignUpEmail.setAlpha(1.0f);
                btnSignUpEmail.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4255FF")));
                btnSignUpEmail.setTextColor(Color.parseColor("#ffffff"));
            }

        }) ;
        viewModel.errorMessage.observe(this, message ->
        {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });
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
                case "main":
                    intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                    break;
            }

            viewModel.onNavigationDone();
        });

    }
}