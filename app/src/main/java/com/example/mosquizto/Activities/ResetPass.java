package com.example.mosquizto.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.mosquizto.R;
import com.example.mosquizto.ViewModels.ResetPassViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ResetPass extends AppCompatActivity {

    private ImageButton btnBack;
    private EditText etMailReset;
    private Button btnSendLink;
    private ProgressBar progressBar;

    private ResetPassViewModel resetPassViewModel ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_resetpass);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.imageButton_back).getRootView(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupListeners();
        prefillEmailIfProvided();
    }

    private void prefillEmailIfProvided() {
        String email = getIntent().getStringExtra(getString(R.string.intent_key_email));
        if (email != null && !email.trim().isEmpty()) {
            etMailReset.setText(email.trim());
            btnSendLink.setEnabled(true);
            btnSendLink.setAlpha(1.0f);
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.imageButton_back);
        etMailReset = findViewById(R.id.editText_MailReset);
        btnSendLink = findViewById(R.id.btn_SendLink);
        progressBar = findViewById(R.id.progress_reset_password);
        resetPassViewModel = new ViewModelProvider(this).get(ResetPassViewModel.class);

        resetPassViewModel.errorMessage.observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            }
        });
        resetPassViewModel.isOtpSent.observe(this, isSent -> {
            if (Boolean.TRUE.equals(isSent)) {
                resetPassViewModel.consumeOtpSent();
                String email = etMailReset.getText().toString().trim();
                Intent intent = new Intent(ResetPass.this, ResetPasswordFlowActivity.class);
                intent.putExtra(ResetPasswordFlowActivity.EXTRA_EMAIL, email);
                startActivity(intent);
            }
        });
        resetPassViewModel.isLoading.observe(this, loading -> {
            boolean isLoading = Boolean.TRUE.equals(loading);
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            etMailReset.setEnabled(!isLoading);
            updateSendButtonState();
        });
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        etMailReset.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                etMailReset.setError(null);
                updateSendButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnSendLink.setOnClickListener(v -> {
            String email = etMailReset.getText().toString().trim();
            if (!isValidEmail(email)) {
                etMailReset.setError(getString(R.string.forgot_password_invalid_email));
                etMailReset.requestFocus();
                return;
            }
            resetPassViewModel.sendOtp(email);
        });
    }

    private void updateSendButtonState() {
        String email = etMailReset.getText().toString().trim();
        boolean loading = Boolean.TRUE.equals(resetPassViewModel.isLoading.getValue());
        boolean enabled = !loading && !email.isEmpty();
        btnSendLink.setEnabled(enabled);
        btnSendLink.setAlpha(enabled ? 1.0f : 0.5f);
    }

    private boolean isValidEmail(String email) {
        return !email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
