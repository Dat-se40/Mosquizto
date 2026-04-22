package com.example.mosquizto;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.mosquizto.Fragments.HomeFragment;
import com.example.mosquizto.Fragments.SearchFragment;
import com.example.mosquizto.Util.FragmentTag;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    private Fragment homeFragment;
    private Fragment searchFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            switchToFragment(FragmentTag.home);
        }
    }

    public void switchToFragment(FragmentTag tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment targetFragment = null;
        String tagStr = tag.name();

        switch (tag) {
            case home:
                if (homeFragment == null) homeFragment = new HomeFragment();
                targetFragment = homeFragment;
                break;
            case search:
                if (searchFragment == null) searchFragment = new SearchFragment();
                targetFragment = searchFragment;
                break;
        }

        if (targetFragment != null) {
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, targetFragment, tagStr)
                    .addToBackStack(tagStr)
                    .commit();
        }
    }
    
    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }
}
