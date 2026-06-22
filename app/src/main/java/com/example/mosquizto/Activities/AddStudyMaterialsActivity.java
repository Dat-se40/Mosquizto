package com.example.mosquizto.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import com.example.mosquizto.Network.itf.CollectionApi;
import com.example.mosquizto.Network.itf.FolderApi;
import com.example.mosquizto.R;
import com.example.mosquizto.Util.ApiErrorHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class AddStudyMaterialsActivity extends AppCompatActivity {

    private Long folderId;
    private ImageView btnBack;
    private EditText etSearch;
    private RecyclerView rvMyCollections;
    private Button btnConfirmAdd, btnCreateNew;

    private SelectCollectionAdapter adapter;
    private List<CollectionResponse> originalList = new ArrayList<>();

    @Inject
    CollectionApi collectionApi;

    @Inject
    FolderApi folderApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_study_materials);

        folderId = getIntent().getLongExtra("FOLDER_ID", -1);

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
        collectionApi.getMyCollections(1, 50).enqueue(new Callback<ApiResponse<PageResponse<CollectionResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<PageResponse<CollectionResponse>>> call, Response<ApiResponse<PageResponse<CollectionResponse>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    originalList = response.body().getData().getContent();
                    setupAdapter(originalList != null ? originalList : new ArrayList<>());
                } else {
                    Toast.makeText(AddStudyMaterialsActivity.this,
                            ApiErrorHelper.extractMessage(response), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PageResponse<CollectionResponse>>> call, Throwable t) {
                Toast.makeText(AddStudyMaterialsActivity.this,
                        ApiErrorHelper.networkError(AddStudyMaterialsActivity.this), Toast.LENGTH_SHORT).show();
            }
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

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keyword = s.toString().toLowerCase();
                List<CollectionResponse> filtered = new ArrayList<>();
                for (CollectionResponse collection : originalList) {
                    String title = collection.getTitle() != null ? collection.getTitle().toLowerCase() : "";
                    if (title.contains(keyword)) {
                        filtered.add(collection);
                    }
                }
                setupAdapter(filtered);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnCreateNew.setOnClickListener(v -> startActivity(new Intent(this, CreateCollectionActivity.class)));
        btnConfirmAdd.setOnClickListener(v -> addSelectedCollections());
    }

    private void addSelectedCollections() {
        if (adapter == null) return;

        Set<Integer> selectedIds = adapter.getSelectedIds();
        if (selectedIds.isEmpty()) return;

        btnConfirmAdd.setEnabled(false);
        btnConfirmAdd.setText("Đang thêm...");

        int total = selectedIds.size();
        final int[] completedCount = {0};
        final int[] successCount = {0};

        for (Integer collectionId : selectedIds) {
            folderApi.addCollectionToFolder(folderId, collectionId).enqueue(new Callback<ApiResponse<FolderResponse>>() {
                @Override
                public void onResponse(Call<ApiResponse<FolderResponse>> call, Response<ApiResponse<FolderResponse>> response) {
                    if (response.isSuccessful()) {
                        successCount[0]++;
                    } else {
                        Log.e("AddStudyMaterials", "addCollection failed: " + ApiErrorHelper.extractMessage(response));
                    }
                    checkIfDone(++completedCount[0], successCount[0], total);
                }

                @Override
                public void onFailure(Call<ApiResponse<FolderResponse>> call, Throwable t) {
                    Log.e("AddStudyMaterials", "addCollection onFailure", t);
                    checkIfDone(++completedCount[0], successCount[0], total);
                }
            });
        }
    }

    private void checkIfDone(int completed, int success, int total) {
        if (completed != total) return;

        if (success > 0) {
            Toast.makeText(this, "Đã thêm " + success + " học phần", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            btnConfirmAdd.setEnabled(true);
            btnConfirmAdd.setText("Thêm " + total + " mục");
            Toast.makeText(this, ApiErrorHelper.networkError(this), Toast.LENGTH_SHORT).show();
        }
    }
}
