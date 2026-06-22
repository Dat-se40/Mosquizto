package com.example.mosquizto.ViewModels;

import android.content.Context;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mosquizto.Dto.response.ApiResponse;
import com.example.mosquizto.Dto.response.CollectionItemResponse;
import com.example.mosquizto.Dto.response.CollectionResponse;
import com.example.mosquizto.Dto.response.PageResponse;
import com.example.mosquizto.Dto.response.StudySessionResponse;
import com.example.mosquizto.Network.itf.CollectionApi;
import com.example.mosquizto.Network.itf.StudyApi;
import com.example.mosquizto.Services.SessionManager;
import com.example.mosquizto.Util.ApiErrorHelper;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@HiltViewModel
public class HomeViewModel extends ViewModel {

    private static final String TAG = "HomeViewModel";

    private final StudyApi studyApi;
    private final CollectionApi collectionApi;
    private final Context appContext;

    private final MutableLiveData<List<StudySessionResponse>> _jumpBackIn = new MutableLiveData<>();
    public LiveData<List<StudySessionResponse>> jumpBackIn = _jumpBackIn;

    private final MutableLiveData<List<CollectionResponse>> _recents = new MutableLiveData<>();
    public LiveData<List<CollectionResponse>> recents = _recents;

    private final MutableLiveData<List<CollectionResponse>> _recommended = new MutableLiveData<>();
    public LiveData<List<CollectionResponse>> recommended = _recommended;

    private final MutableLiveData<List<CollectionItemResponse>> _mcqItems = new MutableLiveData<>();
    public LiveData<List<CollectionItemResponse>> mcqItems = _mcqItems;

    private final MutableLiveData<Boolean> _isMcqLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isMcqLoading = _isMcqLoading;

    private final MutableLiveData<List<CollectionItemResponse>> _randomGameItems = new MutableLiveData<>();
    public LiveData<List<CollectionItemResponse>> randomGameItems = _randomGameItems;

    private final MutableLiveData<Boolean> _isRandomGameLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isRandomGameLoading = _isRandomGameLoading;

    private boolean isDataLoaded = false;
    SessionManager sessionManager;
    @Inject
    public HomeViewModel(StudyApi studyApi, CollectionApi collectionApi, SessionManager sessionManager,
                         @ApplicationContext Context appContext) {
        this.studyApi = studyApi;
        this.collectionApi = collectionApi;
        this.sessionManager = sessionManager;
        this.appContext = appContext;
    }

    // Kích hoạt đồng thời 3 luồng tải dữ liệu bất đồng bộ độc lập
    public void fetchAllData() {
        if (isDataLoaded) return;
        refreshAllData();
        isDataLoaded = true;
    }

    public void refreshAllData() {
        fetchJumpBackIn();
        fetchRecents();
        fetchRecommended();
    }

    public void fetchJumpBackIn() {
        studyApi.getJumpBackIn().enqueue(new Callback<ApiResponse<List<StudySessionResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<StudySessionResponse>>> call, Response<ApiResponse<List<StudySessionResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    _jumpBackIn.setValue(response.body().getData());
                } else {
                    Log.e(TAG, "fetchJumpBackIn failed: " + ApiErrorHelper.extractMessage(response));
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<List<StudySessionResponse>>> call, Throwable t) {
                Log.e(TAG, "fetchJumpBackIn onFailure: " + ApiErrorHelper.networkError(appContext), t);
            }
        });
    }

    public void fetchRecents() {
        collectionApi.getRecentOpenedCollections().enqueue(new Callback<ApiResponse<List<CollectionResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<CollectionResponse>>> call, Response<ApiResponse<List<CollectionResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    _recents.setValue(response.body().getData());
                } else {
                    Log.e(TAG, "fetchRecents failed: " + ApiErrorHelper.extractMessage(response));
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<List<CollectionResponse>>> call, Throwable t) {
                Log.e(TAG, "fetchRecents onFailure: " + ApiErrorHelper.networkError(appContext), t);
            }
        });
    }

    public void fetchRecommended() {
        collectionApi.recommendCollection(0, 10).enqueue(new Callback<ApiResponse<PageResponse<CollectionResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<PageResponse<CollectionResponse>>> call, Response<ApiResponse<PageResponse<CollectionResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    _recommended.setValue(response.body().getData().getContent());
                } else {
                    Log.e(TAG, "fetchRecommended failed: " + ApiErrorHelper.extractMessage(response));
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<PageResponse<CollectionResponse>>> call, Throwable t) {
                Log.e(TAG, "fetchRecommended onFailure: " + ApiErrorHelper.networkError(appContext), t);
            }
        });
    }

    public void deleteRecentItem(Integer id) {
        collectionApi.deleteRecentOpenedCollection(id).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful()) {
                    List<CollectionResponse> currentList = _recents.getValue();
                    if (currentList != null) {
                        List<CollectionResponse> updatedList = new ArrayList<>(currentList);
                        updatedList.removeIf(item -> item.getId().equals(id));
                        _recents.setValue(updatedList);
                    }
                } else {
                    Log.e(TAG, "deleteRecentItem failed: " + ApiErrorHelper.extractMessage(response));
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                Log.e(TAG, "deleteRecentItem onFailure: " + ApiErrorHelper.networkError(appContext), t);
            }
        });
    }

    public void fetchItemsForQuickMcq(Integer collectionId) {
        _isMcqLoading.setValue(true);
        collectionApi.getCollectionItemById(collectionId).enqueue(new Callback<ApiResponse<List<CollectionItemResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<CollectionItemResponse>>> call, Response<ApiResponse<List<CollectionItemResponse>>> response) {
                _isMcqLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    _mcqItems.setValue(response.body().getData());
                } else {
                    _mcqItems.setValue(null);
                    Log.e(TAG, "fetchItemsForQuickMcq failed: " + ApiErrorHelper.extractMessage(response));
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<List<CollectionItemResponse>>> call, Throwable t) {
                _isMcqLoading.setValue(false);
                _mcqItems.setValue(null);
                Log.e(TAG, "fetchItemsForQuickMcq onFailure: " + ApiErrorHelper.networkError(appContext), t);
            }
        });
    }

    public void fetchItemsForRandomGame(Integer collectionId) {
        _isRandomGameLoading.setValue(true);
        collectionApi.getCollectionItemById(collectionId).enqueue(new Callback<ApiResponse<List<CollectionItemResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<CollectionItemResponse>>> call, Response<ApiResponse<List<CollectionItemResponse>>> response) {
                _isRandomGameLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    _randomGameItems.setValue(response.body().getData());
                } else {
                    Log.e(TAG, "fetchItemsForRandomGame failed: " + ApiErrorHelper.extractMessage(response));
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<List<CollectionItemResponse>>> call, Throwable t) {
                _isRandomGameLoading.setValue(false);
                Log.e(TAG, "fetchItemsForRandomGame onFailure: " + ApiErrorHelper.networkError(appContext), t);
            }
        });
    }

    public void clearRandomGameItems() {
        _randomGameItems.setValue(null);
    }

    public String getAvatar() {
        return sessionManager.getUserImgUri() ;
    }
}