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
import com.example.mosquizto.Dto.response.CollectionResponse;
import com.example.mosquizto.Dto.response.PageResponse;
import com.example.mosquizto.Event.OnItemCollectionClickedListener;
import com.example.mosquizto.MainActivity;
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
    private List<CollectionResponse> originalList = new ArrayList<>();
    MainActivity mainActivity ;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_flashcard_sets, container, false);
        rv = v.findViewById(R.id.recycler_flashcard_sets);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        swipeRefreshLayout = v.findViewById(R.id.swipe_refresh);
        ChipGroup filterChipGroup = v.findViewById(R.id.chip_group_filter);
        if (getActivity() instanceof MainActivity)
            mainActivity = (MainActivity) getActivity();
        adapter = new FlashcardSetAdapter(getContext(), new ArrayList<>(), new OnItemCollectionClickedListener() {
            @Override
            public void OnItemClicked(CollectionResponse item) {
                if(mainActivity != null) mainActivity.GoToStudySetActivity(getContext(),item);
            }
        });
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

        Call<ApiResponse<PageResponse<CollectionResponse>>> call = collectionApi.getMyCollections(1, 50);

        call.enqueue(new Callback<ApiResponse<PageResponse<CollectionResponse>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<PageResponse<CollectionResponse>>> call, @NonNull Response<ApiResponse<PageResponse<CollectionResponse>>> response) {
                swipeRefreshLayout.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<CollectionResponse> remoteList = response.body().getData().getContent();
                    originalList = remoteList;
                    adapter.setCollectionList(remoteList);
                } else {
                    Toast.makeText(getContext(), "Không thể tải bộ thẻ", Toast.LENGTH_SHORT).show();
                    Log.e("API_ERROR", "Code: " + response.code() + "\n" + response);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PageResponse<CollectionResponse>>> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                Log.e("API_ERROR", t.getMessage(), t);
            }
        });
    }
}