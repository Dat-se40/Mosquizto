package com.example.mosquizto.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mosquizto.Dto.response.ApiResponse;
import com.example.mosquizto.Dto.response.UserResponse;
import com.example.mosquizto.MainActivity;
import com.example.mosquizto.Models.User;
import com.example.mosquizto.R;
import com.example.mosquizto.Services.SessionManager;
import com.example.mosquizto.Network.itf.UserApi;

import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class ProfilePage extends AppCompatActivity {

    private TextView tvUserName;
    private ImageView imgProfile;

    @Inject
    public SessionManager sessionManager;

    @Inject
    UserApi userApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // Đã sửa lỗi: Trả về đúng giao diện activity_profile (nhánh đang sửa bị nhầm thành activity_register)
        setContentView(R.layout.activity_profile);

        // Xử lý giao diện tràn viền (Edge-to-Edge) từ Master
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.btnBack).getRootView(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Xử lý khi bấm nút Back cứng trên điện thoại từ Master
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(ProfilePage.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        initViews();
        setupListeners();

        // Gọi API để lấy thông tin mới nhất từ server (từ nhánh của bạn)
        loadUserProfile();
    }

    private void initViews() {
        tvUserName = findViewById(R.id.tvUsername);
        imgProfile = findViewById(R.id.ivAvatar);

        // Hiển thị ngay lập tức tên User từ SessionManager để UI không bị trống khi đợi API
        if (sessionManager.getCurrUser() != null) {
            tvUserName.setText(sessionManager.getCurrUser().getUsername());
        } else {
            Log.d("ProfilePage", "User is null in SessionManager");
        }
    }

    private void setupListeners() {
        // Nút Back trên giao diện app
        findViewById(R.id.btnBack).setOnClickListener(v -> {
            getOnBackPressedDispatcher().onBackPressed();
        });

        findViewById(R.id.menuSettings).setOnClickListener(v ->
                startActivity(new Intent(ProfilePage.this, SettingsActivity.class)));
    }

    // Hàm lấy dữ liệu mới nhất từ nhánh của bạn
    private void loadUserProfile() {
        userApi.getMyProfile().enqueue(new Callback<ApiResponse<UserResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserResponse>> call, Response<ApiResponse<UserResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserResponse currentUserData = response.body().getData();
                    // Cập nhật lại giao diện với dữ liệu mới nhất từ server
                    if (currentUserData != null && currentUserData.getUsername() != null) {
                        tvUserName.setText(currentUserData.getUsername());

                        User userToSave = new User();
                        userToSave.setUsername(currentUserData.getUsername());
                        userToSave.setEmail(currentUserData.getEmail());

                        String currentToken = sessionManager.getAccessToken(); // Hoặc getAccessToken() tùy tên hàm bạn đặt
                        String currentRefreshToken = sessionManager.getRefreshToken(); // Hàm lấy refresh token

                        sessionManager.saveSession(currentToken, userToSave, currentRefreshToken);
                    }
                } else {
                    Toast.makeText(ProfilePage.this, "Không thể tải thông tin profile", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<UserResponse>> call, Throwable t) {
                Toast.makeText(ProfilePage.this, "Lỗi lấy thông tin: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
