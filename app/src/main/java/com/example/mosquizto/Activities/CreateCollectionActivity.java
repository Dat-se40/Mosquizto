package com.example.mosquizto.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mosquizto.Adapters.CreateCardAdapter;
import com.example.mosquizto.Dto.request.CollectionItemRequest;
import com.example.mosquizto.Dto.request.CollectionRequest;
import com.example.mosquizto.Dto.response.ApiResponse;
import com.example.mosquizto.Dto.response.CollectionItemResponse;
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
public class CreateCollectionActivity extends AppCompatActivity {

    @Inject
    CollectionApi collectionApi;

    private CreateCardAdapter adapter;
    private final List<CollectionItemRequest> itemList = new ArrayList<>();

    private Integer createdCollectionId = null;
    private Boolean currentVisibility = true;
    private ProgressDialog progressDialog;
    
    private EditText etTitle, etDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_collection);

        initViews();
        handleIncomingData(); // Nhận dữ liệu nếu đây là thao tác Copy
        setupListeners();
    }

    private void initViews() {
        etTitle = findViewById(R.id.et_title);
        etDescription = findViewById(R.id.et_description);
        RecyclerView rvItems = findViewById(R.id.rv_items);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang lưu học phần...");
        progressDialog.setCancelable(false);

        adapter = new CreateCardAdapter(itemList);
        rvItems.setLayoutManager(new LinearLayoutManager(this));
        rvItems.setAdapter(adapter);
    }

    private void handleIncomingData() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("COPY_ITEMS")) {
            // Trường hợp COPY: Điền dữ liệu từ bộ thẻ gốc
            String title = intent.getStringExtra("COPY_TITLE");
            if (title != null) etTitle.setText("Bản sao của " + title);
            
            ArrayList<CollectionItemResponse> incomingItems = intent.getParcelableArrayListExtra("COPY_ITEMS");
            if (incomingItems != null) {
                for (CollectionItemResponse item : incomingItems) {
                    itemList.add(new CollectionItemRequest(item.getTerm(), item.getDefinition()));
                }
                adapter.notifyDataSetChanged();
            }
        } else {
            // Trường hợp TẠO MỚI thông thường: Add 2 thẻ trống
            itemList.add(new CollectionItemRequest("", ""));
            itemList.add(new CollectionItemRequest("", ""));
            adapter.notifyDataSetChanged();
        }
    }

    private void setupListeners() {
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        findViewById(R.id.btn_settings).setOnClickListener(v -> {
            Intent intent = new Intent(this, CollectionSettingsActivity.class);
            intent.putExtra("visibility", currentVisibilityString());
            startActivityForResult(intent, 200);
        });

        findViewById(R.id.btn_add_card).setOnClickListener(v -> {
            itemList.add(new CollectionItemRequest("", ""));
            adapter.notifyItemInserted(itemList.size() - 1);
            RecyclerView rvItems = findViewById(R.id.rv_items);
            rvItems.smoothScrollToPosition(itemList.size() - 1);
        });

        findViewById(R.id.btn_save).setOnClickListener(v -> handleSave());
    }

    private void handleSave() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        
        if (title.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tiêu đề", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();

        CollectionRequest request = new CollectionRequest(title, description, currentVisibility);
        
        collectionApi.createCollection(request).enqueue(new Callback<ApiResponse<Integer>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Integer>> call, @NonNull Response<ApiResponse<Integer>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    createdCollectionId = response.body().getData();
                    createAllItemsSequentially(0);
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(CreateCollectionActivity.this, "Lỗi tạo học phần", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Integer>> call, @NonNull Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(CreateCollectionActivity.this, "Lỗi kết nối mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createAllItemsSequentially(int index) {
        if (index >= itemList.size()) {
            progressDialog.dismiss();
            Toast.makeText(this, "Đã tạo học phần thành công!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        CollectionItemRequest item = itemList.get(index);

        if (item.getTerm().trim().isEmpty() && item.getDefinition().trim().isEmpty()) {
            createAllItemsSequentially(index + 1);
            return;
        }

        item.setCollectionId(createdCollectionId);
        item.setOrderIndex(index);
        item.setImageUrl("");

        collectionApi.createCollectionItem(item).enqueue(new Callback<ApiResponse<CollectionItemResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<CollectionItemResponse>> call, Response<ApiResponse<CollectionItemResponse>> response) {
                createAllItemsSequentially(index + 1);
            }

            @Override
            public void onFailure(Call<ApiResponse<CollectionItemResponse>> call, Throwable t) {
                createAllItemsSequentially(index + 1);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200 && resultCode == RESULT_OK && data != null) {
            String visibilityStr = data.getStringExtra("visibility");
            currentVisibility = parseVisibility(visibilityStr);
        }
    }

    private String currentVisibilityString() {
        return (currentVisibility != null && currentVisibility) ? "PUBLIC" : "PRIVATE";
    }

    private Boolean parseVisibility(String mode) {
        return "PUBLIC".equals(mode);
    }
}