package com.example.mosquizto.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Debug;
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

import com.example.mosquizto.Adapters.FlashcardSetAdapter;
import com.example.mosquizto.Services.SessionManager;
import com.google.android.material.tabs.TabLayout;
import com.example.mosquizto.Dto.response.ApiResponse;
import com.example.mosquizto.Dto.response.CollectionResponse;
import com.example.mosquizto.Dto.response.PageResponse;
import com.example.mosquizto.Event.OnItemCollectionClickedListener;
import com.example.mosquizto.MainActivity;
import com.example.mosquizto.Models.Collection;
import com.example.mosquizto.R;
import com.example.mosquizto.Network.itf.CollectionApi;

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
    @Inject
    SessionManager sessionManager;
    private List<CollectionResponse> originalList = new ArrayList<>();
    private String currentFilterMode;
    private String currentSearchQuery = "";
    MainActivity mainActivity ;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        currentFilterMode = getString(R.string.all);

        View v = inflater.inflate(R.layout.fragment_flashcard_sets, container, false);
        rv = v.findViewById(R.id.recycler_flashcard_sets);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        swipeRefreshLayout = v.findViewById(R.id.swipe_refresh);
        if (getActivity() instanceof MainActivity)
            mainActivity = (MainActivity) getActivity();
        adapter = new FlashcardSetAdapter(getContext(), new ArrayList<>(), new OnItemCollectionClickedListener() {
            @Override
            public void OnItemClicked(CollectionResponse item) {
                if(mainActivity != null) mainActivity.GoToStudySetActivity(getContext(),item);
            }
        });
        rv.setAdapter(adapter);

        // =========== XỬ LÝ TEXTVIEW BẬT MENU THẢ XUỐNG ===========
        TextView tvFilterTerms = v.findViewById(R.id.tvFilterTerms);
        if (tvFilterTerms != null) {
            // Hiển thị chữ mặc định ban đầu là "All" (hoặc từ strings.xml tương ứng)
            tvFilterTerms.setText(currentFilterMode);

            tvFilterTerms.setOnClickListener(view -> {
                android.widget.PopupMenu popupMenu = new android.widget.PopupMenu(getContext(), tvFilterTerms);
                popupMenu.getMenu().add(getString(R.string.all));
                popupMenu.getMenu().add(getString(R.string.created));

                popupMenu.setOnMenuItemClickListener(item -> {
                    String selectedItem = item.getTitle().toString();
                    tvFilterTerms.setText(selectedItem);

                    currentFilterMode = selectedItem;
                    applyFilter();
                    return true;
                });
                popupMenu.show();
            });
        }

        // 1. Xử lý Logic kéo để Refresh
        swipeRefreshLayout.setOnRefreshListener(this::fetchMyCollections);



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
                if (!isAdded()) return; // Chống crash nếu user thoát màn hình trước khi API trả về

                if (response.isSuccessful() && response.body() != null) {
                    List<CollectionResponse> remoteList = response.body().getData().getContent();

                    // Tà đạo: Lưu count vào SessionManager để dùng sau
                    if (remoteList != null) {
                        for (CollectionResponse col : remoteList) {
                            if (col.getId() != null) {
                                sessionManager.saveCollectionCount(col.getId(), col.getCount() != null ? col.getCount() : 0);
                            }
                        }
                    }

                    originalList = remoteList;
                    adapter.setCollectionList(remoteList);
                    cacheCollectionCounts(getContext(), remoteList);
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
    public void cacheCollectionCounts(Context context, List<CollectionResponse> collections) {
        if (context == null || collections == null) return;

        SharedPreferences sharedPref = context.getSharedPreferences("MosquiztoCache", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        for (CollectionResponse collection : collections) {
            // Giả sử collection.getId() trả về String hoặc UUID. Mình nối chuỗi làm Key.
            String key = "COLLECTION_COUNT_" + collection.getId();
            int count = collection.getCount(); // Lấy count từ response

            editor.putInt(key, count);
        }

        editor.apply(); // Lưu bất đồng bộ
    }

    // Hàm này sẽ được LibraryFragment gọi xuống khi gõ ô tìm kiếm
    public void filterData(String query) {
        this.currentSearchQuery = query.toLowerCase().trim();
        applyFilter();
    }

    // Hàm lõi xử lý lọc đồng thời cả Loại (All/Created) lẫn Từ khóa tìm kiếm
    private void applyFilter() {
        if (originalList == null || adapter == null) return;

        List<CollectionResponse> filteredList = new ArrayList<>();

        for (CollectionResponse item : originalList) {
            if (item == null) continue;

            // 1. Kiểm tra điều kiện Loại (All / Created)
            boolean matchesMode = true;
            if (currentFilterMode.equals(getString(R.string.created))) {
                matchesMode = (sessionManager != null && sessionManager.getCurrUser() != null &&
                        item.getUserName() != null &&
                        item.getUserName().equals(sessionManager.getCurrUser().getUsername()));
            }

            // 2. Kiểm tra điều kiện Từ khóa tìm kiếm (Query)
            boolean matchesQuery = true;
            if (!currentSearchQuery.isEmpty()) {
                matchesQuery = (item.getTitle() != null && item.getTitle().toLowerCase().contains(currentSearchQuery));
            }

            // Nếu thỏa mãn đồng thời cả 2 điều kiện thì mới giữ lại hiển thị
            if (matchesMode && matchesQuery) {
                filteredList.add(item);
            }
        }

        // Cập nhật danh sách hiển thị lên RecyclerView
        adapter.setCollectionList(filteredList);
    }
}