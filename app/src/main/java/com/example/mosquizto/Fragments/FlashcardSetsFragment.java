package com.example.mosquizto.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.mosquizto.Adapters.FlashcardSetAdapter;
import com.example.mosquizto.Dto.response.ApiResponse;
import com.example.mosquizto.Dto.response.PageResponse;
import com.example.mosquizto.Models.Collection;
import com.example.mosquizto.R;
import com.example.mosquizto.Network.itf.CollectionApi;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class FlashcardSetsFragment extends Fragment {
    private RecyclerView rv;
    private FlashcardSetAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    @Inject CollectionApi collectionApi;
    private List<Collection> originalList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_flashcard_sets, container, false);
        rv = v.findViewById(R.id.recycler_flashcard_sets);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        swipeRefreshLayout = v.findViewById(R.id.swipe_refresh);
        ChipGroup filterChipGroup = v.findViewById(R.id.chip_group_filter);
        adapter = new FlashcardSetAdapter(getContext(), new ArrayList<>());
        rv.setAdapter(adapter);

        // 1. Xử lý Logic kéo để Refresh
        swipeRefreshLayout.setOnRefreshListener(this::fetchMyCollections);

        // 2. Xử lý Logic lọc (Filters)
        filterChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int selectedId = checkedIds.get(0);

            if (selectedId == R.id.chip_created) {
                // CHÚ THÍCH: Tạm thời Demo lọc list. Bạn có thể sửa logic lọc theo Owner ID thực tế.
                adapter.setCollectionList(originalList);
            } else if (selectedId == R.id.chip_all) {
                adapter.setCollectionList(originalList);
            } else {
                adapter.setCollectionList(originalList);
            }
        });

        // Tự động load API lần đầu tiên
        fetchMyCollections();
        return v;
    }

    private void fetchMyCollections() {
        swipeRefreshLayout.setRefreshing(true);

        // CHÚ THÍCH: Gọi API getMyCollections. Theo Backend, nó trả về PageResponse
        Call<ApiResponse<PageResponse<Collection>>> call = collectionApi.getMyCollections(0, 50); // Lấy trang 0, 50 items

        call.enqueue(new Callback<ApiResponse<PageResponse<Collection>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<PageResponse<Collection>>> call, @NonNull Response<ApiResponse<PageResponse<Collection>>> response) {
                swipeRefreshLayout.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    // 1. Lấy ra ApiResponse
                    ApiResponse<PageResponse<Collection>> apiResponse = response.body();

                    // 2. Lấy ra PageResponse thông qua hàm getData() của ApiResponse
                    PageResponse<Collection> pageResponse = apiResponse.getData();

                    if (pageResponse != null) {
                        // 3. Lấy ra List<Collection> thông qua hàm getContent() của PageResponse
                        List<Collection> collections = pageResponse.getContent();

                        originalList = collections;
                        adapter.setCollectionList(collections);
                    }
                } else {
                    Toast.makeText(getContext(), "Không thể tải bộ thẻ", Toast.LENGTH_SHORT).show();
                    Log.e("API_ERROR", "Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PageResponse<Collection>>> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                Log.e("API_ERROR", t.getMessage(), t);
            }
        });
    }
}