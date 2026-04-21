package com.example.mosquizto.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mosquizto.Adapters.CreateCardAdapter;
import com.example.mosquizto.Dto.request.CollectionItemRequest;
import com.example.mosquizto.Dto.request.CollectionRequest;
import com.example.mosquizto.Dto.response.ApiResponse;
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
public class CreateCollectionActivity extends AppCompatActivity {

    @Inject
    CollectionApi collectionApi;

    private CreateCardAdapter adapter;
    private final List<CollectionItemRequest> itemList = new ArrayList<>();
    private String currentVisibility = "PUBLIC"; // Mặc định là Mọi người

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_collection);

        itemList.add(new CollectionItemRequest("", ""));
        itemList.add(new CollectionItemRequest("", ""));

        RecyclerView rvItems = findViewById(R.id.rv_items);
        adapter = new CreateCardAdapter(itemList);
        rvItems.setLayoutManager(new LinearLayoutManager(this));
        rvItems.setAdapter(adapter);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        findViewById(R.id.btn_settings).setOnClickListener(v -> {
            Intent intent = new Intent(this, CollectionSettingsActivity.class);
            intent.putExtra("visibility", currentVisibility);
            startActivityForResult(intent, 200);
        });

        findViewById(R.id.btn_add_card).setOnClickListener(v -> {
            itemList.add(new CollectionItemRequest("", ""));
            adapter.notifyItemInserted(itemList.size() - 1);
            rvItems.smoothScrollToPosition(itemList.size() - 1);
        });

        findViewById(R.id.btn_save).setOnClickListener(v -> handleSave());
    }

    private void handleSave() {
        String title = ((EditText) findViewById(R.id.et_title)).getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tiêu đề", Toast.LENGTH_SHORT).show();
            return;
        }

        CollectionRequest request = new CollectionRequest(title, "", currentVisibility, itemList);
        collectionApi.createCollection(request).enqueue(new Callback<ApiResponse<Collection>>() {
            @Override
            public void onResponse(Call<ApiResponse<Collection>> call, Response<ApiResponse<Collection>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(CreateCollectionActivity.this, "Đã tạo bộ thẻ thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Collection>> call, Throwable t) {
                Toast.makeText(CreateCollectionActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200 && resultCode == RESULT_OK && data != null) {
            currentVisibility = data.getStringExtra("visibility");
        }
    }
}