package com.example.mosquizto.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mosquizto.Activities.MemoryGameActivity;
import com.example.mosquizto.Activities.ProfilePage;
import com.example.mosquizto.Dto.response.CollectionItemResponse;
import com.example.mosquizto.Dto.response.CollectionResponse;
import com.example.mosquizto.Event.OnItemCollectionClickedListener;
import com.example.mosquizto.MainActivity;
import com.example.mosquizto.R;
import com.example.mosquizto.Adapters.BasedOnRecentAdapter;
import com.example.mosquizto.Adapters.JumpBackInAdapter;
import com.example.mosquizto.Adapters.RecentAdapter;
import com.example.mosquizto.Util.AvatarImageHelper;
import com.example.mosquizto.Util.FragmentTag;
import com.example.mosquizto.Util.GameMode;
import com.example.mosquizto.ViewModels.HomeViewModel;
import com.example.mosquizto.Services.SessionManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HomeFragment extends Fragment {

    private HomeViewModel viewModel;

    @Inject
    SessionManager sessionManager;

    private RecyclerView rvJumpBackIn, rvRecents, rvBasedOnRecent;
    private JumpBackInAdapter jumpAdapter;
    private RecentAdapter recentAdapter;
    private BasedOnRecentAdapter basedAdapter;
    private MainActivity mainActivity;

    private ImageView ivAvatarHome;
    private EditText etSearch;

    private View multiChoiceLayout;
    private TextView tvQuestionNumber, tvQuestionContent, btnAnswer1, btnAnswer2, btnAnswer3, btnAnswer4;
    private ImageView btnReloadMcq;
    private TextView[] quickAnswersArray;

    private View cardRandomGame;
    private TextView tvRandomGameTitle, tvRandomGameDesc;
    private Button btnPlayRandomGame;
    private CollectionResponse currentRandomGameCollection;
    private GameMode currentRandomGameMode;

    private List<CollectionResponse> cachedRecentCollections = new ArrayList<>();
    private List<CollectionItemResponse> totalCollectionItems = new ArrayList<>();
    private List<CollectionItemResponse> quickMcqQueue = new ArrayList<>();
    private int quickMcqCurrentIndex = 0;
    private String correctQuickAnswer;

    private final Handler mcqHandler = new Handler(Looper.getMainLooper());
    private String oldPlayBtnText = "";
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initViews(view);
        createListener();
        setupEmptyRecyclerViews();
        initQuickMcqViews(view);
        initRandomGameViews(view);
        initViewModel();

        return view;
    }

    private void initViews(View view) {
        ivAvatarHome = view.findViewById(R.id.iv_avatar_home);
        if (ivAvatarHome != null) {
            ivAvatarHome.setOnClickListener(v -> startActivity(new Intent(getContext(), ProfilePage.class)));
        }
        rvJumpBackIn = view.findViewById(R.id.rvJumpBackIn);
        rvRecents = view.findViewById(R.id.rvRecents);
        rvBasedOnRecent = view.findViewById(R.id.rvBasedOnRecent);

        etSearch = view.findViewById(R.id.etSearch);

        if (getActivity() instanceof MainActivity) {
            mainActivity = (MainActivity) getActivity();
        }
    }

    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // Đăng ký các bộ lắng nghe LiveData thay đổi từ ViewModel
        viewModel.jumpBackIn.observe(getViewLifecycleOwner(), sessions -> {
            if (sessions != null) jumpAdapter.setSessions(sessions);
        });

        viewModel.recents.observe(getViewLifecycleOwner(), recents -> {
            if (recents != null) {
                cachedRecentCollections = recents;
                recentAdapter.setCollections(recents);
                recentAdapter.notifyDataSetChanged();

                // Tạo phân luồng xử lý nhẹ để giao diện hiển thị danh sách trước không bị khựng
                new Handler(Looper.getMainLooper()).post(() -> {
                    pickRandomCollectionForMcq();
                    setupRandomGameSection();
                });
            }
        });

        viewModel.recommended.observe(getViewLifecycleOwner(), collections -> {
            if (collections != null) {
                basedAdapter.setCollections(collections);
                basedAdapter.notifyDataSetChanged();
            }
        });

        viewModel.mcqItems.observe(getViewLifecycleOwner(), items -> {
            if (items != null) {
                totalCollectionItems = items;
                setupQuickMcqPlan(items);
            } else {
                if (multiChoiceLayout != null) multiChoiceLayout.setVisibility(View.GONE);
            }
        });

        viewModel.isMcqLoading.observe(getViewLifecycleOwner(), loading -> {
            if (btnReloadMcq != null) {
                btnReloadMcq.setEnabled(!loading);
                btnReloadMcq.setAlpha(loading ? 0.5f : 1.0f);
            }
        });

        viewModel.randomGameItems.observe(getViewLifecycleOwner(), items -> {
            if (items != null && !items.isEmpty()) {
                Intent intent = new Intent(getContext(), MemoryGameActivity.class);
                intent.putExtra("COLLECTION_ID", currentRandomGameCollection.getId());
                intent.putExtra("GAME_MODE", currentRandomGameMode.name());
                intent.putParcelableArrayListExtra("ITEMS_LIST", new ArrayList<>(items));
                startActivity(intent);

                // Xóa trạng thái ngay lập tức tránh việc quay lại màn hình Home bị kích hoạt trùng lặp
                viewModel.clearRandomGameItems();
            } else if (items != null) {
                Toast.makeText(getContext(), "Bộ học phần rỗng", Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.isRandomGameLoading.observe(getViewLifecycleOwner(), loading -> {
            if (btnPlayRandomGame != null) {
                btnPlayRandomGame.setEnabled(!loading);
                if (loading) {
                    oldPlayBtnText = btnPlayRandomGame.getText().toString();
                    btnPlayRandomGame.setText("...");
                } else if (!oldPlayBtnText.isEmpty()) {
                    btnPlayRandomGame.setText(oldPlayBtnText);
                }
            }
        });

        loadHomeAvatar();
    }

    private void loadHomeAvatar() {
        if (sessionManager == null || ivAvatarHome == null) {
            return;
        }
        AvatarImageHelper.loadInto(ivAvatarHome, sessionManager.resolveCurrentUserAvatarUri());
    }

    private void initQuickMcqViews(View view) {
        multiChoiceLayout = view.findViewById(R.id.multiChoiceLayout);
        tvQuestionNumber  = view.findViewById(R.id.tvQuestionNumber);
        tvQuestionContent = view.findViewById(R.id.tvQuestionContent);
        btnAnswer1        = view.findViewById(R.id.btnAnswer1);
        btnAnswer2        = view.findViewById(R.id.btnAnswer2);
        btnAnswer3        = view.findViewById(R.id.btnAnswer3);
        btnAnswer4        = view.findViewById(R.id.btnAnswer4);
        btnReloadMcq      = view.findViewById(R.id.btnReloadMcq);

        quickAnswersArray = new TextView[]{btnAnswer1, btnAnswer2, btnAnswer3, btnAnswer4};

        if (btnReloadMcq != null) {
            btnReloadMcq.setOnClickListener(v -> {
                if (tvQuestionContent != null) tvQuestionContent.setText("...");
                pickRandomCollectionForMcq();
            });
        }
    }

    private void initRandomGameViews(View view) {
        cardRandomGame = view.findViewById(R.id.cardRandomGame);
        tvRandomGameTitle = view.findViewById(R.id.tvRandomGameTitle);
        tvRandomGameDesc = view.findViewById(R.id.tvRandomGameDesc);
        btnPlayRandomGame = view.findViewById(R.id.btnPlayRandomGame);

        if (btnPlayRandomGame != null) {
            btnPlayRandomGame.setOnClickListener(v -> playRandomGame());
        }
    }

    private void createListener() {
        if (etSearch != null) etSearch.setOnClickListener(v -> {
            if (mainActivity != null) mainActivity.switchToFragment(FragmentTag.search);
        });
    }

    private void setupEmptyRecyclerViews() {
        jumpAdapter = new JumpBackInAdapter(null, item -> {
            if (mainActivity != null) mainActivity.GoToStudySetActivity(getContext(), item.getCollectionId(), item.getCollectionName());
        });
        rvJumpBackIn.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvJumpBackIn.setAdapter(jumpAdapter);

        recentAdapter = new RecentAdapter(null, item -> {
            if (mainActivity != null) mainActivity.GoToStudySetActivity(getContext(), item);
        });
        recentAdapter.SetOnCloclickListener(new RecentAdapter.OnCollectionActionListener() {
            @Override public void onEdit(CollectionResponse item, int position) {}
            @Override public void onShare(CollectionResponse item, int position) {}
            @Override public void onDelete(CollectionResponse item, int position) {
                viewModel.deleteRecentItem(item.getId());
            }
        });
        rvRecents.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        rvRecents.setAdapter(recentAdapter);

        basedAdapter = new BasedOnRecentAdapter(null);
        basedAdapter.setItemCollectionClickedListener(new OnItemCollectionClickedListener() {
            @Override
            public void OnItemClicked(CollectionResponse item) {
                if (mainActivity != null) mainActivity.GoToStudySetActivity(getContext(), item);
            }
        });
        rvBasedOnRecent.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvBasedOnRecent.setAdapter(basedAdapter);
        rvJumpBackIn.setItemAnimator(null);
        rvRecents.setItemAnimator(null);
        rvBasedOnRecent.setItemAnimator(null);
    }

    private void setupRandomGameSection() {
        if (!isAdded() || getView() == null) return;

        if (cachedRecentCollections != null && !cachedRecentCollections.isEmpty()) {
            int randomIndex = (int) (Math.random() * cachedRecentCollections.size());
            currentRandomGameCollection = cachedRecentCollections.get(randomIndex);

            GameMode[] allModes = GameMode.values();
            currentRandomGameMode = allModes[(int) (Math.random() * allModes.length)];

            if (tvRandomGameTitle != null) tvRandomGameTitle.setText(currentRandomGameCollection.getTitle());

            int descResId;
            int btnResId;

            switch (currentRandomGameMode) {
                case LEARN:
                    descResId = R.string.game_desc_learn;
                    btnResId = R.string.game_btn_learn;
                    break;
                case TEST:
                    descResId = R.string.game_desc_test;
                    btnResId = R.string.game_btn_test;
                    break;
                case ONLY_MCQ:
                    descResId = R.string.game_desc_mcq;
                    btnResId = R.string.game_btn_mcq;
                    break;
                case ONLY_FB:
                    descResId = R.string.game_desc_fb;
                    btnResId = R.string.game_btn_fb;
                    break;
                case ONLY_MATCH:
                    descResId = R.string.game_desc_match;
                    btnResId = R.string.game_btn_match;
                    break;
                default:
                    descResId = R.string.msg_dev_mode;
                    btnResId = R.string.Reload;
            }

            if (tvRandomGameDesc != null) tvRandomGameDesc.setText(descResId);
            if (btnPlayRandomGame != null) btnPlayRandomGame.setText(btnResId);
            if (cardRandomGame != null) cardRandomGame.setVisibility(View.VISIBLE);
        } else {
            if (cardRandomGame != null) cardRandomGame.setVisibility(View.GONE);
        }
    }

    private void playRandomGame() {
        if (currentRandomGameCollection == null) return;
        viewModel.fetchItemsForRandomGame(currentRandomGameCollection.getId());
    }

    private void pickRandomCollectionForMcq() {
        if (cachedRecentCollections != null && !cachedRecentCollections.isEmpty()) {
            int randomIndex = (int) (Math.random() * cachedRecentCollections.size());
            CollectionResponse randomCollection = cachedRecentCollections.get(randomIndex);
            viewModel.fetchItemsForQuickMcq(randomCollection.getId());
        } else {
            if (multiChoiceLayout != null) multiChoiceLayout.setVisibility(View.GONE);
        }
    }

    private void setupQuickMcqPlan(List<CollectionItemResponse> items) {
        if (items == null || items.size() < 4) {
            if (multiChoiceLayout != null) multiChoiceLayout.setVisibility(View.GONE);
            return;
        }

        List<CollectionItemResponse> shuffledList = new ArrayList<>(items);
        Collections.shuffle(shuffledList);

        int randomCount = Math.min(items.size(), 2 + (int)(Math.random() * 4));
        quickMcqQueue = new ArrayList<>(shuffledList.subList(0, randomCount));
        quickMcqCurrentIndex = 0;

        displayQuickQuestion();
    }

    private void displayQuickQuestion() {
        if (!isAdded() || getView() == null) return;

        if (quickMcqCurrentIndex >= quickMcqQueue.size()) {
            if (tvQuestionNumber != null) tvQuestionNumber.setText(R.string.Complete);
            if (tvQuestionContent != null) tvQuestionContent.setText(R.string.Congratulation);
            if (quickAnswersArray != null) {
                for (TextView btn : quickAnswersArray) if (btn != null) btn.setVisibility(View.GONE);
            }
            return;
        }

        if (multiChoiceLayout != null) multiChoiceLayout.setVisibility(View.VISIBLE);
        if (quickAnswersArray != null) {
            for (TextView btn : quickAnswersArray) {
                if (btn != null) {
                    btn.setVisibility(View.VISIBLE);
                    btn.setBackgroundResource(R.drawable.bg_outline_button);
                    btn.setEnabled(true);
                }
            }
        }

        CollectionItemResponse currentItem = quickMcqQueue.get(quickMcqCurrentIndex);
        if (tvQuestionNumber != null) tvQuestionNumber.setText((quickMcqCurrentIndex + 1) + " / " + quickMcqQueue.size());

        boolean showTerm = Math.random() > 0.5;
        if (tvQuestionContent != null) tvQuestionContent.setText(showTerm ? currentItem.getTerm() : currentItem.getDefinition());
        correctQuickAnswer = showTerm ? currentItem.getDefinition() : currentItem.getTerm();

        List<String> choices = new ArrayList<>();
        choices.add(correctQuickAnswer);

        List<CollectionItemResponse> pool = new ArrayList<>(totalCollectionItems);
        pool.remove(currentItem);
        Collections.shuffle(pool);

        int wrongAdded = 0;
        for (CollectionItemResponse item : pool) {
            String wrongAns = showTerm ? item.getDefinition() : item.getTerm();
            if (!wrongAns.equals(correctQuickAnswer) && wrongAdded < 3) {
                choices.add(wrongAns);
                wrongAdded++;
            }
        }
        Collections.shuffle(choices);

        for (int i = 0; i < 4; i++) {
            TextView btn = quickAnswersArray[i];
            if (btn != null) {
                if (i < choices.size()) {
                    btn.setText(choices.get(i));
                    String currentChoiceText = choices.get(i);
                    btn.setOnClickListener(v -> checkQuickAnswer(btn, currentChoiceText));
                } else {
                    btn.setVisibility(View.GONE);
                }
            }
        }
    }

    private void checkQuickAnswer(TextView clickedBtn, String selectedAnswer) {
        if (quickAnswersArray != null) {
            for (TextView btn : quickAnswersArray) if (btn != null) btn.setEnabled(false);
        }

        if (selectedAnswer.equals(correctQuickAnswer)) {
            clickedBtn.setBackgroundResource(R.drawable.bg_option_correct);
        } else {
            clickedBtn.setBackgroundResource(R.drawable.bg_option_wrong);
            if (quickAnswersArray != null) {
                for (TextView btn : quickAnswersArray) {
                    if (btn != null && btn.getText().toString().equals(correctQuickAnswer)) {
                        btn.setBackgroundResource(R.drawable.bg_option_correct);
                    }
                }
            }
        }

        mcqHandler.removeCallbacksAndMessages(null);
        mcqHandler.postDelayed(() -> {
            if (isAdded() && getView() != null) {
                quickMcqCurrentIndex++;
                displayQuickQuestion();
            }
        }, 1500);
    }

    @Override
    public void onPause() {
        super.onPause();
        mcqHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadHomeAvatar();
        if (viewModel != null) {
            viewModel.fetchAllData();
        }
    }
}