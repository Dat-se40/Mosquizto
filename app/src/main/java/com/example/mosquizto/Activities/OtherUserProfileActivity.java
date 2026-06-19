package com.example.mosquizto.Activities;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.mosquizto.Adapters.OtherUserProfilePagerAdapter;
import com.example.mosquizto.Dto.response.ApiResponse;
import com.example.mosquizto.Dto.response.OtherUserProfileResponse;
import com.example.mosquizto.Network.itf.UserApi;
import com.example.mosquizto.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class OtherUserProfileActivity extends AppCompatActivity {

    private String fullName;
    private String userName ;
    private OtherUserProfileResponse profileData;

    private TextView tvProfileName;
    private TextView tvFollowStats;
    private MaterialButton btnFollow;
    private ImageView ivAvatar;

    @Inject
    UserApi userApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_profile);

        View mainView = findViewById(R.id.btnBack).getRootView();
        ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get username from Intent
        userName = getIntent().getStringExtra("intent_key_username");
        fullName = getIntent().getStringExtra("intent_key_full_name");
        if (userName == null || userName.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy người dùng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        initViews();
        setupListeners();
        setupViewPager();
        loadUserProfile();
    }

    private void initViews() {
        tvProfileName = findViewById(R.id.tvProfileName);
        tvFollowStats = findViewById(R.id.tvFollowStats);
        btnFollow = findViewById(R.id.btnFollow);
        ivAvatar = findViewById(R.id.ivAvatar);

        // Hide follow button initially until profile data is loaded
        btnFollow.setVisibility(View.GONE);
        tvProfileName.setText(fullName);
    }

    private void setupListeners() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnOptions).setOnClickListener(v -> showOptionsBottomSheet());

        btnFollow.setOnClickListener(v -> toggleFollow());
    }

    private void setupViewPager() {
        ViewPager2 viewPager = findViewById(R.id.view_pager);
        TabLayout tabLayout = findViewById(R.id.tabLayout);

        OtherUserProfilePagerAdapter pagerAdapter = new OtherUserProfilePagerAdapter(this, userName,fullName);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(getString(R.string.tvStudySet)); // "Bộ thẻ"
        }).attach();
    }

    private void loadUserProfile() {
        userApi.getUserProfile(userName).enqueue(new Callback<ApiResponse<OtherUserProfileResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<OtherUserProfileResponse>> call, @NonNull Response<ApiResponse<OtherUserProfileResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    profileData = response.body().getData();
                    bindProfileData();
                } else {
                    Toast.makeText(OtherUserProfileActivity.this, "Không thể tải hồ sơ người dùng", Toast.LENGTH_SHORT).show();
                    Log.e("OtherUserProfileAct", "Failed: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<OtherUserProfileResponse>> call, @NonNull Throwable t) {
                Toast.makeText(OtherUserProfileActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                Log.e("OtherUserProfileAct", "Failure loading profile", t);
            }
        });

    }

    private void bindProfileData() {
        if (profileData == null) return;

        String displayName = (profileData.getFullName() != null && !profileData.getFullName().isEmpty())
                ? profileData.getFullName()
                : profileData.getUsername();
        tvProfileName.setText(displayName);

        updateFollowUI();
        btnFollow.setVisibility(View.VISIBLE);
        displayAvatar(profileData.getImgUri());
    }

    private void updateFollowUI() {
        if (profileData == null) return;

        String stats = getString(R.string.followers_following, profileData.getFollowersCount(),profileData.getFollowingCount());
        tvFollowStats.setText(stats);

        if (profileData.isFollowed()) {
            btnFollow.setText(R.string.following);

            // Set Followed State colors (Secondary button style: grey/variant background, dark/white text)
            int bgColor = ContextCompat.getColor(this, R.color.app_button_secondary_background);
            int textColor = ContextCompat.getColor(this, R.color.app_button_secondary_text);
            btnFollow.setBackgroundTintList(ColorStateList.valueOf(bgColor));
            btnFollow.setTextColor(textColor);
            btnFollow.setStrokeColor(ColorStateList.valueOf(bgColor));
        } else {
            btnFollow.setText(R.string.follow);
            
            // Set Unfollowed State colors (Primary button style: blue background, white text)
            int bgColor = ContextCompat.getColor(this, R.color.primary_color);
            int textColor = ContextCompat.getColor(this, R.color.white);
            btnFollow.setBackgroundTintList(ColorStateList.valueOf(bgColor));
            btnFollow.setTextColor(textColor);
            btnFollow.setStrokeColor(ColorStateList.valueOf(bgColor));
        }
    }

    private void toggleFollow() {
        if (profileData == null) return;

        boolean currentFollowed = profileData.isFollowed();
        boolean newFollowed = !currentFollowed;
        long currentFollowers = profileData.getFollowersCount();
        long newFollowers = newFollowed ? currentFollowers + 1 : Math.max(0, currentFollowers - 1);

        // Optimistically update UI
        profileData.setFollowed(newFollowed);
        profileData.setFollowersCount(newFollowers);
        updateFollowUI();

        Call<ApiResponse<Void>> call = newFollowed ? userApi.followUser(userName) : userApi.unfollowUser(userName);
        call.enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call, @NonNull Response<ApiResponse<Void>> response) {
                if (!response.isSuccessful()) {
                    // Rollback
                    profileData.setFollowed(currentFollowed);
                    profileData.setFollowersCount(currentFollowers);
                    updateFollowUI();
                    Toast.makeText(OtherUserProfileActivity.this, "Thao tác thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                // Rollback
                profileData.setFollowed(currentFollowed);
                profileData.setFollowersCount(currentFollowers);
                updateFollowUI();
                Toast.makeText(OtherUserProfileActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showOptionsBottomSheet() {
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_user_profile, null);
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(bottomSheetView);

        bottomSheetView.findViewById(R.id.btnHideUser).setOnClickListener(v -> {
            Toast.makeText(this, R.string.msg_dev_mode, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        bottomSheetView.findViewById(R.id.btnReportUser).setOnClickListener(v -> {
            Toast.makeText(this, R.string.msg_dev_mode, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }
    private void displayAvatar(String imgUri) {
        if (imgUri != null && !imgUri.isEmpty()) {
            Picasso.get()
                    .load(imgUri)
                    .placeholder(R.drawable.ic_default_avatar)
                    .error(R.drawable.ic_default_avatar)
                    .into(ivAvatar);
        } else {
            ivAvatar.setImageResource(R.drawable.ic_default_avatar);
        }
    }
}
