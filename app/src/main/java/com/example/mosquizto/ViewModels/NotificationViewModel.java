package com.example.mosquizto.ViewModels;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mosquizto.Dto.response.ApiResponse;
import com.example.mosquizto.Dto.response.CollectionReportResponse;
import com.example.mosquizto.Dto.response.FollowNotificationResponse;
import com.example.mosquizto.Dto.response.ShareCollectionResponse;
import com.example.mosquizto.Dto.response.UserReportResponse;
import com.example.mosquizto.Network.WebSocketManager;
import com.example.mosquizto.Network.itf.CollectionApi;
import com.example.mosquizto.Network.itf.NotificationApi;
import com.example.mosquizto.Network.itf.UserApi;
import com.example.mosquizto.Util.NotificationType;
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
    private static final int TOTAL_REQUESTS = 4;

    private final CollectionApi collectionApi;
    private final UserApi userApi;
    private final WebSocketManager webSocketManager;
    private final NotificationApi notificationApi;

    private final MutableLiveData<List<NotificationWrapper>> _notifications = new MutableLiveData<>();
    public LiveData<List<NotificationWrapper>> notifications = _notifications;

    private final MutableLiveData<Integer> _unreadCount = new MutableLiveData<>(0);
    public LiveData<Integer> unreadCount = _unreadCount;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading = _isLoading;

    private List<ShareCollectionResponse> currentInvites = new ArrayList<>();
    private List<CollectionReportResponse> currentReports = new ArrayList<>();
    private List<FollowNotificationResponse> currentFollows = new ArrayList<>();
    private List<UserReportResponse> currentUserReports = new ArrayList<>();

    private int completedRequests = 0;

    private final androidx.lifecycle.Observer<Boolean> refreshObserver = shouldRefresh -> {
        if (shouldRefresh != null && shouldRefresh) {
            Log.d(TAG, "Force refresh triggered from WebSocket");
            fetchAllNotifications();
        }
    };

    @Inject
    public NotificationViewModel(
            CollectionApi collectionApi,
            UserApi userApi,
            WebSocketManager webSocketManager,
            NotificationApi notificationApi
    ) {
        this.collectionApi = collectionApi;
        this.userApi = userApi;
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
        currentFollows = new ArrayList<>();
        currentUserReports = new ArrayList<>();

        fetchInvitations();
        fetchReports();
        fetchFollowNotifications();
        fetchUserReports();
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

    private void fetchFollowNotifications() {
        userApi.getFollowNotifications().enqueue(new Callback<ApiResponse<List<FollowNotificationResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<FollowNotificationResponse>>> call,
                                   Response<ApiResponse<List<FollowNotificationResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentFollows = response.body().getData() != null ? response.body().getData() : new ArrayList<>();
                }
                onRequestCompleted();
            }

            @Override
            public void onFailure(Call<ApiResponse<List<FollowNotificationResponse>>> call, Throwable t) {
                Log.e(TAG, "fetchFollowNotifications onFailure", t);
                onRequestCompleted();
            }
        });
    }

    private void fetchUserReports() {
        userApi.getMyPendingUserReports().enqueue(new Callback<ApiResponse<List<UserReportResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<UserReportResponse>>> call,
                                   Response<ApiResponse<List<UserReportResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentUserReports = response.body().getData() != null ? response.body().getData() : new ArrayList<>();
                }
                onRequestCompleted();
            }

            @Override
            public void onFailure(Call<ApiResponse<List<UserReportResponse>>> call, Throwable t) {
                Log.e(TAG, "fetchUserReports onFailure", t);
                onRequestCompleted();
            }
        });
    }

    private synchronized void onRequestCompleted() {
        completedRequests++;
        if (completedRequests >= TOTAL_REQUESTS) {
            combineAndPost();
            _isLoading.postValue(false);
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
                if (invite != null && invite.getCollectionId() != null
                        && invite.getTitle() != null && !invite.getTitle().trim().isEmpty()
                        && invite.getInviterUsername() != null) {
                    Long notifId = webSocketManager.getNotificationIdForReference(
                            NotificationType.COLLECTION_SHARED.name(),
                            invite.getCollectionId().longValue());
                    invite.setNotificationId(notifId);
                    mixedList.add(invite);
                }
            }
        }

        if (currentReports != null) {
            for (CollectionReportResponse report : currentReports) {
                if (report != null && report.getId() != null && report.getCollectionId() != null) {
                    Long notifId = webSocketManager.getNotificationIdForReference(
                            NotificationType.COLLECTION_REPORTED.name(),
                            report.getCollectionId().longValue());
                    report.setNotificationId(notifId);
                    mixedList.add(report);
                }
            }
        }

        if (currentFollows != null) {
            for (FollowNotificationResponse follow : currentFollows) {
                if (follow != null && follow.getFollowerId() != null) {
                    Long refId = follow.getId() != null ? follow.getId() : follow.getFollowerId();
                    Long notifId = webSocketManager.getNotificationIdForReference(
                            NotificationType.HAS_FOLLOWER.name(),
                            refId);
                    follow.setNotificationId(notifId);
                    mixedList.add(follow);
                }
            }
        }

        if (currentUserReports != null) {
            for (UserReportResponse report : currentUserReports) {
                if (report != null && report.getId() != null) {
                    Long notifId = webSocketManager.getNotificationIdForReference(
                            NotificationType.USER_REPORTED.name(),
                            report.getId().longValue());
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
                _isLoading.postValue(false);
                markAsRead(invite);
                removeFromList(invite);
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
                removeFromList(report);
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                _isLoading.postValue(false);
            }
        });
    }

    public void dismissUserReport(UserReportResponse report) {
        if (report == null || report.getId() == null) return;
        _isLoading.postValue(true);
        userApi.processUserReport(report.getId().longValue(), "DISMISSED").enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                _isLoading.postValue(false);
                markAsRead(report);
                removeFromList(report);
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                _isLoading.postValue(false);
            }
        });
    }

    private void removeFromList(NotificationWrapper item) {
        List<NotificationWrapper> currentList = _notifications.getValue();
        if (currentList != null) {
            currentList.remove(item);
            _notifications.postValue(currentList);
            syncBadgeWithList(currentList);
        }
    }

    public void markAsRead(NotificationWrapper item) {
        Long notifId = item.getNotificationId();
        if (notifId == null) return;

        notificationApi.markAsRead(notifId).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (!response.isSuccessful()) return;

                if (item instanceof ShareCollectionResponse) {
                    webSocketManager.removeNotificationFromMap(
                            NotificationType.COLLECTION_SHARED.name(),
                            ((ShareCollectionResponse) item).getCollectionId().longValue());
                } else if (item instanceof CollectionReportResponse) {
                    webSocketManager.removeNotificationFromMap(
                            NotificationType.COLLECTION_REPORTED.name(),
                            ((CollectionReportResponse) item).getCollectionId().longValue());
                } else if (item instanceof FollowNotificationResponse) {
                    FollowNotificationResponse follow = (FollowNotificationResponse) item;
                    Long refId = follow.getId() != null ? follow.getId() : follow.getFollowerId();
                    webSocketManager.removeNotificationFromMap(NotificationType.HAS_FOLLOWER.name(), refId);
                } else if (item instanceof UserReportResponse) {
                    webSocketManager.removeNotificationFromMap(
                            NotificationType.USER_REPORTED.name(),
                            ((UserReportResponse) item).getId().longValue());
                }
                Log.d(TAG, "Mark as read: " + notifId);
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
            }
        });
    }
}
