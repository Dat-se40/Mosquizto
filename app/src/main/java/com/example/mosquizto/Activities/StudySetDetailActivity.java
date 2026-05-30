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
import com.example.mosquizto.Dto.response.StarredCollectionItemResponse;
// Nhớ import file Model mới tạo
import com.example.mosquizto.Models.TermItemUIModel;
import com.example.mosquizto.Network.itf.CollectionApi;
import com.example.mosquizto.Network.itf.FolderApi;
import com.example.mosquizto.R;
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
    private ViewPager2 viewPagerFlashcards;
    private RecyclerView rvTerms;
    private ExtendedFloatingActionButton fabStudy;
    private NestedScrollView nestedScrollView;
    private TextView tvSetTitle;

    private TermListAdapter termListAdapter;

    // Tách riêng 2 list: 1 list gốc để truyền đi nơi khác, 1 list UI để hiện lên RecyclerView
    private List<CollectionItemResponse> originalItems = new ArrayList<>();
    private List<TermItemUIModel> uiItems = new ArrayList<>();

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

                initViews();

                if (title != null && tvSetTitle != null) {
                    tvSetTitle.setText(title);
                }

                setupListeners();

                if (collectionId != -1) {
                    getWindow().getDecorView().post(() -> {
                        fetchCollectionData(); // Gọi hàm gốc, nó sẽ tự chain sang lấy Star
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
        } catch (Exception e) {
            Log.e(TAG, "initViews: FAILED: " + e.getMessage(), e);
        }
    }

    private void setupListeners() {
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
            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    int position = tab.getPosition();
                    if (uiItems != null)
                    {
                        if (position == 0 )
                        {
                            termListAdapter.updateData(uiItems);
                        }else if  (position == 1)
                        {
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

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {

                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {

                }
            });

        }catch (Exception e) {
            Log.e(TAG, "setupListeners: FAILED: " + e.getMessage(), e);
        }
    }

    // BƯỚC 1: LẤY DATA THẺ TỪ SERVER
    private void fetchCollectionData() {
        if (collectionApi == null) return;

        collectionApi.getCollectionItemById(collectionId).enqueue(new Callback<ApiResponse<List<CollectionItemResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<CollectionItemResponse>>> call, Response<ApiResponse<List<CollectionItemResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    originalItems = response.body().getData();
                    if (originalItems == null) originalItems = new ArrayList<>();

                    // Lấy xong thẻ thì gọi tiếp API lấy sao
                    fetchStarredItemInCollection();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<CollectionItemResponse>>> call, Throwable t) {
                Toast.makeText(StudySetDetailActivity.this, "Lỗi kết nối dữ liệu!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // BƯỚC 2: LẤY DATA SAO VÀ GỘP LẠI
    private void fetchStarredItemInCollection() {
        if (collectionApi == null) return;

        collectionApi.getStarredCollections().enqueue(new Callback<ApiResponse<List<StarredCollectionItemResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<StarredCollectionItemResponse>>> call, Response<ApiResponse<List<StarredCollectionItemResponse>>> response) {
                List<StarredCollectionItemResponse> starredItems = new ArrayList<>();
                if (response.isSuccessful() && response.body() != null) {
                    starredItems = response.body().getData();
                }
                // Nếu thành công thì mang đi map data
                mapDataAndSetupUI(starredItems);
            }

            @Override
            public void onFailure(Call<ApiResponse<List<StarredCollectionItemResponse>>> call, Throwable t) {
                // Nếu lỗi lấy sao thì coi như không có thẻ nào được sao
                mapDataAndSetupUI(new ArrayList<>());
            }
        });
    }

    // BƯỚC 3: XỬ LÝ DỮ LIỆU ĐỔ RA GIAO DIỆN
    private void mapDataAndSetupUI(List<StarredCollectionItemResponse> starredItems) {
        uiItems.clear();

        // Khớp ID để tạo ra UI Model
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

        // Cài đặt ViewPager (Vẫn xài list gốc không sao)
        if (viewPagerFlashcards != null) {
            FlashcardCarouselAdapter pagerAdapter = new FlashcardCarouselAdapter(this, originalItems);
            viewPagerFlashcards.setAdapter(pagerAdapter);
        }

        // Cài đặt RecyclerView (Xài list UI Model)
        if (termListAdapter != null) {
            termListAdapter.updateData(uiItems);

            // Lắng nghe sự kiện click sao
            termListAdapter.SetOnDetailItemClickedListener(event -> {
                CollectionItemResponse clickedItem = event.item;
                boolean newStarState = !termListAdapter.isItemStarred(clickedItem.getId());


                try {
                    if (newStarState) StarItem(clickedItem.getId());
                    else UStarItem(clickedItem.getId());
                }catch (Exception e)
                {
                    Toast.makeText(StudySetDetailActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
                termListAdapter.updateStarState(clickedItem.getId(), newStarState);

                Toast.makeText(StudySetDetailActivity.this, newStarState ? "Đã đánh dấu sao" : "Đã bỏ đánh dấu", Toast.LENGTH_SHORT).show();
            });
        }
    }
    public void StarItem(Integer itemId) {
        collectionApi.starCollectionItem(itemId).enqueue(new Callback<ApiResponse<StarredCollectionItemResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<StarredCollectionItemResponse>> call, Response<ApiResponse<StarredCollectionItemResponse>> response) {

            }

            @Override
            public void onFailure(Call<ApiResponse<StarredCollectionItemResponse>> call, Throwable t) {

            }
        });
    }
    public void UStarItem(Integer itemId) {
        collectionApi.unstarCollectionItem(itemId).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {

            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {

            }
        }) ;
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
        if (collectionId == -1) return;

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

                    // Truyền thẳng originalItems sang Activity Game
                    ArrayList<CollectionItemResponse> items = new ArrayList<>(originalItems);
                    intent.putParcelableArrayListExtra("ITEMS_LIST", items);
                    Log.d(TAG, "Starting MemoryGameActivity with " + items.size() + " items");

                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Test Mode đang phát triển", Toast.LENGTH_SHORT).show();
                }
            });

            dialog.setContentView(view);
            dialog.show();
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi hiển thị Learn Mode!", Toast.LENGTH_SHORT).show();
        }
    }
}