package com.example.mosquizto.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.mosquizto.R;

public class FoldersFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // CHÚ THÍCH: Tạm thời bạn có thể trả về null hoặc một layout trống (Ví dụ res/layout/fragment_folders.xml).
        // Mình tái sử dụng layout trống nếu bạn chưa thiết kế Folders.
        return new View(getContext());
    }
}