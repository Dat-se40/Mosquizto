package com.example.mosquizto.Activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.mosquizto.Dialogs.FlashcardSettingsDialog;
import com.example.mosquizto.Models.Flashcard;
import com.example.mosquizto.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FlashcardActivity extends AppCompatActivity
        implements FlashcardSettingsDialog.SettingsListener {

    private TextView    tvCounter, tvSwipeHint, tvLearningCount, tvKnownCount;
    private FrameLayout cardContainer;
    private CardView    cardFront, cardBack;
    private TextView    tvFrontContent, tvBackContent;
    private ImageButton btnClose, btnSettings, btnPrev, btnNext;
    private View        progressFill;

    private final List<Flashcard> flashcards = new ArrayList<>();
    private final List<Flashcard> originalFlashcards = new ArrayList<>();
    private int currentIndex = 0;
    private boolean isFlipped = false;

    private boolean isAnimating = false;

    private int knownCount    = 0;
    private int learningCount = 0;

    private boolean showTermFirst = true;
    private boolean ttsEnabled    = true;
    private boolean shuffleCards  = true;

    private android.os.Handler autoPlayHandler;
    private Runnable           autoPlayRunnable;
    private boolean            isAutoPlaying = false;

    private GestureDetector gestureDetector;
    private static final int SWIPE_THRESHOLD          = 80;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcard);
        initViews();
        loadFlashcardsFromIntent();
        setupGestureDetector();
        setupClickListeners();
        showCurrentCard();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Chỉ cancel handler để tránh leak, không gọi stopAutoPlayByUser()
        if (autoPlayHandler != null && autoPlayRunnable != null) {
            autoPlayHandler.removeCallbacks(autoPlayRunnable);
        }
    }

    private void initViews() {
        tvCounter       = findViewById(R.id.tv_counter);
        tvSwipeHint     = findViewById(R.id.tv_swipe_hint);
        tvLearningCount = findViewById(R.id.tv_learning_count);
        tvKnownCount    = findViewById(R.id.tv_known_count);
        cardContainer   = findViewById(R.id.card_view);
        cardFront       = findViewById(R.id.card_front);
        cardBack        = findViewById(R.id.card_back);
        tvFrontContent  = findViewById(R.id.tv_front_content);
        tvBackContent   = findViewById(R.id.tv_back_content);
        btnClose        = findViewById(R.id.btn_close);
        btnSettings     = findViewById(R.id.btn_settings);
        btnPrev         = findViewById(R.id.btn_prev);
        btnNext         = findViewById(R.id.btn_next);
        progressFill    = findViewById(R.id.progress_fill);

        float cameraDistance = 8000 * getResources().getDisplayMetrics().density;
        cardFront.setCameraDistance(cameraDistance);
        cardBack.setCameraDistance(cameraDistance);
    }

    private void loadFlashcardsFromIntent() {
        // Đọc "terms"/"definitions" — key này được dùng cho cả:
        // 1) Session đầu từ StudySetDetailActivity (full list)
        // 2) Session "Tiếp tục ôn" (chỉ learning subset)
        // 3) Session "Đặt lại" (full list, lấy từ orig_terms)
        ArrayList<String> terms       = getIntent().getStringArrayListExtra("terms");
        ArrayList<String> definitions = getIntent().getStringArrayListExtra("definitions");

        if (terms != null && definitions != null) {
            int size = Math.min(terms.size(), definitions.size());
            for (int i = 0; i < size; i++) {
                flashcards.add(new Flashcard(terms.get(i), definitions.get(i)));
            }
        }

        // Fallback sample data — chỉ khi không có dữ liệu nào
        if (flashcards.isEmpty()) {
            flashcards.add(new Flashcard("honor",               "(v/n) vinh danh/ danh dự"));
            flashcards.add(new Flashcard("complex/complicated", "(adj) phức tạp"));
            flashcards.add(new Flashcard("ambitious",           "(adj) tham vọng"));
            flashcards.add(new Flashcard("perseverance",        "(n) sự kiên trì"));
            flashcards.add(new Flashcard("collaborate",         "(v) hợp tác"));
        }

        boolean forceNoShuffle = getIntent().getBooleanExtra("force_no_shuffle", false);
        originalFlashcards.addAll(flashcards);
        if (shuffleCards && !forceNoShuffle) Collections.shuffle(flashcards);

        // Nếu có start_index, nhảy thẳng đến thẻ đó
        int startIndex = getIntent().getIntExtra("start_index", 0);
        if (startIndex > 0 && startIndex < flashcards.size()) {
            currentIndex = startIndex;
        }
    }

    private void setupGestureDetector() {
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override public boolean onDown(MotionEvent e) { return true; }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (!isAnimating) flipCard();
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2,
                                   float velocityX, float velocityY) {
                if (isAnimating || isFinishing() || isDestroyed()) return false;
                if (e1 == null || e2 == null) return false;
                float diffX = e2.getX() - e1.getX();
                float diffY = e2.getY() - e1.getY();
                if (Math.abs(diffX) > Math.abs(diffY)
                        && Math.abs(diffX) > SWIPE_THRESHOLD
                        && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    swipeCard(diffX > 0);
                    return true;
                }
                return false;
            }
        });

        cardContainer.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return true;
        });
    }

    private void setupClickListeners() {
        btnClose.setOnClickListener(v -> showExitDialog());

        btnSettings.setOnClickListener(v -> {
            if (isAnimating) return;
            FlashcardSettingsDialog dialog = new FlashcardSettingsDialog(
                    this, showTermFirst, ttsEnabled, shuffleCards, this);
            dialog.show();
        });

        btnPrev.setOnClickListener(v -> {
            if (isAnimating) return;
            if (currentIndex > 0) {
                currentIndex--;
                // Hoàn tác trạng thái thẻ vừa quay lại
                Flashcard prevCard = flashcards.get(currentIndex);
                if (prevCard.getStatus() == Flashcard.STATUS_KNOWN) {
                    knownCount--;
                    prevCard.setStatus(Flashcard.STATUS_UNSEEN);
                } else if (prevCard.getStatus() == Flashcard.STATUS_LEARNING) {
                    learningCount--;
                    prevCard.setStatus(Flashcard.STATUS_UNSEEN);
                }
                isFlipped = false;
                showCurrentCard();
            }
        });

        btnNext.setOnClickListener(v -> {
            if (isAnimating) return;
            if (isAutoPlaying) {
                stopAutoPlayByUser();
            } else {
                startAutoPlay();
            }
        });
    }

    private void flipCard() {
        isAnimating = true;
        View fromView = isFlipped ? cardBack  : cardFront;
        View toView   = isFlipped ? cardFront : cardBack;

        ObjectAnimator rotateOut = ObjectAnimator.ofFloat(fromView, "rotationY", 0f, 90f);
        rotateOut.setDuration(200);
        rotateOut.setInterpolator(new AccelerateDecelerateInterpolator());

        ObjectAnimator rotateIn = ObjectAnimator.ofFloat(toView, "rotationY", -90f, 0f);
        rotateIn.setDuration(200);
        rotateIn.setInterpolator(new AccelerateDecelerateInterpolator());

        rotateOut.addListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(Animator animation) {
                if (isFinishing() || isDestroyed()) return;
                fromView.setVisibility(View.GONE);
                toView.setVisibility(View.VISIBLE);
                rotateIn.start();
            }
        });
        rotateIn.addListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(Animator animation) {
                isAnimating = false;
            }
        });

        rotateOut.start();
        isFlipped = !isFlipped;
        tvSwipeHint.setText(isFlipped
                ? getString(R.string.hint_swipe_flipped)
                : getString(R.string.hint_tap_to_flip));
    }

    private void swipeCard(boolean known) {
        isAnimating = true;

        Flashcard card = flashcards.get(currentIndex);
        if (known) {
            card.setStatus(Flashcard.STATUS_KNOWN);
            knownCount++;
        } else {
            card.setStatus(Flashcard.STATUS_LEARNING);
            learningCount++;
        }
        final int swipedIndex = currentIndex;

        float screenW  = getResources().getDisplayMetrics().widthPixels;
        float targetX  = known ? screenW * 1.5f : -screenW * 1.5f;
        float rotation = known ? 20f : -20f;

        // CẬP NHẬT MÀU VIỀN: VUỐT PHẢI = XANH LÁ, VUỐT TRÁI = CAM
        int strokeColor;
        if (known) {
            strokeColor = android.graphics.Color.parseColor("#4CAF50"); // Đổi thành màu xanh lá cây khi biết
        } else {
            strokeColor = android.graphics.Color.parseColor("#FF9800"); // Giữ màu viền cam khi đang học
        }

        // Ép kiểu sang MaterialCardView để cập nhật màu viền (stroke)
        if (cardFront instanceof com.google.android.material.card.MaterialCardView) {
            ((com.google.android.material.card.MaterialCardView) cardFront)
                    .setStrokeColor(android.content.res.ColorStateList.valueOf(strokeColor));
        }
        if (cardBack instanceof com.google.android.material.card.MaterialCardView) {
            ((com.google.android.material.card.MaterialCardView) cardBack)
                    .setStrokeColor(android.content.res.ColorStateList.valueOf(strokeColor));
        }

        AnimatorSet animOut = new AnimatorSet();
        animOut.playTogether(
                ObjectAnimator.ofFloat(cardContainer, "translationX", 0f, targetX),
                ObjectAnimator.ofFloat(cardContainer, "alpha",        1f, 0f),
                ObjectAnimator.ofFloat(cardContainer, "rotation",     0f, rotation)
        );
        animOut.setDuration(350);
        animOut.addListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(Animator animation) {
                if (isFinishing() || isDestroyed()) return;

                cardContainer.setTranslationX(0f);
                cardContainer.setAlpha(1f);
                cardContainer.setRotation(0f);

                // Trả về viền màu xám mặc định của hệ thống khi thẻ tiếp theo xuất hiện
                int defaultStroke = getColor(R.color.quizlet_border);
                if (cardFront instanceof com.google.android.material.card.MaterialCardView) {
                    ((com.google.android.material.card.MaterialCardView) cardFront)
                            .setStrokeColor(android.content.res.ColorStateList.valueOf(defaultStroke));
                }
                if (cardBack instanceof com.google.android.material.card.MaterialCardView) {
                    ((com.google.android.material.card.MaterialCardView) cardBack)
                            .setStrokeColor(android.content.res.ColorStateList.valueOf(defaultStroke));
                }

                isAnimating = false;
                updateCounters();

                int nextIndex = swipedIndex + 1;
                currentIndex  = nextIndex;
                if (currentIndex >= flashcards.size()) {
                    openSummary();
                } else {
                    isFlipped = false;
                    showCurrentCard();
                }
            }

            @Override public void onAnimationCancel(Animator animation) {
                isAnimating = false;
            }
        });
        animOut.start();
    }

    private void showCurrentCard() {
        if (flashcards.isEmpty() || currentIndex >= flashcards.size()) return;
        Flashcard card = flashcards.get(currentIndex);
        tvFrontContent.setText(showTermFirst ? card.getTerm()       : card.getDefinition());
        tvBackContent .setText(showTermFirst ? card.getDefinition() : card.getTerm());

        cardFront.setVisibility(View.VISIBLE);
        cardBack .setVisibility(View.GONE);
        cardFront.setRotationY(0f);
        cardBack .setRotationY(0f);

        tvCounter  .setText((currentIndex + 1) + " / " + flashcards.size());
        tvSwipeHint.setText(getString(R.string.hint_tap_to_flip));

        updateProgress();
        updateCounters();
    }

    private void updateProgress() {
        if (progressFill == null || flashcards.isEmpty()) return;
        // Dùng progress_background làm chuẩn width, không dùng parent
        View progressBg = findViewById(R.id.progress_background);
        if (progressBg == null) return;
        progressBg.post(() -> {
            if (isFinishing() || isDestroyed()) return;
            int bgWidth = progressBg.getWidth();
            if (bgWidth == 0) return;
            float ratio = (float) currentIndex / flashcards.size();
            progressFill.getLayoutParams().width = (int)(bgWidth * ratio);
            progressFill.requestLayout();
        });
    }

    private void updateCounters() {
        tvLearningCount.setText(String.valueOf(learningCount));
        tvKnownCount   .setText(String.valueOf(knownCount));
    }

    private void openSummary() {
        // Build list learning từ flashcards hiện tại (chính xác sau shuffle)
        ArrayList<String> learningTerms = new ArrayList<>();
        ArrayList<String> learningDefs  = new ArrayList<>();
        for (Flashcard f : flashcards) {
            if (f.isLearning()) {
                learningTerms.add(f.getTerm());
                learningDefs .add(f.getDefinition());
            }
        }

        // Lấy full list gốc để truyền cho nút "Đặt lại"
        // Ưu tiên "orig_terms" nếu session này là "Tiếp tục ôn"
        // fallback về "terms" nếu đây là session đầu từ StudySetDetailActivity
        ArrayList<String> origTerms = getIntent().getStringArrayListExtra("orig_terms");
        if (origTerms == null) origTerms = getIntent().getStringArrayListExtra("terms");
        ArrayList<String> origDefs = getIntent().getStringArrayListExtra("orig_defs");
        if (origDefs == null) origDefs = getIntent().getStringArrayListExtra("definitions");

        Intent intent = new Intent(this, FlashcardSummaryActivity.class);
        intent.putExtra("total",    flashcards.size());
        intent.putExtra("known",    knownCount);
        intent.putExtra("learning", learningCount);
        // List chỉ chứa thẻ đang học → cho nút "Tiếp tục ôn"
        intent.putStringArrayListExtra("learning_terms", learningTerms);
        intent.putStringArrayListExtra("learning_defs",  learningDefs);
        // Full list gốc → cho nút "Đặt lại"
        intent.putStringArrayListExtra("orig_terms", origTerms);
        intent.putStringArrayListExtra("orig_defs",  origDefs);
        startActivity(intent);
        finish();
    }

    private void startAutoPlay() {
        isAutoPlaying = true;
        btnNext.setImageResource(R.drawable.ic_pause);
        tvSwipeHint.setText(R.string.auto_scroll_is_on);
        if (autoPlayHandler == null) autoPlayHandler = new android.os.Handler();

        if (autoPlayRunnable != null) {
            autoPlayHandler.removeCallbacks(autoPlayRunnable);
        }

        scheduleNextAutoPlay();
    }

    private void scheduleNextAutoPlay() {
        if (!isAutoPlaying) return;

        autoPlayRunnable = () -> {
            if (isFinishing() || isDestroyed() || !isAutoPlaying) return;

            // Nếu đang animate (user vừa swipe tay) → đợi thêm 1s rồi thử lại
            if (isAnimating) {
                scheduleNextAutoPlay();
                return;
            }

            // Bước 1: lật thẻ nếu chưa lật
            if (!isFlipped) {
                flipCard();
            }

            // Bước 2: sau 2s mới swipe — dùng runnable mới, không reuse cái cũ
            autoPlayHandler.postDelayed(() -> {
                if (isFinishing() || isDestroyed() || !isAutoPlaying) return;
                if (isAnimating) {
                    // Vẫn đang animate → hoãn lại
                    scheduleNextAutoPlay();
                    return;
                }
                swipeCard(true);
                // Lên lịch thẻ tiếp theo sau khi swipe xong
                // swipeCard có duration 350ms, đợi nó xong rồi mới schedule
                autoPlayHandler.postDelayed(() -> scheduleNextAutoPlay(), 1000);
            }, 2000);
        };

        autoPlayHandler.postDelayed(autoPlayRunnable, 2000);
    }

    private void stopAutoPlay() {
        isAutoPlaying = false;
        if (autoPlayHandler != null && autoPlayRunnable != null) {
            autoPlayHandler.removeCallbacks(autoPlayRunnable);
        }
    }

    private void stopAutoPlayByUser() {
        stopAutoPlay();
        if (btnNext != null) btnNext.setImageResource(R.drawable.ic_play_arrow);
        if (tvSwipeHint != null) tvSwipeHint.setText(R.string.auto_scroll_is_off);
    }

    private void showExitDialog() {
        // Tạo dialog từ Builder
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        // Nạp custom layout
        View view = getLayoutInflater().inflate(R.layout.dialog_exit_confirmation, null);
        builder.setView(view);

        androidx.appcompat.app.AlertDialog dialog = builder.create();

        // Làm trong suốt background mặc định để lộ viền bo góc của custom layout
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Ánh xạ các view trong dialog
        TextView tvMessage = view.findViewById(R.id.tvDialogMessage);
        Button btnCancel = view.findViewById(R.id.btnCancelExit);
        Button btnExit = view.findViewById(R.id.btnConfirmExit);

        // Customize nội dung text riêng cho Flashcard (vì Flashcard không lưu kết quả lên server)
        tvMessage.setText(R.string.ask_to_exit_2);

        // Bắt sự kiện cho nút
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnExit.setOnClickListener(v -> {
            dialog.dismiss();
            finish(); // Thoát khỏi Activity
        });

        dialog.show();
    }

    @Override
    public void onSettingsChanged(boolean showTermFirst, boolean ttsEnabled, boolean shuffle) {
        this.showTermFirst = showTermFirst;
        this.ttsEnabled    = ttsEnabled;
        if (this.shuffleCards != shuffle) {
            this.shuffleCards = shuffle;
            // Reset lại danh sách từ bản gốc
            flashcards.clear();
            flashcards.addAll(originalFlashcards);

            if (shuffle) {
                Collections.shuffle(flashcards);
            }
            currentIndex = 0; // Quay về thẻ đầu tiên khi đổi chế độ trộn
        }
        isFlipped   = false;
        isAnimating = false;
        showCurrentCard();
    }

    @Override
    public void onResetCards() {
        for (Flashcard f : flashcards) f.reset();
        knownCount    = 0;
        learningCount = 0;
        currentIndex  = 0;
        isFlipped     = false;
        isAnimating   = false;
        showCurrentCard();
        updateCounters();
    }
}