package com.example.mosquizto.Adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.mosquizto.Fragments.OtherUserCollectionsFragment;

public class OtherUserProfilePagerAdapter extends FragmentStateAdapter {

    private final String username;
    private final String fullName;
    public OtherUserProfilePagerAdapter(@NonNull FragmentActivity fragmentActivity, String username , String fullName) {
        super(fragmentActivity);
        this.username = username;
        this.fullName = fullName ;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Tab 0: Public Collections (Study Sets)
        return OtherUserCollectionsFragment.newInstance(username , fullName);
    }

    @Override
    public int getItemCount() {
        return 1; // 1 tab: Study Sets
    }
}
