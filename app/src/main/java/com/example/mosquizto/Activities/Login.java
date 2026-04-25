package com.example.mosquizto.Activities;

import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.content.Intent;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.mosquizto.MainActivity;
import com.example.mosquizto.R;
import com.example.mosquizto.ViewModels.LoginViewModel;
import com.example.mosquizto.Services.SessionManager;
import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class Login extends AppCompatActivity {

    private EditText edtMailOrUsername, edtPassword;
    private Button btnLogin;
    @Inject
    public SessionManager sessionManager;
    private LoginViewModel loginViewModel ;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        edtMailOrUsername = findViewById(R.id.editText_MailOrUsername); // ID tùy theo file XML của bạn
        edtPassword = findViewById(R.id.editText_Password);
        btnLogin = findViewById(R.id.btn_login);

        android.widget.ImageButton btnBack = findViewById(R.id.imageButton_back);
        btnBack.setOnClickListener(v -> finish());

        View btnForgotPassword = findViewById(R.id.textView_ForgotPassword);
        if (btnForgotPassword != null) {
            btnForgotPassword.setOnClickListener(v -> {
                Intent intent = new Intent(Login.this, ResetPass.class);
                startActivity(intent);
            });
        }

        // 1. Lắng nghe nút bấm
        btnLogin.setOnClickListener(v -> {
            String user = edtMailOrUsername.getText().toString();
            String pass = edtPassword.getText().toString();
            loginViewModel.login(user, pass);
        });

        // 2. Lắng nghe kết quả thành công từ API
        loginViewModel.loginSuccess.observe(this, isSuccess -> {
            if (isSuccess != null && isSuccess) {
                Toast.makeText(Login.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                // Chuyển sang màn hình chính
                Intent intent = new Intent(Login.this, MainActivity.class);
                startActivity(intent);
                finish(); // Đóng màn hình login
            }
        });

        // 3. Lắng nghe lỗi
        loginViewModel.errorMessage.observe(this, error -> {
            if (error != null) {
                Toast.makeText(Login.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

}