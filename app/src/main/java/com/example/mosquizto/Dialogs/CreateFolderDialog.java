package com.example.mosquizto.Dialogs;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.mosquizto.R;

public class CreateFolderDialog extends DialogFragment {

    private EditText etFolderName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Trỏ đúng vào layout dialog folder
        View view = inflater.inflate(R.layout.dialog_create_folder, container, false);

        etFolderName = view.findViewById(R.id.et_folder_name);
        TextView btnCancel = view.findViewById(R.id.tv_cancel);
        ImageButton btnSave = view.findViewById(R.id.btn_save_folder);

        // Chức năng Hủy (Cancel) -> Đóng cửa sổ
        btnCancel.setOnClickListener(v -> dismiss());

        // Chức năng Lưu
        btnSave.setOnClickListener(v -> {
            String name = etFolderName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập tên thư mục", Toast.LENGTH_SHORT).show();
            } else {
                // TODO: Gọi API lưu thư mục xuống Backend
                Toast.makeText(getContext(), "Đã tạo thư mục: " + name, Toast.LENGTH_SHORT).show();
                dismiss();
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                // Set Dialog này full toàn màn hình để giống y chang Quizlet
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                // Đặt màu nền mặc định nếu bị lỗi viền đen
                window.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#F6F7FB")));
            }
        }
    }
}