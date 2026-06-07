package com.example.mosquizto.Activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mosquizto.Adapters.NotificationAdapter;
import com.example.mosquizto.Dto.response.CollectionReportResponse;
import com.example.mosquizto.Dto.response.ShareCollectionResponse;
import com.example.mosquizto.Network.WebSocketManager;
import com.example.mosquizto.R;
import com.example.mosquizto.Services.SessionManager;
import com.example.mosquizto.ViewModels.NotificationViewModel;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class NotificationActivity extends AppCompatActivity {

    private NotificationViewModel viewModel;
    private RecyclerView rvNotifications;
    private ProgressBar pbLoading;
    private LinearLayout layoutEmpty;
    private NotificationAdapter adapter;
    private BadgeDrawable badge;
    
    @Inject
    SessionManager sessionManager;
    @Inject
    WebSocketManager webSocketManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        initViews();
        setupRecyclerView();
        initViewModel();
    }

    private void initViews() {
        rvNotifications = findViewById(R.id.rvNotifications);
        pbLoading = findViewById(R.id.pbLoading);
        layoutEmpty = findViewById(R.id.layoutEmpty);

        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void setupRecyclerView() {
        adapter = new NotificationAdapter(null, sessionManager, new NotificationAdapter.OnNotificationActionListener() {
            @Override
            public void onAcceptInvite(ShareCollectionResponse invite, int position) {
                if (invite.getCollectionId() != null) {
                    viewModel.respondInvitation(invite, "ENABLE");
                    webSocketManager.readNotification();
                }
            }

            @Override
            public void onDenyInvite(ShareCollectionResponse invite, int position) {
                if (invite.getCollectionId() != null) {
                    viewModel.respondInvitation(invite, "DENIED");
                    webSocketManager.readNotification();
                }
            }

            @Override
            public void onDismissReport(CollectionReportResponse report, int position) {
                if (report.getId() != null) {
                    viewModel.dismissReport(report);
                    webSocketManager.readNotification();
                }
            }
        });

        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        rvNotifications.setAdapter(adapter);
    }

    @SuppressLint("UnsafeOptInUsageError")
    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(NotificationViewModel.class);

        viewModel.notifications.observe(this, notifications -> {
            if (notifications != null && !notifications.isEmpty()) {
                adapter.updateData(notifications);
                rvNotifications.setVisibility(View.VISIBLE);
                layoutEmpty.setVisibility(View.GONE);
            } else {
                rvNotifications.setVisibility(View.GONE);
                layoutEmpty.setVisibility(View.VISIBLE);
            }
        });

        viewModel.isLoading.observe(this, loading -> {
            pbLoading.setVisibility(loading != null && loading ? View.VISIBLE : View.GONE);
        });

        ImageView notificationIcon = findViewById(R.id.notificationIcon);
        FrameLayout badgeContainer = findViewById(R.id.notificationBadgeContainer);
        
        if (notificationIcon != null && badgeContainer != null) {
            badge = BadgeDrawable.create(this);
            badge.setMaxCharacterCount(3);

            // Use post to ensure the view hierarchy is fully established before attaching
            notificationIcon.post(() -> {
                if (!isFinishing() && !isDestroyed()) {
                    BadgeUtils.attachBadgeDrawable(badge, notificationIcon, badgeContainer);
                }
            });
        }

        webSocketManager.getNotificationCount().observe(this, count -> {
            if (badge != null && count != null) {
                if (count > 0) {
                    badge.setVisible(true);
                    badge.setNumber(count);
                } else {
                    badge.clearNumber();
                    badge.setVisible(false);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.fetchAllNotifications();
        }
    }
}
