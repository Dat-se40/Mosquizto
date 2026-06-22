package com.example.mosquizto.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mosquizto.Adapters.EditCardAdapter;
import com.example.mosquizto.Dto.request.CollectionItemRequest;
import com.example.mosquizto.Dto.request.CollectionRequest;
import com.example.mosquizto.Dto.response.ApiResponse;
import com.example.mosquizto.Dto.response.CollectionItemResponse;
import com.example.mosquizto.Dto.response.CollectionResponse;
import com.example.mosquizto.Network.itf.CollectionApi;
import com.example.mosquizto.R;
import com.example.mosquizto.Util.ApiErrorHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class EditCollectionActivity extends AppCompatActivity {

    private static final String TAG = "EditCollectionActivity";

    @Inject
    CollectionApi collectionApi;

    private int collectionId = -1;
    private EditCardAdapter adapter;
    private final List<CollectionItemResponse> itemList = new ArrayList<>();
    private ProgressDialog progressDialog;
    private boolean currentVisibility = true;
    private ImageButton btnSetting ;
    // Bộ đếm nguyên tử để theo dõi các tác vụ chạy song song
    private final AtomicInteger pendingTasks = new AtomicInteger(0);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_collection);

        collectionId = getIntent().getIntExtra("COLLECTION_ID", -1);
        if (collectionId == -1) {
            Toast.makeText(this, "ID không hợp lệ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        Intent intent = getIntent();
        var vis = intent.getStringExtra("visibility") ;
        if(vis == null || vis.isEmpty()  || vis.equals("PUBLIC"))
        {
            currentVisibility = true ;
        }else currentVisibility = false ;
        initViews();
        fetchData();
    }

    private void initViews() {
        RecyclerView rvItems = findViewById(R.id.rv_items);
        
        // Tối ưu RecyclerView: Tắt animation và tăng bộ nhớ đệm
        rvItems.setItemAnimator(null); 
        rvItems.setItemViewCacheSize(30); 
        
        adapter = new EditCardAdapter(itemList, () -> {
            // Logic thêm thẻ mới từ Footer
            CollectionItemResponse newItem = new CollectionItemResponse(null, "", "", "", itemList.size(), collectionId, null, null);
            itemList.add(newItem);
            adapter.notifyItemInserted(itemList.size()); // +1 cho header nhưng list bắt đầu từ 1
            rvItems.smoothScrollToPosition(itemList.size() + 1); // Cuộn tới Footer
        });

        rvItems.setLayoutManager(new LinearLayoutManager(this));
        rvItems.setAdapter(adapter);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang lưu siêu tốc...");
        progressDialog.setCancelable(false);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_save).setOnClickListener(v -> handleSaveParallel());
        btnSetting = findViewById(R.id.btn_settings);
        btnSetting.setOnClickListener(v ->
        {
            Intent intent = new Intent(this, CollectionSettingsActivity.class);
            intent.putExtra("visibility", currentVisibility ? "PUBLIC" : "PRIVATE");
            startActivityForResult(intent, 200);
        }) ;
    }

    private void fetchData() {
        // 1. Lấy thông tin bộ thẻ
        collectionApi.getCollectionById(collectionId).enqueue(new Callback<ApiResponse<CollectionResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<CollectionResponse>> call, Response<ApiResponse<CollectionResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CollectionResponse collection = response.body().getData();
                    adapter.setHeaderData(collection.getTitle(), collection.getDescription());
                    if (collection.getVisibility() != null) {
                        currentVisibility = collection.getVisibility();
                    }
                } else {
                    Toast.makeText(EditCollectionActivity.this,
                            ApiErrorHelper.extractMessage(response), Toast.LENGTH_LONG).show();
                }
            }
            @Override public void onFailure(Call<ApiResponse<CollectionResponse>> call, Throwable t) {
                Toast.makeText(EditCollectionActivity.this,
                        ApiErrorHelper.networkError(EditCollectionActivity.this), Toast.LENGTH_SHORT).show();
            }
        });

        // 2. Lấy danh sách item
        collectionApi.getCollectionItemById(collectionId).enqueue(new Callback<ApiResponse<List<CollectionItemResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<CollectionItemResponse>>> call, Response<ApiResponse<List<CollectionItemResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    itemList.clear();
                    itemList.addAll(response.body().getData());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(EditCollectionActivity.this,
                            ApiErrorHelper.extractMessage(response), Toast.LENGTH_LONG).show();
                }
            }
            @Override public void onFailure(Call<ApiResponse<List<CollectionItemResponse>>> call, Throwable t) {
                Toast.makeText(EditCollectionActivity.this,
                        ApiErrorHelper.networkError(EditCollectionActivity.this), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleSaveParallel() {
        // Buộc các ô nhập liệu mất focus để lưu dữ liệu vào List trước khi gửi API
        if (getCurrentFocus() != null) getCurrentFocus().clearFocus();

        String title = adapter.getTitle().trim();
        String description = adapter.getDescription().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, R.string.ntEnterStudySetTitle, Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();

        List<Integer> deletedIds = adapter.getDeletedItemIds();

        // Đếm tổng số task: Xóa + Upsert Item + Cập nhật Collection
        int totalTasks = deletedIds.size() + itemList.size() + 1;
        pendingTasks.set(totalTasks);

        // 1. Xóa (Parallel)
        for (Integer id : deletedIds) {
            // Thêm dấu <> vào đây
            collectionApi.deleteCollectionItem(id).enqueue(new ParallelCallback<>());
        }

        // 2. Upsert Items (Parallel)
        for (int i = 0; i < itemList.size(); i++) {
            CollectionItemResponse item = itemList.get(i);

            // Bỏ qua thẻ trống nhưng vẫn phải giảm task counter
            if (item.getTerm().trim().isEmpty() && item.getDefinition().trim().isEmpty()) {
                decrementAndCheck();
                continue;
            }

            CollectionItemRequest req = new CollectionItemRequest(item.getTerm(), item.getDefinition());
            req.setCollectionId(collectionId);
            req.setOrderIndex(i);
            req.setImageUrl(item.getImageUrl() != null ? item.getImageUrl() : "");

            if (item.getId() == null) {
                // Thêm dấu <> vào đây
                collectionApi.createCollectionItem(req).enqueue(new ParallelCallback<>());
            } else {
                // Thêm dấu <> vào đây
                collectionApi.updateCollectionItem(item.getId(), req).enqueue(new ParallelCallback<>());
            }
        }

        // 3. Cập nhật Collection (Parallel)
        CollectionRequest collReq = new CollectionRequest(title, description, currentVisibility);
        // Thêm dấu <> vào đây
        collectionApi.updateCollection(collectionId, collReq).enqueue(new ParallelCallback<>());
    }

    private void decrementAndCheck() {
        if (pendingTasks.decrementAndGet() <= 0) {
            runOnUiThread(() -> {
                if (progressDialog.isShowing()) progressDialog.dismiss();
                Toast.makeText(this, "Đã lưu tất cả thay đổi!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            });
        }
    }

    private class ParallelCallback<T> implements Callback<T> {
        @Override
        public void onResponse(@NonNull Call<T> call, @NonNull Response<T> response) {
            if (!response.isSuccessful()) {
                Log.e(TAG, "Save task failed: " + ApiErrorHelper.extractMessage(response));
            }
            decrementAndCheck();
        }

        @Override
        public void onFailure(@NonNull Call<T> call, @NonNull Throwable t) {
            Log.e(TAG, "Save task onFailure: " + ApiErrorHelper.networkError(EditCollectionActivity.this), t);
            decrementAndCheck();
        }
    }
}