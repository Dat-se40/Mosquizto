package com.example.mosquizto.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mosquizto.R;
import com.example.mosquizto.Models.Collection;
import com.example.mosquizto.Models.User;
import com.example.mosquizto.Adapters.BasedOnRecentAdapter;
import com.example.mosquizto.Adapters.JumpBackInAdapter;
import com.example.mosquizto.Adapters.RecentAdapter;
import com.example.mosquizto.Dto.response.ApiResponse;
import com.example.mosquizto.Services.itf.StudyApi;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.example.mosquizto.Activities.ProfilePage;
@AndroidEntryPoint
public class HomeFragment extends Fragment {
    private RecyclerView rvJumpBackIn, rvRecents, rvBasedOnRecent;
    private JumpBackInAdapter jumpAdapter;
    private RecentAdapter recentAdapter;
    private BasedOnRecentAdapter basedAdapter;

    @Inject
    StudyApi studyApi;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        View ivAvatar = view.findViewById(R.id.iv_avatar);
        if (ivAvatar != null) {
            ivAvatar.setOnClickListener(v -> {
                startActivity(new Intent(getContext(), ProfilePage.class));
            });
        }

        rvJumpBackIn = view.findViewById(R.id.rvJumpBackIn);
        rvRecents = view.findViewById(R.id.rvRecents);
        rvBasedOnRecent = view.findViewById(R.id.rvBasedOnRecent);

        setupEmptyRecyclerViews();
        fetchJumpBackIn();
        fetchRecents();
        // fetchBasedOnRecent(); // Mở comment khi backend có API này

        return view;
    }

    private void setupEmptyRecyclerViews() {
        jumpAdapter = new JumpBackInAdapter(null);
        rvJumpBackIn.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvJumpBackIn.setAdapter(jumpAdapter);

        recentAdapter = new RecentAdapter(null);
        rvRecents.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        rvRecents.setAdapter(recentAdapter);

        basedAdapter = new BasedOnRecentAdapter(null);
        rvBasedOnRecent.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvBasedOnRecent.setAdapter(basedAdapter);
    }

    // --- CÁC HÀM GỌI API ---

    private void fetchJumpBackIn() {
        studyApi.getJumpBackIn().enqueue(new Callback<ApiResponse<List<Collection>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Collection>>> call, Response<ApiResponse<List<Collection>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Collection> data = response.body().getData();
                    // Cập nhật adapter và refresh giao diện
                    jumpAdapter.setCollections(data);
                    jumpAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Collection>>> call, Throwable t) {
                Log.e("HomeFragment", "Lỗi JumpBackIn: " + t.getMessage());
            }
        });
    }

    private void fetchRecents() {
        studyApi.getRecents().enqueue(new Callback<ApiResponse<List<Collection>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Collection>>> call, Response<ApiResponse<List<Collection>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    recentAdapter.setCollections(response.body().getData());
                    recentAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Collection>>> call, Throwable t) {
                Log.e("HomeFragment", "Lỗi Recents: " + t.getMessage());
            }
        });
    }
}