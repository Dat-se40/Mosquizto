package com.example.mosquizto.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mosquizto.Dto.response.ApiResponse;
import com.example.mosquizto.Dto.response.StreakResponse;
import com.example.mosquizto.R;
import com.example.mosquizto.Network.itf.UserApi;
import com.example.mosquizto.Util.ApiErrorHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class AchievementActivity extends AppCompatActivity {

    private TextView tvCurrentStreakDays;
    private TextView tvStudiedToday;
    private TextView tvLongestStreak;
    private TextView tvTotalStudyDays;
    private TextView tvTotalStudySessions;
    private TextView tvCompletedSessions;
    private TextView tvLastStudiedAt;
    private TextView tvNextMilestone;

    @Inject
    UserApi userApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_achievement);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.btnBack).getRootView(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });

        initViews();
        setupListeners();
        loadStreakData();
    }

    private void initViews() {
        tvCurrentStreakDays   = findViewById(R.id.tvCurrentStreakDays);
        tvStudiedToday        = findViewById(R.id.tvStudiedToday);
        tvLongestStreak       = findViewById(R.id.tvLongestStreak);
        tvTotalStudyDays      = findViewById(R.id.tvTotalStudyDays);
        tvTotalStudySessions  = findViewById(R.id.tvTotalStudySessions);
        tvCompletedSessions   = findViewById(R.id.tvCompletedSessions);
        tvLastStudiedAt       = findViewById(R.id.tvLastStudiedAt);
        tvNextMilestone       = findViewById(R.id.tvNextMilestone);
    }

    private void setupListeners() {
        findViewById(R.id.btnBack).setOnClickListener(v ->
                getOnBackPressedDispatcher().onBackPressed()
        );
    }

    private void loadStreakData() {
        userApi.getUserStreak().enqueue(new Callback<ApiResponse<StreakResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<StreakResponse>> call,
                                   Response<ApiResponse<StreakResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    StreakResponse streak = response.body().getData();
                    if (streak != null) {
                        bindStreakData(streak);
                    }
                } else {
                    Toast.makeText(AchievementActivity.this,
                            ApiErrorHelper.extractMessage(response), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<StreakResponse>> call, Throwable t) {
                Toast.makeText(AchievementActivity.this,
                        ApiErrorHelper.networkError(AchievementActivity.this), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindStreakData(StreakResponse streak) {
        // currentStreakDays
        tvCurrentStreakDays.setText(String.valueOf(streak.getCurrentStreakDays()));

        // studiedToday — hiện badge nếu true
        if (streak.isStudiedToday()) {
            tvStudiedToday.setVisibility(View.VISIBLE);
        } else {
            tvStudiedToday.setVisibility(View.GONE);
        }

        // longestStreakDays
        tvLongestStreak.setText(String.valueOf(streak.getLongestStreakDays()));

        // totalStudyDays
        tvTotalStudyDays.setText(String.valueOf(streak.getTotalStudyDays()));

        // totalStudySessions
        tvTotalStudySessions.setText(String.valueOf(streak.getTotalStudySessions()));

        // completedStudySessions
        tvCompletedSessions.setText(String.valueOf(streak.getCompletedStudySessions()));

        // lastStudiedAt
        String lastStudied = streak.getLastStudiedAt();
        if (lastStudied != null) {
            tvLastStudiedAt.setText(formatIsoDate(lastStudied));
        } else {
            tvLastStudiedAt.setText("Chưa có dữ liệu");
        }

        // nextMilestoneDays
        int milestone = streak.getNextMilestoneDays();
        if (milestone > 0) {
            tvNextMilestone.setText(
                    getString(R.string.streak_next_milestone_value, milestone)
            );
        } else {
            tvNextMilestone.setText(R.string.streak_next_milestone_none);
        }
    }

    /**
     * Chuyển chuỗi ISO 8601 sang "10/06/2026 13:28"
     * Thử nhiều format đề phòng server trả milliseconds không đủ 3 chữ số
     */
    private String formatIsoDate(String isoDate) {
        if (isoDate == null || isoDate.isEmpty()) return "--";

        String[] formats = {
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd'T'HH:mm:ssX",
                "yyyy-MM-dd'T'HH:mm:ss.SSSX"
        };

        for (String fmt : formats) {
            try {
                SimpleDateFormat sdfIn = new SimpleDateFormat(fmt, Locale.getDefault());
                sdfIn.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date date = sdfIn.parse(isoDate);
                if (date != null) {
                    SimpleDateFormat sdfOut = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                    sdfOut.setTimeZone(TimeZone.getDefault());
                    return sdfOut.format(date);
                }
            } catch (ParseException ignored) {}
        }

        return isoDate; // fallback
    }
}