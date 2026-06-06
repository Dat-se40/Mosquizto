package com.example.mosquizto.ViewModels;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mosquizto.Dto.response.ApiResponse;
import com.example.mosquizto.Dto.response.CollectionReportResponse;
import com.example.mosquizto.Dto.response.ShareCollectionResponse;
import com.example.mosquizto.Network.WebSocketManager;
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
    private final WebSocketManager webSocketManager ;
    private final MutableLiveData<List<NotificationWrapper>> _notifications = new MutableLiveData<>();
    public LiveData<List<NotificationWrapper>> notifications = _notifications;

    private final MutableLiveData<Integer> _unreadCount = new MutableLiveData<>(0);
    public LiveData<Integer> unreadCount = _unreadCount;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading = _isLoading;

    private List<ShareCollectionResponse> currentInvites = new ArrayList<>();
    private List<CollectionReportResponse> currentReports = new ArrayList<>();

    @Inject
    public NotificationViewModel(CollectionApi collectionApi , WebSocketManager webSocketManager) {
        this.collectionApi = collectionApi;
        this.webSocketManager = webSocketManager;
    }
    public void fetchAllNotifications() {
        Log.d(TAG, "fetchAllNotifications: Start");
        _isLoading.setValue(true);
        fetchInvitations();
        fetchReports();

        // bảo đảm đúng số lượng
        webSocketManager.clearNotificationCount();
        webSocketManager.updateNotificationCount(currentInvites.size() + currentReports.size());
    }

    private void fetchInvitations() {
        collectionApi.getMyInvitations().enqueue(new Callback<ApiResponse<List<ShareCollectionResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<ShareCollectionResponse>>> call, Response<ApiResponse<List<ShareCollectionResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ShareCollectionResponse> data = response.body().getData();
                    currentInvites = (data != null) ? data : new ArrayList<>();
                } else {
                    currentInvites = new ArrayList<>();
                }
                combineAndPost();
            }
            @Override
            public void onFailure(Call<ApiResponse<List<ShareCollectionResponse>>> call, Throwable t) {
                currentInvites = new ArrayList<>();
                combineAndPost();
            }
        });
    }

    private void fetchReports() {
        collectionApi.getMyPendingReports().enqueue(new Callback<ApiResponse<List<CollectionReportResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<CollectionReportResponse>>> call, Response<ApiResponse<List<CollectionReportResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<CollectionReportResponse> data = response.body().getData();
                    currentReports = (data != null) ? data : new ArrayList<>();
                } else {
                    currentReports = new ArrayList<>();
                }
                combineAndPost();
            }
            @Override
            public void onFailure(Call<ApiResponse<List<CollectionReportResponse>>> call, Throwable t) {
                currentReports = new ArrayList<>();
                combineAndPost();
            }
        });
    }

    private void combineAndPost() {
        List<NotificationWrapper> mixedList = new ArrayList<>();
        if (currentInvites != null) {
            for (ShareCollectionResponse invite : currentInvites) {
                if (invite != null) mixedList.add(invite);
            }
        }
        if (currentReports != null) {
            for (CollectionReportResponse report : currentReports) {
                if (report != null) mixedList.add(report);
            }
        }

        _notifications.setValue(mixedList);
        _unreadCount.setValue(mixedList.size());
        _isLoading.setValue(false);
    }

    public void respondInvitation(Integer collectionId, String status) {
        if (collectionId == null) return;
        collectionApi.respondToInvitation(collectionId, status).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful()) fetchInvitations();
            }
            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {}
        });
    }

    public void dismissReport(Long reportId) {
        if (reportId == null) return;
        collectionApi.processReport(reportId, "DISMISSED").enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful()) fetchReports();
            }
            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {}
        });
    }
}
