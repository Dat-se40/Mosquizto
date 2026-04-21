package com.example.mosquizto;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.mosquizto.Activities.CreateCollectionActivity;
import com.example.mosquizto.Dialogs.CreateFolderDialog;
import com.example.mosquizto.Fragments.FlashcardSetsFragment;
import com.example.mosquizto.Fragments.HomeFragment;
import com.example.mosquizto.Fragments.LibraryFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_library);
            loadFragment(new LibraryFragment());
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (id == R.id.nav_library) {
                // Trang Library của Quizlet chứa TabLayout,
                // ở đây mình gọi thẳng FlashcardSetsFragment để bạn thấy kết quả ngay
                selectedFragment = new LibraryFragment();;
            }
            // Thêm logic cho nav_create và nav_account ở đây

            return loadFragment(selectedFragment);
        });
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    private void showCreateMenu() {
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
}