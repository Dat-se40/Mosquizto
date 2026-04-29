package com.example.mosquizto.Activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
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
import com.example.mosquizto.Network.itf.CollectionApi;
import com.example.mosquizto.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class StudySetDetailActivity extends AppCompatActivity {

    private int collectionId = -1;
    private ViewPager2 viewPagerFlashcards;
    private RecyclerView rvTerms;
    private ExtendedFloatingActionButton fabStudy;
    private NestedScrollView nestedScrollView;
    private TextView tvSetTitle;
    private TermListAdapter termListAdapter;

    @Inject
    CollectionApi collectionApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            setContentView(R.layout.activity_study_set_detail);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi hiển thị giao diện!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Lấy ID an toàn
        if (getIntent() != null) {
            collectionId = getIntent().getIntExtra("COLLECTION_ID", -1);
            String title = getIntent().getStringExtra("COLLECTION_TITLE");
            
            initViews();
            
            if (title != null && tvSetTitle != null) {
                tvSetTitle.setText(title);
            }
            
            setupListeners();
            
            if (collectionId != -1) {
                fetchCollectionData();
            } else {
                Toast.makeText(this, "ID bộ thẻ không hợp lệ!", Toast.LENGTH_SHORT).show();
            }
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

        if (fabStudy != null) fabStudy.setOnClickListener(v -> showStudyModesBottomSheet());
        
        View btnSave = findViewById(R.id.btnSave);
        if (btnSave != null) btnSave.setOnClickListener(v -> showSaveToFolderDialog());
        
        View btnOptions = findViewById(R.id.btnOptions);
        if (btnOptions != null) btnOptions.setOnClickListener(v -> showOptionsBottomSheet());
    }

    private void fetchCollectionData() {
        if (collectionApi == null) return;
        
        collectionApi.getCollectionItemById(collectionId).enqueue(new Callback<ApiResponse<List<CollectionItemResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<CollectionItemResponse>>> call, Response<ApiResponse<List<CollectionItemResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<CollectionItemResponse> items = response.body().getData();
                    if (items != null) {
                        setupAdapters(items);
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<CollectionItemResponse>>> call, Throwable t) {
                Toast.makeText(StudySetDetailActivity.this, "Lỗi kết nối dữ liệu!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupAdapters(List<CollectionItemResponse> items) {
        if (viewPagerFlashcards != null) {
            FlashcardCarouselAdapter pagerAdapter = new FlashcardCarouselAdapter(this, items);
            viewPagerFlashcards.setAdapter(pagerAdapter);
        }
        if (termListAdapter != null) {
            termListAdapter.updateData(items);
        }
    }

    private void showOptionsBottomSheet() {
        try {
            BottomSheetDialog dialog = new BottomSheetDialog(this);
            View view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_options, null);
            dialog.setContentView(view);
            dialog.show();
        } catch (Exception e) {
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
        } catch (Exception e) {
            Toast.makeText(this, "Chưa có layout Save Folder!", Toast.LENGTH_SHORT).show();
        }
    }

    private void showStudyModesBottomSheet() {
        try {
            BottomSheetDialog dialog = new BottomSheetDialog(this);
            View view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_study_modes, null);
            dialog.setContentView(view);
            dialog.show();
        } catch (Exception e) {
            Toast.makeText(this, "Chưa có layout Study Modes!", Toast.LENGTH_SHORT).show();
        }
    }
}
