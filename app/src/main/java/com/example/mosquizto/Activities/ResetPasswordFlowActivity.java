package com.example.mosquizto.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.mosquizto.R;
import com.example.mosquizto.ViewModels.ResetPassViewModel;

import java.util.regex.Pattern;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ResetPasswordFlowActivity extends AppCompatActivity {
    public static final String EXTRA_EMAIL = "reset_password_email";

    private static final Pattern OTP_PATTERN = Pattern.compile("[A-Z0-9]{8}");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$",
            Pattern.DOTALL
    );

    private ResetPassViewModel viewModel;
    private String email;

    private TextView title;
    private TextView subtitle;
    private LinearLayout otpSection;
    private LinearLayout passwordSection;
    private EditText otpInput;
    private EditText newPasswordInput;
    private EditText confirmPasswordInput;
    private Button verifyButton;
    private Button resendButton;
    private Button resetButton;
    private Button requestNewCodeButton;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reset_password_flow);

        email = getIntent().getStringExtra(EXTRA_EMAIL);
        if (email == null || email.trim().isEmpty()) {
            finish();
            return;
        }
        email = email.trim();

        bindViews();
        applyWindowInsets();
        bindViewModel();
        bindActions();

        if (viewModel.hasResetAuthorization()) {
            showPasswordStep();
        } else {
            showOtpStep();
        }
    }

    private void bindViews() {
        title = findViewById(R.id.text_reset_flow_title);
        subtitle = findViewById(R.id.text_reset_flow_subtitle);
        otpSection = findViewById(R.id.section_reset_otp);
        passwordSection = findViewById(R.id.section_new_password);
        otpInput = findViewById(R.id.edit_reset_otp);
        newPasswordInput = findViewById(R.id.edit_new_password);
        confirmPasswordInput = findViewById(R.id.edit_confirm_new_password);
        verifyButton = findViewById(R.id.button_verify_reset_otp);
        resendButton = findViewById(R.id.button_resend_reset_otp);
        resetButton = findViewById(R.id.button_reset_password);
        requestNewCodeButton = findViewById(R.id.button_request_new_code);
        progressBar = findViewById(R.id.progress_reset_flow);
        viewModel = new ViewModelProvider(this).get(ResetPassViewModel.class);

        ImageButton backButton = findViewById(R.id.imageButton_back);
        backButton.setOnClickListener(view -> finish());
    }

    private void applyWindowInsets() {
        View root = findViewById(R.id.reset_password_flow_root);
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
    }

    private void bindViewModel() {
        viewModel.errorMessage.observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.isLoading.observe(this, loadingValue -> {
            boolean loading = Boolean.TRUE.equals(loadingValue);
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            verifyButton.setEnabled(!loading);
            resendButton.setEnabled(!loading);
            resetButton.setEnabled(!loading);
            requestNewCodeButton.setEnabled(!loading);
        });

        viewModel.isOtpSent.observe(this, sent -> {
            if (Boolean.TRUE.equals(sent)) {
                viewModel.consumeOtpSent();
                otpInput.setText("");
                Toast.makeText(this, R.string.reset_otp_resent, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.isOtpVerified.observe(this, verified -> {
            if (Boolean.TRUE.equals(verified)) {
                viewModel.consumeOtpVerified();
                showPasswordStep();
            }
        });

        viewModel.isPasswordReset.observe(this, reset -> {
            if (Boolean.TRUE.equals(reset)) {
                viewModel.consumePasswordReset();
                Toast.makeText(this, R.string.reset_password_success, Toast.LENGTH_LONG).show();
                navigateToLogin();
            }
        });
    }

    private void bindActions() {
        verifyButton.setOnClickListener(view -> {
            String code = otpInput.getText().toString().trim();
            if (!OTP_PATTERN.matcher(code).matches()) {
                otpInput.setError(getString(R.string.reset_otp_invalid_format));
                otpInput.requestFocus();
                return;
            }
            viewModel.verifyOtp(email, code);
        });

        resendButton.setOnClickListener(view -> viewModel.sendOtp(email));

        resetButton.setOnClickListener(view -> {
            String newPassword = newPasswordInput.getText().toString();
            String confirmPassword = confirmPasswordInput.getText().toString();
            if (!PASSWORD_PATTERN.matcher(newPassword).matches()) {
                newPasswordInput.setError(getString(R.string.reset_password_invalid_format));
                newPasswordInput.requestFocus();
                return;
            }
            if (!newPassword.equals(confirmPassword)) {
                confirmPasswordInput.setError(getString(R.string.reset_error_password_mismatch));
                confirmPasswordInput.requestFocus();
                return;
            }
            viewModel.resetPassword(newPassword, confirmPassword);
        });

        requestNewCodeButton.setOnClickListener(view -> {
            viewModel.discardResetAuthorization();
            newPasswordInput.setText("");
            confirmPasswordInput.setText("");
            showOtpStep();
            viewModel.sendOtp(email);
        });
    }

    private void showOtpStep() {
        title.setText(R.string.reset_otp_title);
        subtitle.setText(getString(R.string.reset_otp_sent_to, email));
        otpSection.setVisibility(View.VISIBLE);
        passwordSection.setVisibility(View.GONE);
    }

    private void showPasswordStep() {
        title.setText(R.string.reset_new_password_title);
        subtitle.setText(R.string.reset_new_password_note);
        otpSection.setVisibility(View.GONE);
        passwordSection.setVisibility(View.VISIBLE);
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, Login.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
