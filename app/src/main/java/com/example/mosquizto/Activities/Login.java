package com.example.mosquizto.Activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        edtMailOrUsername = findViewById(R.id.editText_MailOrUsername);
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

        // Cài đặt trạng thái ban đầu của nút (Vô hiệu hóa và làm mờ)
        updateLoginButtonState();

        // Tạo TextWatcher để lắng nghe khi người dùng gõ chữ
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Mỗi lần gõ chữ, kiểm tra lại xem đã điền đủ 2 ô chưa
                updateLoginButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        // Gắn TextWatcher vào cả 2 ô nhập liệu
        edtMailOrUsername.addTextChangedListener(textWatcher);
        edtPassword.addTextChangedListener(textWatcher);

        // --- KẾT THÚC PHẦN THÊM MỚI ---

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

         //3. Lắng nghe lỗi
        loginViewModel.errorMessage.observe(this, error -> {
            if (error != null) {
                Toast.makeText(Login.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    // Hàm kiểm tra và cập nhật giao diện của nút Đăng nhập
    private void updateLoginButtonState() {
        String user = edtMailOrUsername.getText().toString().trim();
        String pass = edtPassword.getText().toString().trim();

        if (!user.isEmpty() && !pass.isEmpty()) {
            btnLogin.setEnabled(true);
            btnLogin.setAlpha(1.0f);
        } else {
            btnLogin.setEnabled(false);
            btnLogin.setAlpha(0.5f);
        }
    }
}