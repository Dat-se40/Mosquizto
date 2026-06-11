package com.example.mosquizto.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.mosquizto.Activities.StudySetDetailActivity;
import com.example.mosquizto.Adapters.SearchResultAdapter;
import com.example.mosquizto.Dto.response.SearchApiResponse;
import com.example.mosquizto.Dto.response.SearchCollectionResultItem;
import com.example.mosquizto.Network.itf.CollectionApi;
import com.example.mosquizto.R;
import com.example.mosquizto.Util.SearchResultWrapper;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class OtherUserCollectionsFragment extends Fragment {

    private static final String ARG_USERNAME = "username";

    private String username;
    private RecyclerView rv;
    private SearchResultAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Inject
    CollectionApi collectionApi;

    public static OtherUserCollectionsFragment newInstance(String username) {
        OtherUserCollectionsFragment fragment = new OtherUserCollectionsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USERNAME, username);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            username = getArguments().getString(ARG_USERNAME);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_flashcard_sets, container, false);
        
        rv = v.findViewById(R.id.recycler_flashcard_sets);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        swipeRefreshLayout = v.findViewById(R.id.swipe_refresh);

        // Hide filter dropdown since we are viewing someone else's profile
        TextView tvFilterTerms = v.findViewById(R.id.tvFilterTerms);
        if (tvFilterTerms != null) {
            tvFilterTerms.setVisibility(View.GONE);
        }

        adapter = new SearchResultAdapter(new ArrayList<>(), new SearchResultAdapter.OnResultClickListener() {
            @Override
            public void onResultClick(SearchResultWrapper item) {
                if (item instanceof SearchCollectionResultItem) {
                    SearchCollectionResultItem collection = (SearchCollectionResultItem) item;
                    Intent intent = new Intent(getContext(), StudySetDetailActivity.class);
                    intent.putExtra(getString(R.string.intent_key_collection_id), collection.getId() != null ? collection.getId().intValue() : -1);
                    intent.putExtra(getString(R.string.intent_key_collection_title), collection.getTitle());
                    intent.putExtra(getString(R.string.intent_key_author), collection.getCreatedByUsername());
                    startActivity(intent);
                }
            }

            @Override
            public void onMoreClick(SearchResultWrapper item) {
                // Not needed for public profile view
            }
        });
        rv.setAdapter(adapter);

        swipeRefreshLayout.setOnRefreshListener(this::fetchUserCollections);

        fetchUserCollections();

        return v;
    }

    private void fetchUserCollections() {
        if (username == null || username.isEmpty()) {
            swipeRefreshLayout.setRefreshing(false);
            return;
        }

        swipeRefreshLayout.setRefreshing(true);
        // Call the collection/search endpoint with author set to the target user
        collectionApi.searchCollections("", 1, 50, username).enqueue(new Callback<SearchApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<SearchApiResponse> call, @NonNull Response<SearchApiResponse> response) {
                swipeRefreshLayout.setRefreshing(false);
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<SearchCollectionResultItem> hits = response.body().getData().getHits();
                    List<SearchResultWrapper> items = new ArrayList<>();
                    if (hits != null) {
                        items.addAll(hits);
                    }
                    adapter.updateData(items);
                } else {
                    Toast.makeText(getContext(), "Không thể tải danh sách học phần", Toast.LENGTH_SHORT).show();
                    Log.e("OtherUserColFrag", "Failed response: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<SearchApiResponse> call, @NonNull Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                if (!isAdded()) return;

                Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                Log.e("OtherUserColFrag", "API failure", t);
            }
        });
    }
}
