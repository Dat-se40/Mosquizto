package com.example.mosquizto.Fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mosquizto.Activities.StudySetDetailActivity;
import com.example.mosquizto.MainActivity;
import com.example.mosquizto.R;
import com.example.mosquizto.Adapters.RecentSearchAdapter;
import com.example.mosquizto.Adapters.SearchResultAdapter;
import com.example.mosquizto.Adapters.SuggestionAdapter;
import com.example.mosquizto.Dto.response.SearchResultItem;
import com.example.mosquizto.ViewModels.SearchViewModel;

import java.util.ArrayList;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SearchFragment extends Fragment {

    private SearchViewModel viewModel;

    private EditText etSearch;
    private ImageView ivClear;
    private ImageView ivCamera;
    private TextView tvCancel;
    private TextView tvClearAll;

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
    private MainActivity mainActivity;
    private com.google.android.material.chip.ChipGroup cgSearchType;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initAdapters();
        initViewModel();
        setupListeners();

        etSearch.requestFocus();
        showKeyboard(etSearch);
    }

    private void initViews(View view) {
        etSearch = view.findViewById(R.id.etSearch);
        ivClear = view.findViewById(R.id.ivClear);
        tvCancel = view.findViewById(R.id.tvCancel);
        tvClearAll = view.findViewById(R.id.tvClearAll);
        cgSearchType = view.findViewById(R.id.cgSearchType);
        recentSection = view.findViewById(R.id.recentSection);
        suggestionSection = view.findViewById(R.id.suggestionSection);
        resultsSection = view.findViewById(R.id.resultsSection);
        emptyState = view.findViewById(R.id.emptyState);
        progressBar = view.findViewById(R.id.progressBar);

        rvRecentSearches = view.findViewById(R.id.rvRecentSearches);
        rvSuggestions = view.findViewById(R.id.rvSuggestions);
        rvSearchResults = view.findViewById(R.id.rvSearchResults);
        if (getActivity() instanceof MainActivity)
            mainActivity = (MainActivity) getActivity();
    }

    private void initAdapters() {
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
        rvRecentSearches.setLayoutManager(new LinearLayoutManager(getContext()));
        rvRecentSearches.setAdapter(recentSearchAdapter);

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
        rvSuggestions.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSuggestions.setAdapter(suggestionAdapter);

        searchResultAdapter = new SearchResultAdapter(new ArrayList<>(), new SearchResultAdapter.OnResultClickListener() {
            @Override
            public void onResultClick(SearchResultItem item) {
                if(mainActivity != null)
                    mainActivity.GoToStudySetActivity(getContext(), Math.toIntExact(item.getId()), item.getTitle(), item.getCreatedByUsername());
            }

            @Override
            public void onMoreClick(SearchResultItem item) {
                Toast.makeText(getContext(), "More: " + item.getTitle(), Toast.LENGTH_SHORT).show();
            }
        });
        rvSearchResults.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSearchResults.setAdapter(searchResultAdapter);
    }

    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(SearchViewModel.class);

        viewModel.recentSearches.observe(getViewLifecycleOwner(), recentItems -> {
            recentSearchAdapter.updateData(recentItems);
            tvClearAll.setVisibility(recentItems != null && !recentItems.isEmpty() ? View.VISIBLE : View.GONE);
        });

        viewModel.suggestions.observe(getViewLifecycleOwner(), suggestionList -> {
            suggestionAdapter.updateData(suggestionList);
        });

        viewModel.searchResults.observe(getViewLifecycleOwner(), results -> {
            searchResultAdapter.updateData(results);
        });

        viewModel.isLoading.observe(getViewLifecycleOwner(), loading -> {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        });

        viewModel.searchState.observe(getViewLifecycleOwner(), this::updateUIByState);
    }

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
            if (actionId == EditorInfo.IME_ACTION_SEARCH || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
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
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });
        cgSearchType.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int checkedId = checkedIds.get(0);

            if (checkedId == R.id.chipStudySet) {
                viewModel.setSearchType(SearchViewModel.SearchType.COLLECTION);
            } else if (checkedId == R.id.chipUser) {
                viewModel.setSearchType(SearchViewModel.SearchType.USER);
            }
        });
        tvClearAll.setOnClickListener(v -> viewModel.clearAllRecentSearches());

    }

    private void updateUIByState(SearchViewModel.SearchState state) {
        recentSection.setVisibility(View.GONE);
        suggestionSection.setVisibility(View.GONE);
        resultsSection.setVisibility(View.GONE);
        emptyState.setVisibility(View.GONE);

        switch (state) {
            case IDLE:
                recentSection.setVisibility(View.VISIBLE);
                break;
            case TYPING:
                suggestionSection.setVisibility(View.VISIBLE);
                break;
            case LOADING:
                // ProgressBar handles this
                break;
            case HAS_RESULTS:
                resultsSection.setVisibility(View.VISIBLE);
                break;
            case EMPTY:
                emptyState.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void showKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        View focus = requireActivity().getCurrentFocus();
        if (imm != null && focus != null) imm.hideSoftInputFromWindow(focus.getWindowToken(), 0);
    }
}
