package com.example.mosquizto.Dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.mosquizto.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class RemoveFromFolderBottomSheet extends BottomSheetDialogFragment {

    private String collectionTitle;
    private RemoveListener listener;

    public interface RemoveListener {
        void onRemoveClicked();
    }

    public RemoveFromFolderBottomSheet(String collectionTitle, RemoveListener listener) {
        this.collectionTitle = collectionTitle;
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_remove_from_folder, container, false);

        TextView tvCollectionName = view.findViewById(R.id.tvCollectionName);
        tvCollectionName.setText(collectionTitle);

        view.findViewById(R.id.tvRemoveSet).setOnClickListener(v -> {
            if(listener != null) listener.onRemoveClicked();
            dismiss();
        });

        return view;
    }
}