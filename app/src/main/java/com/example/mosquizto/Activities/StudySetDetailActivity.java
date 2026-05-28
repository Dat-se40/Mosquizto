package com.example.mosquizto.Activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.mosquizto.Adapters.FlashcardCarouselAdapter;
import com.example.mosquizto.Adapters.TermListAdapter;
import com.example.mosquizto.Dto.response.ApiResponse;
import com.example.mosquizto.Dto.response.CollectionItemResponse;
import com.example.mosquizto.Dto.response.FolderResponse;
import com.example.mosquizto.Dto.response.FolderSummaryResponse;
import com.example.mosquizto.Network.itf.CollectionApi;
import com.example.mosquizto.Network.itf.FolderApi;
import com.example.mosquizto.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class StudySetDetailActivity extends AppCompatActivity {

    private static final String TAG = "StudySetDetailActivity";

    private int collectionId = -1;
    private ViewPager2 viewPagerFlashcards;
    private RecyclerView rvTerms;
    private ExtendedFloatingActionButton fabStudy;
    private NestedScrollView nestedScrollView;
    private TextView tvSetTitle;
    private TermListAdapter termListAdapter;

    @Inject
    CollectionApi collectionApi;

    @Inject
    FolderApi folderApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: START");
        try {
            Class<?> valueAnimatorClass = Class.forName("android.animation.ValueAnimator");
            Method setDurationScaleMethod = valueAnimatorClass.getDeclaredMethod("setDurationScale", float.class);
            setDurationScaleMethod.invoke(null, 1.0f);
        } catch (Exception e) {
            e.printStackTrace(); // Có thể bị chặn trên các bản Android rất mới do hạn chế Reflection
        }
        try {
            setContentView(R.layout.activity_study_set_detail);
            Log.d(TAG, "onCreate: setContentView DONE");
        } catch (Exception e) {
            Log.e(TAG, "onCreate: setContentView FAILED: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi hiển thị giao diện!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        try {
            // Lấy ID an toàn
            if (getIntent() != null) {
                collectionId = getIntent().getIntExtra("COLLECTION_ID", -1);
                String title = getIntent().getStringExtra("COLLECTION_TITLE");
                Log.d(TAG, "onCreate: Intent data [ID: " + collectionId + ", Title: " + title + "]");
                
                initViews();
                
                if (title != null && tvSetTitle != null) {
                    tvSetTitle.setText(title);
                }
                
                setupListeners();
                
                if (collectionId != -1) {
                    getWindow().getDecorView().post(() -> {
                        fetchCollectionData();
                    });

                } else {
                    Log.w(TAG, "onCreate: collectionId is -1");
                    Toast.makeText(this, "ID bộ thẻ không hợp lệ!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.w(TAG, "onCreate: Intent is null");
            }
        } catch (Exception e) {
            Log.e(TAG, "onCreate: CRITICAL ERROR: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi khởi tạo Activity!", Toast.LENGTH_SHORT).show();
        }
    }

    private void initViews() {
        Log.d(TAG, "initViews: START");
        try {
            Toolbar toolbar = findViewById(R.id.toolbar);
            if (toolbar != null) {
                setSupportActionBar(toolbar);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setDisplayShowTitleEnabled(false);
                }
                toolbar.setNavigationOnClickListener(v -> finish());
            }

            viewPagerFlashcards = findViewById(R.id.viewPagerFlashcards);
            rvTerms = findViewById(R.id.rvTerms);
            rvTerms.setItemAnimator(null);
            if (rvTerms != null) {
                rvTerms.setLayoutManager(new LinearLayoutManager(this));
                termListAdapter = new TermListAdapter(new ArrayList<>());
                rvTerms.setAdapter(termListAdapter);

            } else {
                Log.w(TAG, "initViews: rvTerms is NULL");
            }

            fabStudy = findViewById(R.id.fabStudy);
            nestedScrollView = findViewById(R.id.nestedScrollView);
            tvSetTitle = findViewById(R.id.tvSetTitle);
            Log.d(TAG, "initViews: DONE");
        } catch (Exception e) {
            Log.e(TAG, "initViews: FAILED: " + e.getMessage(), e);
        }
    }

    private void setupListeners() {
        Log.d(TAG, "setupListeners: START");
        try {
            if (nestedScrollView != null && viewPagerFlashcards != null && fabStudy != null) {
                nestedScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                    if (scrollY > viewPagerFlashcards.getBottom()) {
                        fabStudy.show();
                    } else {
                        fabStudy.hide();
                    }
                });
            }

            if (fabStudy != null) fabStudy.setOnClickListener(v -> showLearnModeBottomSheet());
            
            View btnSave = findViewById(R.id.btnSave);
            if (btnSave != null) btnSave.setOnClickListener(v -> showSaveToFolderDialog());
            
            View btnOptions = findViewById(R.id.btnOptions);
            if (btnOptions != null) btnOptions.setOnClickListener(v -> showOptionsBottomSheet());
            Log.d(TAG, "setupListeners: DONE");
        } catch (Exception e) {
            Log.e(TAG, "setupListeners: FAILED: " + e.getMessage(), e);
        }
    }

    private void fetchCollectionData() {
        Log.d(TAG, "fetchCollectionData: START for ID " + collectionId);
        if (collectionApi == null) {
            Log.e(TAG, "fetchCollectionData: collectionApi is NULL (Injection failed?)");
            return;
        }
        
        try {
            collectionApi.getCollectionItemById(collectionId).enqueue(new Callback<ApiResponse<List<CollectionItemResponse>>>() {
                @Override
                public void onResponse(Call<ApiResponse<List<CollectionItemResponse>>> call, Response<ApiResponse<List<CollectionItemResponse>>> response) {
                    Log.d(TAG, "fetchCollectionData: onResponse code " + response.code());
                    if (response.isSuccessful() && response.body() != null) {
                        List<CollectionItemResponse> items = response.body().getData();
                        if (items != null) {
                            Log.d(TAG, "fetchCollectionData: Received " + items.size() + " items");
                            setupAdapters(items);
                        } else {
                            Log.w(TAG, "fetchCollectionData: Response data is NULL");
                        }
                    } else {
                        Log.e(TAG, "fetchCollectionData: Response not successful or body null");
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<List<CollectionItemResponse>>> call, Throwable t) {
                    Log.e(TAG, "fetchCollectionData: onFailure: " + t.getMessage(), t);
                    Toast.makeText(StudySetDetailActivity.this, "Lỗi kết nối dữ liệu!", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "fetchCollectionData: EXCEPTION: " + e.getMessage(), e);
        }
    }

    private void setupAdapters(List<CollectionItemResponse> items) {
        Log.d(TAG, "setupAdapters: START");
        try {
            if (viewPagerFlashcards != null) {
                FlashcardCarouselAdapter pagerAdapter = new FlashcardCarouselAdapter(this, items);
                viewPagerFlashcards.setAdapter(pagerAdapter);
                Log.d(TAG, "setupAdapters: ViewPager2 adapter set");
            }
            if (termListAdapter != null) {
                termListAdapter.updateData(items);
                Log.d(TAG, "setupAdapters: RecyclerView adapter updated");
            }
            Log.d(TAG, "setupAdapters: DONE");
        } catch (Exception e) {
            Log.e(TAG, "setupAdapters: FAILED: " + e.getMessage(), e);
        }
    }

    private void showOptionsBottomSheet() {
        try {
            BottomSheetDialog dialog = new BottomSheetDialog(this);
            View view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_options, null);
            dialog.setContentView(view);
            dialog.show();
        } catch (Exception e) {
            Log.e(TAG, "showOptionsBottomSheet: FAILED", e);
            Toast.makeText(this, "Chưa có layout Options!", Toast.LENGTH_SHORT).show();
        }
    }

    private void showSaveToFolderDialog() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View view = LayoutInflater.from(this).inflate(R.layout.dialog_save_folder, null);
            builder.setView(view);
            AlertDialog dialog = builder.create();
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            }
            view.findViewById(R.id.btnDone).setOnClickListener(v -> dialog.dismiss());
            dialog.show();
            loadFoldersForSaveDialog(view, dialog);
        } catch (Exception e) {
            Log.e(TAG, "showSaveToFolderDialog: FAILED", e);
            Toast.makeText(this, "Chưa có layout Save Folder!", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadFoldersForSaveDialog(View view, AlertDialog dialog) {
        LinearLayout layoutFolderList = view.findViewById(R.id.layoutFolderList);
        layoutFolderList.removeAllViews();
        addFolderDialogText(layoutFolderList, "Đang tải thư mục...", false, null);

        folderApi.getAllOwnFolders().enqueue(new Callback<ApiResponse<List<FolderSummaryResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<FolderSummaryResponse>>> call, Response<ApiResponse<List<FolderSummaryResponse>>> response) {
                layoutFolderList.removeAllViews();
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<FolderSummaryResponse> folders = response.body().getData();
                    if (folders.isEmpty()) {
                        addFolderDialogText(layoutFolderList, "Bạn chưa có thư mục nào", false, null);
                    } else {
                        for (FolderSummaryResponse folder : folders) {
                            addFolderDialogText(layoutFolderList, folder.getName(), true, () -> saveCurrentSetToFolder(folder, dialog));
                        }
                    }
                } else {
                    addFolderDialogText(layoutFolderList, "Không thể tải danh sách thư mục", false, null);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<FolderSummaryResponse>>> call, Throwable t) {
                layoutFolderList.removeAllViews();
                addFolderDialogText(layoutFolderList, "Lỗi kết nối", false, null);
            }
        });
    }

    private void addFolderDialogText(LinearLayout parent, String text, boolean clickable, Runnable action) {
        TextView row = new TextView(this);
        row.setText(text);
        row.setTextSize(16);
        row.setTextColor(android.graphics.Color.parseColor(clickable ? "#282E3E" : "#586380"));
        row.setPadding(0, 18, 0, 18);
        if (clickable) {
            row.setOnClickListener(v -> action.run());
        }
        parent.addView(row);
    }

    private void saveCurrentSetToFolder(FolderSummaryResponse folder, AlertDialog dialog) {
        if (collectionId == -1) {
            Toast.makeText(this, "ID bộ thẻ không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        folderApi.addCollectionToFolder(folder.getId(), collectionId).enqueue(new Callback<ApiResponse<FolderResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<FolderResponse>> call, Response<ApiResponse<FolderResponse>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(StudySetDetailActivity.this, "Đã lưu vào " + folder.getName(), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } else {
                    Toast.makeText(StudySetDetailActivity.this, "Lưu vào thư mục thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<FolderResponse>> call, Throwable t) {
                Toast.makeText(StudySetDetailActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLearnModeBottomSheet(){
        try {
            BottomSheetDialog dialog = new BottomSheetDialog(this);
            View view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_learn_mode, null);

            MaterialCardView cardTest = view.findViewById(R.id.cardModeTest);
            MaterialCardView cardMemory = view.findViewById(R.id.cardModeMemory);
            Button btnStartLearn = view.findViewById(R.id.btnStartLearn);

            final String[] selectedMode = {"LEARN"};

            Runnable updateSelectionUI = () -> {
                try {
                    if (selectedMode[0].equals("LEARN")) {
                        cardMemory.setStrokeColor(android.graphics.Color.parseColor("#4255FF"));
                        cardMemory.setStrokeWidth(4);
                        cardMemory.getChildAt(0).setBackgroundColor(android.graphics.Color.parseColor("#E8EAFF"));

                        cardTest.setStrokeColor(android.graphics.Color.parseColor("#E0E0E0"));
                        cardTest.setStrokeWidth(2);
                        cardTest.getChildAt(0).setBackgroundColor(android.graphics.Color.TRANSPARENT);
                    } else {
                        cardTest.setStrokeColor(android.graphics.Color.parseColor("#4255FF"));
                        cardTest.setStrokeWidth(4);
                        cardTest.getChildAt(0).setBackgroundColor(android.graphics.Color.parseColor("#E8EAFF"));

                        cardMemory.setStrokeColor(android.graphics.Color.parseColor("#E0E0E0"));
                        cardMemory.setStrokeWidth(2);
                        cardMemory.getChildAt(0).setBackgroundColor(android.graphics.Color.TRANSPARENT);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "updateSelectionUI: FAILED", e);
                }
            };

            cardTest.setOnClickListener(v -> {
                selectedMode[0] = "TEST";
                updateSelectionUI.run();
            });

            cardMemory.setOnClickListener(v -> {
                selectedMode[0] = "LEARN";
                updateSelectionUI.run();
            });

            btnStartLearn.setOnClickListener(v -> {
                dialog.dismiss();
                if (selectedMode[0] != null || !selectedMode[0].isEmpty()) {
                    Intent intent = new Intent(this, MemoryGameActivity.class);
                    intent.putExtra("COLLECTION_ID", collectionId);
                    intent.putExtra("GAME_MODE", selectedMode[0]);
                    // Lấy List từ adapter và truyền đi
                    if (termListAdapter != null) {
                        ArrayList<CollectionItemResponse> items = new ArrayList<>(termListAdapter.getItems());
                        intent.putParcelableArrayListExtra("ITEMS_LIST", items);
                        Log.d(TAG, "Starting MemoryGameActivity with " + (items != null ? items.size() : 0) + " items");
                    }
                    
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Test Mode đang phát triển", Toast.LENGTH_SHORT).show();
                }
            });

            dialog.setContentView(view);
            dialog.show();
        } catch (Exception e) {
            Log.e(TAG, "showLearnModeBottomSheet: FAILED", e);
            Toast.makeText(this, "Lỗi hiển thị Learn Mode!", Toast.LENGTH_SHORT).show();
        }
    }
}
