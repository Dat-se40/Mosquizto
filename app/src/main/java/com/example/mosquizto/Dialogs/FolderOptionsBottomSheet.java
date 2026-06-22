package com.example.mosquizto.Dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.mosquizto.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class FolderOptionsBottomSheet extends BottomSheetDialogFragment {

    private OptionsListener listener;

    public interface OptionsListener {
        void onAddMaterials();
        void onEditFolder();
        void onShareFolder();
        void onDeleteFolder();
    }

    public void setListener(OptionsListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_folder_menu, container, false);

        view.findViewById(R.id.tvAddMaterials).setOnClickListener(v -> {
            if(listener != null) listener.onAddMaterials();
            dismiss();
        });

        view.findViewById(R.id.tvEditFolder).setOnClickListener(v -> {
            if(listener != null) listener.onEditFolder();
            dismiss();
        });

        View tvShareFolder = view.findViewById(R.id.tvShareFolder);
        if (tvShareFolder != null) {
            tvShareFolder.setVisibility(View.GONE);
        }

        view.findViewById(R.id.tvDeleteFolder).setOnClickListener(v -> {
            if(listener != null) listener.onDeleteFolder();
            dismiss();
        });

        return view;
    }
}