package com.example.mosquizto.ViewModels;

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

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@HiltViewModel
public class HomeViewModel extends ViewModel {

    private final StudyApi studyApi;
    private final CollectionApi collectionApi;

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

    @Inject
    public HomeViewModel(StudyApi studyApi, CollectionApi collectionApi) {
        this.studyApi = studyApi;
        this.collectionApi = collectionApi;
    }

    // Kích hoạt đồng thời 3 luồng tải dữ liệu bất đồng bộ độc lập
    public void fetchAllData() {
        if (isDataLoaded) return;
        fetchJumpBackIn();
        fetchRecents();
        fetchRecommended();
        isDataLoaded = true;
    }

    public void fetchJumpBackIn() {
        studyApi.getJumpBackIn().enqueue(new Callback<ApiResponse<List<StudySessionResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<StudySessionResponse>>> call, Response<ApiResponse<List<StudySessionResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    _jumpBackIn.setValue(response.body().getData());
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<List<StudySessionResponse>>> call, Throwable t) {
                Log.e("HomeViewModel", "Lỗi JumpBackIn: " + t.getMessage());
            }
        });
    }

    public void fetchRecents() {
        collectionApi.getRecentOpenedCollections().enqueue(new Callback<ApiResponse<List<CollectionResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<CollectionResponse>>> call, Response<ApiResponse<List<CollectionResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    _recents.setValue(response.body().getData());
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<List<CollectionResponse>>> call, Throwable t) {
                Log.e("HomeViewModel", "Lỗi Recents: " + t.getMessage());
            }
        });
    }

    public void fetchRecommended() {
        collectionApi.recommendCollection(0, 10).enqueue(new Callback<ApiResponse<PageResponse<CollectionResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<PageResponse<CollectionResponse>>> call, Response<ApiResponse<PageResponse<CollectionResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    _recommended.setValue(response.body().getData().getContent());
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<PageResponse<CollectionResponse>>> call, Throwable t) {
                Log.e("HomeViewModel", "Lỗi Recommended: " + t.getMessage());
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
                        _recents.setValue(updatedList); // Tự động cập nhật lại danh sách trên UI qua Observer
                    }
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                Log.e("HomeViewModel", "Lỗi xóa Recent: " + t.getMessage());
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
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<List<CollectionItemResponse>>> call, Throwable t) {
                _isMcqLoading.setValue(false);
                _mcqItems.setValue(null);
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
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<List<CollectionItemResponse>>> call, Throwable t) {
                _isRandomGameLoading.setValue(false);
                Log.e("HomeViewModel", "Lỗi tải game ngẫu nhiên: " + t.getMessage());
            }
        });
    }

    public void clearRandomGameItems() {
        _randomGameItems.setValue(null);
    }
}