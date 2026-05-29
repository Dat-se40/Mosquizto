package com.example.mosquizto.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mosquizto.Activities.ProfilePage;
import com.example.mosquizto.Activities.StudySetDetailActivity;
import com.example.mosquizto.Dto.response.CollectionResponse;
import com.example.mosquizto.Dto.response.StudySessionResponse;
import com.example.mosquizto.Dto.response.StudySessionResultResponse;
import com.example.mosquizto.Event.OnItemCollectionClickedListener;
import com.example.mosquizto.Event.OnItemJumpBackInListener;
import com.example.mosquizto.MainActivity;
import com.example.mosquizto.Network.itf.CollectionApi;
import com.example.mosquizto.R;
import com.example.mosquizto.Models.Collection;
import com.example.mosquizto.Models.User;
import com.example.mosquizto.Adapters.BasedOnRecentAdapter;
import com.example.mosquizto.Adapters.JumpBackInAdapter;
import com.example.mosquizto.Adapters.RecentAdapter;
import com.example.mosquizto.Dto.response.ApiResponse;
import com.example.mosquizto.Network.itf.StudyApi;
import com.example.mosquizto.Util.FragmentTag;

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
    private MainActivity mainActivity ;
    @Inject
    StudyApi studyApi;

    @Inject
    CollectionApi collectionApi ;
    private ImageView imgView ;
    private EditText etSearch ;
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
        imgView = view.findViewById(R.id.iv_avatar) ;
        etSearch = view.findViewById(R.id.etSearch) ;
        if (getActivity() instanceof MainActivity)
            mainActivity = (MainActivity) getActivity();

        createListener();


        setupEmptyRecyclerViews();
        fetchJumpBackIn();
        fetchRecents();
        // fetchBasedOnRecent(); // Mở comment khi backend có API này

        return view;
    }

    private void createListener() {
        imgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ProfilePage.class);
                startActivity(intent);
            }
        });
        etSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               mainActivity.switchToFragment(FragmentTag.search);
            }
        }) ;
    }

    private void setupEmptyRecyclerViews() {
        jumpAdapter = new JumpBackInAdapter(null, new OnItemJumpBackInListener() {
            @Override
            public void OnItemClicked(StudySessionResponse item) {
                if (mainActivity != null) mainActivity.GoToStudySetActivity(getContext(), item.getCollectionId(), item.getCollectionName());
            }
        });
        rvJumpBackIn.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvJumpBackIn.setAdapter(jumpAdapter);

        recentAdapter = new RecentAdapter(null, new OnItemCollectionClickedListener() {
            @Override
            public void OnItemClicked(CollectionResponse item) {
                mainActivity.GoToStudySetActivity(getContext(), item);
            }
        } ) ;
        recentAdapter.SetOnCloclickListener(new RecentAdapter.OnCollectionActionListener() {
            @Override
            public void onEdit(CollectionResponse item, int position) {
                // Mở màn hình Edit
            }

            @Override
            public void onShare(CollectionResponse item, int position) {
                // Gọi Intent Share
            }

            @Override
            public void onDelete(CollectionResponse item, int position) {
                // Hiện Dialog xác nhận -> Xóa API -> Xóa khỏi UI
                // recentAdapter.removeItem(position);
            }
        });
        rvRecents.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        rvRecents.setAdapter(recentAdapter);

        basedAdapter = new BasedOnRecentAdapter(null);
        rvBasedOnRecent.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvBasedOnRecent.setAdapter(basedAdapter);

        // Tắt animation để đỡ lag
        rvJumpBackIn.setItemAnimator(null);
        rvRecents.setItemAnimator(null);
        rvBasedOnRecent.setItemAnimator(null);
    }

    // --- CÁC HÀM GỌI API ---

    private void fetchJumpBackIn() {
        studyApi.getJumpBackIn().enqueue(new Callback<ApiResponse<List<StudySessionResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<StudySessionResponse>>> call, Response<ApiResponse<List<StudySessionResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<StudySessionResponse> data = response.body().getData();
                    // Cập nhật adapter và refresh giao diện
                    jumpAdapter.setSessions(data);
                }else
                {
                    Log.e("HomeFragment", "Lỗi JumpBackIn: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<StudySessionResponse>>> call, Throwable t) {
                Log.e("HomeFragment", "Lỗi JumpBackIn: " + t.getMessage());
            }
        });
    }

    private void fetchRecents() {
        collectionApi.getRecentOpenedCollections().enqueue(new Callback<ApiResponse<List<CollectionResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<CollectionResponse>>> call, Response<ApiResponse<List<CollectionResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    recentAdapter.setCollections(response.body().getData());
                    recentAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<CollectionResponse>>> call, Throwable t) {
                Log.e("HomeFragment", "Lỗi Recents: " + t.getMessage());
            }
        });
    }
    @Override
    public void onResume() {
        super.onResume();
        fetchJumpBackIn();
        fetchRecents();
    }
}