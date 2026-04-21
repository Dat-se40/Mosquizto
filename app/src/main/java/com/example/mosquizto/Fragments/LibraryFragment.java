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

        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        ViewPager2 viewPager = view.findViewById(R.id.view_pager);

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

        return view;
    }
}