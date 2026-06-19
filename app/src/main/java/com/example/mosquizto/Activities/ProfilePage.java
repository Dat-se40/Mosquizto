package com.example.mosquizto.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.mosquizto.Dto.request.UpdateAvatarRequest;
import com.example.mosquizto.Dto.response.ApiResponse;
import com.example.mosquizto.Dto.response.MediaSignResponse;
import com.example.mosquizto.Dto.response.OtherUserProfileResponse;
import com.example.mosquizto.MainActivity;
import com.example.mosquizto.Network.WebSocketManager;
import com.example.mosquizto.Network.itf.MediaApi;
import com.example.mosquizto.R;
import com.example.mosquizto.Services.LogoutManager;
import com.example.mosquizto.Services.SessionManager;
import com.example.mosquizto.Network.itf.UserApi;
import com.example.mosquizto.Util.CloudinaryHelper;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class ProfilePage extends AppCompatActivity {
    private static final String TAG = "ProfilePage";
    private static final String AVATAR_FOLDER = "mosquizto/avatars";

    private Uri selectedImagePath;
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
    @Inject
    MediaApi mediaApi;

    private BadgeDrawable badge;
    private ActivityResultLauncher<String> pickImageLauncher;
    private boolean isUploading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri == null) return;
                    selectedImagePath = uri;
                    Picasso.get().load(selectedImagePath).into(imgProfile);
                    Snackbar.make(findViewById(android.R.id.content), R.string.profile_pic_updated, Snackbar.LENGTH_SHORT)
                            .setAction(R.string.upload, v -> uploadImage())
                            .show();
                }
        );

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
            displayAvatar(sessionManager.getCurrUser().getAvatarUrl());
        }

        ImageView icNotification = findViewById(R.id.ic_notification);
        FrameLayout badgeContainer = findViewById(R.id.notificationBadgeContainer);

        if (icNotification != null && badgeContainer != null) {
            badge = BadgeDrawable.create(this);
            badge.setMaxCharacterCount(3);
            badge.setBackgroundColor(ContextCompat.getColor(this, R.color.error_red));
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
        findViewById(R.id.btnBack).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        findViewById(R.id.menuSettings).setOnClickListener(v ->
                startActivity(new Intent(ProfilePage.this, SettingsActivity.class)));

        findViewById(R.id.btn_logout).setOnClickListener(v -> {
            logoutManager.logout();
            startActivity(new Intent(ProfilePage.this, WelcomeActivity.class));
            finish();
        });

        findViewById(R.id.menuActivity).setOnClickListener(v ->
                startActivity(new Intent(ProfilePage.this, NotificationActivity.class)));

        findViewById(R.id.tvSeeAll).setOnClickListener(v ->
                startActivity(new Intent(ProfilePage.this, AchievementActivity.class)));

        findViewById(R.id.cardStreakStart).setOnClickListener(v ->
                startActivity(new Intent(ProfilePage.this, AchievementActivity.class)));

        imgProfile.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
    }

    private void loadUserProfile() {
        if (sessionManager.getCurrUser() == null) return;

        userApi.getUserProfile(sessionManager.getCurrUser().getUsername())
                .enqueue(new Callback<ApiResponse<OtherUserProfileResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<OtherUserProfileResponse>> call,
                                           Response<ApiResponse<OtherUserProfileResponse>> response) {
                        if (!response.isSuccessful() || response.body() == null) return;

                        OtherUserProfileResponse userProfile = response.body().getData();
                        if (userProfile == null) return;

                        tvUserName.setText(userProfile.getUsername());
                        displayAvatar(userProfile.getImgUri());

                        if (sessionManager.getCurrUser() != null) {
                            sessionManager.getCurrUser().setAvatarUrl(userProfile.getImgUri());
                        }

                        String stats = getString(
                                R.string.followers_following,
                                userProfile.getFollowersCount(),
                                userProfile.getFollowingCount()
                        );
                        tvFollowStats.setText(stats);
                        tvFollowStats.setVisibility(View.VISIBLE);
                        tvFollowStats.setOnClickListener(v -> {
                            Intent intent = new Intent(ProfilePage.this, FollowListActivity.class);
                            intent.putExtra(FollowListActivity.INTENT_KEY_TAB_INDEX, 0);
                            startActivity(intent);
                        });
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<OtherUserProfileResponse>> call, Throwable t) {
                        Log.e(TAG, "loadUserProfile failed", t);
                    }
                });
    }

    private void uploadImage() {
        if (selectedImagePath == null || isUploading) {
            if (selectedImagePath == null) {
                Snackbar.make(findViewById(android.R.id.content), R.string.profile_pic_upload_failed, Snackbar.LENGTH_SHORT).show();
            }
            return;
        }

        View root = findViewById(android.R.id.content);
        isUploading = true;
        Log.d(TAG, "Requesting upload signature for uri=" + selectedImagePath);

        mediaApi.getUploadSignature(AVATAR_FOLDER).enqueue(new Callback<ApiResponse<MediaSignResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<MediaSignResponse>> call,
                                   Response<ApiResponse<MediaSignResponse>> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().getData() == null) {
                    isUploading = false;
                    Log.e(TAG, "Sign failed: HTTP " + response.code());
                    Snackbar.make(root, R.string.profile_pic_upload_failed, Snackbar.LENGTH_SHORT).show();
                    return;
                }

                MediaSignResponse sign = response.body().getData();
                CloudinaryHelper.initIfNeeded(ProfilePage.this, sign.getCloudName());
                uploadToCloudinary(selectedImagePath, sign);
            }

            @Override
            public void onFailure(Call<ApiResponse<MediaSignResponse>> call, Throwable t) {
                isUploading = false;
                Log.e(TAG, "Sign request failed", t);
                Snackbar.make(root, R.string.profile_pic_upload_failed, Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadToCloudinary(Uri imageUri, MediaSignResponse sign) {
        Map<String, Object> options = new HashMap<>();
        options.put("api_key", sign.getApiKey());
        options.put("timestamp", sign.getTimestamp());
        options.put("signature", sign.getSignature());
        options.put("folder", sign.getFolder());
        options.put("public_id", sign.getPublicId());

        View root = findViewById(android.R.id.content);

        MediaManager.get()
                .upload(imageUri)
                .options(options)
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        Log.d(TAG, "Cloudinary upload started, requestId=" + requestId);
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                        Log.d(TAG, "Cloudinary progress " + bytes + "/" + totalBytes);
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String imageUrl = resultData.get("secure_url") != null
                                ? resultData.get("secure_url").toString()
                                : null;
                        Log.d(TAG, "Cloudinary upload success url=" + imageUrl);
                        saveAvatarUrl(imageUrl);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        isUploading = false;
                        Log.e(TAG, "Cloudinary upload error: " + error.getDescription());
                        Snackbar.make(root, R.string.profile_pic_upload_failed, Snackbar.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        Log.w(TAG, "Cloudinary upload rescheduled: " + error.getDescription());
                    }
                })
                .dispatch();
    }

    private void saveAvatarUrl(String avatarUrl) {
        View root = findViewById(android.R.id.content);

        if (avatarUrl == null || avatarUrl.isEmpty()) {
            isUploading = false;
            Snackbar.make(root, R.string.profile_pic_upload_failed, Snackbar.LENGTH_SHORT).show();
            return;
        }

        userApi.updateAvatar(new UpdateAvatarRequest(avatarUrl)).enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                isUploading = false;
                if (response.isSuccessful()) {
                    Log.d(TAG, "PATCH /user/avatar success");
                    displayAvatar(avatarUrl);
                    selectedImagePath = null;
                    if (sessionManager.getCurrUser() != null) {
                        sessionManager.getCurrUser().setAvatarUrl(avatarUrl);
                    }
                    Snackbar.make(root, R.string.profile_pic_upload_success, Snackbar.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "PATCH /user/avatar failed: HTTP " + response.code());
                    Snackbar.make(root, R.string.profile_pic_upload_failed, Snackbar.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                isUploading = false;
                Log.e(TAG, "PATCH /user/avatar failed", t);
                Snackbar.make(root, R.string.profile_pic_upload_failed, Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void displayAvatar(String imgUri) {
        if (imgUri != null && !imgUri.isEmpty()) {
            Picasso.get()
                    .load(imgUri)
                    .placeholder(R.drawable.ic_default_avatar)
                    .error(R.drawable.ic_default_avatar)
                    .into(imgProfile);
        } else {
            imgProfile.setImageResource(R.drawable.ic_default_avatar);
        }
    }
}
