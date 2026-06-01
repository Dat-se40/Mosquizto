package com.example.mosquizto.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.example.mosquizto.R;
import com.google.android.material.button.MaterialButton;

public class FlashcardSettingsDialog extends Dialog {

    public interface SettingsListener {
        void onSettingsChanged(boolean showTermFirst, boolean textToSpeech, boolean shuffle);
        void onResetCards();
    }

    private SettingsListener listener;
    private boolean showTermFirst;
    private boolean textToSpeech;
    private boolean shuffle;

    public FlashcardSettingsDialog(@NonNull Context context,
                                   boolean showTermFirst,
                                   boolean textToSpeech,
                                   boolean shuffle,
                                   SettingsListener listener) {
        super(context, R.style.BottomSheetDialogTheme);
        this.showTermFirst = showTermFirst;
        this.textToSpeech = textToSpeech;
        this.shuffle = shuffle;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_flashcard_settings);

        Switch switchTts = findViewById(R.id.switch_tts);
        Switch switchShuffle = findViewById(R.id.switch_shuffle);

        // Khai báo chuẩn MaterialButton
        MaterialButton btnTermFirst = findViewById(R.id.btn_term_first);
        MaterialButton btnDefinitionFirst = findViewById(R.id.btn_definition_first);

        TextView tvReset = findViewById(R.id.tv_reset);
        TextView tvClose = findViewById(R.id.tv_close);

        // Đặt trạng thái ban đầu
        switchTts.setChecked(textToSpeech);
        switchShuffle.setChecked(shuffle);
        updateFrontToggle(btnTermFirst, btnDefinitionFirst, showTermFirst);

        switchTts.setOnCheckedChangeListener((buttonView, isChecked) -> {
            this.textToSpeech = isChecked;
            notifyListener();
        });

        switchShuffle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            this.shuffle = isChecked;
            notifyListener();
        });

        btnTermFirst.setOnClickListener(v -> {
            showTermFirst = true;
            updateFrontToggle(btnTermFirst, btnDefinitionFirst, true);
            notifyListener();
        });

        btnDefinitionFirst.setOnClickListener(v -> {
            showTermFirst = false;
            updateFrontToggle(btnTermFirst, btnDefinitionFirst, false);
            notifyListener();
        });

        tvReset.setOnClickListener(v -> {
            if (listener != null) listener.onResetCards();
            dismiss();
        });

        tvClose.setOnClickListener(v -> dismiss());
    }

    private void updateFrontToggle(MaterialButton matBtnTerm, MaterialButton matBtnDef, boolean termFirst) {
        // Khai báo các màu tĩnh
        int colorSelected = ContextCompat.getColor(getContext(), R.color.background_white);
        int colorTransparent = ContextCompat.getColor(getContext(), android.R.color.transparent);
        int colorTextPrimary = ContextCompat.getColor(getContext(), R.color.text_color_primary);
        int colorTextSecondary = ContextCompat.getColor(getContext(), R.color.text_color_secondary);

        if (termFirst) {
            // Thuật ngữ được chọn -> Đổ nền trắng, chữ đậm nổi bật
            matBtnTerm.setBackgroundTintList(android.content.res.ColorStateList.valueOf(colorSelected));
            matBtnTerm.setTextColor(colorTextPrimary);

            // Định nghĩa bị bỏ chọn -> Nền trong suốt, chữ phụ mờ đi
            matBtnDef.setBackgroundTintList(android.content.res.ColorStateList.valueOf(colorTransparent));
            matBtnDef.setTextColor(colorTextSecondary);
        } else {
            // Định nghĩa được chọn -> Đổ nền trắng, chữ đậm nổi bật
            matBtnDef.setBackgroundTintList(android.content.res.ColorStateList.valueOf(colorSelected));
            matBtnDef.setTextColor(colorTextPrimary);

            // Thuật ngữ bị bỏ chọn -> Nền trong suốt, chữ phụ mờ đi
            matBtnTerm.setBackgroundTintList(android.content.res.ColorStateList.valueOf(colorTransparent));
            matBtnTerm.setTextColor(colorTextSecondary);
        }
    }

    private void notifyListener() {
        if (listener != null) {
            listener.onSettingsChanged(showTermFirst, textToSpeech, shuffle);
        }
    }
}