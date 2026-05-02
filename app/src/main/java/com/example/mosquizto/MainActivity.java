package com.example.mosquizto;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.mosquizto.Activities.CreateCollectionActivity;
import com.example.mosquizto.Activities.StudySetDetailActivity;
import com.example.mosquizto.Dialogs.CreateFolderDialog;
import com.example.mosquizto.Dto.response.CollectionResponse;
import com.example.mosquizto.Fragments.HomeFragment;
import com.example.mosquizto.Fragments.LibraryFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.example.mosquizto.Activities.ProfilePage;
import dagger.hilt.android.AndroidEntryPoint;
import com.example.mosquizto.Fragments.SearchFragment;
import com.example.mosquizto.Util.FragmentTag;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    private Fragment homeFragment;
    private Fragment searchFragment;
    private Fragment libraryFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
                    getSupportFragmentManager().popBackStack();
                } else {
                    finish(); // Thoát app nếu không còn Fragment nào để lùi
                }
            }
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
            switchToFragment(FragmentTag.home);
        }

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                switchToFragment(FragmentTag.home); // Dùng hàm của master
                return true;
            } else if (id == R.id.nav_create) {
                showCreateMenu(); // Của bạn: Hiện Popup, không chuyển màu tab
                return false;
            } else if (id == R.id.nav_library) {
                switchToLibrary(); // Của bạn nhưng được tối ưu theo master
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(MainActivity.this, ProfilePage.class));
                return false; // Chuyển trang, không đổi màu tab
            }
            return false;
        });
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
            // Nếu bạn có thêm 'library' vào enum FragmentTag, bạn có thể đưa nó vào switch này
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

        // Chỉ tạo mới 1 lần duy nhất, các lần sau dùng lại để chạy mượt
        if (libraryFragment == null) {
            libraryFragment = new LibraryFragment();
        }

        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, libraryFragment, "library")
                .addToBackStack("library")
                .commit();
    }

    // Hàm của bạn: Hiện Menu tạo mới
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
            folderDialog.show(getSupportFragmentManager(), "FolderDialog");
            dialog.dismiss();
        });

        dialog.show();
    }
    public void GoToStudySetActivity(Context context , CollectionResponse item)
    {
        GoToStudySetActivity(context , item.getId(), item.getTitle());
    }
    public void GoToStudySetActivity(Context context , Integer id , String title)
    {

        Intent intent = new Intent(context, StudySetDetailActivity.class);
        intent.putExtra("COLLECTION_ID", id != null ? id : -1   );
        intent.putExtra("COLLECTION_TITLE", title);
        startActivity(intent);

    }
}