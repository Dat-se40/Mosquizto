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
import com.example.mosquizto.Dto.response.CollectionResponse;
import com.example.mosquizto.Dto.response.FolderResponse;
import com.example.mosquizto.Dto.response.FolderSummaryResponse;
import com.example.mosquizto.Network.itf.CollectionApi;
import com.example.mosquizto.Network.itf.FolderApi;
import com.example.mosquizto.R;
import com.example.mosquizto.Services.SessionManager;
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
    private String author;
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
            Log.e(TAG, "onCreate: setContentView FAILED: " + e.getMessage(), e);
            finish();
            return;
        }

        if (getIntent() != null) {
            collectionId = getIntent().getIntExtra("COLLECTION_ID", -1);
            String title = getIntent().getStringExtra("COLLECTION_TITLE");
            author = getIntent().getStringExtra("AUTHOR");
            
            if (author == null && collectionId != -1) {
                author = sessionManager.getCollectionAuthor(collectionId);
            }
            
            initViews();
            if (title != null && tvSetTitle != null) tvSetTitle.setText(title);
            setupListeners();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (collectionId != -1) {
            fetchCollectionData();
        }
    }

    private void initViews() {
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
    }

    private void setupListeners() {
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
    }

    private void fetchCollectionData() {
        if (collectionApi == null) return;

        collectionApi.getCollectionById(collectionId).enqueue(new Callback<ApiResponse<CollectionResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<CollectionResponse>> call, Response<ApiResponse<CollectionResponse>> response) {
                    if (response != null && response.body() != null && response.body().getData() != null)
                    {
                        CollectionResponse collection = response.body().getData();
                        if (collection != null) {
                            tvSetTitle.setText(collection.getTitle());
                        }
                    }
            }

            @Override
            public void onFailure(Call<ApiResponse<CollectionResponse>> call, Throwable t) {

            }
        });

        collectionApi.getCollectionItemById(collectionId).enqueue(new Callback<ApiResponse<List<CollectionItemResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<CollectionItemResponse>>> call, Response<ApiResponse<List<CollectionItemResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<CollectionItemResponse> items = response.body().getData();
                    if (items != null) setupAdapters(items);
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<List<CollectionItemResponse>>> call, Throwable t) {
                Toast.makeText(StudySetDetailActivity.this, "Lỗi kết nối dữ liệu!", Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void setupAdapters(List<CollectionItemResponse> items) {
        FlashcardCarouselAdapter pagerAdapter = new FlashcardCarouselAdapter(this, items);
        viewPagerFlashcards.setAdapter(pagerAdapter);
        termListAdapter.updateData(items);
    }

    private void showOptionsBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_options, null);
        
        View btnEdit = view.findViewById(R.id.btnEdit);
        View btnAddToFolder = view.findViewById(R.id.btnAddToFolder);

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

        if (btnAddToFolder != null) btnAddToFolder.setOnClickListener(v -> {
            dialog.dismiss();
            showSaveToFolderDialog();
        });

        dialog.setContentView(view);
        dialog.show();
    }

    private void showSaveToFolderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_save_folder, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        view.findViewById(R.id.btnDone).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
        loadFoldersForSaveDialog(view, dialog);
    }

    private void loadFoldersForSaveDialog(View view, AlertDialog dialog) {
        LinearLayout layoutFolderList = view.findViewById(R.id.layoutFolderList);
        layoutFolderList.removeAllViews();
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
            @Override
            public void onFailure(Call<ApiResponse<List<FolderSummaryResponse>>> call, Throwable t) {}
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
        folderApi.addCollectionToFolder(folder.getId(), collectionId).enqueue(new Callback<ApiResponse<FolderResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<FolderResponse>> call, Response<ApiResponse<FolderResponse>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(StudySetDetailActivity.this, "Đã lưu vào " + folder.getName(), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<FolderResponse>> call, Throwable t) {}
        });
    }

    private void showLearnModeBottomSheet(){
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_learn_mode, null);
        MaterialCardView cardMemory = view.findViewById(R.id.cardModeMemory);
        Button btnStartLearn = view.findViewById(R.id.btnStartLearn);

        btnStartLearn.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(this, MemoryGameActivity.class);
            intent.putExtra("COLLECTION_ID", collectionId);
            intent.putExtra("GAME_MODE", "LEARN");
            if (termListAdapter != null) {
                ArrayList<CollectionItemResponse> items = new ArrayList<>(termListAdapter.getItems());
                intent.putParcelableArrayListExtra("ITEMS_LIST", items);
            }
            startActivity(intent);
        });

        dialog.setContentView(view);
        dialog.show();
    }
}
