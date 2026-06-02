package com.example.mosquizto.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.mosquizto.Adapters.LibraryPagerAdapter;
import com.example.mosquizto.MainActivity;
import com.example.mosquizto.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class LibraryFragment extends Fragment {
    private TextView tvTitle;
    private EditText etSearch;
    private ImageButton btnSearch;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Load layout fragment_library.xml đã tạo ở bài trước
        View view = inflater.inflate(R.layout.fragment_library, container, false);

        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        ViewPager2 viewPager = view.findViewById(R.id.view_pager);
        tvTitle = view.findViewById(R.id.tv_library_title);
        etSearch = view.findViewById(R.id.et_library_search);
        btnSearch = view.findViewById(R.id.btn_search);

        // Gắn adapter cho ViewPager
        viewPager.setAdapter(new LibraryPagerAdapter(this));

        // Liên kết TabLayout với ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText("Flashcard sets");
            } else {
                tab.setText("Folders");
            }
        }).attach();

        View btnAdd = view.findViewById(R.id.btn_add);
        if (btnAdd != null) {
            btnAdd.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).showCreateMenu();
                }
            });
        }

        btnSearch.setOnClickListener(v -> toggleSearchBar());

        // --- 1. TỰ ĐỘNG ẨN THANH SEARCH KHI BẤM RA NGOÀI (MẤT FOCUS) ---
        etSearch.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                if (etSearch.getVisibility() == View.VISIBLE) {
                    toggleSearchBar();
                }
            }
        });

        // --- 2. TỰ ĐỘNG ẨN THANH SEARCH KHI CHUYỂN TAB ---
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (etSearch != null && etSearch.getVisibility() == View.VISIBLE) {
                    toggleSearchBar();
                }
            }
        });

        // --- BẮT SỰ KIỆN KHI NGƯỜI DÙNG GÕ CHỮ ---
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().toLowerCase().trim();

                // Lấy danh sách các Fragment con bên trong ViewPager và gọi hàm filter của chúng
                for (Fragment fragment : getChildFragmentManager().getFragments()) {
                    if (fragment instanceof FlashcardSetsFragment) {
                        ((FlashcardSetsFragment) fragment).filterData(query);
                    } else if (fragment instanceof FoldersFragment) {
                        ((FoldersFragment) fragment).filterData(query);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        return view;
    }

    private void toggleSearchBar() {
        if (etSearch.getVisibility() == View.GONE) {
            // Mở thanh search
            etSearch.setVisibility(View.VISIBLE);
            tvTitle.setVisibility(View.GONE);
            etSearch.requestFocus();
            // Bật bàn phím ảo
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.showSoftInput(etSearch, InputMethodManager.SHOW_IMPLICIT);
        } else {
            // Đóng thanh search
            etSearch.setVisibility(View.GONE);
            tvTitle.setVisibility(View.VISIBLE);
            etSearch.setText(""); // Xóa trắng chữ đang tìm
            etSearch.clearFocus(); //Xóa bỏ tiêu điểm focus cũ
            // Tắt bàn phím ảo
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
        }
    }
}