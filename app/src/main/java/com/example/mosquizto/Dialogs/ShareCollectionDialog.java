package com.example.mosquizto.Dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.mosquizto.R;
import com.example.mosquizto.Util.CollectionRole;
import com.example.mosquizto.databinding.DialogShareCollectionBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class ShareCollectionDialog extends DialogFragment {

    public interface OnShareListener {
        void onShare(String username, CollectionRole role);
    }

    private static final String ARG_IS_OWNER = "is_owner";

    // Tất cả role có thể share — OWNER bị loại vì không thể cấp quyền owner cho người khác
    private static final CollectionRole[] ALL_ROLES = {
            CollectionRole.EDITOR,
            CollectionRole.VIEWER
    };

    private static final String[] ALL_ROLE_LABELS = {
            "Editor",
            "Viewer"
    };

    private static final String[] ALL_ROLE_HINTS = {
            "Can edit the collection",
            "Can view the collection"
    };

    // Nếu không phải owner, chỉ được share VIEWER
    private static final CollectionRole[] VIEWER_ONLY_ROLES = {
            CollectionRole.VIEWER
    };

    private static final String[] VIEWER_ONLY_LABELS = {
            "Viewer"
    };

    private static final String[] VIEWER_ONLY_HINTS = {
            "Can view the collection"
    };

    private DialogShareCollectionBinding binding;
    private OnShareListener listener;
    private CollectionRole selectedRole = CollectionRole.VIEWER;

    // Role arrays thực sự dùng, tùy theo isOwner
    private CollectionRole[] availableRoles;
    private String[] roleLabels;
    private String[] roleHints;

    public static ShareCollectionDialog newInstance(boolean isOwner) {
        ShareCollectionDialog dialog = new ShareCollectionDialog();
        Bundle args = new Bundle();
        args.putBoolean(ARG_IS_OWNER, isOwner);
        dialog.setArguments(args);
        return dialog;
    }

    public void setOnShareListener(OnShareListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = DialogShareCollectionBinding.inflate(LayoutInflater.from(requireContext()));

        boolean isOwner = getArguments() != null && getArguments().getBoolean(ARG_IS_OWNER, false);
        if (isOwner) {
            availableRoles = ALL_ROLES;
            roleLabels = ALL_ROLE_LABELS;
            roleHints = ALL_ROLE_HINTS;
        } else {
            availableRoles = VIEWER_ONLY_ROLES;
            roleLabels = VIEWER_ONLY_LABELS;
            roleHints = VIEWER_ONLY_HINTS;
        }

        setupRoleDropdown();
        setupButtons();

        return new MaterialAlertDialogBuilder(requireContext())
                .setView(binding.getRoot())
                .create();
    }

    private void setupRoleDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                roleLabels
        );
        binding.actvRole.setAdapter(adapter);

        // Mặc định chọn role cuối (VIEWER) — an toàn nhất
        int defaultIndex = roleLabels.length - 1;
        binding.actvRole.setText(roleLabels[defaultIndex], false);
        binding.tvRoleHint.setText(roleHints[defaultIndex]);
        selectedRole = availableRoles[defaultIndex];

        // Nếu chỉ có 1 role thì disable dropdown, người dùng không cần chọn
        if (availableRoles.length == 1) {
            binding.actvRole.setEnabled(false);
            binding.actvRole.setFocusable(false);
            binding.tilRole.setEndIconMode(com.google.android.material.textfield.TextInputLayout.END_ICON_NONE);
        }

        binding.actvRole.setOnItemClickListener((parent, view, position, id) -> {
            selectedRole = availableRoles[position];
            binding.tvRoleHint.setText(roleHints[position]);
        });
    }

    private void setupButtons() {
        binding.btnCancelShare.setOnClickListener(v -> dismiss());

        binding.btnShare.setOnClickListener(v -> {
            if (!validateInput()) return;

            String username = binding.etUsername.getText().toString().trim();
            if (listener != null) {
                listener.onShare(username, selectedRole);
            }
            dismiss();
        });
    }

    private boolean validateInput() {
        String username = binding.etUsername.getText() != null
                ? binding.etUsername.getText().toString().trim()
                : "";

        if (TextUtils.isEmpty(username)) {
            showUsernameError(getString(R.string.username_must_not_be_empty));
            return false;
        }

        if (username.length() > 100) {
            showUsernameError(getString(R.string.username_must_not_exceed_100_characters));
            return false;
        }

        hideUsernameError();
        return true;
    }

    private void showUsernameError(String message) {
        binding.tvUsernameError.setText(message);
        binding.tvUsernameError.setVisibility(View.VISIBLE);
        binding.tilUsernameShare.setError(" ");
    }

    private void hideUsernameError() {
        binding.tvUsernameError.setVisibility(View.GONE);
        binding.tilUsernameShare.setError(null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}