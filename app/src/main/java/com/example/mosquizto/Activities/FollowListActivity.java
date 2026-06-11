package com.example.mosquizto.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mosquizto.Adapters.SearchResultAdapter;
import com.example.mosquizto.Dto.response.ApiResponse;
import com.example.mosquizto.Dto.response.PageResponse;
import com.example.mosquizto.Dto.response.UserResponse;
import com.example.mosquizto.Network.itf.UserApi;
import com.example.mosquizto.R;
import com.example.mosquizto.Services.SessionManager;
import com.example.mosquizto.Util.SearchResultWrapper;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class FollowListActivity extends AppCompatActivity {

    public static final String INTENT_KEY_TAB_INDEX = "intent_key_tab_index";

    private TabLayout tabLayout;
    private TextView tvTitle;
    private ProgressBar pbLoading;
    private RecyclerView rvUsers;
    private View layoutEmpty;
    private TextView tvEmpty;

    private SearchResultAdapter adapter;
    private Call<ApiResponse<PageResponse<UserResponse>>> currentApiCall;
    private int initialTabIndex = 0;

    @Inject
    UserApi userApi;

    @Inject
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_follow_list);

        View mainView = findViewById(R.id.btnBack).getRootView();
        ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initialTabIndex = getIntent().getIntExtra(INTENT_KEY_TAB_INDEX, 0);

        initViews();
        setupListeners();
        setupTabs();
    }

    private void initViews() {
        tabLayout = findViewById(R.id.tabLayout);
        tvTitle = findViewById(R.id.tvTitle);
        pbLoading = findViewById(R.id.pbLoading);
        rvUsers = findViewById(R.id.rvUsers);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        tvEmpty = findViewById(R.id.tvEmpty);

        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SearchResultAdapter(new ArrayList<>(), new SearchResultAdapter.OnResultClickListener() {
            @Override
            public void onResultClick(SearchResultWrapper item) {
                if (item instanceof UserResponse) {
                    UserResponse user = (UserResponse) item;
                    String targetUsername = user.getUsername();
                    if (targetUsername != null) {
                        Intent intent;
                        if (sessionManager.getCurrUser() != null && targetUsername.equals(sessionManager.getCurrUser().getUsername())) {
                            intent = new Intent(FollowListActivity.this, ProfilePage.class);
                        } else {
                            intent = new Intent(FollowListActivity.this, OtherUserProfileActivity.class);
                            intent.putExtra("intent_key_username", targetUsername);
                        }
                        startActivity(intent);
                    }
                }
            }

            @Override
            public void onMoreClick(SearchResultWrapper item) {
                // Not needed for follow lists
            }
        });
        rvUsers.setAdapter(adapter);
    }

    private void setupListeners() {
        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                tvTitle.setText(tab.getText());
                loadUsers(position);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupTabs() {
        TabLayout.Tab tabFollowers = tabLayout.newTab().setText(R.string.followers);
        TabLayout.Tab tabFollowing = tabLayout.newTab().setText(R.string.following_label);

        tabLayout.addTab(tabFollowers);
        tabLayout.addTab(tabFollowing);

        TabLayout.Tab targetTab = initialTabIndex == 1 ? tabFollowing : tabFollowers;
        targetTab.select();
        tvTitle.setText(targetTab.getText());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (tabLayout != null) {
            loadUsers(tabLayout.getSelectedTabPosition());
        }
    }

    private void loadUsers(int tabIndex) {
        if (currentApiCall != null) {
            currentApiCall.cancel();
        }

        showLoading(true);
        adapter.updateData(new ArrayList<>());

        if (tabIndex == 0) {
            currentApiCall = userApi.getFollowing(1, 100);
            tvEmpty.setText("Bạn chưa có người theo dõi nào");
        } else {
            currentApiCall = userApi.getFollowers(1, 100);
            tvEmpty.setText("Bạn chưa theo dõi người dùng nào");
        }

        currentApiCall.enqueue(new Callback<ApiResponse<PageResponse<UserResponse>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<PageResponse<UserResponse>>> call, @NonNull Response<ApiResponse<PageResponse<UserResponse>>> response) {
                showLoading(false);
                if (isFinishing()) return;

                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<UserResponse> list = response.body().getData().getContent();
                    List<SearchResultWrapper> wrappers = new ArrayList<>();
                    if (list != null) {
                        wrappers.addAll(list);
                    }
                    showResults(wrappers);
                } else {
                    showResults(new ArrayList<>());
                    Toast.makeText(FollowListActivity.this, "Không thể tải danh sách", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<PageResponse<UserResponse>>> call, @NonNull Throwable t) {
                if (call.isCanceled()) return;
                showLoading(false);
                if (isFinishing()) return;

                showResults(new ArrayList<>());
                Toast.makeText(FollowListActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean loading) {
        pbLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
        rvUsers.setVisibility(loading ? View.GONE : View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);
    }

    private void showResults(List<SearchResultWrapper> users) {
        adapter.updateData(users);
        boolean isEmpty = users.isEmpty();
        layoutEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvUsers.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }
}
