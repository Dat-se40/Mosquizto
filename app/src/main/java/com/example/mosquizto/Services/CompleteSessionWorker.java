package com.example.mosquizto.Services;

import android.content.Context;
import android.os.Debug;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.hilt.work.HiltWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.mosquizto.Dto.request.StudySessionDetailRequest;
import com.example.mosquizto.Dto.response.ApiResponse;
import com.example.mosquizto.Dto.response.StudySessionResultResponse;
import com.example.mosquizto.Network.itf.StudyApi;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import retrofit2.Response;

@HiltWorker
public class CompleteSessionWorker extends Worker {

    public static final String KEY_SESSION_ID = "session_id";
    public static final String KEY_BULK_RESULTS = "bulk_results_json";

    public static final String KEY_IS_FULL_TEST = "is_full_test";
    public static  final String TAG = "COMPLETE_SESSION_WORKER";
    private final StudyApi studyApi;

    @AssistedInject
    public CompleteSessionWorker(
            @Assisted @NonNull Context context,
            @Assisted @NonNull WorkerParameters params,
            StudyApi studyApi
    ) {
        super(context, params);
        this.studyApi = studyApi;
    }

    @NonNull
    @Override
    public Result doWork() {
        long sessionId = getInputData().getLong(KEY_SESSION_ID, -1);
        String json = getInputData().getString(KEY_BULK_RESULTS);
        Boolean isFullTest = getInputData().getBoolean(KEY_IS_FULL_TEST, false);
        Log.d(TAG, "sessionId: " + sessionId + ", json: " + json);
        if (sessionId == -1 || json == null) return Result.failure();

        try {
            Gson gson = new Gson();
            Type type = new TypeToken<List<StudySessionDetailRequest>>(){}.getType();
            List<StudySessionDetailRequest> results = gson.fromJson(json, type);

            // Gọi đồng bộ bằng execute() vì đang ở background thread của Worker
            Response<ApiResponse<StudySessionResultResponse>> response =
                    studyApi.completeStudySession(sessionId, results,isFullTest).execute();
            Log.d(TAG, "API Response: " + response.code());
            if (response.isSuccessful()) return Result.success();
            else if (response.code() >= 500) return Result.retry(); // Lỗi server thì thử lại
            else return Result.failure(); // Lỗi 4xx thì không cần thử lại

        } catch (Exception e) {
            Log.d(TAG, "API Error: " + e.getMessage());
            return Result.retry(); // Lỗi mạng thì thử lại
        }
    }
}