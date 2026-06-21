package com.example.mosquizto.Activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.mosquizto.Adapters.FlashcardCarouselAdapter;
import com.example.mosquizto.Adapters.TermListAdapter;
import com.example.mosquizto.Dialogs.ShareCollectionDialog;
import com.example.mosquizto.Dto.request.CollectionReportRequest;
import com.example.mosquizto.Dto.request.CreateFolderRequest;
import com.example.mosquizto.Dto.request.ShareCollectionRequest;
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
import com.example.mosquizto.Util.AvatarImageHelper;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
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
    private String authorImgUri;
    private ImageView imgAuthorAvatar;
    private ViewPager2 viewPagerFlashcards;
    private RecyclerView rvTerms;
    private Button fabStudy;
    private NestedScrollView nestedScrollView;
    private TextView tvSetTitle;
    private TermListAdapter termListAdapter;

    private List<CollectionItemResponse> originalItems = new ArrayList<>();
    private List<TermItemUIModel> uiItems = new ArrayList<>();
    private TextView tvAuthorName;

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
            Toast.makeText(this, getString(R.string.DialogError), Toast.LENGTH_LONG).show();
            Log.d(TAG, "onCreate: FAILED: " + e.getMessage());
            finish();
            return;
        }

        try {
            if (getIntent() != null) {
                // ĐỒNG BỘ: Sử dụng resource string làm key cho đồng bộ với GoToStudySetActivity
                collectionId = getIntent().getIntExtra(getString(R.string.intent_key_collection_id), -1);
                String title = getIntent().getStringExtra(getString(R.string.intent_key_collection_title));
                author = getIntent().getStringExtra(getString(R.string.intent_key_author));
                authorImgUri = getIntent().getStringExtra(getString(R.string.intent_key_author_img_uri));

                if (author == null && collectionId != -1) {
                    author = sessionManager.getCollectionAuthor(collectionId);
                }

                initViews();

                // Hiển thị ngay lập tức nếu dữ liệu Intent có sẵn và không rỗng
                if (title != null && !title.trim().isEmpty() && tvSetTitle != null) {
                    tvSetTitle.setText(title);
                }

                if (author != null && !author.trim().isEmpty() && tvAuthorName != null) {
                    tvAuthorName.setText(author);
                }
                displayAuthorAvatar(authorImgUri);

                setupListeners();

                if (collectionId != -1) {
                    getWindow().getDecorView().post(this::fetchCollectionData);

                    if (author == null || author.trim().isEmpty()
                            || title == null || title.trim().isEmpty()
                            || authorImgUri == null || authorImgUri.trim().isEmpty()) {
                        Log.d(TAG, "Missing metadata from intent, fetching via API...");
                        getWindow().getDecorView().post(this::fetchCollectionMetaData);
                    }
                } else {
                    Toast.makeText(this, R.string.InvalidId, Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "onCreate: CRITICAL ERROR: " + e.getMessage(), e);
            Toast.makeText(this, R.string.ActivityInitializationError, Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchCollectionMetaData() {
        collectionApi.getCollectionById(collectionId).enqueue(new Callback<ApiResponse<CollectionResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<CollectionResponse>> call, @NonNull Response<ApiResponse<CollectionResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    CollectionResponse data = response.body().getData();

                    author = data.getUserName();
                    authorImgUri = data.getAuthorImgUri();

                    if (tvSetTitle != null) {
                        tvSetTitle.setText(data.getTitle());
                    }
                    if (tvAuthorName != null && author != null) {
                        tvAuthorName.setText(author);
                    }
                    displayAuthorAvatar(authorImgUri);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<CollectionResponse>> call, @NonNull Throwable t) {
                Log.e(TAG, "fetchCollectionMetaData onFailure", t);
            }
        });
    }

    private void displayAuthorAvatar(String imgUri) {
        AvatarImageHelper.loadInto(imgAuthorAvatar, imgUri);
    }

    private void initViews() {
        try {
            Toolbar toolbar = findViewById(R.id.toolbar);
            if (toolbar != null) {
                setSupportActionBar(toolbar);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                    getSupportActionBar().setDisplayShowTitleEnabled(false);
                }
                toolbar.setNavigationOnClickListener(v -> finish());
            }
            tvAuthorName = findViewById(R.id.tvAuthorName);
            imgAuthorAvatar = findViewById(R.id.imgAuthorAvatar);
            ImageButton btnBack = findViewById(R.id.btnBack);
            if (btnBack != null) btnBack.setOnClickListener(v -> finish());

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
            tvSetTitle = findViewById(R.id.tvSetTitleHeader);
            Log.d(TAG, "initViews: DONE");
        } catch (Exception e) {
            Log.e(TAG, "initViews: FAILED: " + e.getMessage(), e);
        }
    }

    private void setupListeners() {
        try {
            if (tvAuthorName != null) {
                tvAuthorName.setOnClickListener(v -> {
                    if (author != null && !author.trim().isEmpty()) {
                        Intent intent = new Intent(StudySetDetailActivity.this, OtherUserProfileActivity.class);
                        // Truyền username qua OtherUserProfileActivity
                        intent.putExtra("intent_key_username", author);
                        // Do hiện tại biến author đang chứa username, ta có thể truyền tạm fullName cũng là author luôn
                        // (hoặc nếu có API lấy họ tên thật thì thay thế vào đây)
                        intent.putExtra("intent_key_full_name", author);

                        startActivity(intent);
                    }
                });
            }

            if (nestedScrollView != null && viewPagerFlashcards != null && fabStudy != null) {
                nestedScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                    if (scrollY > viewPagerFlashcards.getBottom()) {
                        fabStudy.setVisibility(View.VISIBLE);
                    } else {
                        fabStudy.setVisibility(View.GONE);
                    }
                });
            }

            if (fabStudy != null) fabStudy.setOnClickListener(v -> showLearnModeBottomSheet());

            View btnSave = findViewById(R.id.btnSave);
            if (btnSave != null) btnSave.setOnClickListener(v -> showSaveToFolderDialog());

            View btnOptions = findViewById(R.id.btnOptions);
            if (btnOptions != null) btnOptions.setOnClickListener(v -> showOptionsBottomSheet());

            View rowFlashcard = findViewById(R.id.cardFlashcardsGame);
            if (rowFlashcard != null) {
                rowFlashcard.setOnClickListener(v -> openFlashcardActivity());
            }

            View cardLearnGame = findViewById(R.id.cardLearnGame);
            if (cardLearnGame != null) {
                cardLearnGame.setOnClickListener(v -> showLearnModeBottomSheet());
            }

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
                                    if (item.isStarred()) starredItems.add(item);
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

    private void openFlashcardActivity() {
        if (originalItems == null || originalItems.isEmpty()) {
            Toast.makeText(this, "Chưa có dữ liệu thẻ!", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayList<String> terms       = new ArrayList<>();
        ArrayList<String> definitions = new ArrayList<>();

        for (CollectionItemResponse item : originalItems) {
            String term = item.getTerm();
            String def  = item.getDefinition();
            if (term != null) terms.add(term);
            if (def  != null) definitions.add(def);
        }

        Intent intent = new Intent(this, FlashcardActivity.class);
        intent.putStringArrayListExtra("terms",       terms);
        intent.putStringArrayListExtra("definitions", definitions);

        if (tvSetTitle != null && tvSetTitle.getText() != null) {
            ArrayList<String> titleList = new ArrayList<>();
            titleList.add(tvSetTitle.getText().toString());
            intent.putStringArrayListExtra("title", titleList);
        }
        startActivity(intent);
    }

    private void fetchCollectionData() {
        if (collectionApi == null) return;
        collectionApi.getCollectionItemById(collectionId).enqueue(new Callback<ApiResponse<List<CollectionItemResponse>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<CollectionItemResponse>>> call, @NonNull Response<ApiResponse<List<CollectionItemResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    originalItems = response.body().getData();
                    if (originalItems == null) originalItems = new ArrayList<>();
                    fetchStarredItemInCollection();
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<CollectionItemResponse>>> call, @NonNull Throwable t) {
                Toast.makeText(StudySetDetailActivity.this, R.string.msg_network_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchStarredItemInCollection() {
        if (collectionApi == null) return;
        collectionApi.getStarredCollections().enqueue(new Callback<ApiResponse<List<StarredCollectionItemResponse>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<StarredCollectionItemResponse>>> call, @NonNull Response<ApiResponse<List<StarredCollectionItemResponse>>> response) {
                List<StarredCollectionItemResponse> starredItems = new ArrayList<>();
                if (response.isSuccessful() && response.body() != null) {
                    starredItems = response.body().getData();
                }
                mapDataAndSetupUI(starredItems);
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<StarredCollectionItemResponse>>> call, @NonNull Throwable t) {
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
            pagerAdapter.setOnZoomClickListener(this::openFlashcardActivity);
            viewPagerFlashcards.setAdapter(pagerAdapter);

            viewPagerFlashcards.setOffscreenPageLimit(3);

            ViewPager2.PageTransformer transformer = (page, position) -> {
                float r = 1 - Math.abs(position);
                page.setScaleY(0.85f + r * 0.15f);
                page.setScaleX(0.90f + r * 0.10f);
            };
            viewPagerFlashcards.setPageTransformer(transformer);

            TabLayout tabLayoutIndicator = findViewById(R.id.tabLayoutIndicator);
            if (tabLayoutIndicator != null) {
                new com.google.android.material.tabs.TabLayoutMediator(tabLayoutIndicator, viewPagerFlashcards,
                        (tab, position) -> {}).attach();
            }
        }

        TextView tvTotalCardsCount = findViewById(R.id.tvTotalCardsCount);
        if (tvTotalCardsCount != null && originalItems != null) {
            tvTotalCardsCount.setText(String.format(getString(R.string.tvTotalCardsCount), originalItems.size()));
        }

        // ĐÃ XÓA: Bỏ dòng gán text author cũ ở đây để tránh đè dữ liệu vô ích của hàm fetchCollectionMetaData

        if (termListAdapter != null) {
            termListAdapter.updateData(uiItems);
            termListAdapter.SetOnDetailItemClickedListener(event -> {
                CollectionItemResponse clickedItem = event.item;
                boolean newStarState = !termListAdapter.isItemStarred(clickedItem.getId());
                if (newStarState) StarItem(clickedItem.getId());
                else UStarItem(clickedItem.getId());
                termListAdapter.updateStarState(clickedItem.getId(), newStarState);
            });
        }
    }

    public void StarItem(Integer itemId) {
        collectionApi.starCollectionItem(itemId).enqueue(new Callback<ApiResponse<StarredCollectionItemResponse>>() {
            @Override public void onResponse(@NonNull Call<ApiResponse<StarredCollectionItemResponse>> call, @NonNull Response<ApiResponse<StarredCollectionItemResponse>> response) {}
            @Override public void onFailure(@NonNull Call<ApiResponse<StarredCollectionItemResponse>> call, @NonNull Throwable t) {}
        });
    }

    public void UStarItem(Integer itemId) {
        collectionApi.unstarCollectionItem(itemId).enqueue(new Callback<ApiResponse<Void>>() {
            @Override public void onResponse(@NonNull Call<ApiResponse<Void>> call, @NonNull Response<ApiResponse<Void>> response) {}
            @Override public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {}
        });
    }

    private void showOptionsBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_options, null);

        View btnEdit = view.findViewById(R.id.btnEdit);
        View btnAddToFolder = view.findViewById(R.id.btnAddToFolder);
        View btnCopy = view.findViewById(R.id.btnMakeCopy);
        View btnReport = view.findViewById(R.id.btnReport);
        View btnShare = view.findViewById(R.id.btnShareCollection);

        if (author != null && sessionManager.getCurrUser() != null) {
            boolean isOwner = author.equals(sessionManager.getCurrUser().getUsername());
            if (btnEdit != null) btnEdit.setVisibility(isOwner ? View.VISIBLE : View.GONE);
        }

        if (btnEdit != null) btnEdit.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(this, EditCollectionActivity.class);
            intent.putExtra(getString(R.string.intent_key_collection_id), collectionId); // ĐỒNG BỘ KEY INTENT
            startActivity(intent);
        });

        if (btnCopy != null) btnCopy.setOnClickListener(v -> {
            dialog.dismiss();
            if (originalItems == null || originalItems.isEmpty()) {
                Toast.makeText(this, R.string.msg_loading_data, Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, CreateCollectionActivity.class);
            intent.putExtra("COPY_TITLE", tvSetTitle != null ? tvSetTitle.getText().toString() : "");
            intent.putParcelableArrayListExtra("COPY_ITEMS", new ArrayList<>(originalItems));
            startActivity(intent);
        });

        if (btnAddToFolder != null) btnAddToFolder.setOnClickListener(v -> {
            dialog.dismiss();
            showSaveToFolderDialog();
        });

        if (btnReport != null) btnReport.setOnClickListener(v -> {
            dialog.dismiss();
            showReportDialog();
        });

        if (btnShare != null) btnShare.setOnClickListener(v -> {
            dialog.dismiss();
            showShareCollectionDialog();
        });

        dialog.setContentView(view);
        dialog.show();
    }

    private void showShareCollectionDialog() {
        boolean isOwner = author != null
                && sessionManager.getCurrUser() != null
                && author.equals(sessionManager.getCurrUser().getUsername());

        ShareCollectionDialog shareDialog = ShareCollectionDialog.newInstance(isOwner);
        shareDialog.setOnShareListener((username, role) -> {
            collectionApi.shareCollection(collectionId, new ShareCollectionRequest(username, role))
                    .enqueue(new Callback<ApiResponse<Void>>() {
                        @Override
                        public void onResponse(@NonNull Call<ApiResponse<Void>> call,
                                               @NonNull Response<ApiResponse<Void>> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(StudySetDetailActivity.this,
                                        R.string.msg_share_success, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(StudySetDetailActivity.this,
                                        R.string.msg_share_failed, Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<ApiResponse<Void>> call,
                                              @NonNull Throwable t) {
                            Toast.makeText(StudySetDetailActivity.this,
                                    R.string.msg_network_error, Toast.LENGTH_SHORT).show();
                        }
                    });
        });
        shareDialog.show(getSupportFragmentManager(), "ShareCollectionDialog");
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

            android.widget.RadioGroup rgReasons = view.findViewById(R.id.rgReportReasons);
            EditText edtDescription = view.findViewById(R.id.edtReportDescription);
            Button btnCancel = view.findViewById(R.id.btnCancelReport);
            Button btnSubmit = view.findViewById(R.id.btnSubmitReport);

            btnCancel.setOnClickListener(v -> reportDialog.dismiss());
            btnSubmit.setOnClickListener(v -> {
                int selectedId = rgReasons.getCheckedRadioButtonId();
                if (selectedId == -1) {
                    Toast.makeText(this, R.string.msg_report_select_reason, Toast.LENGTH_SHORT).show();
                    return;
                }
                android.widget.RadioButton selectedRadioButton = view.findViewById(selectedId);
                String reason = selectedRadioButton.getText().toString();
                String description = edtDescription.getText().toString().trim();
                sendReport(new CollectionReportRequest(reason, description), reportDialog);
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
            public void onResponse(@NonNull Call<ApiResponse<CollectionReportResponse>> call, @NonNull Response<ApiResponse<CollectionReportResponse>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(StudySetDetailActivity.this, R.string.msg_report_success, Toast.LENGTH_SHORT).show();
                    reportDialog.dismiss();
                } else {
                    Toast.makeText(StudySetDetailActivity.this, R.string.msg_report_error, Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<CollectionReportResponse>> call, @NonNull Throwable t) {
                Toast.makeText(StudySetDetailActivity.this, R.string.msg_network_error, Toast.LENGTH_SHORT).show();
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
            view.findViewById(R.id.btnCreateNewFolderInDialog).setOnClickListener(v -> {
                showSimpleCreateFolderDialog(view, dialog);
            });

            loadFoldersForSaveDialog(view, dialog);
            dialog.show();
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.DialogError), Toast.LENGTH_SHORT).show();
        }
    }

    private void showSimpleCreateFolderDialog(View parentDialogView, AlertDialog parentDialog) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.new_folder);
        final EditText input = new EditText(this);
        input.setHint(R.string.folder_name);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        input.setLayoutParams(lp);
        builder.setView(input);

        builder.setPositiveButton(R.string.create, (dialog, which) -> {
            String folderName = input.getText().toString().trim();
            if (!folderName.isEmpty()) {
                createNewFolderAndAddCollection(folderName, parentDialog);
            }
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void createNewFolderAndAddCollection(String name, AlertDialog dialogToClose) {
        folderApi.createFolder(new CreateFolderRequest(name, "")).enqueue(new Callback<ApiResponse<FolderResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<FolderResponse>> call, @NonNull Response<ApiResponse<FolderResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Long newFolderId = response.body().getData().getId();
                    addCollectionToSpecificFolder(newFolderId, name, dialogToClose);
                } else {
                    Toast.makeText(StudySetDetailActivity.this, R.string.msg_failed_create_folder, Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(@NonNull Call<ApiResponse<FolderResponse>> call, @NonNull Throwable t) {
                Toast.makeText(StudySetDetailActivity.this, R.string.msg_network_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addCollectionToSpecificFolder(Long folderId, String folderName, AlertDialog dialog) {
        folderApi.addCollectionToFolder(folderId, collectionId).enqueue(new Callback<ApiResponse<FolderResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<FolderResponse>> call, @NonNull Response<ApiResponse<FolderResponse>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(StudySetDetailActivity.this, getString(R.string.msg_added_to_folder, folderName), Toast.LENGTH_SHORT).show();
                    if (dialog != null) dialog.dismiss();
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<FolderResponse>> call, @NonNull Throwable t) {
                Toast.makeText(StudySetDetailActivity.this, R.string.msg_network_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadFoldersForSaveDialog(View dialogView, AlertDialog dialog) {
        LinearLayout layoutFolderList = dialogView.findViewById(R.id.layoutFolderList);
        layoutFolderList.removeAllViews();

        TextView tvMsg = new TextView(this);
        tvMsg.setText(R.string.msg_fetching_folders);
        tvMsg.setPadding(32, 32, 32, 32);
        layoutFolderList.addView(tvMsg);

        folderApi.getAllOwnFolders().enqueue(new Callback<ApiResponse<List<FolderSummaryResponse>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<FolderSummaryResponse>>> call, @NonNull Response<ApiResponse<List<FolderSummaryResponse>>> response) {
                if (dialog.isShowing()) {
                    layoutFolderList.removeAllViews();
                    if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                        List<FolderSummaryResponse> folders = response.body().getData();
                        if (folders.isEmpty()) {
                            tvMsg.setText(R.string.msg_no_folders);
                            layoutFolderList.addView(tvMsg);
                        } else {
                            for (FolderSummaryResponse folder : folders) {
                                addFolderSelectionRow(layoutFolderList, folder, dialog);
                            }
                        }
                    } else {
                        tvMsg.setText(R.string.msg_failed_add_to_folder);
                        layoutFolderList.addView(tvMsg);
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<FolderSummaryResponse>>> call, @NonNull Throwable t) {
                if (dialog.isShowing()) {
                    layoutFolderList.removeAllViews();
                    tvMsg.setText(R.string.msg_network_error);
                    layoutFolderList.addView(tvMsg);
                }
            }
        });
    }

    private void addFolderSelectionRow(LinearLayout parent, FolderSummaryResponse folder, AlertDialog dialog) {
        View itemView = LayoutInflater.from(this).inflate(R.layout.item_folder_selection, parent, false);
        TextView tvFolderName = itemView.findViewById(R.id.tvFolderName);
        ImageView ivStatus = itemView.findViewById(R.id.ivStatus);

        tvFolderName.setText(folder.getName());
        ivStatus.setImageResource(R.drawable.ic_add);

        itemView.setOnClickListener(v -> {
            ivStatus.setEnabled(false);
            folderApi.addCollectionToFolder(folder.getId(), collectionId).enqueue(new Callback<ApiResponse<FolderResponse>>() {
                @Override
                public void onResponse(@NonNull Call<ApiResponse<FolderResponse>> call, @NonNull Response<ApiResponse<FolderResponse>> response) {
                    if (response.isSuccessful()) {
                        ivStatus.setImageResource(R.drawable.ic_check);
                        Toast.makeText(StudySetDetailActivity.this, getString(R.string.msg_added_to_folder, folder.getName()), Toast.LENGTH_SHORT).show();
                        v.postDelayed(dialog::dismiss, 600);
                    } else {
                        Toast.makeText(StudySetDetailActivity.this, R.string.msg_failed_add_to_folder, Toast.LENGTH_SHORT).show();
                        ivStatus.setEnabled(true);
                    }
                }
                @Override
                public void onFailure(@NonNull Call<ApiResponse<FolderResponse>> call, @NonNull Throwable t) {
                    Toast.makeText(StudySetDetailActivity.this, R.string.msg_network_error, Toast.LENGTH_SHORT).show();
                    ivStatus.setEnabled(true);
                }
            });
        });

        parent.addView(itemView);
    }

    private void showLearnModeBottomSheet() {
        try {
            BottomSheetDialog dialog = new BottomSheetDialog(this);
            View view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_learn_mode, null);
            MaterialCardView cardTest = view.findViewById(R.id.cardModeTest);
            MaterialCardView cardMemory = view.findViewById(R.id.cardModeMemory);
            Button btnStartLearn = view.findViewById(R.id.btnStartLearn);

            LinearLayout layoutTest = (LinearLayout) cardTest.getChildAt(0);
            TextView tvTest = (TextView) layoutTest.getChildAt(0);

            LinearLayout layoutMemory = (LinearLayout) cardMemory.getChildAt(0);
            TextView tvMemory = (TextView) layoutMemory.getChildAt(0);

            final String[] selectedMode = {"LEARN"};
            Runnable updateSelectionUI = () -> {
                try {
                    boolean isLearn = selectedMode[0].equals("LEARN");

                    int colorPurple = getColor(R.color.primary_color);
                    int colorLightPurple = getColor(R.color.primary_color_light);
                    int colorGrayBorder = getColor(R.color.quizlet_border);
                    int colorWhite = getColor(R.color.background_white);
                    int colorTextPrimary = getColor(R.color.text_color_primary);

                    cardMemory.setStrokeColor(isLearn ? colorPurple : colorGrayBorder);
                    cardMemory.setStrokeWidth(isLearn ? 4 : 2);
                    layoutMemory.setBackgroundColor(isLearn ? colorLightPurple : colorWhite);
                    tvMemory.setTextColor(isLearn ? colorPurple : colorTextPrimary);

                    cardTest.setStrokeColor(!isLearn ? colorPurple : colorGrayBorder);
                    cardTest.setStrokeWidth(!isLearn ? 4 : 2);
                    layoutTest.setBackgroundColor(!isLearn ? colorLightPurple : colorWhite);
                    tvTest.setTextColor(!isLearn ? colorPurple : colorTextPrimary);
                } catch (Exception e) { Log.e(TAG, "updateSelectionUI: FAILED", e); }
            };

            updateSelectionUI.run();

            cardTest.setOnClickListener(v -> { selectedMode[0] = "TEST"; updateSelectionUI.run(); });
            cardMemory.setOnClickListener(v -> { selectedMode[0] = "LEARN"; updateSelectionUI.run(); });

            btnStartLearn.setOnClickListener(v -> {
                dialog.dismiss();
                if (selectedMode[0] != null) {
                    Intent intent = new Intent(this, MemoryGameActivity.class);
                    intent.putExtra("COLLECTION_ID", collectionId);
                    intent.putExtra("GAME_MODE", selectedMode[0]);
                    intent.putParcelableArrayListExtra("ITEMS_LIST", new ArrayList<>(originalItems));
                    startActivity(intent);
                }
            });
            dialog.setContentView(view);
            dialog.show();
        } catch (Exception e) {
            Toast.makeText(this, R.string.LearnModeDisplayError, Toast.LENGTH_SHORT).show();
        }
    }
}