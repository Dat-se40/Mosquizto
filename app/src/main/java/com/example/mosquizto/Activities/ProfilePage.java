package com.example.mosquizto.Activities;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.mosquizto.Dto.response.ApiResponse;
import com.example.mosquizto.Models.User;
import com.example.mosquizto.Services.itf.UserApi;

import com.example.mosquizto.R;
import com.example.mosquizto.Services.SessionManager;
import com.example.mosquizto.ViewModels.CreateAccountViewModel;

import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class ProfilePage extends AppCompatActivity {
    @Inject
    UserApi userApi;
    private TextView tvUserName, tvEmail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        tvUserName = findViewById(R.id.tvUsername); // ID tạm, thay bằng ID thật trong XML

        loadUserProfile();
    }

    private void loadUserProfile() {
        // Nhờ có AuthInterceptor bạn đã cấu hình trước đó, nó sẽ tự động lấy Token
        // từ SessionManager nhét vào Header, nên ở đây cứ gọi gọi trực tiếp thôi.
        userApi.getCurrentProfile().enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User currentUser = response.body().getData();
                    // Cập nhật giao diện
                    tvUserName.setText(currentUser.getUsername());
                    // tvEmail.setText(currentUser.getEmail()); // Nếu Model User có email
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                Toast.makeText(ProfilePage.this, "Lỗi lấy thông tin: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
