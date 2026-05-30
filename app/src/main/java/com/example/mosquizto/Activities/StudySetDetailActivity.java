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
import com.example.mosquizto.Dto.request.CollectionReportRequest;
import com.example.mosquizto.Dto.response.ApiResponse;
import com.example.mosquizto.Dto.response.CollectionItemResponse;
import com.example.mosquizto.Dto.response.CollectionReportResponse;
import com.example.mosquizto.Dto.response.CollectionResponse;
import com.example.mosquizto.Dto.response.FolderResponse;
import com.example.mosquizto.Dto.response.FolderSummaryResponse;
import com.example.mosquizto.Dto.response.StarredCollectionItemResponse;
import com.example.mosquizto.Models.TermItemUIModel;
import com.example.mosquizto.Network.itf.CollectionApi;
import com.example.mosquizto.Network.itf.FolderApi;
import com.example.mosquizto.R;
import com.example.mosquizto.Services.SessionManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.tabs.TabLayout;

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
    private String author;
    private ViewPager2 viewPagerFlashcards;
    private RecyclerView rvTerms;
    private ExtendedFloatingActionButton fabStudy;
    private NestedScrollView nestedScrollView;
    private TextView tvSetTitle;

    private TermListAdapter termListAdapter;

    private List<CollectionItemResponse> originalItems = new ArrayList<>();
    private List<TermItemUIModel> uiItems = new ArrayList<>();

    @Inject
    CollectionApi collectionApi;

    @Inject
    FolderApi folderApi;

    @Inject
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: START");
        try {
            Class<?> valueAnimatorClass = Class.forName("android.animation.ValueAnimator");
            Method setDurationScaleMethod = valueAnimatorClass.getDeclaredMethod("setDurationScale", float.class);
            setDurationScaleMethod.invoke(null, 1.0f);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            setContentView(R.layout.activity_study_set_detail);
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi hiển thị giao diện!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        try {
            if (getIntent() != null) {
                collectionId = getIntent().getIntExtra("COLLECTION_ID", -1);
                String title = getIntent().getStringExtra("COLLECTION_TITLE");
                author = getIntent().getStringExtra("AUTHOR");

                if (author == null && collectionId != -1) {
                    author = sessionManager.getCollectionAuthor(collectionId);
                }
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
                    Toast.makeText(this, "ID bộ thẻ không hợp lệ!", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "onCreate: CRITICAL ERROR: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi khởi tạo Activity!", Toast.LENGTH_SHORT).show();
        }
    }

    private void initViews() {
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

            TabLayout tabLayout = findViewById(R.id.tabLayoutTerms);
            if (tabLayout != null) {
                tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        int position = tab.getPosition();
                        if (uiItems != null) {
                            if (position == 0) {
                                termListAdapter.updateData(uiItems);
                            } else if (position == 1) {
                                List<TermItemUIModel> starredItems = new ArrayList<>();
                                for (TermItemUIModel item : uiItems) {
                                    if (item.isStarred()) {
                                        starredItems.add(item);
                                    }
                                }
                                termListAdapter.updateData(starredItems);
                            }
                        }
                    }

                    @Override public void onTabUnselected(TabLayout.Tab tab) {}
                    @Override public void onTabReselected(TabLayout.Tab tab) {}
                });
            }

        } catch (Exception e) {
            Log.e(TAG, "setupListeners: FAILED: " + e.getMessage(), e);
        }
    }

    private void fetchCollectionData() {
        if (collectionApi == null) return;

        collectionApi.getCollectionItemById(collectionId).enqueue(new Callback<ApiResponse<List<CollectionItemResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<CollectionItemResponse>>> call, Response<ApiResponse<List<CollectionItemResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    originalItems = response.body().getData();
                    if (originalItems == null) originalItems = new ArrayList<>();
                    fetchStarredItemInCollection();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<CollectionItemResponse>>> call, Throwable t) {
                Toast.makeText(StudySetDetailActivity.this, "Lỗi kết nối dữ liệu!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchStarredItemInCollection() {
        if (collectionApi == null) return;

        collectionApi.getStarredCollections().enqueue(new Callback<ApiResponse<List<StarredCollectionItemResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<StarredCollectionItemResponse>>> call, Response<ApiResponse<List<StarredCollectionItemResponse>>> response) {
                List<StarredCollectionItemResponse> starredItems = new ArrayList<>();
                if (response.isSuccessful() && response.body() != null) {
                    starredItems = response.body().getData();
                }
                mapDataAndSetupUI(starredItems);
            }

            @Override
            public void onFailure(Call<ApiResponse<List<StarredCollectionItemResponse>>> call, Throwable t) {
                mapDataAndSetupUI(new ArrayList<>());
            }
        });
    }

    private void mapDataAndSetupUI(List<StarredCollectionItemResponse> starredItems) {
        uiItems.clear();
        for (CollectionItemResponse item : originalItems) {
            boolean isStarred = false;
            if (starredItems != null) {
                for (StarredCollectionItemResponse starred : starredItems) {
                    if (item.getId().equals(starred.getItemId())) {
                        isStarred = true;
                        break;
                    }
                }
            }
            uiItems.add(new TermItemUIModel(item, isStarred));
        }

        if (viewPagerFlashcards != null) {
            FlashcardCarouselAdapter pagerAdapter = new FlashcardCarouselAdapter(this, originalItems);
            viewPagerFlashcards.setAdapter(pagerAdapter);
        }

        if (termListAdapter != null) {
            termListAdapter.updateData(uiItems);
            termListAdapter.SetOnDetailItemClickedListener(event -> {
                CollectionItemResponse clickedItem = event.item;
                boolean newStarState = !termListAdapter.isItemStarred(clickedItem.getId());

                try {
                    if (newStarState) StarItem(clickedItem.getId());
                    else UStarItem(clickedItem.getId());
                } catch (Exception e) {
                    Toast.makeText(StudySetDetailActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
                termListAdapter.updateStarState(clickedItem.getId(), newStarState);
                Toast.makeText(StudySetDetailActivity.this, newStarState ? "Đã đánh dấu sao" : "Đã bỏ đánh dấu", Toast.LENGTH_SHORT).show();
            });
        }
    }

    public void StarItem(Integer itemId) {
        collectionApi.starCollectionItem(itemId).enqueue(new Callback<ApiResponse<StarredCollectionItemResponse>>() {
            @Override public void onResponse(Call<ApiResponse<StarredCollectionItemResponse>> call, Response<ApiResponse<StarredCollectionItemResponse>> response) {}
            @Override public void onFailure(Call<ApiResponse<StarredCollectionItemResponse>> call, Throwable t) {}
        });
    }

    public void UStarItem(Integer itemId) {
        collectionApi.unstarCollectionItem(itemId).enqueue(new Callback<ApiResponse<Void>>() {
            @Override public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {}
            @Override public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {}
        });
    }

    private void showOptionsBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_options, null);

        View btnEdit = view.findViewById(R.id.btnEdit);
        View btnAddToFolder = view.findViewById(R.id.btnAddToFolder);
        View btnCopy = view.findViewById(R.id.btnMakeCopy);
        View btnReport = view.findViewById(R.id.btnReport);
        if (author != null && sessionManager.getCurrUser() != null) {
            boolean isOwner = author.equals(sessionManager.getCurrUser().getUsername());
            if (btnEdit != null) btnEdit.setVisibility(isOwner ? View.VISIBLE : View.GONE);
        }

        if (btnEdit != null) btnEdit.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(this, EditCollectionActivity.class);
            intent.putExtra("COLLECTION_ID", collectionId);
            startActivity(intent);
        });

        if (btnCopy != null) btnCopy.setOnClickListener(v -> {
            dialog.dismiss();
            if (originalItems == null || originalItems.isEmpty()) {
                Toast.makeText(this, "Đang tải dữ liệu, vui lòng thử lại sau", Toast.LENGTH_SHORT).show();
                return;
            }
            // Mở màn hình tạo mới với dữ liệu Copy
            Intent intent = new Intent(this, CreateCollectionActivity.class);
            intent.putExtra("COPY_TITLE", tvSetTitle != null ? tvSetTitle.getText().toString() : "");
            intent.putParcelableArrayListExtra("COPY_ITEMS", new ArrayList<>(originalItems));
            startActivity(intent);
        });

        if (btnAddToFolder != null) btnAddToFolder.setOnClickListener(v -> {
            dialog.dismiss();
            showSaveToFolderDialog();
        });
        if (btnReport != null) 
        {
            btnReport.setOnClickListener(v -> {
                dialog.dismiss();
                showReportDialog();
            });
        }
        dialog.setContentView(view);
        dialog.show();
    }

    private void showReportDialog() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View view = LayoutInflater.from(this).inflate(R.layout.dialog_report_collection, null);
            builder.setView(view);
            AlertDialog reportDialog = builder.create();

            if (reportDialog.getWindow() != null) {
                reportDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            }

            // Khởi tạo các view trong dialog
            android.widget.RadioGroup rgReasons = view.findViewById(R.id.rgReportReasons);
            android.widget.EditText edtDescription = view.findViewById(R.id.edtReportDescription);
            android.widget.Button btnCancel = view.findViewById(R.id.btnCancelReport);
            android.widget.Button btnSubmit = view.findViewById(R.id.btnSubmitReport);

            btnCancel.setOnClickListener(v -> reportDialog.dismiss());

            btnSubmit.setOnClickListener(v -> {
                // Lấy reason từ RadioGroup
                int selectedId = rgReasons.getCheckedRadioButtonId();
                if (selectedId == -1) {
                    Toast.makeText(this, "Vui lòng chọn lý do báo cáo!", Toast.LENGTH_SHORT).show();
                    return;
                }

                android.widget.RadioButton selectedRadioButton = view.findViewById(selectedId);
                String reason = selectedRadioButton.getText().toString();
                String description = edtDescription.getText().toString().trim();

                // Tạo request
                com.example.mosquizto.Dto.request.CollectionReportRequest request =
                        new com.example.mosquizto.Dto.request.CollectionReportRequest(reason, description);

                // Gọi API
                sendReport(request, reportDialog);
            });

            reportDialog.show();
        } catch (Exception e) {
            Log.e(TAG, "showReportDialog: FAILED", e);
        }
    }

    private void sendReport(CollectionReportRequest request, AlertDialog reportDialog) {
        if (collectionId == -1) return;
        collectionApi.reportCollection(collectionId, request).enqueue(new Callback<ApiResponse<CollectionReportResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<CollectionReportResponse>> call, Response<ApiResponse<CollectionReportResponse>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(StudySetDetailActivity.this, "Đã gửi báo cáo thành công. Cảm ơn bạn!", Toast.LENGTH_SHORT).show();
                    reportDialog.dismiss();
                } else {
                    Log.e(TAG, "reportCollection onFailure: ", new Exception(response.message()));
                    Toast.makeText(StudySetDetailActivity.this, "Gửi báo cáo thất bại, thử lại sau.", Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onFailure(Call<ApiResponse<CollectionReportResponse>> call, Throwable t) {
                Log.e(TAG, "reportCollection onFailure: ", t);
                Toast.makeText(StudySetDetailActivity.this, "Lỗi kết nối mạng!", Toast.LENGTH_SHORT).show();
            }

        });
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
                    for (FolderSummaryResponse folder : response.body().getData()) {
                        addFolderDialogText(layoutFolderList, folder.getName(), true, () -> saveCurrentSetToFolder(folder, dialog));
                    }
                }
            }
            @Override public void onFailure(Call<ApiResponse<List<FolderSummaryResponse>>> call, Throwable t) {}
        });
    }

    private void addFolderDialogText(LinearLayout parent, String text, boolean clickable, Runnable action) {
        TextView row = new TextView(this);
        row.setText(text);
        row.setTextSize(16);
        row.setTextColor(android.graphics.Color.parseColor(clickable ? "#282E3E" : "#586380"));
        row.setPadding(0, 18, 0, 18);
        if (clickable) row.setOnClickListener(v -> action.run());
        parent.addView(row);
    }

    private void saveCurrentSetToFolder(FolderSummaryResponse folder, AlertDialog dialog) {
        if (collectionId == -1) return;
        folderApi.addCollectionToFolder(folder.getId(), collectionId).enqueue(new Callback<ApiResponse<FolderResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<FolderResponse>> call, Response<ApiResponse<FolderResponse>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(StudySetDetailActivity.this, "Đã lưu vào " + folder.getName(), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            }
            @Override public void onFailure(Call<ApiResponse<FolderResponse>> call, Throwable t) {}
        });
    }

    private void showLearnModeBottomSheet() {
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

                    ArrayList<CollectionItemResponse> items = new ArrayList<>(originalItems);
                    intent.putParcelableArrayListExtra("ITEMS_LIST", items);
                    startActivity(intent);
                }
            });

            dialog.setContentView(view);
            dialog.show();
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi hiển thị Learn Mode!", Toast.LENGTH_SHORT).show();
        }
    }
}