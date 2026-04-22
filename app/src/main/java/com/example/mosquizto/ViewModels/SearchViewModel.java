package com.example.mosquizto.ViewModels;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mosquizto.Models.RecentSearchItem;
import com.example.mosquizto.Dto.response.SearchApiResponse;
import com.example.mosquizto.Dto.response.SearchResultItem;
import com.example.mosquizto.Network.itf.CollectionApi;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@HiltViewModel
public class SearchViewModel extends ViewModel {

    private final CollectionApi collectionApi;

    private final MutableLiveData<List<String>> _suggestions = new MutableLiveData<>(new ArrayList<>());
    public LiveData<List<String>> suggestions = _suggestions;

    private final MutableLiveData<List<SearchResultItem>> _searchResults = new MutableLiveData<>(new ArrayList<>());
    public LiveData<List<SearchResultItem>> searchResults = _searchResults;

    private final MutableLiveData<List<RecentSearchItem>> _recentSearches = new MutableLiveData<>(new ArrayList<>());
    public LiveData<List<RecentSearchItem>> recentSearches = _recentSearches;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> errorMessage = _errorMessage;

    private final MutableLiveData<SearchState> _searchState = new MutableLiveData<>(SearchState.IDLE);
    public LiveData<SearchState> searchState = _searchState;

    public enum SearchState {
        IDLE, TYPING, LOADING, HAS_RESULTS, EMPTY
    }

    private final Handler debounceHandler = new Handler(Looper.getMainLooper());
    private Runnable debounceRunnable;
    private static final long DEBOUNCE_DELAY_MS = 300;

    @Inject
    public SearchViewModel(CollectionApi collectionApi) {
        this.collectionApi = collectionApi;
    }

    public void onQueryChanged(String query) {
        if (query == null || query.trim().isEmpty()) {
            if (debounceRunnable != null) {
                debounceHandler.removeCallbacks(debounceRunnable);
            }
            _suggestions.setValue(new ArrayList<>());
            _searchState.setValue(SearchState.IDLE);
            return;
        }

        _searchState.setValue(SearchState.TYPING);

        if (debounceRunnable != null) {
            debounceHandler.removeCallbacks(debounceRunnable);
        }
        debounceRunnable = () -> fetchSuggestions(query.trim());
        debounceHandler.postDelayed(debounceRunnable, DEBOUNCE_DELAY_MS);
    }

    public void onSearchSubmitted(String query) {
        if (query == null || query.trim().isEmpty()) return;

        if (debounceRunnable != null) {
            debounceHandler.removeCallbacks(debounceRunnable);
        }

        saveRecentSearch(query.trim());
        performSearch(query.trim());
    }

    public void removeRecentSearch(String text) {
        List<RecentSearchItem> current = _recentSearches.getValue();
        if (current == null) return;
        List<RecentSearchItem> updated = new ArrayList<>(current);
        updated.removeIf(item -> item.getText().equals(text));
        _recentSearches.setValue(updated);
    }

    public void clearAllRecentSearches() {
        _recentSearches.setValue(new ArrayList<>());
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

        collectionApi.searchCollections(query, 1, 10, null).enqueue(new Callback<SearchApiResponse>() {
            @Override
            public void onResponse(Call<SearchApiResponse> call, Response<SearchApiResponse> response) {
                _isLoading.setValue(false);

                if (response.isSuccessful() && response.body() != null) {
                    SearchApiResponse.SearchPaginatedData data = response.body().getData();

                    if (data != null && data.getHits() != null && !data.getHits().isEmpty()) {
                        _searchResults.setValue(data.getHits());
                        _searchState.setValue(SearchState.HAS_RESULTS);
                    } else {
                        _searchResults.setValue(new ArrayList<>());
                        _searchState.setValue(SearchState.EMPTY);
                    }
                } else {
                    _searchState.setValue(SearchState.EMPTY);
                    _errorMessage.setValue("Lỗi server: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<SearchApiResponse> call, Throwable t) {
                _isLoading.setValue(false);
                _searchState.setValue(SearchState.EMPTY);
                _errorMessage.setValue("Không kết nối được server: " + t.getMessage());
            }
        });
    }

    private void saveRecentSearch(String query) {
        List<RecentSearchItem> current = _recentSearches.getValue();
        List<RecentSearchItem> updated = (current != null) ? new ArrayList<>(current) : new ArrayList<>();

        updated.removeIf(item -> item.getText().equalsIgnoreCase(query));
        updated.add(0, new RecentSearchItem(query));

        if (updated.size() > 10) {
            updated = updated.subList(0, 10);
        }

        _recentSearches.setValue(updated);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (debounceRunnable != null) {
            debounceHandler.removeCallbacks(debounceRunnable);
        }
    }
}
