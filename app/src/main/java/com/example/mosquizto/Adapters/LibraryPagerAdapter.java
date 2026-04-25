package com.example.mosquizto.Adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.example.mosquizto.Fragments.FoldersFragment;
import com.example.mosquizto.Fragments.FlashcardSetsFragment;

public class LibraryPagerAdapter extends FragmentStateAdapter {

    public LibraryPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Tab 0: Flashcard sets, Tab 1: Folders
        if (position == 1) {
            return new FoldersFragment();
        }
        return new FlashcardSetsFragment();
    }

    @Override
    public int getItemCount() {
        return 2; // 2 tabs: Sets, Folders
    }
}