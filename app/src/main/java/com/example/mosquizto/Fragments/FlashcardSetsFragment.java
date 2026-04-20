package com.example.mosquizto.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mosquizto.Adapters.FlashcardSetAdapter;
import com.example.mosquizto.Dto.response.ApiResponse;
import com.example.mosquizto.Dto.response.PageResponse;
import com.example.mosquizto.Models.Collection;
import com.example.mosquizto.R;
import com.example.mosquizto.Services.itf.CollectionApi;
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
    @Inject CollectionApi collectionApi;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_flashcard_sets, container, false);
        rv = v.findViewById(R.id.rvFlashcardSets);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new FlashcardSetAdapter(new ArrayList<>());
        rv.setAdapter(adapter);

        loadData();
        return v;
    }

    private void loadData() {
        // Gọi API lấy dữ liệu trang 0, size 20
        collectionApi.getMyCollections(0, 20).enqueue(new Callback<ApiResponse<PageResponse<Collection>>>() {
            @Override
            public void onResponse(Call<ApiResponse<PageResponse<Collection>>> call, Response<ApiResponse<PageResponse<Collection>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setData(response.body().getData().getContent());
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<PageResponse<Collection>>> call, Throwable t) {}
        });
    }
}