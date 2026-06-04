package com.example.mosquizto.ViewModels;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mosquizto.Dto.response.ApiResponse;
import com.example.mosquizto.Dto.response.CollectionReportResponse;
import com.example.mosquizto.Dto.response.ShareCollectionResponse;
import com.example.mosquizto.Network.itf.CollectionApi;
import com.example.mosquizto.Util.NotificationWrapper;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@HiltViewModel
public class NotificationViewModel extends ViewModel {

    private static final String TAG = "NotificationVM";
    private final CollectionApi collectionApi;

    private final MutableLiveData<List<NotificationWrapper>> _notifications = new MutableLiveData<>();
    public LiveData<List<NotificationWrapper>> notifications = _notifications;

    // Biến đếm số lượng thông báo (Cái chuông)
    private final MutableLiveData<Integer> _unreadCount = new MutableLiveData<>(0);
    public LiveData<Integer> unreadCount = _unreadCount;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading = _isLoading;

    // Lưu tạm data của 2 luồng
    private List<ShareCollectionResponse> currentInvites = new ArrayList<>();
    private List<CollectionReportResponse> currentReports = new ArrayList<>();

    @Inject
    public NotificationViewModel(CollectionApi collectionApi) {
        this.collectionApi = collectionApi;
    }

    public void fetchAllNotifications() {
        Log.d(TAG, "fetchAllNotifications: Start fetching all notifications");
        _isLoading.setValue(true);
        fetchInvitations();
        fetchReports();
    }

    private void fetchInvitations() {
        Log.d(TAG, "fetchInvitations: Calling API to get invitations...");
        collectionApi.getMyInvitations().enqueue(new Callback<ApiResponse<List<ShareCollectionResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<ShareCollectionResponse>>> call, Response<ApiResponse<List<ShareCollectionResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentInvites = response.body().getData();
                    Log.d(TAG, "fetchInvitations SUCCESS: Received " + (currentInvites != null ? currentInvites.size() : 0) + " items");
                } else {
                    Log.e(TAG, "fetchInvitations FAILED: Code " + response.code() + " - " + response.message());
                    currentInvites.clear();
                }
                combineAndPost();
            }
            @Override
            public void onFailure(Call<ApiResponse<List<ShareCollectionResponse>>> call, Throwable t) {
                Log.e(TAG, "fetchInvitations ERROR: " + t.getMessage());
                currentInvites.clear();
                combineAndPost();
            }
        });
    }

    private void fetchReports() {
        Log.d(TAG, "fetchReports: Calling API to get pending reports...");
        collectionApi.getMyPendingReports().enqueue(new Callback<ApiResponse<List<CollectionReportResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<CollectionReportResponse>>> call, Response<ApiResponse<List<CollectionReportResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentReports = response.body().getData();
                    Log.d(TAG, "fetchReports SUCCESS: Received " + (currentReports != null ? currentReports.size() : 0) + " items");
                } else {
                    Log.e(TAG, "fetchReports FAILED: Code " + response.code() + " - " + response.message());
                    currentReports.clear();
                }
                combineAndPost();
            }
            @Override
            public void onFailure(Call<ApiResponse<List<CollectionReportResponse>>> call, Throwable t) {
                Log.e(TAG, "fetchReports ERROR: " + t.getMessage());
                currentReports.clear();
                combineAndPost();
            }
        });
    }

    // Hàm bí thuật: Trộn 2 list lại và đếm tổng số
    private void combineAndPost() {
        List<NotificationWrapper> mixedList = new ArrayList<>();
        if (currentInvites != null) mixedList.addAll(currentInvites);
        if (currentReports != null) mixedList.addAll(currentReports);

        Log.d(TAG, "combineAndPost: Combined list size = " + mixedList.size());

        _notifications.setValue(mixedList);
        _unreadCount.setValue(mixedList.size()); // Cập nhật số trên chuông
        _isLoading.setValue(false);
        Log.d(TAG, "combineAndPost: Finished updating LiveData. Loading hidden.");
    }

    // --- Các hàm xử lý Action ---
    public void respondInvitation(Integer collectionId, String status) {
        Log.d(TAG, "respondInvitation: collectionId=" + collectionId + ", status=" + status);
        collectionApi.respondToInvitation(collectionId, status).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "respondInvitation SUCCESS");
                    fetchInvitations(); // Reload lại list lời mời
                } else {
                    Log.e(TAG, "respondInvitation FAILED: Code " + response.code());
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                Log.e(TAG, "respondInvitation ERROR: " + t.getMessage());
            }
        });
    }

    public void dismissReport(Long reportId) {
        Log.d(TAG, "dismissReport: reportId=" + reportId);
        // Ví dụ frontend gửi chữ DISMISSED để báo cáo đã xem và ẩn
        collectionApi.processReport(reportId, "DISMISSED").enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "dismissReport SUCCESS");
                    fetchReports(); // Reload lại list báo cáo
                } else {
                    Log.e(TAG, "dismissReport FAILED: Code " + response.code());
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                Log.e(TAG, "dismissReport ERROR: " + t.getMessage());
            }
        });
    }
}
