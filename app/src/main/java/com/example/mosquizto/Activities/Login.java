package com.example.mosquizto.Activities;

import android.os.Bundle;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mosquizto.R;

public class Login extends AppCompatActivity {

    private EditText etMailOrUsername, etPassword;
    private Button btnLogin;
    private TextView tvLoginTitle, tvForgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initViews();
        setupListeners();
    }

    private void initViews() {
        etMailOrUsername = findViewById(R.id.editText_MailOrUsername);
        etPassword = findViewById(R.id.editText_Password);
        btnLogin = findViewById(R.id.btn_login);
        tvLoginTitle = findViewById(R.id.textView_Login);
        tvForgotPassword = findViewById(R.id.textView_ForgotPassword);

        // Ban đầu làm mờ nút và khóa không cho bấm
        btnLogin.setEnabled(false);
        btnLogin.setAlpha(0.5f);
    }

    private void setupListeners() {
        // Theo dõi thay đổi text trong 2 ô nhập liệu
        TextWatcher loginWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkInputs();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        etMailOrUsername.addTextChangedListener(loginWatcher);
        etPassword.addTextChangedListener(loginWatcher);

        // Sự kiện nút Login
        btnLogin.setOnClickListener(v -> {
            String user = etMailOrUsername.getText().toString().trim();
            // Sử dụng String Format để hiện thông báo có tên User
            String successMsg = getString(R.string.msg_login_success, user);
            Toast.makeText(Login.this, successMsg, Toast.LENGTH_SHORT).show();
        });

        // Sự kiện Quên mật khẩu
        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, ResetPass.class);
            startActivity(intent);
        });
    }

    private void checkInputs() {
        String user = etMailOrUsername.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();

        if (!user.isEmpty() && !pass.isEmpty()) {
            // TRẠNG THÁI: ĐÃ NHẬP ĐỦt
            btnLogin.setEnabled(true);
            btnLogin.setAlpha(1.0f);

            // Đổi màu nền nút
            btnLogin.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.parseColor("#4254ff")));

            btnLogin.setTextColor(android.graphics.Color.parseColor("#f4f2f8"));

        } else {
            // TRẠNG THÁI: CHƯA ĐỦ THÔNG TIN
            btnLogin.setEnabled(false);
            btnLogin.setAlpha(0.5f);

            // Trả về màu nền cũ (Màu xanh đen mờ bạn đã chọn)
            btnLogin.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.parseColor("#2f3855")));

            // Trả về màu chữ cũ (Màu xám xanh)
            btnLogin.setTextColor(android.graphics.Color.parseColor("#4f5877"));
        }
    }
}