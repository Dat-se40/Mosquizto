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
import com.example.mosquizto.Util.NotificationType;
import com.example.mosquizto.Util.NotificationWrapper;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.example.mosquizto.Network.itf.NotificationApi;

@HiltViewModel
public class NotificationViewModel extends ViewModel {

    private static final String TAG = "NotificationVM";
    private final CollectionApi collectionApi;
    private final WebSocketManager webSocketManager ;
    private final NotificationApi notificationApi;
    
    private final MutableLiveData<List<NotificationWrapper>> _notifications = new MutableLiveData<>();
    public LiveData<List<NotificationWrapper>> notifications = _notifications;

    private final MutableLiveData<Integer> _unreadCount = new MutableLiveData<>(0);
    public LiveData<Integer> unreadCount = _unreadCount;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading = _isLoading;

    private List<ShareCollectionResponse> currentInvites = new ArrayList<>();
    private List<CollectionReportResponse> currentReports = new ArrayList<>();
    
    private int completedRequests = 0;
    
    private final androidx.lifecycle.Observer<Boolean> refreshObserver = shouldRefresh -> {
        if (shouldRefresh != null && shouldRefresh) {
            Log.d(TAG, "Force refresh triggered from WebSocket");
            fetchAllNotifications();
        }
    };

    @Inject
    public NotificationViewModel(CollectionApi collectionApi , WebSocketManager webSocketManager, NotificationApi notificationApi) {
        this.collectionApi = collectionApi;
        this.webSocketManager = webSocketManager;
        this.notificationApi = notificationApi;
        webSocketManager.getForceRefreshTrigger().observeForever(refreshObserver);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        webSocketManager.getForceRefreshTrigger().removeObserver(refreshObserver);
    }

    public void fetchAllNotifications() {
        if (Boolean.TRUE.equals(_isLoading.getValue())) return;
        
        Log.d(TAG, "fetchAllNotifications: Start");
        _isLoading.postValue(true);
        completedRequests = 0;
        currentInvites = new ArrayList<>();
        currentReports = new ArrayList<>();
        
        fetchInvitations();
        fetchReports();
    }

    private void fetchInvitations() {
        collectionApi.getMyInvitations().enqueue(new Callback<ApiResponse<List<ShareCollectionResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<ShareCollectionResponse>>> call, Response<ApiResponse<List<ShareCollectionResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentInvites = response.body().getData() != null ? response.body().getData() : new ArrayList<>();
                }
                onRequestCompleted();
            }
            @Override
            public void onFailure(Call<ApiResponse<List<ShareCollectionResponse>>> call, Throwable t) {
                Log.e(TAG, "fetchInvitations onFailure", t);
                onRequestCompleted();
            }
        });
    }

    private void fetchReports() {
        collectionApi.getMyPendingReports().enqueue(new Callback<ApiResponse<List<CollectionReportResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<CollectionReportResponse>>> call, Response<ApiResponse<List<CollectionReportResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentReports = response.body().getData() != null ? response.body().getData() : new ArrayList<>();
                }
                onRequestCompleted();
            }
            @Override
            public void onFailure(Call<ApiResponse<List<CollectionReportResponse>>> call, Throwable t) {
                Log.e(TAG, "fetchReports onFailure", t);
                onRequestCompleted();
            }
        });
    }

    private synchronized void onRequestCompleted() {
        completedRequests++;
        if (completedRequests >= 2) {
            combineAndPost();
            _isLoading.postValue(false);
            
            // Đồng bộ badge với số thông báo thực tế sau khi lọc dữ liệu
            List<NotificationWrapper> list = _notifications.getValue();
            syncBadgeWithList(list);
        }
    }

    private void syncBadgeWithList(List<NotificationWrapper> list) {
        int total = list != null ? list.size() : 0;
        webSocketManager.setNotificationCount(total);
    }

    private void combineAndPost() {
        List<NotificationWrapper> mixedList = new ArrayList<>();

        if (currentInvites != null) {
            for (ShareCollectionResponse invite : currentInvites) {
                // LỌC DỮ LIỆU: Bỏ qua nếu thiếu collectionId hoặc tiêu đề hoặc inviter
                if (invite != null && invite.getCollectionId() != null 
                        && invite.getTitle() != null && !invite.getTitle().trim().isEmpty()
                        && invite.getInviterUsername() != null) {
                    
                    Long notifId = webSocketManager.getNotificationIdForReference(NotificationType.COLLECTION_SHARED.name(), invite.getCollectionId().longValue());
                    invite.setNotificationId(notifId);
                    mixedList.add(invite);
                }
            }
        }
        if (currentReports != null) {
            for (CollectionReportResponse report : currentReports) {
                // LỌC DỮ LIỆU: Đảm bảo có ID và CollectionId
                if (report != null && report.getId() != null && report.getCollectionId() != null) {
                    Long notifId = webSocketManager.getNotificationIdForReference(
                            NotificationType.COLLECTION_REPORTED.name(),
                            report.getCollectionId().longValue());
                    report.setNotificationId(notifId);
                    mixedList.add(report);
                }
            }
        }

        _notifications.postValue(mixedList);
        syncBadgeWithList(mixedList);
    }

    public void respondInvitation(ShareCollectionResponse invite, String status) {
        if (invite == null || invite.getCollectionId() == null) return;
        _isLoading.postValue(true);
        collectionApi.respondToInvitation(invite.getCollectionId(), status).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                // Luôn fetch lại dù markAsRead có thành công hay không để update UI
                _isLoading.postValue(false);
                markAsRead(invite);
                List<NotificationWrapper> currentList = _notifications.getValue();
                if (currentList != null) {
                    currentList.remove(invite);
                    _notifications.postValue(currentList);
                    syncBadgeWithList(currentList);
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                _isLoading.postValue(false);
            }
        });
    }

    public void dismissReport(CollectionReportResponse report) {
        if (report == null || report.getId() == null) return;
        _isLoading.postValue(true);
        collectionApi.processReport(report.getId().longValue(), "DISMISSED").enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                _isLoading.postValue(false);
                markAsRead(report);
                List<NotificationWrapper> currentList = _notifications.getValue();
                if (currentList != null) {
                    currentList.remove(report);
                    _notifications.postValue(currentList);
                    syncBadgeWithList(currentList);
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                _isLoading.postValue(false);
            }
        });
    }

    public void markAsRead(NotificationWrapper item) {
        Long notifId = item.getNotificationId();
        if (notifId != null) {
            notificationApi.markAsRead(notifId).enqueue(new Callback<ApiResponse<Void>>() {
                @Override
                public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                    if (response.isSuccessful()) {
                        // Xóa khỏi map để không bị lặp lại
                        if (item instanceof ShareCollectionResponse) {
                            webSocketManager.removeNotificationFromMap(NotificationType.COLLECTION_SHARED.name(), ((ShareCollectionResponse) item).getCollectionId().longValue());
                        } else if (item instanceof CollectionReportResponse) {
                            webSocketManager.removeNotificationFromMap(
                                    NotificationType.COLLECTION_REPORTED.name(),
                                    ((CollectionReportResponse) item).getCollectionId().longValue());
                        }
                        Log.e("WEBSOCK", "Mark as read: " + notifId);
                    }
                }
                @Override
                public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {}
            });
        }
    }
}
