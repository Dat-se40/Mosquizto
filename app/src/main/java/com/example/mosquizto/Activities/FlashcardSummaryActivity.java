package com.example.mosquizto.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mosquizto.R;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

public class FlashcardSummaryActivity extends AppCompatActivity {

    private TextView       tvTitle, tvKnownCount, tvLearningCount, tvSummaryCounter;
    private MaterialButton btnContinue, btnStudyMode;
    private TextView       tvReset, tvBack;

    private int total, known, learning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcard_summary);

        total    = getIntent().getIntExtra("total",    0);
        known    = getIntent().getIntExtra("known",    0);
        learning = getIntent().getIntExtra("learning", 0);

        initViews();
        displayData();
        setupClickListeners();
    }

    private void initViews() {
        tvSummaryCounter = findViewById(R.id.tv_summary_counter);
        tvTitle         = findViewById(R.id.tv_summary_title);
        tvKnownCount    = findViewById(R.id.tv_known_count);
        tvLearningCount = findViewById(R.id.tv_learning_count);
        btnContinue     = findViewById(R.id.btn_continue_review);
        btnStudyMode    = findViewById(R.id.btn_study_mode);
        tvReset         = findViewById(R.id.tv_reset_flashcard);
        tvBack          = findViewById(R.id.tv_back_last);
    }

    private void displayData() {
        int percent = total > 0 ? (known * 100) / total : 0;

        if (tvSummaryCounter != null) tvSummaryCounter.setText(total + " / " + total);

        if (percent >= 80) {
            tvTitle.setText(R.string.summary_title_excellent);
        } else if (percent >= 60) {
            tvTitle.setText(R.string.tv_summary_title);
        } else {
            tvTitle.setText(R.string.summary_title_keep_practicing);
        }

        tvKnownCount   .setText(String.valueOf(known));
        tvLearningCount.setText(String.valueOf(learning));

        // Ẩn nút "Tiếp tục ôn" nếu không còn thẻ đang học
        if (learning == 0) {
            btnContinue.setVisibility(View.GONE);
        } else {
            btnContinue.setVisibility(View.VISIBLE);
        }
    }

    private void setupClickListeners() {

        // ── Tiếp tục ôn: chỉ load thẻ đang học ─────────────────────────────
        btnContinue.setOnClickListener(v -> {
            // "learning_terms"/"learning_defs" được build từ flashcards.isLearning()
            // trong FlashcardActivity.openSummary() — luôn chính xác, không phụ thuộc shuffle
            ArrayList<String> learningTerms = getIntent().getStringArrayListExtra("learning_terms");
            ArrayList<String> learningDefs  = getIntent().getStringArrayListExtra("learning_defs");

            // Truyền tiếp orig_terms/orig_defs để session sau vẫn có thể "Đặt lại"
            ArrayList<String> origTerms = getIntent().getStringArrayListExtra("orig_terms");
            ArrayList<String> origDefs  = getIntent().getStringArrayListExtra("orig_defs");

            Intent intent = new Intent(this, FlashcardActivity.class);
            intent.putStringArrayListExtra("terms",       learningTerms); // session này chỉ học thẻ đang học
            intent.putStringArrayListExtra("definitions", learningDefs);
            intent.putStringArrayListExtra("orig_terms",  origTerms);     // giữ nguyên full list gốc
            intent.putStringArrayListExtra("orig_defs",   origDefs);
            startActivity(intent);
            finish();
        });

        // ── Ôn luyện chế độ Học → về màn trước ─────────────────────────────
        btnStudyMode.setOnClickListener(v -> finish());

        // ── Đặt lại: luôn dùng full list gốc ───────────────────────────────
        tvReset.setOnClickListener(v -> {
            // "orig_terms"/"orig_defs" là full list từ StudySetDetailActivity
            // được truyền suốt qua các session, không bao giờ bị thay thế
            ArrayList<String> origTerms = getIntent().getStringArrayListExtra("orig_terms");
            ArrayList<String> origDefs  = getIntent().getStringArrayListExtra("orig_defs");

            Intent intent = new Intent(this, FlashcardActivity.class);
            intent.putStringArrayListExtra("terms",       origTerms); // reset = full list gốc
            intent.putStringArrayListExtra("definitions", origDefs);
            intent.putStringArrayListExtra("orig_terms",  origTerms); // vẫn giữ để session sau dùng
            intent.putStringArrayListExtra("orig_defs",   origDefs);
            startActivity(intent);
            finish();
        });

        tvBack.setOnClickListener(v -> {
            // Truyền lại toàn bộ data như "Tiếp tục ôn" nhưng yêu cầu bắt đầu từ thẻ cuối
            ArrayList<String> origTerms = getIntent().getStringArrayListExtra("orig_terms");
            ArrayList<String> origDefs  = getIntent().getStringArrayListExtra("orig_defs");

            Intent intent = new Intent(this, FlashcardActivity.class);
            intent.putStringArrayListExtra("terms",       origTerms);
            intent.putStringArrayListExtra("definitions", origDefs);
            intent.putStringArrayListExtra("orig_terms",  origTerms);
            intent.putStringArrayListExtra("orig_defs",   origDefs);
            // Truyền index thẻ cuối để FlashcardActivity biết bắt đầu từ đâu
            intent.putExtra("start_index", total - 1);
            // Tắt shuffle để giữ đúng thứ tự
            intent.putExtra("force_no_shuffle", true);
            startActivity(intent);
            finish();
        });

        View btnClose = findViewById(R.id.btn_close);
        if (btnClose != null) btnClose.setOnClickListener(v -> finish());
    }
}