package com.example.mosquizto.Activities;

import android.os.Bundle;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.content.Intent;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mosquizto.R;
import com.example.mosquizto.dto.request.LoginRequest;
import com.example.mosquizto.dto.response.ApiResponse;
import com.example.mosquizto.dto.response.LoginResponse;
import com.example.mosquizto.services.ApiService;
import com.example.mosquizto.services.itf.UserApi;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Login extends AppCompatActivity {

    private EditText etMailOrUsername, etPassword;
    private Button btnLogin;
    private TextView tvLoginTitle, tvForgotPassword;

    private ApiService apiService;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
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
        apiService = new ApiService(this) ;

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
        // Theo dõi thay đổi text trong 2 ô nhập liệu, tạm block để đỡ lag
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

        etMailOrUsername.addTextChangedListener(loginWatcher)   ;
        etPassword.addTextChangedListener(loginWatcher);

        // Sự kiện nút Login


        btnLogin.setOnClickListener(v -> {
//            String user = etMailOrUsername.getText().toString().trim();
//            // Sử dụng String Format để hiện thông báo có tên User
//            String successMsg = getString(R.string.msg_login_success, user);
//            Toast.makeText(Login.this, successMsg, Toast.LENGTH_SHORT).show() ;
            var retro  = apiService.getRetrofit() ;
            if(retro != null)
            {
                UserApi userApi = retro.create(UserApi.class);
                LoginRequest request = new LoginRequest(etMailOrUsername.getText().toString(),
                        etPassword.getText().toString()) ;

                Logger.getLogger(Login.class.getName()).info(request.toString());
                userApi.signUp(request).enqueue(new Callback<ApiResponse<LoginResponse>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<LoginResponse>> call, @NonNull Response<ApiResponse<LoginResponse>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            // TRƯỜNG HỢP OK (Code 200-299)
                            String logMsg = "Success: " + response.body().toString();
                            Logger.getLogger(Login.class.getName()).info(logMsg);
                            Toast.makeText(Login.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

                            // TODO: Lưu token và chuyển màn hình ở đây
                        } else {
                            // TRƯỜNG HỢP LỖI (Code 4xx, 5xx)
                            String errorMsg = "Login failed with code: " + response.code();

                            // Thử đọc chi tiết lỗi từ errorBody nếu có
                            try {
                                if (response.errorBody() != null) {
                                    errorMsg += " | Detail: " + response.errorBody().string();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            Logger.getLogger(Login.class.getName()).warning(errorMsg);
                            Toast.makeText(Login.this, "Sai tài khoản hoặc mật khẩu!", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<ApiResponse<LoginResponse>> call, Throwable t) {
                        Logger.getLogger(Login.class.getName()).log(Level.WARNING,call.request().toString() +
                                " with exception: " + t.getMessage() );
                    }
                });
            }else
            {
                Toast.makeText(this, "Retro is null to call api", Toast.LENGTH_SHORT).show();
            }


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