package com.example.mosquizto.Activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mosquizto.R;
import com.example.mosquizto.adapter.RecentSearchAdapter;
import com.example.mosquizto.adapter.SearchResultAdapter;
import com.example.mosquizto.adapter.SuggestionAdapter;
import com.example.mosquizto.model.RecentSearchItem;
import com.example.mosquizto.model.SearchResultItem;
import com.example.mosquizto.ViewModels.SearchViewModel;

import java.util.ArrayList;

public class Search extends AppCompatActivity {

    private SearchViewModel viewModel;

    private EditText etSearch;
    private ImageView ivClear;
    private ImageView ivCamera;
    private TextView tvCancel;
    private TextView tvClearAll;
    private LinearLayout btnScan;

    private LinearLayout recentSection;
    private LinearLayout suggestionSection;
    private LinearLayout resultsSection;
    private LinearLayout emptyState;
    private ProgressBar progressBar;

    private RecyclerView rvRecentSearches;
    private RecyclerView rvSuggestions;
    private RecyclerView rvSearchResults;

    private RecentSearchAdapter recentSearchAdapter;
    private SuggestionAdapter suggestionAdapter;
    private SearchResultAdapter searchResultAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initViews();
        initAdapters();
        initViewModel();
        setupListeners();

        etSearch.requestFocus();
        showKeyboard(etSearch);
    }

    // =========================================================
    // INIT
    // =========================================================

    private void initViews() {
        etSearch = findViewById(R.id.etSearch);
        ivClear = findViewById(R.id.ivClear);
        ivCamera = findViewById(R.id.ivCamera);
        tvCancel = findViewById(R.id.tvCancel);
        tvClearAll = findViewById(R.id.tvClearAll);
        btnScan = findViewById(R.id.btnScan);

        recentSection = findViewById(R.id.recentSection);
        suggestionSection = findViewById(R.id.suggestionSection);
        resultsSection = findViewById(R.id.resultsSection);
        emptyState = findViewById(R.id.emptyState);
        progressBar = findViewById(R.id.progressBar);

        rvRecentSearches = findViewById(R.id.rvRecentSearches);
        rvSuggestions = findViewById(R.id.rvSuggestions);
        rvSearchResults = findViewById(R.id.rvSearchResults);
    }

    private void initAdapters() {
        // Recent searches adapter
        recentSearchAdapter = new RecentSearchAdapter(new ArrayList<>(), new RecentSearchAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String text) {
                etSearch.setText(text);
                etSearch.setSelection(text.length());
                viewModel.onSearchSubmitted(text);
                hideKeyboard();
            }

            @Override
            public void onArrowClick(String text) {
                etSearch.setText(text);
                etSearch.setSelection(text.length());
            }
        });
        rvRecentSearches.setLayoutManager(new LinearLayoutManager(this));
        rvRecentSearches.setAdapter(recentSearchAdapter);

        // Suggestion adapter
        suggestionAdapter = new SuggestionAdapter(new ArrayList<>(), new SuggestionAdapter.OnSuggestionClickListener() {
            @Override
            public void onSuggestionClick(String text) {
                etSearch.setText(text);
                etSearch.setSelection(text.length());
                viewModel.onSearchSubmitted(text);
                hideKeyboard();
            }

            @Override
            public void onFillClick(String text) {
                etSearch.setText(text);
                etSearch.setSelection(text.length());
            }
        });
        rvSuggestions.setLayoutManager(new LinearLayoutManager(this));
        rvSuggestions.setAdapter(suggestionAdapter);

        // Search result adapter
        searchResultAdapter = new SearchResultAdapter(new ArrayList<>(), new SearchResultAdapter.OnResultClickListener() {
            @Override
            public void onResultClick(SearchResultItem item) {
                // TODO: Navigate đến màn hình chi tiết
                Toast.makeText(Search.this, "Mở: " + item.getTitle(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onMoreClick(SearchResultItem item) {
                // TODO: Hiện bottom sheet options
                Toast.makeText(Search.this, "More: " + item.getTitle(), Toast.LENGTH_SHORT).show();
            }
        });
        rvSearchResults.setLayoutManager(new LinearLayoutManager(this));
        rvSearchResults.setAdapter(searchResultAdapter);
    }

    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(SearchViewModel.class);

        // Observe recent searches
        viewModel.recentSearches.observe(this, recentItems -> {
            recentSearchAdapter.updateData(recentItems);
            tvClearAll.setVisibility(recentItems != null && !recentItems.isEmpty() ? View.VISIBLE : View.GONE);
        });

        // Observe suggestions
        viewModel.suggestions.observe(this, suggestionList -> {
            suggestionAdapter.updateData(suggestionList);
        });

        // Observe search results
        viewModel.searchResults.observe(this, results -> {
            searchResultAdapter.updateData(results);
        });

        // Observe loading
        viewModel.isLoading.observe(this, loading -> {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        });

        // Observe state → điều khiển show/hide các section
        viewModel.searchState.observe(this, state -> {
            switch (state) {
                case IDLE:
                    showSection(Section.RECENT);
                    break;
                case TYPING:
                    showSection(Section.SUGGESTIONS);
                    break;
                case LOADING:
                    showSection(Section.LOADING);
                    break;
                case HAS_RESULTS:
                    showSection(Section.RESULTS);
                    break;
                case EMPTY:
                    showSection(Section.EMPTY);
                    break;
            }
        });
    }

    // =========================================================
    // LISTENERS
    // =========================================================

    private void setupListeners() {

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();

                ivClear.setVisibility(query.isEmpty() ? View.GONE : View.VISIBLE);
                ivCamera.setVisibility(query.isEmpty() ? View.VISIBLE : View.GONE);

                viewModel.onQueryChanged(query);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                String query = etSearch.getText().toString().trim();
                if (!query.isEmpty()) {
                    viewModel.onSearchSubmitted(query);
                    hideKeyboard();
                }
                return true;
            }
            return false;
        });

        ivClear.setOnClickListener(v -> {
            etSearch.setText("");
            etSearch.requestFocus();
            showKeyboard(etSearch);
        });

        tvCancel.setOnClickListener(v -> {
            hideKeyboard();
            finish();
        });

        ivCamera.setOnClickListener(v -> {
            Toast.makeText(this, "Camera search (chưa làm)", Toast.LENGTH_SHORT).show();
        });

        btnScan.setOnClickListener(v -> {
            Toast.makeText(this, "Scan SGK (chưa làm)", Toast.LENGTH_SHORT).show();
        });

        tvClearAll.setOnClickListener(v -> {
            viewModel.clearAllRecentSearches();
        });
    }

    // =========================================================
    // UI HELPERS
    // =========================================================

    private enum Section { RECENT, SUGGESTIONS, LOADING, RESULTS, EMPTY }

    /**
     * Ẩn tất cả sections, chỉ hiện section được chỉ định
     */
    private void showSection(Section section) {
        recentSection.setVisibility(View.GONE);
        suggestionSection.setVisibility(View.GONE);
        resultsSection.setVisibility(View.GONE);
        emptyState.setVisibility(View.GONE);

        switch (section) {
            case RECENT:
                recentSection.setVisibility(View.VISIBLE);
                break;
            case SUGGESTIONS:
                suggestionSection.setVisibility(View.VISIBLE);
                break;
            case LOADING:
                // progressBar được handle bởi isLoading observer
                break;
            case RESULTS:
                resultsSection.setVisibility(View.VISIBLE);
                break;
            case EMPTY:
                emptyState.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void showKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        View focus = getCurrentFocus();
        if (imm != null && focus != null) imm.hideSoftInputFromWindow(focus.getWindowToken(), 0);
    }
}