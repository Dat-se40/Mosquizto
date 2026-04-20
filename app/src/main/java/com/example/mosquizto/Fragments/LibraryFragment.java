package com.example.mosquizto.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.mosquizto.Adapters.LibraryPagerAdapter;
import com.example.mosquizto.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class LibraryFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Load layout fragment_library.xml đã tạo ở bài trước
        View view = inflater.inflate(R.layout.fragment_library, container, false);

        TabLayout tabLayout = view.findViewById(R.id.library_tabs);
        ViewPager2 viewPager = view.findViewById(R.id.library_viewpager);

        // Gắn adapter cho ViewPager
        viewPager.setAdapter(new LibraryPagerAdapter(this));

        // Liên kết TabLayout với ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Học phần"); // Sets
                    break;
                case 1:
                    tab.setText("Thư mục");  // Folders
                    break;
                case 2:
                    tab.setText("Lớp học");  // Classes
                    break;
            }
        }).attach();

        return view;
    }
}