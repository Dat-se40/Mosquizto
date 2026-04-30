package com.example.mosquizto.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mosquizto.Dto.request.StartStudySessionRequest;
import com.example.mosquizto.Dto.request.StudySessionDetailRequest;
import com.example.mosquizto.Dto.response.ApiResponse;
import com.example.mosquizto.Dto.response.CollectionItemResponse;
import com.example.mosquizto.Dto.response.StudySessionResponse;
import com.example.mosquizto.Dto.response.StudySessionResultResponse;
import com.example.mosquizto.Network.RetrofitClient;
import com.example.mosquizto.Network.itf.CollectionApi;
import com.example.mosquizto.Network.itf.StudyApi;
import com.example.mosquizto.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;
import jakarta.inject.Inject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class MemoryGameActivity extends AppCompatActivity {

    private Integer collectionId;
    private Long sessionId; // ID session lấy từ API start

    // Data list
    private List<CollectionItemResponse> allItems = new ArrayList<>();
    private List<CollectionItemResponse> currentRoundItems = new ArrayList<>();
    private List<StudySessionDetailRequest> bulkResults = new ArrayList<>();

    // State Variables
    private int currentRound = 1; // 1: Multiple Choice, 2: Fill Blank
    private int currentQuestionIndex = 0;
    private long questionStartTime;
    private CollectionItemResponse currentItem;

    // UI - General
    private TextView tvRoundName;
    private ProgressBar progressGame;
    private View viewMultipleChoice, viewFillBlank, viewSummary;

    // UI - MC
    private TextView tvQuestionMc, btnOption1, btnOption2, btnOption3, btnOption4;
    private View layoutResultMc;
    private ImageView imgResultIconMc;
    private TextView tvResultMessageMc;
    private Button btnContinueMc;
    private TextView[] optionsArray;

    // UI - Fill Blank
    private TextView tvQuestionFb, tvCorrectAnswerFb;
    private EditText edtAnswer;
    private View layoutSkippedResult;
    private TextView btnDontKnow;
    private Button btnSubmitFb, btnContinueFb;

    // UI - Summary
    private Button btnContinueSummary;

    @Inject
    StudyApi studyApi;

    @Inject
    CollectionApi collectionApi ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_game);

        collectionId = getIntent().getIntExtra("COLLECTION_ID",-1);
        ArrayList<CollectionItemResponse> items = getIntent().getParcelableArrayListExtra("ITEMS_LIST");
        if (items != null) {
            this.allItems = items;
        }

        initViews();
        getWindow().getDecorView().post(() -> {
            fetchDataAndStartSession();
        });
    }

    private void initViews() {
        tvRoundName = findViewById(R.id.tvRoundName);
        progressGame = findViewById(R.id.progressGame);
        viewMultipleChoice = findViewById(R.id.viewMultipleChoice);
        viewFillBlank = findViewById(R.id.viewFillBlank);
        viewSummary = findViewById(R.id.viewSummary);
        findViewById(R.id.btnCloseGame).setOnClickListener(v -> handleClose());

        // Setup MC
        tvQuestionMc = findViewById(R.id.tvQuestionMc);
        btnOption1 = findViewById(R.id.btnOption1);
        btnOption2 = findViewById(R.id.btnOption2);
        btnOption3 = findViewById(R.id.btnOption3);
        btnOption4 = findViewById(R.id.btnOption4);
        optionsArray = new TextView[]{btnOption1, btnOption2, btnOption3, btnOption4};
        layoutResultMc = findViewById(R.id.layoutResultMc);
        imgResultIconMc = findViewById(R.id.imgResultIconMc);
        tvResultMessageMc = findViewById(R.id.tvResultMessageMc);
        btnContinueMc = findViewById(R.id.btnContinueMc);

        // Setup FB
        tvQuestionFb = findViewById(R.id.tvQuestionFb);
        edtAnswer = findViewById(R.id.edtAnswer);
        layoutSkippedResult = findViewById(R.id.layoutSkippedResult);
        tvCorrectAnswerFb = findViewById(R.id.tvCorrectAnswerFb);
        btnDontKnow = findViewById(R.id.btnDontKnow);
        btnSubmitFb = findViewById(R.id.btnSubmitFb);
        btnContinueFb = findViewById(R.id.btnContinueFb);

        // Setup Summary
        btnContinueSummary = findViewById(R.id.btnContinueSummary);
    }

    private void fetchDataAndStartSession() {

        String uniqueKey = java.util.UUID.randomUUID().toString();
        Log.d("DEBUG_GAME", "collectionId: " + collectionId + ", uniqueKey: " + uniqueKey);
        // 2. Start Session
        StartStudySessionRequest request = new StartStudySessionRequest(collectionId);
        studyApi.startStudySession(uniqueKey,request).enqueue(new Callback<ApiResponse<Long>>() {
            @Override
            public void onResponse(Call<ApiResponse<Long>> call, Response<ApiResponse<Long>> response) {
                if(response.isSuccessful() && response.body() != null) {
                    sessionId = response.body().getData();
                    Log.d("DEBUG_GAME", "API Start Session Success: " + sessionId);
                    if (sessionId != null)
                    {
                        startRound1();
                    }

                } else {
                    Log.e("DEBUG_GAME", "API Start Session Failed: " + response.code());
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<Long>> call, Throwable t) {
                Log.e("DEBUG_GAME", "API Error: " + t.getMessage());
            }
        });
    }

    private void startRound1() {
        currentRound = 1;
        tvRoundName.setText("Round 1");
        Collections.shuffle(allItems);

        // Lấy tối đa 10 câu cho vòng 1
        currentRoundItems = allItems.subList(0, Math.min(10, allItems.size()));
        currentQuestionIndex = 0;

        viewSummary.setVisibility(View.GONE);
        viewFillBlank.setVisibility(View.GONE);
        viewMultipleChoice.setVisibility(View.VISIBLE);

        loadNextQuestion();
    }

    private void loadNextQuestion() {
        if (currentQuestionIndex >= currentRoundItems.size()) {
            showSummary();
            return;
        }

        currentItem = currentRoundItems.get(currentQuestionIndex);
        questionStartTime = System.currentTimeMillis();

        // Tính % cho Progress Bar
        int progress = (int) (((float) currentQuestionIndex / currentRoundItems.size()) * 100);
        progressGame.setProgress(progress);

        if (currentRound == 1) {
            setupMultipleChoiceUI();
        } else {
            setupFillBlankUI();
        }
    }

    private void setupFillBlankUI() {
    }

    // ==========================================
    // LOGIC VÒNG 1: TRẮC NGHIỆM
    // ==========================================
    private void setupMultipleChoiceUI() {
        layoutResultMc.setVisibility(View.GONE);
        btnContinueMc.setVisibility(View.GONE);

        // Random xem hiện Term hay Def làm câu hỏi
        boolean showTerm = Math.random() > 0.5;
        tvQuestionMc.setText(showTerm ? currentItem.getTerm() : currentItem.getDefinition());
        String correctAnswer = showTerm ? currentItem.getDefinition() : currentItem.getTerm();

        // Tạo list 4 đáp án
        List<String> choices = new ArrayList<>();
        choices.add(correctAnswer);

        // Lấy 3 đáp án sai random từ allItems
        List<CollectionItemResponse> temp = new ArrayList<>(allItems);
        Collections.shuffle(temp);
        int wrongCount = 0;
        for (CollectionItemResponse item : temp) {
            String wrongAnswer = showTerm ? item.getDefinition() : item.getTerm();
            if (!wrongAnswer.equals(correctAnswer) && wrongCount < 3) {
                choices.add(wrongAnswer);
                wrongCount++;
            }
        }
        Collections.shuffle(choices);

        // Gắn vào 4 nút
        for (int i = 0; i < 4; i++) {
            TextView btn = optionsArray[i];
            btn.setText(choices.get(i));
            btn.setBackgroundResource(R.drawable.bg_option_default); // Reset style
            btn.setEnabled(true);

            btn.setOnClickListener(v -> handleMcAnswer(btn, btn.getText().toString().equals(correctAnswer)));
        }
    }

    private void handleMcAnswer(TextView clickedBtn, boolean isCorrect) {
        double timeSpent = System.currentTimeMillis() - questionStartTime;
        bulkResults.add(new StudySessionDetailRequest(sessionId,currentItem.getId(), isCorrect, timeSpent));

        for (TextView btn : optionsArray) btn.setEnabled(false); // Khoá click

        layoutResultMc.setVisibility(View.VISIBLE);

        if (isCorrect) {
            clickedBtn.setBackgroundResource(R.drawable.bg_option_correct);
            tvResultMessageMc.setText("You've got this!");
            tvResultMessageMc.setTextColor(Color.parseColor("#4CAF50")); // Xanh
            // Tự động chuyển câu sau 1 giây
            new Handler().postDelayed(() -> {
                currentQuestionIndex++;
                loadNextQuestion();
            }, 1000);
        } else {
            clickedBtn.setBackgroundResource(R.drawable.bg_option_wrong);
            tvResultMessageMc.setText("Not quite, you're still learning");
            tvResultMessageMc.setTextColor(Color.parseColor("#F44336")); // Đỏ

            // Tìm và bôi nét đứt cho đáp án đúng
            String correctAnsText = currentItem.getDefinition(); // Giả định
            for (TextView btn : optionsArray) {
                if (btn.getText().toString().equals(correctAnsText)) {
                    btn.setBackgroundResource(R.drawable.bg_option_dashed_correct);
                }
            }

            btnContinueMc.setVisibility(View.VISIBLE);
            btnContinueMc.setOnClickListener(v -> {
                currentQuestionIndex++;
                loadNextQuestion();
            });
        }
    }

    // ==========================================
    // LOGIC VÒNG 2: ĐIỀN TỪ
    // ==========================================
    // Tương tự, nếu bạn cần tôi viết tiếp hàm logic cho Vòng 2, báo tôi nhé! (Để tránh giới hạn ký tự quá dài).

    // ==========================================
    // SUMMARY VÀ HOÀN THÀNH (LƯU BULK INSERT)
    // ==========================================
    private void showSummary() {
        progressGame.setProgress(100);
        viewMultipleChoice.setVisibility(View.GONE);
        viewFillBlank.setVisibility(View.GONE);
        viewSummary.setVisibility(View.VISIBLE);

        if (currentRound == 1 && currentQuestionIndex < currentRoundItems.size()) {
            btnContinueSummary.setText("Continue to round 2");
            btnContinueSummary.setOnClickListener(v -> {
                // Sang vòng 2 (Fill blank)
                currentRound = 2;
                tvRoundName.setText("Round 2");
                currentQuestionIndex = 0;
                viewSummary.setVisibility(View.GONE);
                viewFillBlank.setVisibility(View.VISIBLE);
                loadNextQuestion();
            });
        } else {
            btnContinueSummary.setText("Finish");
            btnContinueSummary.setOnClickListener(v -> submitAllResultsAndExit());
        }
    }

    private void submitAllResultsAndExit() {
        if (sessionId == null) return;

        // Gọi API /complete với toàn bộ bulkResults
        studyApi.completeStudySession(sessionId, bulkResults).enqueue(new Callback<ApiResponse<StudySessionResultResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<StudySessionResultResponse>> call, Response<ApiResponse<StudySessionResultResponse>> response) {
                if(response.isSuccessful()){
                    Toast.makeText(MemoryGameActivity.this, "Completed!", Toast.LENGTH_SHORT).show();
                    finish(); // Thoát game
                }else
                {
                    Log.e("DEBUG_GAME", "API Complete Session Failed: " + response.code());
                }
            }
            @Override public void onFailure(Call<ApiResponse<StudySessionResultResponse>> call, Throwable t)
            {
                Log.e("DEBUG_GAME", "API Error: " + t.getMessage());
            }
        });
    }
    private void handleClose() {
        // Có thể show dialog hỏi có chắc thoát không. Nếu thoát thì cũng có thể gọi complete sớm để lưu quá trình
        submitAllResultsAndExit();
    }
}