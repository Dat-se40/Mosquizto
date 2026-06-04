package com.example.mosquizto.ViewModels;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mosquizto.Dto.response.ApiResponse;
import com.example.mosquizto.Dto.response.PageResponse;
import com.example.mosquizto.Dto.response.UserResponse;
import com.example.mosquizto.Models.RecentSearchItem;
import com.example.mosquizto.Dto.response.SearchApiResponse;
import com.example.mosquizto.Network.itf.CollectionApi;
import com.example.mosquizto.Network.itf.UserApi;
import com.example.mosquizto.Util.SearchResultWrapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@HiltViewModel
public class SearchViewModel extends ViewModel {

    private final CollectionApi collectionApi;
    // private final UserApi userApi; // Thêm API lấy người dùng vào đây sau này

    private final SharedPreferences sharedPreferences;
    private final Gson gson;

    private static final String PREF_NAME = "SearchPreferences";
    private static final String KEY_RECENT_SEARCHES = "RecentSearchesList";

    // --- ENUM VÀ STATE TÌM KIẾM ---
    public enum SearchType { COLLECTION, USER }
    public enum SearchState { IDLE, TYPING, LOADING, HAS_RESULTS, EMPTY }

    private final MutableLiveData<SearchType> _searchType = new MutableLiveData<>(SearchType.COLLECTION);
    public LiveData<SearchType> searchType = _searchType;

    private String currentQuery = ""; // Lưu lại chuỗi đang search
    // ------------------------------

    private final MutableLiveData<List<String>> _suggestions = new MutableLiveData<>(new ArrayList<>());
    public LiveData<List<String>> suggestions = _suggestions;

    private final MutableLiveData<List<SearchResultWrapper>> _searchResults = new MutableLiveData<>(new ArrayList<>());
    public LiveData<List<SearchResultWrapper>> searchResults = _searchResults;

    private final MutableLiveData<List<RecentSearchItem>> _recentSearches = new MutableLiveData<>(new ArrayList<>());
    public LiveData<List<RecentSearchItem>> recentSearches = _recentSearches;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> errorMessage = _errorMessage;

    private final MutableLiveData<SearchState> _searchState = new MutableLiveData<>(SearchState.IDLE);
    public LiveData<SearchState> searchState = _searchState;

    private final Handler debounceHandler = new Handler(Looper.getMainLooper());
    private Runnable debounceRunnable;
    private static final long DEBOUNCE_DELAY_MS = 300;

    private final UserApi userApi ;


    @Inject
    public SearchViewModel(CollectionApi collectionApi, @ApplicationContext Context context,
                            UserApi userApi) {
        this.collectionApi = collectionApi;
        // Khởi tạo SharedPreferences và Gson
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
        this.userApi = userApi ;
        // Load lại lịch sử từ bộ nhớ khi mở màn hình Search
        loadRecentSearchesFromPrefs();
    }

    public void setSearchType(SearchType type) {
        if (_searchType.getValue() != type) {
            _searchType.setValue(type);
            // Nếu đổi tab trong lúc đang có chữ -> search lại ngay lập tức
            if (currentQuery != null && !currentQuery.isEmpty()) {
                performSearch(currentQuery);
            }
        }
    }
    // --- CÁC HÀM XỬ LÝ LƯU TRỮ (PERSISTENCE) ---
    private void loadRecentSearchesFromPrefs() {
        String json = sharedPreferences.getString(KEY_RECENT_SEARCHES, null);
        if (json != null) {
            Type type = new TypeToken<List<RecentSearchItem>>(){}.getType();
            List<RecentSearchItem> savedList = gson.fromJson(json, type);
            if (savedList != null) {
                _recentSearches.setValue(savedList);
            }
        }
    }

    private void saveRecentSearchesToPrefs(List<RecentSearchItem> currentList) {
        String json = gson.toJson(currentList);
        sharedPreferences.edit().putString(KEY_RECENT_SEARCHES, json).apply();
        _recentSearches.setValue(currentList);
    }
    // -------------------------------------------

    public void onQueryChanged(String query) {
        this.currentQuery = query != null ? query.trim() : "";

        if (currentQuery.isEmpty()) {
            if (debounceRunnable != null) debounceHandler.removeCallbacks(debounceRunnable);
            _suggestions.setValue(new ArrayList<>());
            _searchState.setValue(SearchState.IDLE);
            return;
        }

        _searchState.setValue(SearchState.TYPING);

        if (debounceRunnable != null) debounceHandler.removeCallbacks(debounceRunnable);
        debounceRunnable = () -> fetchSuggestions(currentQuery);
        debounceHandler.postDelayed(debounceRunnable, DEBOUNCE_DELAY_MS);
    }

    public void onSearchSubmitted(String query) {
        if (query == null || query.trim().isEmpty()) return;
        this.currentQuery = query.trim();

        if (debounceRunnable != null) debounceHandler.removeCallbacks(debounceRunnable);

        saveRecentSearch(currentQuery);
        performSearch(currentQuery);
    }



    public void removeRecentSearch(String text) {
        List<RecentSearchItem> current = _recentSearches.getValue();
        if (current == null) return;
        List<RecentSearchItem> updated = new ArrayList<>(current);
        updated.removeIf(item -> item.getText().equals(text));

        // Gọi hàm lưu để ghi đè vào bộ nhớ
        saveRecentSearchesToPrefs(updated);
    }

    public void clearAllRecentSearches() {
        // Gọi hàm lưu list rỗng để xóa sạch trong bộ nhớ
        saveRecentSearchesToPrefs(new ArrayList<>());
    }

    private void fetchSuggestions(String query) {
        List<RecentSearchItem> recents = _recentSearches.getValue();
        List<String> filtered = new ArrayList<>();

        if (recents != null) {
            for (RecentSearchItem item : recents) {
                if (item.getText().toLowerCase().contains(query.toLowerCase())) {
                    filtered.add(item.getText());
                }
            }
        }

        if (!filtered.contains(query)) {
            filtered.add(0, query);
        }

        _suggestions.setValue(filtered);
    }

    private void performSearch(String query) {
        _isLoading.setValue(true);
        _searchState.setValue(SearchState.LOADING);

        SearchType currentType = _searchType.getValue();

        if (currentType == SearchType.COLLECTION) {
            // TÌM KIẾM HỌC PHẦN
//            collectionApi.searchCollections(query, 0, 10, null).enqueue(new Callback<ApiResponse<PageResponse<CollectionResponse>>>() {
//                @Override
//                public void onResponse(Call<ApiResponse<PageResponse<CollectionResponse>>> call, Response<ApiResponse<PageResponse<CollectionResponse>>> response) {
//                    _isLoading.setValue(false);
//                    Log.d("SearchViewModel", "Search response: " + response.body());
//                    if (response.isSuccessful() && response.body() != null) {
//                            List<CollectionResponse> data = response.body().getData().getContent() ;
//                        if (data != null && ! data.isEmpty() ) {
//                            List<SearchResultWrapper> searchResultWrappers = new ArrayList<>(data);
//                            _searchResults.setValue(searchResultWrappers);
//                            _searchState.setValue(SearchState.HAS_RESULTS);
//                            Log.d("SearchViewModel", "Search results: " + searchResultWrappers);
//                        } else {
//                            _searchResults.setValue(new ArrayList<>());
//                            _searchState.setValue(SearchState.EMPTY);
//                            Log.d("SearchViewModel", "Search results empty");
//                        }
//
//                    } else {
//                        _searchState.setValue(SearchState.EMPTY);
//                    }
//                }
//                @Override
//                public void onFailure(Call<ApiResponse<PageResponse<CollectionResponse>>> call, Throwable t) {
//                    _isLoading.setValue(false);
//                    _searchState.setValue(SearchState.EMPTY);
//                    _errorMessage.setValue("Lỗi kết nối");
//                    Log.d("SearchViewmodel", Objects.requireNonNull(t.getMessage()));
//                }
//            });
            collectionApi.searchCollections(query, 1, 10, null).enqueue(new Callback<SearchApiResponse>() {
                @Override
                public void onResponse(Call<SearchApiResponse> call, Response<SearchApiResponse> response) {
                    _isLoading.setValue(false);
                    if (response.isSuccessful() && response.body() != null) {
                        SearchApiResponse.SearchPaginatedData data = response.body().getData();
                        if (data != null && data.getHits() != null && !data.getHits().isEmpty()) {
                            List<SearchResultWrapper> searchResultWrappers = new ArrayList<>(data.getHits());
                            _searchResults.setValue(searchResultWrappers);
                            _searchState.setValue(SearchState.HAS_RESULTS);
                        } else {
                            _searchResults.setValue(new ArrayList<>());
                            _searchState.setValue(SearchState.EMPTY);
                        }
                    } else {
                        _searchState.setValue(SearchState.EMPTY);
                    }
                }
                @Override
                public void onFailure(Call<SearchApiResponse> call, Throwable t) {
                    _isLoading.setValue(false);
                    _searchState.setValue(SearchState.EMPTY);
                    _errorMessage.setValue("Lỗi kết nối");
                }
            });
        } else {

            userApi.searchUser(query, 1, 10).enqueue(new Callback<ApiResponse<PageResponse<UserResponse>>>() {
                @Override
                public void onResponse(Call<ApiResponse<PageResponse<UserResponse>>> call, Response<ApiResponse<PageResponse<UserResponse>>> response) {
                    _isLoading.setValue(false);
                    if (response.body() != null && response.isSuccessful())
                    {
                        PageResponse<UserResponse> data = response.body().getData();
                        if (data != null && !data.getContent().isEmpty())
                        {
                            List<SearchResultWrapper> searchResultWrappers = new ArrayList<>(data.getContent());
                            _searchResults.setValue(searchResultWrappers);
                            _searchState.setValue(SearchState.HAS_RESULTS);
                        }
                        else
                        {
                            _searchResults.setValue(new ArrayList<>());
                            _searchState.setValue(SearchState.EMPTY);
                        }
                    }else _searchState.setValue(SearchState.EMPTY);
                }

                @Override
                public void onFailure(Call<ApiResponse<PageResponse<UserResponse>>> call, Throwable t) {
                    _isLoading.setValue(false);
                    _searchState.setValue(SearchState.EMPTY);
                    _errorMessage.setValue("Lỗi kết nối");
                }
            });

            _isLoading.setValue(false);
            _searchResults.setValue(new ArrayList<>());
            _searchState.setValue(SearchState.EMPTY);
        }
    }

    private void saveRecentSearch(String query) {
        List<RecentSearchItem> current = _recentSearches.getValue();
        List<RecentSearchItem> updated = (current != null) ? new ArrayList<>(current) : new ArrayList<>();

        updated.removeIf(item -> item.getText().equalsIgnoreCase(query));
        updated.add(0, new RecentSearchItem(query));

        if (updated.size() > 10) {
            updated = updated.subList(0, 10);
        }

        // Gọi hàm lưu để ghi vào bộ nhớ
        saveRecentSearchesToPrefs(updated);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (debounceRunnable != null) {
            debounceHandler.removeCallbacks(debounceRunnable);
        }
    }
}