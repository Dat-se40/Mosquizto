package com.example.mosquizto.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mosquizto.Adapters.RecentAdapter;
import com.example.mosquizto.Dialogs.FolderOptionsBottomSheet;
import com.example.mosquizto.Dialogs.RemoveFromFolderBottomSheet;
import com.example.mosquizto.Dto.response.ApiResponse;
import com.example.mosquizto.Dto.response.FolderResponse;
import com.example.mosquizto.Network.RetrofitClient;
import com.example.mosquizto.Network.itf.FolderApi;
import com.example.mosquizto.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FolderDetailActivity extends AppCompatActivity {

    private Long folderId;
    private String folderTitle;

    private TextView tvToolbarTitle;
    private ImageView btnBack, btnAddHeader, btnMore;
    private LinearLayout layoutEmpty;
    private Button btnBigAddMaterials;
    private RecyclerView rvFolderCollections;

    private RecentAdapter adapter; // Tái sử dụng adapter có sẵn
    private FolderApi folderApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder_detail);

        folderId = getIntent().getLongExtra("FOLDER_ID", -1);
        folderTitle = getIntent().getStringExtra("FOLDER_TITLE");
        folderApi = RetrofitClient.getInstance().create(FolderApi.class);

        initViews();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFolderDetails(); // Load lại mỗi khi quay lại trang này
    }

    private void initViews() {
        tvToolbarTitle = findViewById(R.id.tvToolbarTitle);
        btnBack = findViewById(R.id.btnBack);
        btnAddHeader = findViewById(R.id.btnAddHeader);
        btnMore = findViewById(R.id.btnMore);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        btnBigAddMaterials = findViewById(R.id.btnBigAddMaterials);
        rvFolderCollections = findViewById(R.id.rvFolderCollections);

        tvToolbarTitle.setText(folderTitle != null ? folderTitle : "Thư mục");

        rvFolderCollections.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        View.OnClickListener addMaterialsAction = v -> {
            Intent intent = new Intent(FolderDetailActivity.this, com.example.mosquizto.Activities.AddStudyMaterialsActivity.class);
            intent.putExtra("FOLDER_ID", folderId);
            startActivity(intent);
        };

        btnAddHeader.setOnClickListener(addMaterialsAction);
        btnBigAddMaterials.setOnClickListener(addMaterialsAction);

        btnMore.setOnClickListener(v -> {
            FolderOptionsBottomSheet bottomSheet = new FolderOptionsBottomSheet();
            bottomSheet.setListener(new FolderOptionsBottomSheet.OptionsListener() {
                @Override
                public void onAddMaterials() {
                    addMaterialsAction.onClick(null);
                }

                @Override
                public void onEditFolder() {
                    Toast.makeText(FolderDetailActivity.this, "Tính năng Edit đang phát triển", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onShareFolder() {
                    Toast.makeText(FolderDetailActivity.this, "Tính năng Share đang phát triển", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onDeleteFolder() {
                    deleteFolder();
                }
            });
            bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
        });
    }

    private void loadFolderDetails() {
        if (folderId == -1) return;

        folderApi.getDetailFolder(folderId).enqueue(new Callback<ApiResponse<FolderResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<FolderResponse>> call, Response<ApiResponse<FolderResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    FolderResponse data = response.body().getData();
                    tvToolbarTitle.setText(data.getTitle());

                    if (data.getCollections() == null || data.getCollections().isEmpty()) {
                        layoutEmpty.setVisibility(View.VISIBLE);
                        rvFolderCollections.setVisibility(View.GONE);
                    } else {
                        layoutEmpty.setVisibility(View.GONE);
                        rvFolderCollections.setVisibility(View.VISIBLE);

                        // Khởi tạo adapter hiển thị danh sách
                        adapter = new RecentAdapter(data.getCollections(),
                                // Sự kiện 1: Nhấn vào item để đi học
                                item -> {
                                    Intent intent = new Intent(FolderDetailActivity.this, StudySetDetailActivity.class);
                                    intent.putExtra("COLLECTION_ID", item.getId());
                                    startActivity(intent);
                                },
                                // Sự kiện 2: Nhấn vào 3 chấm để gỡ
                                (item, position) -> {
                                    RemoveFromFolderBottomSheet removeDialog = new RemoveFromFolderBottomSheet(item.getTitle(), () -> {
                                        removeCollectionFromFolder(item.getId(), position);
                                    });
                                    removeDialog.show(getSupportFragmentManager(), removeDialog.getTag());
                                }
                        );
                        rvFolderCollections.setAdapter(adapter);
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<FolderResponse>> call, Throwable t) {
                Toast.makeText(FolderDetailActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteFolder() {
        folderApi.deleteFolder(folderId).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                Toast.makeText(FolderDetailActivity.this, "Đã xóa thư mục", Toast.LENGTH_SHORT).show();
                finish();
            }
            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {}
        });
    }

    private void removeCollectionFromFolder(Integer collectionId, int position) {
        // Gọi API xóa Collection khỏi Folder
        folderApi.removeCollectionFromFolder(folderId, collectionId).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(FolderDetailActivity.this, "Đã xóa khỏi thư mục", Toast.LENGTH_SHORT).show();

                    // Xóa UI ngay lập tức không cần tải lại toàn bộ list để mượt hơn
                    if (adapter != null) {
                        adapter.removeItem(position);

                        // Nếu xóa hết sạch thẻ rồi thì hiện lại màn hình "Thư mục trống"
                        if (adapter.getItemCount() == 0) {
                            layoutEmpty.setVisibility(View.VISIBLE);
                            rvFolderCollections.setVisibility(View.GONE);
                        }
                    }
                } else {
                    Toast.makeText(FolderDetailActivity.this, "Xóa thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                Toast.makeText(FolderDetailActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }
}