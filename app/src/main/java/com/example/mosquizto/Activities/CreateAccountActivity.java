package com.example.mosquizto.Activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.mosquizto.MainActivity;
import com.example.mosquizto.R;
import com.example.mosquizto.ViewModels.CreateAccountViewModel;

import java.util.Calendar;
import java.util.regex.Pattern;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CreateAccountActivity extends AppCompatActivity {

    private CreateAccountViewModel viewModel;

    private ImageButton btnBack;
    private EditText etEmail;
    private EditText etPassword;
    private ImageButton btnTogglePassword;
    private Button btnCreateAccount;

    private EditText etConfirmPassword;
    private EditText etFullName ;
    private EditText etUserName ;
    private boolean isPasswordVisible = false;
    private String selectedBirthdate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.btnBack).getRootView(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        viewModel = new ViewModelProvider(this).get(CreateAccountViewModel.class);

        initViews();
        setupListeners();
        observeViewModel();
    }

    private void initViews() {
        btnBack          = findViewById(R.id.btnBack);
        etEmail          = findViewById(R.id.etEmail);
        etFullName      = findViewById(R.id.etFullName);
        etUserName = findViewById(R.id.etUserName);
        etPassword       = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword) ;
        btnTogglePassword = findViewById(R.id.btnTogglePassword);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
    }

    private void setupListeners() {
        // Back
        btnBack.setOnClickListener(v -> finish());

        // DatePicker
//        etBirthdate.setFocusable(false);
//        etBirthdate.setClickable(true);
//        etBirthdate.setOnClickListener(v -> showDatePicker());

        // Toggle password
        btnTogglePassword.setOnClickListener(v -> togglePasswordVisibility());

        // Validate on input
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { validateForm(); }
        };
        etEmail.addTextChangedListener(watcher);
        etPassword.addTextChangedListener(watcher);

        // Submit
        btnCreateAccount.setOnClickListener(v -> {
            String email     = etEmail.getText().toString().trim();
            String password  = etPassword.getText().toString();
            String confirmPassword = etConfirmPassword.getText().toString() ;
            String fullName = etFullName.getText().toString() ;
            String userName = etUserName.getText().toString();

            viewModel.signup(fullName,userName,email, password ,confirmPassword);
        });
    }

    private void observeViewModel() {
        viewModel.getUiState().observe(this, state -> {
            switch (state) {
                case LOADING:
                    setLoading(true);
                    break;
                case SUCCESS:
                    setLoading(false);
                    navigateToMain();
                    break;
                case ERROR:
                    setLoading(false);
                    Toast.makeText(this, viewModel.getErrorMessage(), Toast.LENGTH_LONG).show();
                    viewModel.resetState();
                    break;
                default:
                    setLoading(false);
                    break;
            }
        });
        viewModel.message.observe(this, msg ->
        {
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            // Thông báo gửi otp bên mail
        });
    }

    private void setLoading(boolean loading) {
        btnCreateAccount.setEnabled(!loading && isFormValid());
        btnCreateAccount.setText(loading ? "Processing…" : getString(R.string.btn_create_account));
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, day) -> {
                    selectedBirthdate = String.format("%02d/%02d/%04d", day, month + 1, year);
                //    etBirthdate.setText(selectedBirthdate);
                    validateForm();
                },
                cal.get(Calendar.YEAR) - 18,
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );
        dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        dialog.show();
    }

    private void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;
        etPassword.setTransformationMethod(
                isPasswordVisible
                        ? HideReturnsTransformationMethod.getInstance()
                        : PasswordTransformationMethod.getInstance()
        );
        btnTogglePassword.setImageResource(
                isPasswordVisible ? R.drawable.ic_visibility_on : R.drawable.ic_visibility_off
        );
        etPassword.setSelection(etPassword.getText().length());
    }

    private boolean isFormValid() {
        boolean emailOk = Pattern
                .compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
                .matcher(etEmail.getText().toString().trim())
                .matches();
        boolean passwordOk = etPassword.getText().toString().length() >= 8;
        return emailOk && passwordOk && !selectedBirthdate.isEmpty();
    }

    private void validateForm() {
        btnCreateAccount.setEnabled(isFormValid());
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}