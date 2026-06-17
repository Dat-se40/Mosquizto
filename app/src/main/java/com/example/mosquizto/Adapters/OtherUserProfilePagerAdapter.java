package com.example.mosquizto.Adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.mosquizto.Fragments.OtherUserCollectionsFragment;

public class OtherUserProfilePagerAdapter extends FragmentStateAdapter {

    private final String username;

    public OtherUserProfilePagerAdapter(@NonNull FragmentActivity fragmentActivity, String username) {
        super(fragmentActivity);
        this.username = username;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Tab 0: Public Collections (Study Sets)
        return OtherUserCollectionsFragment.newInstance(username);
    }

    @Override
    public int getItemCount() {
        return 1; // 1 tab: Study Sets
    }
}
