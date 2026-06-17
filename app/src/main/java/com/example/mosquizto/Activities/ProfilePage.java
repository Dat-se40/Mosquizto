package com.example.mosquizto.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mosquizto.Dto.response.ApiResponse;
import com.example.mosquizto.Dto.response.OtherUserProfileResponse;
import com.example.mosquizto.Dto.response.UserResponse;
import com.example.mosquizto.MainActivity;
import com.example.mosquizto.Models.User;
import com.example.mosquizto.Network.WebSocketManager;
import com.example.mosquizto.R;
import com.example.mosquizto.Services.LogoutManager;
import com.example.mosquizto.Services.SessionManager;
import com.example.mosquizto.Network.itf.UserApi;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;

import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class ProfilePage extends AppCompatActivity {

    private TextView tvUserName;
    private TextView tvFollowStats;
    private ImageView imgProfile;

    @Inject
    public SessionManager sessionManager;
    @Inject
    public WebSocketManager webSocketManager;
    @Inject
    LogoutManager logoutManager;
    @Inject
    UserApi userApi;
    
    private BadgeDrawable badge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.btnBack).getRootView(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

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
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserProfile();
    }

    @SuppressLint("UnsafeOptInUsageError")
    private void initViews() {
        tvUserName = findViewById(R.id.tvUsername);
        tvFollowStats = findViewById(R.id.tvFollowStats);
        imgProfile = findViewById(R.id.ivAvatar);

        if (tvFollowStats != null) {
            tvFollowStats.setVisibility(View.GONE);
        }

        if (sessionManager.getCurrUser() != null) {
            tvUserName.setText(sessionManager.getCurrUser().getUsername());
        }

        ImageView icNotification = findViewById(R.id.ic_notification);
        FrameLayout badgeContainer = findViewById(R.id.notificationBadgeContainer);
        
        if (icNotification != null && badgeContainer != null) {
            badge = BadgeDrawable.create(this);
            badge.setMaxCharacterCount(3);
            // Use ContextCompat to be safe
            badge.setBackgroundColor(ContextCompat.getColor(this, R.color.error_red));
            
            // Attach badge to the icon within its FrameLayout container
            BadgeUtils.attachBadgeDrawable(badge, icNotification, badgeContainer);
        }

        webSocketManager.getNotificationCount().observe(this, count -> {
            if (badge == null || count == null) return;
            if (count > 0) {
                badge.setVisible(true);
                badge.setNumber(count);
            } else {
                badge.clearNumber();
                badge.setVisible(false);
            }
        });
    }

    private void setupListeners() {
        findViewById(R.id.btnBack).setOnClickListener(v -> {
            getOnBackPressedDispatcher().onBackPressed();
        });

        findViewById(R.id.menuSettings).setOnClickListener(v -> {
            Intent intent = new Intent(ProfilePage.this, SettingsActivity.class);
            startActivity(intent);
        });
        
        findViewById(R.id.btn_logout).setOnClickListener(v -> {
            logoutManager.logout();
            Intent intent = new Intent(ProfilePage.this, WelcomeActivity.class);
            startActivity(intent);
            finish();
        });
        
        findViewById(R.id.menuActivity).setOnClickListener(v -> {
            Intent intent = new Intent(ProfilePage.this, NotificationActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.tvSeeAll).setOnClickListener(v -> {
            Intent intent = new Intent(ProfilePage.this, AchievementActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.cardStreakStart).setOnClickListener(v -> {
            Intent intent = new Intent(ProfilePage.this, AchievementActivity.class);
            startActivity(intent);
        });
    }

    private void loadUserProfile() {
//        userApi.getMyProfile().enqueue(new Callback<ApiResponse<UserResponse>>() {
//            @Override
//            public void onResponse(@NonNull Call<ApiResponse<UserResponse>> call, @NonNull Response<ApiResponse<UserResponse>> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    UserResponse currentUserData = response.body().getData();
//                    if (currentUserData != null && currentUserData.getUsername() != null) {
//                        tvUserName.setText(currentUserData.getUsername());
//
//                        if (tvFollowStats != null) {
//                            String stats = getString(R.string.followers_following, currentUserData.getFollowersCount(),currentUserData.getFollowingCount());
//                            tvFollowStats.setText(stats);
//                            tvFollowStats.setVisibility(View.VISIBLE);
//                            tvFollowStats.setOnClickListener(v -> {
//                                Intent intent = new Intent(ProfilePage.this, FollowListActivity.class);
//                                intent.putExtra(FollowListActivity.INTENT_KEY_TAB_INDEX, 0); // Open Followers tab by default
//                                startActivity(intent);
//                            });
//                        }
//
//                        User userToSave = new User();
//                        userToSave.setUsername(currentUserData.getUsername());
//                        userToSave.setEmail(currentUserData.getEmail());
//
//                        sessionManager.saveSession(sessionManager.getAccessToken(), userToSave, sessionManager.getRefreshToken());
//                    }
//                }
//            }
//
//            @Override
//            public void onFailure(@NonNull Call<ApiResponse<UserResponse>> call, @NonNull Throwable t) {
//                Log.e("ProfilePage", "Error loading profile", t);
//            }
//        });

      userApi.getUserProfile(sessionManager.getCurrUser().getUsername()).enqueue(new Callback<ApiResponse<OtherUserProfileResponse>>() {
          @Override
          public void onResponse(Call<ApiResponse<OtherUserProfileResponse>> call, Response<ApiResponse<OtherUserProfileResponse>> response) {
              if (response.isSuccessful() && response.body() != null)
              {
                  // other này là mình :v
                  OtherUserProfileResponse userProfile = response.body().getData();
                  if(userProfile == null) return ;
                  tvUserName.setText(response.body().getData().getUsername());
                  String stats = getString(R.string.followers_following, userProfile.getFollowersCount(),userProfile.getFollowingCount());
                  tvFollowStats.setText(stats);
                  tvFollowStats.setVisibility(View.VISIBLE);
                  tvFollowStats.setOnClickListener(v -> {
                    Intent intent = new Intent(ProfilePage.this, FollowListActivity.class);
                    intent.putExtra(FollowListActivity.INTENT_KEY_TAB_INDEX, 0); // Open Followers tab by default
                    startActivity(intent);
                  });
              }
          }

          @Override
          public void onFailure(Call<ApiResponse<OtherUserProfileResponse>> call, Throwable t) {

          }
      });
    }
}
