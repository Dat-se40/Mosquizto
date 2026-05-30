package com.example.mosquizto.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mosquizto.Activities.MemoryGameActivity;
import com.example.mosquizto.Activities.ProfilePage;
import com.example.mosquizto.Dto.response.CollectionItemResponse;
import com.example.mosquizto.Dto.response.CollectionResponse;
import com.example.mosquizto.Dto.response.StudySessionResponse;
import com.example.mosquizto.MainActivity;
import com.example.mosquizto.Network.itf.CollectionApi;
import com.example.mosquizto.R;
import com.example.mosquizto.Adapters.BasedOnRecentAdapter;
import com.example.mosquizto.Adapters.JumpBackInAdapter;
import com.example.mosquizto.Adapters.RecentAdapter;
import com.example.mosquizto.Dto.response.ApiResponse;
import com.example.mosquizto.Network.itf.StudyApi;
import com.example.mosquizto.Util.FragmentTag;
import com.example.mosquizto.Util.GameMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class HomeFragment extends Fragment {

    private RecyclerView rvJumpBackIn, rvRecents, rvBasedOnRecent;
    private JumpBackInAdapter jumpAdapter;
    private RecentAdapter recentAdapter;
    private BasedOnRecentAdapter basedAdapter;
    private MainActivity mainActivity;

    @Inject StudyApi studyApi;
    @Inject CollectionApi collectionApi;

    private ImageView imgView;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        View ivAvatar = view.findViewById(R.id.iv_avatar);
        if (ivAvatar != null) {
            ivAvatar.setOnClickListener(v -> startActivity(new Intent(getContext(), ProfilePage.class)));
        }

        rvJumpBackIn = view.findViewById(R.id.rvJumpBackIn);
        rvRecents = view.findViewById(R.id.rvRecents);
        rvBasedOnRecent = view.findViewById(R.id.rvBasedOnRecent);
        imgView = view.findViewById(R.id.iv_avatar);
        etSearch = view.findViewById(R.id.etSearch);

        if (getActivity() instanceof MainActivity) {
            mainActivity = (MainActivity) getActivity();
        }

        createListener();
        setupEmptyRecyclerViews();
        initQuickMcqViews(view);
        initRandomGameViews(view);

        fetchJumpBackIn();
        fetchRecents();

        return view;
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
        if (imgView != null) imgView.setOnClickListener(v -> startActivity(new Intent(getContext(), ProfilePage.class)));
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
            @Override public void onDelete(CollectionResponse item, int position) {}
        });
        rvRecents.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        rvRecents.setAdapter(recentAdapter);

        basedAdapter = new BasedOnRecentAdapter(null);
        rvBasedOnRecent.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvBasedOnRecent.setAdapter(basedAdapter);

        rvJumpBackIn.setItemAnimator(null);
        rvRecents.setItemAnimator(null);
        rvBasedOnRecent.setItemAnimator(null);
    }

    private void fetchJumpBackIn() {
        studyApi.getJumpBackIn().enqueue(new Callback<ApiResponse<List<StudySessionResponse>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<StudySessionResponse>>> call, @NonNull Response<ApiResponse<List<StudySessionResponse>>> response) {
                if (!isAdded() || getView() == null) return;
                if (response.isSuccessful() && response.body() != null) {
                    jumpAdapter.setSessions(response.body().getData());
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<StudySessionResponse>>> call, @NonNull Throwable t) {
                Log.e("HomeFragment", "Lỗi JumpBackIn: " + t.getMessage());
            }
        });
    }

    private void fetchRecents() {
        collectionApi.getRecentOpenedCollections().enqueue(new Callback<ApiResponse<List<CollectionResponse>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<CollectionResponse>>> call, @NonNull Response<ApiResponse<List<CollectionResponse>>> response) {
                if (!isAdded() || getView() == null) return;
                if (response.isSuccessful() && response.body() != null) {
                    cachedRecentCollections = response.body().getData();
                    recentAdapter.setCollections(cachedRecentCollections);
                    recentAdapter.notifyDataSetChanged();

                    pickRandomCollectionForMcq();
                    setupRandomGameSection();
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<CollectionResponse>>> call, @NonNull Throwable t) {
                Log.e("HomeFragment", "Lỗi Recents: " + t.getMessage());
            }
        });
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

        btnPlayRandomGame.setEnabled(false);
        String oldText = btnPlayRandomGame.getText().toString();
        btnPlayRandomGame.setText("...");

        collectionApi.getCollectionItemById(currentRandomGameCollection.getId()).enqueue(new Callback<ApiResponse<List<CollectionItemResponse>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<CollectionItemResponse>>> call, @NonNull Response<ApiResponse<List<CollectionItemResponse>>> response) {
                if (!isAdded()) return;
                btnPlayRandomGame.setEnabled(true);
                btnPlayRandomGame.setText(oldText);

                if (response.isSuccessful() && response.body() != null) {
                    List<CollectionItemResponse> items = response.body().getData();
                    if (items != null && !items.isEmpty()) {
                        Intent intent = new Intent(getContext(), MemoryGameActivity.class);
                        intent.putExtra("COLLECTION_ID", currentRandomGameCollection.getId());
                        intent.putExtra("GAME_MODE", currentRandomGameMode.name());
                        intent.putParcelableArrayListExtra("ITEMS_LIST", new ArrayList<>(items));
                        startActivity(intent);
                    } else {
                        Toast.makeText(getContext(), "Empty set", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<CollectionItemResponse>>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                btnPlayRandomGame.setEnabled(true);
                btnPlayRandomGame.setText(oldText);
                Toast.makeText(getContext(), "Error loading game", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void pickRandomCollectionForMcq() {
        if (cachedRecentCollections != null && !cachedRecentCollections.isEmpty()) {
            int randomIndex = (int) (Math.random() * cachedRecentCollections.size());
            CollectionResponse randomCollection = cachedRecentCollections.get(randomIndex);

            if (btnReloadMcq != null) {
                btnReloadMcq.setEnabled(false);
                btnReloadMcq.setAlpha(0.5f);
            }

            fetchItemsForQuickMcq(randomCollection.getId());
        } else {
            if (multiChoiceLayout != null) multiChoiceLayout.setVisibility(View.GONE);
        }
    }

    private void fetchItemsForQuickMcq(Integer collectionId) {
        collectionApi.getCollectionItemById(collectionId).enqueue(new Callback<ApiResponse<List<CollectionItemResponse>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<CollectionItemResponse>>> call, @NonNull Response<ApiResponse<List<CollectionItemResponse>>> response) {
                if (!isAdded()) return;
                if (btnReloadMcq != null) {
                    btnReloadMcq.setEnabled(true);
                    btnReloadMcq.setAlpha(1.0f);
                }

                if (response.isSuccessful() && response.body() != null) {
                    totalCollectionItems = response.body().getData();
                    setupQuickMcqPlan(totalCollectionItems);
                } else {
                    if (multiChoiceLayout != null) multiChoiceLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<CollectionItemResponse>>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                if (btnReloadMcq != null) {
                    btnReloadMcq.setEnabled(true);
                    btnReloadMcq.setAlpha(1.0f);
                }
                if (multiChoiceLayout != null) multiChoiceLayout.setVisibility(View.GONE);
            }
        });
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
        fetchJumpBackIn();
        fetchRecents();
    }
}