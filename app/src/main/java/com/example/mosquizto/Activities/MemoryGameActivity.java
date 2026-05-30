package com.example.mosquizto.Activities;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.mosquizto.Adapters.TermListAdapter;
import com.example.mosquizto.Dto.request.StartStudySessionRequest;
import com.example.mosquizto.Dto.request.StudySessionDetailRequest;
import com.example.mosquizto.Dto.response.ApiResponse;
import com.example.mosquizto.Dto.response.CollectionItemResponse;
import com.example.mosquizto.Dto.response.StudySessionResultResponse;
import com.example.mosquizto.Network.itf.CollectionApi;
import com.example.mosquizto.Network.itf.StudyApi;
import com.example.mosquizto.R;
import com.example.mosquizto.Services.CompleteSessionWorker;
import com.example.mosquizto.Util.GameMode;
import com.example.mosquizto.Util.QuestionType;
import com.google.gson.Gson;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import dagger.hilt.android.AndroidEntryPoint;
import jakarta.inject.Inject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class MemoryGameActivity extends AppCompatActivity {
    // === ENUMS ===

    // === DATA STATE ===
    private boolean hasSubmitted = false;
    private Integer collectionId;
    private Long sessionId;
    private GameMode currentGameMode = GameMode.LEARN;

    private List<CollectionItemResponse> allItems = new ArrayList<>();
    private List<StudySessionDetailRequest> bulkResults = new ArrayList<>();
    private List<QuestionWrapper> questionQueue = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private long questionStartTime;

    // Điểm cho phần TEST
    private int totalCorrectTest = 0;
    private int totalTestQuestions = 0;

    // === UI COMPONENTS ===
    private TextView tvRoundName;
    private ProgressBar progressGame;
    private View viewMultipleChoice, viewFillBlank, viewMatch, viewSummary;
    private TextView tvSummaryTitle;
    private Button btnContinueSummary;

    // UI MC
    private TextView tvModeLabelMc, tvQuestionMc, btnOption1, btnOption2, btnOption3, btnOption4;
    private TextView[] optionsArray;
    private View layoutResultMc;
    private TextView tvResultMessageMc;
    private Button btnContinueMc;

    // UI FB
    private TextView tvModeLabelFb, tvQuestionFb, tvCorrectAnswerFb;
    private EditText edtAnswer;
    private View layoutSkippedResult, layoutActionsFb;
    private TextView btnDontKnow;
    private Button btnSubmitFb, btnContinueFb;
    private androidx.recyclerview.widget.RecyclerView rvLearnedTerms;

    // UI MATCH
    private TextView[] matchLeftViews;
    private TextView[] matchRightViews;

    @Inject StudyApi studyApi;

    // ==========================================
    // 1. ARCHITECTURE: STRATEGY PATTERN (Inner Interfaces)
    // ==========================================
    private interface QuestionStrategy {
        void execute(QuestionWrapper question);
    }

    // Class gói dữ liệu 1 câu hỏi
    private static class QuestionWrapper {
        QuestionType type;
        List<CollectionItemResponse> items; // MC/FB cần 1 item, Match cần 3 item

        QuestionWrapper(QuestionType type, List<CollectionItemResponse> items) {
            this.type = type;
            this.items = items;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_game);
        try {
            Class<?> valueAnimatorClass = Class.forName("android.animation.ValueAnimator");
            Method setDurationScaleMethod = valueAnimatorClass.getDeclaredMethod("setDurationScale", float.class);
            setDurationScaleMethod.invoke(null, 1.0f);
        } catch (Exception e) {
            e.printStackTrace(); // Có thể bị chặn trên các bản Android rất mới do hạn chế Reflection
        }
        // Đọc Intent xem đây là Mode gì
        String modeStr = getIntent().getStringExtra("GAME_MODE");
        if (modeStr != null && modeStr.equals("TEST")) {
            currentGameMode = GameMode.TEST;
        }

        collectionId = getIntent().getIntExtra("COLLECTION_ID", -1);
        ArrayList<CollectionItemResponse> items = getIntent().getParcelableArrayListExtra("ITEMS_LIST");
        if (items != null) {
            this.allItems = items;
        }

        initViews();
        getWindow().getDecorView().post(this::fetchDataAndStartSession);
    }

    private void initViews() {
        tvRoundName = findViewById(R.id.tvRoundName);
        progressGame = findViewById(R.id.progressGame);
        viewMultipleChoice = findViewById(R.id.viewMultipleChoice);
        viewFillBlank = findViewById(R.id.viewFillBlank);
        viewMatch = findViewById(R.id.viewTestMatch);
        viewSummary = findViewById(R.id.viewSummary);
        tvSummaryTitle = findViewById(R.id.tvSummaryTitle);
        btnContinueSummary = findViewById(R.id.btnContinueSummary);
        rvLearnedTerms = findViewById(R.id.rvLearnedTerms);
        rvLearnedTerms.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));

        findViewById(R.id.btnCloseGame).setOnClickListener(v -> handleClose());

        // MC Setup
        tvModeLabelMc = findViewById(R.id.tvModeLabelMc);
        tvQuestionMc = findViewById(R.id.tvQuestionMc);
        btnOption1 = findViewById(R.id.btnOption1);
        btnOption2 = findViewById(R.id.btnOption2);
        btnOption3 = findViewById(R.id.btnOption3);
        btnOption4 = findViewById(R.id.btnOption4);
        optionsArray = new TextView[]{btnOption1, btnOption2, btnOption3, btnOption4};
        layoutResultMc = findViewById(R.id.layoutResultMc);
        tvResultMessageMc = findViewById(R.id.tvResultMessageMc);
        btnContinueMc = findViewById(R.id.btnContinueMc);

        // FB Setup
        tvModeLabelFb = findViewById(R.id.tvModeLabelFb);
        tvQuestionFb = findViewById(R.id.tvQuestionFb);
        edtAnswer = findViewById(R.id.edtAnswer);
        layoutSkippedResult = findViewById(R.id.layoutSkippedResult);
        tvCorrectAnswerFb = findViewById(R.id.tvCorrectAnswerFb);
        layoutActionsFb = findViewById(R.id.layoutActionsFb);
        btnDontKnow = findViewById(R.id.btnDontKnow);
        btnSubmitFb = findViewById(R.id.btnSubmitFb);
        btnContinueFb = findViewById(R.id.btnContinueFb);

        // Match Setup
        matchLeftViews = new TextView[]{findViewById(R.id.btnMatchL1), findViewById(R.id.btnMatchL2), findViewById(R.id.btnMatchL3)};
        matchRightViews = new TextView[]{findViewById(R.id.btnMatchR1), findViewById(R.id.btnMatchR2), findViewById(R.id.btnMatchR3)};
    }

    private void fetchDataAndStartSession() {
        String uniqueKey = java.util.UUID.randomUUID().toString();
        StartStudySessionRequest request = new StartStudySessionRequest(collectionId);
        studyApi.startStudySession(uniqueKey, request).enqueue(new Callback<ApiResponse<Long>>() {
            @Override
            public void onResponse(Call<ApiResponse<Long>> call, Response<ApiResponse<Long>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    sessionId = response.body().getData();
                    if (sessionId != null) {
                        generateGamePlan();
                        loadNextQuestion();
                    }
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<Long>> call, Throwable t) {
                Log.e("DEBUG_GAME", "API Error: " + t.getMessage());
            }
        });
    }

    // ==========================================
    // 2. GENERATE GAME PLAN (LEARN vs TEST)
    // ==========================================
    private void generateGamePlan() {
        questionQueue.clear();
        currentQuestionIndex = 0;
        List<CollectionItemResponse> shuffledDeck = new ArrayList<>(allItems);
        Collections.shuffle(shuffledDeck);

        if (currentGameMode == GameMode.LEARN) {
            tvRoundName.setText(getString(R.string.tvRound1));
            // Learn: 10 câu MC, sau đó đổi mode sang FB (Giả lập logic cũ nhưng dùng chung mảng)
            int limit = Math.min(10, shuffledDeck.size());
            for (int i = 0; i < limit; i++) {
                questionQueue.add(new QuestionWrapper(QuestionType.MULTIPLE_CHOICE, Collections.singletonList(shuffledDeck.get(i))));
            }
            totalTestQuestions = limit;
        } else {
            tvRoundName.setText(getString(R.string.tvRound2));
            // Test: Mix MC, FB, Match. Max 20 questions
            int limit = Math.min(20, shuffledDeck.size());
            int i = 0;
            while (i < limit) {
                double rand = Math.random();
                // Nếu còn đủ 3 câu cho Match
                if (rand < 0.33 && i + 2 < limit) {
                    List<CollectionItemResponse> matchGroup = new ArrayList<>();
                    matchGroup.add(shuffledDeck.get(i));
                    matchGroup.add(shuffledDeck.get(i+1));
                    matchGroup.add(shuffledDeck.get(i+2));
                    questionQueue.add(new QuestionWrapper(QuestionType.MATCHING, matchGroup));
                    i += 3;
                } else if (rand < 0.66) {
                    questionQueue.add(new QuestionWrapper(QuestionType.FILL_BLANK, Collections.singletonList(shuffledDeck.get(i))));
                    i++;
                } else {
                    questionQueue.add(new QuestionWrapper(QuestionType.MULTIPLE_CHOICE, Collections.singletonList(shuffledDeck.get(i))));
                    i++;
                }
            }
            totalTestQuestions = i; // Tổng số items tham gia test
        }
    }

    // ==========================================
    // 3. ENGINE ROUTER
    // ==========================================
    private void loadNextQuestion() {
        // 1. CHUYỂN ĐOẠN ẨN VIEW LÊN TRÊN CÙNG
        // Ẩn hết View trước khi quyết định hiển thị màn hình nào tiếp theo
        viewMultipleChoice.setVisibility(View.GONE);
        viewFillBlank.setVisibility(View.GONE);
        viewMatch.setVisibility(View.GONE);
        viewSummary.setVisibility(View.GONE);

        // 2. KIỂM TRA ĐIỀU KIỆN KẾT THÚC
        if (currentQuestionIndex >= questionQueue.size()) {
            if (currentGameMode == GameMode.LEARN && isFirstRoundLearn()) {
                showLearnMidwaySummary();
            } else {
                showFinalSummary();
            }
            return;
        }

        // 3. XỬ LÝ CÂU HỎI TIẾP THEO (NẾU CHƯA KẾT THÚC)
        // Tính Progress dựa trên item đã làm
        int progress = (int) (((float) currentQuestionIndex / questionQueue.size()) * 100);
        progressGame.setProgress(progress);

        QuestionWrapper nextQuestion = questionQueue.get(currentQuestionIndex);
        questionStartTime = System.currentTimeMillis();

        // Chạy Strategy tương ứng
        switch (nextQuestion.type) {
            case MULTIPLE_CHOICE:
                new MultipleChoiceStrategy().execute(nextQuestion);
                break;
            case FILL_BLANK:
                new FillBlankStrategy().execute(nextQuestion);
                break;
            case MATCHING:
                new MatchingStrategy().execute(nextQuestion);
                break;
        }
    }

    private void moveToNext() {
        currentQuestionIndex++;
        loadNextQuestion();
    }

    // ==========================================
    // 4. STRATEGY IMPLEMENTATIONS
    // ==========================================

    /**
     * MULTIPLE CHOICE STRATEGY
     */
    private class MultipleChoiceStrategy implements QuestionStrategy {
        @Override
        public void execute(QuestionWrapper question) {
            viewMultipleChoice.setVisibility(View.VISIBLE);
            tvModeLabelMc.setVisibility(currentGameMode == GameMode.TEST ? View.VISIBLE : View.GONE);
            layoutResultMc.setVisibility(View.GONE);
            btnContinueMc.setVisibility(View.GONE);

            CollectionItemResponse item = question.items.get(0);
            boolean showTerm = Math.random() > 0.5;
            tvQuestionMc.setText(showTerm ? item.getTerm() : item.getDefinition());
            String correctAnswer = showTerm ? item.getDefinition() : item.getTerm();

            List<String> choices = new ArrayList<>();
            choices.add(correctAnswer);

            List<CollectionItemResponse> temp = new ArrayList<>(allItems);
            Collections.shuffle(temp);
            int wrongCount = 0;
            for (CollectionItemResponse t : temp) {
                String wrongAnswer = showTerm ? t.getDefinition() : t.getTerm();
                if (!wrongAnswer.equals(correctAnswer) && wrongCount < 3) {
                    choices.add(wrongAnswer);
                    wrongCount++;
                }
            }
            Collections.shuffle(choices);

            for (int i = 0; i < 4; i++) {
                TextView btn = optionsArray[i];
                if (i < choices.size()) {
                    btn.setVisibility(View.VISIBLE);
                    btn.setText(choices.get(i));
                    btn.setBackgroundResource(R.drawable.bg_option_default);
                    btn.setEnabled(true);
                    btn.setOnClickListener(v -> handleAnswer(btn, btn.getText().toString().equals(correctAnswer), item));
                } else {
                    btn.setVisibility(View.GONE);
                }
            }
        }

        private void handleAnswer(TextView clickedBtn, boolean isCorrect, CollectionItemResponse item) {
            recordResult(item.getId(), isCorrect);
            for (TextView btn : optionsArray) btn.setEnabled(false);

            layoutResultMc.setVisibility(View.VISIBLE);
            if (isCorrect) {
                clickedBtn.setBackgroundResource(R.drawable.bg_option_correct);
                tvResultMessageMc.setText(getString(R.string.tvCorrectAnswer));
                tvResultMessageMc.setTextColor(Color.parseColor("#4CAF50"));
                new Handler().postDelayed(MemoryGameActivity.this::moveToNext, 1000);
            } else {
                clickedBtn.setBackgroundResource(R.drawable.bg_option_wrong);
                tvResultMessageMc.setText(getString(R.string.tvNotQuiteAnswer));
                tvResultMessageMc.setTextColor(Color.parseColor("#F44336"));
                btnContinueMc.setVisibility(View.VISIBLE);
                btnContinueMc.setOnClickListener(v -> moveToNext());
            }
        }
    }

    /**
     * FILL BLANK STRATEGY
     */
    private class FillBlankStrategy implements QuestionStrategy {
        @Override
        public void execute(QuestionWrapper question) {
            viewFillBlank.setVisibility(View.VISIBLE);
            tvModeLabelFb.setVisibility(currentGameMode == GameMode.TEST ? View.VISIBLE : View.GONE);
            layoutSkippedResult.setVisibility(View.GONE);
            btnContinueFb.setVisibility(View.GONE);
            layoutActionsFb.setVisibility(View.VISIBLE);

            CollectionItemResponse item = question.items.get(0);
            edtAnswer.setText("");
            edtAnswer.setEnabled(true);
            tvQuestionFb.setText(item.getDefinition());

            String correctAnswer = item.getTerm();

            btnSubmitFb.setOnClickListener(v -> {
                String userAns = edtAnswer.getText().toString().trim();
                if (!userAns.isEmpty()) {
                    handleAnswer(userAns.equalsIgnoreCase(correctAnswer), correctAnswer, item);
                }
            });
            btnDontKnow.setOnClickListener(v -> handleAnswer(false, correctAnswer, item));
        }

        private void handleAnswer(boolean isCorrect, String correctAnswer, CollectionItemResponse item) {
            recordResult(item.getId(), isCorrect);
            edtAnswer.setEnabled(false);
            layoutActionsFb.setVisibility(View.GONE);

            if (isCorrect) {
                Toast.makeText(MemoryGameActivity.this, getString(R.string.tvExcellentAnswer), Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(MemoryGameActivity.this::moveToNext, 1000);
            } else {
                layoutSkippedResult.setVisibility(View.VISIBLE);
                tvCorrectAnswerFb.setText(correctAnswer);
                btnContinueFb.setVisibility(View.VISIBLE);
                btnContinueFb.setOnClickListener(v -> moveToNext());
            }
        }
    }

    /**
     * MATCHING STRATEGY
     */
    private class MatchingStrategy implements QuestionStrategy {
        private TextView selectedLeft = null;
        private TextView selectedRight = null;
        private Map<TextView, CollectionItemResponse> viewToItemMap = new HashMap<>();
        private int matchCompletedCount = 0;

        @Override
        public void execute(QuestionWrapper question) {
            viewMatch.setVisibility(View.VISIBLE);
            matchCompletedCount = 0;
            viewToItemMap.clear();

            List<CollectionItemResponse> group = question.items; // size 3

            // Xáo trộn Term (Cột trái) và Def (Cột phải)
            List<CollectionItemResponse> leftGroup = new ArrayList<>(group);
            List<CollectionItemResponse> rightGroup = new ArrayList<>(group);
            Collections.shuffle(leftGroup);
            Collections.shuffle(rightGroup);

            for (int i = 0; i < 3; i++) {
                // Setup cột trái
                TextView leftBtn = matchLeftViews[i];
                leftBtn.setText(leftGroup.get(i).getTerm());
                leftBtn.setVisibility(View.VISIBLE);
                leftBtn.setBackgroundResource(R.drawable.bg_option_default);
                leftBtn.setEnabled(true);
                viewToItemMap.put(leftBtn, leftGroup.get(i));

                // Setup cột phải
                TextView rightBtn = matchRightViews[i];
                rightBtn.setText(rightGroup.get(i).getDefinition());
                rightBtn.setVisibility(View.VISIBLE);
                rightBtn.setBackgroundResource(R.drawable.bg_option_default);
                rightBtn.setEnabled(true);
                viewToItemMap.put(rightBtn, rightGroup.get(i));

                // Click logic
                leftBtn.setOnClickListener(v -> handleSelect(leftBtn, true));
                rightBtn.setOnClickListener(v -> handleSelect(rightBtn, false));
            }
        }

        private void handleSelect(TextView view, boolean isLeft) {
            view.setBackgroundResource(R.drawable.bg_match_selected);

            if (isLeft) {
                if (selectedLeft != null && selectedLeft != view) selectedLeft.setBackgroundResource(R.drawable.bg_option_default);
                selectedLeft = view;
            } else {
                if (selectedRight != null && selectedRight != view) selectedRight.setBackgroundResource(R.drawable.bg_option_default);
                selectedRight = view;
            }

            if (selectedLeft != null && selectedRight != null) {
                checkMatch();
            }
        }

        private void checkMatch() {
            CollectionItemResponse leftItem = viewToItemMap.get(selectedLeft);
            CollectionItemResponse rightItem = viewToItemMap.get(selectedRight);

            boolean isCorrect = leftItem.getId().equals(rightItem.getId());
            recordResult(leftItem.getId(), isCorrect);

            if (isCorrect) {
                selectedLeft.setBackgroundResource(R.drawable.bg_option_correct);
                selectedRight.setBackgroundResource(R.drawable.bg_option_correct);
                selectedLeft.setEnabled(false);
                selectedRight.setEnabled(false);

                TextView savedLeft = selectedLeft;
                TextView savedRight = selectedRight;

                new Handler().postDelayed(() -> {
                    savedLeft.setVisibility(View.INVISIBLE);
                    savedRight.setVisibility(View.INVISIBLE);
                }, 500);

                matchCompletedCount++;
                selectedLeft = null;
                selectedRight = null;

                if (matchCompletedCount == 3) {
                    new Handler().postDelayed(MemoryGameActivity.this::moveToNext, 1000);
                }
            } else {
                selectedLeft.setBackgroundResource(R.drawable.bg_option_wrong);
                selectedRight.setBackgroundResource(R.drawable.bg_option_wrong);

                TextView savedLeft = selectedLeft;
                TextView savedRight = selectedRight;

                new Handler().postDelayed(() -> {
                    if (savedLeft.isEnabled()) savedLeft.setBackgroundResource(R.drawable.bg_option_default);
                    if (savedRight.isEnabled()) savedRight.setBackgroundResource(R.drawable.bg_option_default);
                }, 500);

                selectedLeft = null;
                selectedRight = null;
            }
        }
    }

    // ==========================================
    // 5. HELPER & SUMMARY
    // ==========================================

    private void recordResult(Integer itemId, boolean isCorrect) {
        double timeSpent = (double) (System.currentTimeMillis() - questionStartTime);
        bulkResults.add(new StudySessionDetailRequest(sessionId, itemId, isCorrect, timeSpent));
        if(isCorrect) totalCorrectTest++;
        questionStartTime = System.currentTimeMillis(); // Reset timer cho item kế tiếp (đặc biệt cho Match)
    }

    private boolean isFirstRoundLearn() {
        // Nếu toàn MC tức là đang ở vòng 1 Learn
        return !questionQueue.isEmpty() && questionQueue.get(0).type == QuestionType.MULTIPLE_CHOICE;
    }

    private void showLearnMidwaySummary() {
        progressGame.setProgress(100);
        viewSummary.setVisibility(View.VISIBLE);
        tvSummaryTitle.setText(R.string.round_1_done);
        btnContinueSummary.setText(R.string.tvContinueRound2);

        btnContinueSummary.setOnClickListener(v -> {
            // Biến MC queue thành FB queue để làm vòng 2
            for (QuestionWrapper q : questionQueue) {
                q.type = QuestionType.FILL_BLANK;
            }
            currentQuestionIndex = 0;
            loadNextQuestion();
        });
    }

    private void showFinalSummary() {
        progressGame.setProgress(100);
        viewSummary.setVisibility(View.VISIBLE);

        if (currentGameMode != GameMode.LEARN) {
            int percentage = 0;
            if (!bulkResults.isEmpty()) {
                percentage = (int)(((float)totalCorrectTest / bulkResults.size()) * 100);
            }
            tvSummaryTitle.setText(getString(R.string.tvTestFinished) + percentage + "%");
        } else {
            tvSummaryTitle.setText(R.string.tvYouMaster);
        }

        TermListAdapter adapter = new TermListAdapter(allItems);
        rvLearnedTerms.setAdapter(adapter);

        btnContinueSummary.setText(R.string.tvfinish_and_save);
        btnContinueSummary.setOnClickListener(v -> submitAllResultsAndExit());
    }

    private void submitAllResultsAndExit() {
        if (sessionId == null) return;

        // Hiển thị một thông báo nhẹ hoặc loading nếu muốn
        Toast.makeText(this, getString(R.string.tvSavingProgress), Toast.LENGTH_SHORT).show();

        boolean isFullTest = (currentGameMode == GameMode.TEST);

        // Gọi trực tiếp API completeSession qua studyApi đã được Inject sẵn
        studyApi.completeStudySession(sessionId, bulkResults, isFullTest).enqueue(new Callback<ApiResponse<StudySessionResultResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<StudySessionResultResponse>> call, Response<ApiResponse<StudySessionResultResponse>> response) {
                // Đánh dấu đã submit thành công để luồng onStop() ko gọi WorkManager trùng lặp nữa
                hasSubmitted = true;

                // Lưu xong xuôi mới đóng màn hình chơi game để về lại Home
                finish();
            }

            @Override
            public void onFailure(Call<ApiResponse<StudySessionResultResponse>> call, Throwable t) {
                Log.e("DEBUG_GAME", getString(R.string.nt_direct_save_failed_fallback_to_workmanager) + t.getMessage());
                // Nếu lỗi mạng/lỗi kết nối trực tiếp, dùng WorkManager làm cứu cánh để sync sau
                enqueueCompleteWorker();
                finish();
            }
        });
    }

    private void handleClose() {
        if (sessionId != null) enqueueCompleteWorker();
        finish();
    }

    private void enqueueCompleteWorker() {
        if (sessionId == null || bulkResults.isEmpty() || hasSubmitted) return;
        hasSubmitted = true;

        String json = new Gson().toJson(bulkResults);
        boolean isFullTest = (currentGameMode == GameMode.TEST);

        Data inputData = new Data.Builder()
                .putLong(CompleteSessionWorker.KEY_SESSION_ID, sessionId)
                .putString(CompleteSessionWorker.KEY_BULK_RESULTS, json)
                .putBoolean(CompleteSessionWorker.KEY_IS_FULL_TEST, isFullTest)
                .build();

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(CompleteSessionWorker.class)
                .setInputData(inputData)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(this).enqueueUniqueWork(
                getString(R.string.ntCompleteSession) + sessionId,
                ExistingWorkPolicy.KEEP,
                work
        );
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (sessionId != null) enqueueCompleteWorker();
    }
}