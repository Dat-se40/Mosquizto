package com.example.mosquizto;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.example.mosquizto.Activities.CreateCollectionActivity;
import com.example.mosquizto.Activities.StudySetDetailActivity;
import com.example.mosquizto.Dialogs.CreateFolderDialog;
import com.example.mosquizto.Dto.response.CollectionResponse;
import com.example.mosquizto.Event.LoginSuccessEvent;
import com.example.mosquizto.Fragments.HomeFragment;
import com.example.mosquizto.Fragments.LibraryFragment;
import com.example.mosquizto.Services.SessionManager;
import com.example.mosquizto.ViewModels.MainViewModel;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.example.mosquizto.Activities.ProfilePage;
import dagger.hilt.android.AndroidEntryPoint;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;
import ua.naiksoftware.stomp.dto.StompHeader;

import com.example.mosquizto.Fragments.SearchFragment;
import com.example.mosquizto.Util.FragmentTag;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {
    @Inject
    public SessionManager sessionManager; // Inject để xài
    private MainViewModel viewModel ;

    private StompClient stompClient;
    private Fragment homeFragment;
    private Fragment searchFragment;
    private Fragment libraryFragment;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        setupNavigation();
        observeNotifications();

        EventBus.getDefault().register(this);
        if (sessionManager.isLoggedIn()) {
            viewModel.connectStomp(sessionManager.getAccessToken());
        }
         if (viewModel.getNotificationCount().getValue() > 0)
         {
             BadgeDrawable badge = bottomNav.getOrCreateBadge(R.id.nav_profile);
             badge.setVisible(true);
             badge.setNumber(viewModel.getNotificationCount().getValue());
         }
    }

    private void observeNotifications() {
        viewModel.getNotifications().observe(this, message ->
        {
        });
        viewModel.getNotificationCount().observe(this, count ->
        {
            BadgeDrawable badge = bottomNav.getOrCreateBadge(R.id.nav_profile);
            if (count != null && count > 0) {
                badge.setVisible(true);
                badge.setNumber(count);
            } else {
                badge.setVisible(false);
                badge.clearNumber();
            }
        });
    }

    private void setupNavigation() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
                    getSupportFragmentManager().popBackStack();
                } else {
                    finish();
                }
            }
        });

        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_home);
        switchToFragment(FragmentTag.home);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                switchToFragment(FragmentTag.home);
                return true;
            } else if (id == R.id.nav_create) {
                showCreateMenu();
                return false;
            } else if (id == R.id.nav_library) {
                switchToLibrary();
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfilePage.class));
                return false;
            }
            return false;
        });
    }
    public void switchToFragment(FragmentTag tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment targetFragment = null;
        String tagStr;

        switch (tag) {
            case home:
                tagStr = getString(R.string.tag_home);
                if (homeFragment == null) homeFragment = new HomeFragment();
                targetFragment = homeFragment;
                break;
            case search:
                tagStr = getString(R.string.tag_search);
                if (searchFragment == null) searchFragment = new SearchFragment();
                targetFragment = searchFragment;
                break;
            default:
                tagStr = tag.name();
                break;
        }

        if (targetFragment != null) {
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, targetFragment, tagStr)
                    .addToBackStack(tagStr)
                    .commit();
        }
    }

    private void switchToLibrary() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (libraryFragment == null) {
            libraryFragment = new LibraryFragment();
        }

        String tagLibrary = getString(R.string.tag_library);
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, libraryFragment, tagLibrary)
                .addToBackStack(tagLibrary)
                .commit();
    }
    public void showCreateMenu() {
        View v = getLayoutInflater().inflate(R.layout.dialog_create_menu, null);
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(v);

        // Bấm Tạo Học Phần (Flashcard)
        v.findViewById(R.id.option_flashcard_set).setOnClickListener(view -> {
            startActivity(new Intent(this, CreateCollectionActivity.class));
            dialog.dismiss();
        });

        // Bấm Tạo Thư Mục (Folder) -> Mở Dialog
        v.findViewById(R.id.option_folder).setOnClickListener(view -> {
            CreateFolderDialog folderDialog = new CreateFolderDialog();
            folderDialog.show(getSupportFragmentManager(), getString(R.string.tag_folder_dialog));
            dialog.dismiss();
        });

        dialog.show();
    }

    public static void GoToStudySetActivity(Context context , CollectionResponse item)
    {
        GoToStudySetActivity(context , item.getId(), item.getTitle(), item.getUserName());
    }

    public static void GoToStudySetActivity(Context context , Integer id , String title)
    {
        GoToStudySetActivity(context, id, title, null);
    }

    public static void GoToStudySetActivity(Context context , Integer id , String title, String author)
    {
        Intent intent = new Intent(context, StudySetDetailActivity.class);
        intent.putExtra(context.getString(R.string.intent_key_collection_id), id != null ? id : -1);
        intent.putExtra(context.getString(R.string.intent_key_collection_title), title);
        intent.putExtra(context.getString(R.string.intent_key_author), author);
        context.startActivity(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (stompClient != null && stompClient.isConnected()) {
            stompClient.disconnect();
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onLoginEvent(LoginSuccessEvent event) {
        Log.i(getString(R.string.log_tag_ws), "Login event received, connecting STOMP...");
        viewModel.connectStomp(event.token);
        EventBus.getDefault().removeStickyEvent(event);
    }
}
