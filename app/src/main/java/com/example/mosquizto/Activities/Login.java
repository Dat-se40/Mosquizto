package com.example.mosquizto.Activities;

import android.os.Bundle;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.content.Intent;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import com.example.mosquizto.MainActivity;
import com.example.mosquizto.R;
import com.example.mosquizto.ViewModels.LoginViewModel;
import com.example.mosquizto.Services.SessionManager;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class Login extends AppCompatActivity {

    private EditText edtMailOrUsername, edtPassword;
    private Button btnLogin;
    private SessionManager sessionManager;

    private LoginViewModel loginViewModel ;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(this); // Khởi tạo SessionManager
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        edtMailOrUsername = findViewById(R.id.editText_MailOrUsername); // ID tùy theo file XML của bạn
        edtPassword = findViewById(R.id.editText_Password);
        btnLogin = findViewById(R.id.btn_login);

        // 1. Lắng nghe nút bấm
        btnLogin.setOnClickListener(v -> {
            String user = edtMailOrUsername.getText().toString();
            String pass = edtPassword.getText().toString();
            loginViewModel.login(user, pass);
        });

        // 2. Lắng nghe kết quả thành công từ API
        loginViewModel.getLoginResult().observe(this, response -> {
            if (response.getData() != null) {
                // Lưu token vào SessionManager.
                sessionManager.saveAuthToken(response.getData().getAccessToken());

                Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

                // Chuyển sang màn hình chính
                Intent intent = new Intent(Login.this, MainActivity.class);
                startActivity(intent);
                finish(); // Đóng màn hình login
            }
        });

        // 3. Lắng nghe lỗi
        loginViewModel.getErrorMessage().observe(this, error -> {
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        });
    }
}