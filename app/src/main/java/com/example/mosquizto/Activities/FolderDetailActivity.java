package com.example.mosquizto.Activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.example.mosquizto.Dto.request.UpdateFolderRequest;
import com.example.mosquizto.Dto.response.ApiResponse;
import com.example.mosquizto.Dto.response.CollectionResponse;
import com.example.mosquizto.Dto.response.CollectionSummaryResponse;
import com.example.mosquizto.Dto.response.FolderResponse;
import com.example.mosquizto.Network.itf.FolderApi;
import com.example.mosquizto.R;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class FolderDetailActivity extends AppCompatActivity {

    private Long folderId;
    private String folderName;
    private String folderDescription;

    private TextView tvToolbarTitle;
    private ImageView btnBack, btnAddHeader, btnMore;
    private LinearLayout layoutEmpty;
    private Button btnBigAddMaterials;
    private RecyclerView rvFolderCollections;

    private RecentAdapter adapter;

    @Inject
    FolderApi folderApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder_detail);

        folderId = getIntent().getLongExtra("FOLDER_ID", -1);
        folderName = getIntent().getStringExtra("FOLDER_TITLE");

        initViews();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFolderDetails();
    }

    private void initViews() {
        tvToolbarTitle = findViewById(R.id.tvToolbarTitle);
        btnBack = findViewById(R.id.btnBack);
        btnAddHeader = findViewById(R.id.btnAddHeader);
        btnMore = findViewById(R.id.btnMore);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        btnBigAddMaterials = findViewById(R.id.btnBigAddMaterials);
        rvFolderCollections = findViewById(R.id.rvFolderCollections);

        tvToolbarTitle.setText(folderName != null ? folderName : "Thư mục");
        rvFolderCollections.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        View.OnClickListener addMaterialsAction = v -> {
            if (folderId == null || folderId == -1) {
                Toast.makeText(this, "Folder không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(this, AddStudyMaterialsActivity.class);
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
                    showEditFolderDialog();
                }

                @Override
                public void onShareFolder() {
                    Toast.makeText(FolderDetailActivity.this, "Chức năng chia sẻ đang phát triển", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onDeleteFolder() {
                    showDeleteConfirmDialog();
                }
            });
            bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
        });
    }

    private void loadFolderDetails() {
        if (folderId == null || folderId == -1) return;

        folderApi.getDetailFolder(folderId).enqueue(new Callback<ApiResponse<FolderResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<FolderResponse>> call, Response<ApiResponse<FolderResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    FolderResponse data = response.body().getData();
                    folderName = data.getName();
                    folderDescription = data.getDescription();
                    tvToolbarTitle.setText(folderName);
                    showCollections(data.getCollections());
                } else {
                    Toast.makeText(FolderDetailActivity.this, "Không thể tải thư mục", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<FolderResponse>> call, Throwable t) {
                Toast.makeText(FolderDetailActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCollections(List<CollectionSummaryResponse> summaries) {
        if (summaries == null || summaries.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvFolderCollections.setVisibility(View.GONE);
            return;
        }

        layoutEmpty.setVisibility(View.GONE);
        rvFolderCollections.setVisibility(View.VISIBLE);

        adapter = new RecentAdapter(mapCollectionSummaries(summaries),
                item -> {
                    Intent intent = new Intent(this, StudySetDetailActivity.class);
                    intent.putExtra("COLLECTION_ID", item.getId());
                    intent.putExtra("COLLECTION_TITLE", item.getTitle());
                    startActivity(intent);
                },
                (item, position) -> {
                    RemoveFromFolderBottomSheet removeDialog = new RemoveFromFolderBottomSheet(item.getTitle(), () ->
                            removeCollectionFromFolder(item.getId(), position));
                    removeDialog.show(getSupportFragmentManager(), removeDialog.getTag());
                }
        );
        rvFolderCollections.setAdapter(adapter);
    }

    private void showEditFolderDialog() {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (24 * getResources().getDisplayMetrics().density);
        form.setPadding(padding, 8, padding, 0);

        EditText etName = new EditText(this);
        etName.setHint("Tên thư mục");
        etName.setText(folderName);
        form.addView(etName);

        EditText etDescription = new EditText(this);
        etDescription.setHint("Mô tả");
        etDescription.setText(folderDescription);
        form.addView(etDescription);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Chỉnh sửa thư mục")
                .setView(form)
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Lưu", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String description = etDescription.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tên thư mục", Toast.LENGTH_SHORT).show();
            } else {
                updateFolder(name, description, dialog);
            }
        }));
        dialog.show();
    }

    private void updateFolder(String name, String description, AlertDialog dialog) {
        UpdateFolderRequest request = new UpdateFolderRequest(name, description);
        folderApi.updateFolder(folderId, request).enqueue(new Callback<ApiResponse<FolderResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<FolderResponse>> call, Response<ApiResponse<FolderResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    FolderResponse folder = response.body().getData();
                    folderName = folder.getName();
                    folderDescription = folder.getDescription();
                    tvToolbarTitle.setText(folderName);
                    Toast.makeText(FolderDetailActivity.this, "Đã cập nhật thư mục", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } else {
                    Toast.makeText(FolderDetailActivity.this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<FolderResponse>> call, Throwable t) {
                Toast.makeText(FolderDetailActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Xóa thư mục?")
                .setMessage("Các học phần sẽ được gỡ khỏi thư mục này.")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (dialog, which) -> deleteFolder())
                .show();
    }

    private void deleteFolder() {
        folderApi.deleteFolder(folderId).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(FolderDetailActivity.this, "Đã xóa thư mục", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(FolderDetailActivity.this, "Xóa thư mục thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                Toast.makeText(FolderDetailActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeCollectionFromFolder(Integer collectionId, int position) {
        folderApi.removeCollectionFromFolder(folderId, collectionId).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(FolderDetailActivity.this, "Đã xóa khỏi thư mục", Toast.LENGTH_SHORT).show();
                    if (adapter != null) {
                        adapter.removeItem(position);
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

    private List<CollectionResponse> mapCollectionSummaries(List<CollectionSummaryResponse> summaries) {
        List<CollectionResponse> collections = new ArrayList<>();
        for (CollectionSummaryResponse summary : summaries) {
            collections.add(new CollectionResponse(
                    summary.getId(),
                    summary.getTitle(),
                    null,
                    null,
                    null,
                    null,
                    null
            ));
        }
        return collections;
    }
}
