package com.example.mosquizto.Activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
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
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.io.Serializable;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: START");
        
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
        } catch (Exception e) {
            Log.e(TAG, "showSaveToFolderDialog: FAILED", e);
            Toast.makeText(this, "Chưa có layout Save Folder!", Toast.LENGTH_SHORT).show();
        }
    }


    private void showLearnModeBottomSheet(){
        try {
            BottomSheetDialog dialog = new BottomSheetDialog(this);
            View view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_learn_mode, null);

            MaterialCardView cardTest = view.findViewById(R.id.cardModeTest);
            MaterialCardView cardMemory = view.findViewById(R.id.cardModeMemory);
            Button btnStartLearn = view.findViewById(R.id.btnStartLearn);

            final String[] selectedMode = {"MEMORY"};

            Runnable updateSelectionUI = () -> {
                try {
                    if (selectedMode[0].equals("MEMORY")) {
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
                selectedMode[0] = "MEMORY";
                updateSelectionUI.run();
            });

            btnStartLearn.setOnClickListener(v -> {
                dialog.dismiss();
                if (selectedMode[0].equals("MEMORY")) {
                    Intent intent = new Intent(this, MemoryGameActivity.class);
                    intent.putExtra("COLLECTION_ID", collectionId);
                    
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
