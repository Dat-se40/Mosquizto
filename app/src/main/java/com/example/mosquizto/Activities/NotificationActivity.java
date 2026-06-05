package com.example.mosquizto.Activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
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
import com.example.mosquizto.R;
import com.example.mosquizto.Services.SessionManager;
import com.example.mosquizto.ViewModels.NotificationViewModel;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class NotificationActivity extends AppCompatActivity {

    private NotificationViewModel viewModel;
    private RecyclerView rvNotifications;
    private ProgressBar pbLoading;
    private LinearLayout layoutEmpty;
    private NotificationAdapter adapter;

    @Inject
    SessionManager sessionManager;

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
                viewModel.respondInvitation(invite.getCollectionId(), "ENABLE");
            }

            @Override
            public void onDenyInvite(ShareCollectionResponse invite, int position) {
                viewModel.respondInvitation(invite.getCollectionId(), "DENIED");
            }

            @Override
            public void onDismissReport(CollectionReportResponse report, int position) {
                viewModel.dismissReport(report.getId().longValue());
            }
        });

        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        rvNotifications.setAdapter(adapter);
    }

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
            pbLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
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
