package com.example.mosquizto.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mosquizto.Adapters.SelectCollectionAdapter;
import com.example.mosquizto.Dto.response.ApiResponse;
import com.example.mosquizto.Dto.response.CollectionResponse;
import com.example.mosquizto.Dto.response.FolderResponse;
import com.example.mosquizto.Dto.response.PageResponse;
import com.example.mosquizto.Network.RetrofitClient;
import com.example.mosquizto.Network.itf.CollectionApi;
import com.example.mosquizto.Network.itf.FolderApi;
import com.example.mosquizto.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddStudyMaterialsActivity extends AppCompatActivity {

    private Long folderId;
    private ImageView btnBack;
    private EditText etSearch;
    private RecyclerView rvMyCollections;
    private Button btnConfirmAdd, btnCreateNew;

    private SelectCollectionAdapter adapter;
    private List<CollectionResponse> originalList = new ArrayList<>();

    private CollectionApi collectionApi;
    private FolderApi folderApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_study_materials);

        folderId = getIntent().getLongExtra("FOLDER_ID", -1);
        collectionApi = RetrofitClient.getInstance().create(CollectionApi.class);
        folderApi = RetrofitClient.getInstance().create(FolderApi.class);

        initViews();
        loadMyCollections();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        etSearch = findViewById(R.id.etSearch);
        rvMyCollections = findViewById(R.id.rvMyCollections);
        btnConfirmAdd = findViewById(R.id.btnConfirmAdd);
        btnCreateNew = findViewById(R.id.btnCreateNew);

        rvMyCollections.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadMyCollections() {
        // Lấy danh sách bộ thẻ của người dùng (Có thể cần paging nhưng tạm lấy page 1 size 50)
        collectionApi.getMyCollections(1, 50).enqueue(new Callback<ApiResponse<PageResponse<CollectionResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<PageResponse<CollectionResponse>>> call, Response<ApiResponse<PageResponse<CollectionResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    originalList = response.body().getData().getContent();
                    setupAdapter(originalList);
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<PageResponse<CollectionResponse>>> call, Throwable t) {}
        });
    }

    private void setupAdapter(List<CollectionResponse> list) {
        adapter = new SelectCollectionAdapter(list, selectedCount -> {
            if (selectedCount > 0) {
                btnConfirmAdd.setVisibility(View.VISIBLE);
                btnConfirmAdd.setText("Thêm " + selectedCount + " mục");
            } else {
                btnConfirmAdd.setVisibility(View.GONE);
            }
        });
        rvMyCollections.setAdapter(adapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        // Lọc danh sách khi gõ text
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                List<CollectionResponse> filtered = new ArrayList<>();
                for (CollectionResponse c : originalList) {
                    if (c.getTitle().toLowerCase().contains(s.toString().toLowerCase())) {
                        filtered.add(c);
                    }
                }
                setupAdapter(filtered); // Reload adapter with filtered list
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Xử lý nút Create New
        btnCreateNew.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateCollectionActivity.class);
            startActivity(intent);
        });

        // Xử lý khi xác nhận Thêm
        btnConfirmAdd.setOnClickListener(v -> {
            Set<Integer> selectedIds = adapter.getSelectedIds();
            if (selectedIds.isEmpty()) return;

            btnConfirmAdd.setEnabled(false);
            btnConfirmAdd.setText("Đang thêm...");

            // Do API backend chỉ cho phép add từng cái 1, ta sẽ loop (Vì số lượng ít)
            int total = selectedIds.size();
            final int[] successCount = {0};

            for (Integer collectionId : selectedIds) {
                folderApi.addCollectionToFolder(folderId, collectionId).enqueue(new Callback<ApiResponse<FolderResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<FolderResponse>> call, Response<ApiResponse<FolderResponse>> response) {
                        successCount[0]++;
                        checkIfDone(successCount[0], total);
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<FolderResponse>> call, Throwable t) {
                        successCount[0]++;
                        checkIfDone(successCount[0], total);
                    }
                });
            }
        });
    }

    private void checkIfDone(int current, int total) {
        if (current == total) {
            Toast.makeText(this, "Đã thêm thành công!", Toast.LENGTH_SHORT).show();
            finish(); // Quay lại màn hình Folder Detail
        }
    }
}