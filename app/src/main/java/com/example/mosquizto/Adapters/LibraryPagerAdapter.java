package com.example.mosquizto.Adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.mosquizto.Fragments.FlashcardSetsFragment;

public class LibraryPagerAdapter extends FragmentStateAdapter {

    public LibraryPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Tạm thời cả 3 tab đều load danh sách Flashcard (bạn có thể tạo thêm FolderFragment sau)
        return new FlashcardSetsFragment();
    }

    @Override
    public int getItemCount() {
        return 3; // 3 tabs: Sets, Folders, Classes
    }
}