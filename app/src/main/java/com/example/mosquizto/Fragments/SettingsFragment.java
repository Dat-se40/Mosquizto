package com.example.mosquizto.Fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mosquizto.Activities.WelcomeActivity;
import com.example.mosquizto.Models.User;
import com.example.mosquizto.R;
import com.example.mosquizto.Services.SessionManager;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SettingsFragment extends Fragment {

    @Inject
    SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Đọc thông tin từ SessionManager
        TextView tvUsername = view.findViewById(R.id.tvUsername);
        TextView tvEmail    = view.findViewById(R.id.tvEmail);

        User currentUser = sessionManager.getCurrUser();
        if (currentUser != null) {
            tvUsername.setText(currentUser.getUsername());
            tvEmail.setText(currentUser.getEmail());
        }

        // Back button
        view.findViewById(R.id.btnBack).setOnClickListener(v ->
                requireActivity().finish()
        );

        // Switches
        Switch switchOffline      = view.findViewById(R.id.switchOffline);
        Switch switchNotification = view.findViewById(R.id.switchNotification);
        Switch switchSound        = view.findViewById(R.id.switchSound);
        Switch switchHaptic       = view.findViewById(R.id.switchHaptic);

        // Giá trị mặc định
        switchSound.setChecked(true);
        switchHaptic.setChecked(true);

        // Logout
        view.findViewById(R.id.btnLogout).setOnClickListener(v ->
                showLogoutDialog()
        );

        // Xóa tài khoản
        view.findViewById(R.id.btnDeleteAccount).setOnClickListener(v ->
                showDeleteDialog()
        );
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc muốn đăng xuất không?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    sessionManager.clearSession();
                    navigateToWelcome();
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void showDeleteDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa tài khoản")
                .setMessage("Hành động này không thể hoàn tác. Toàn bộ dữ liệu sẽ bị xóa vĩnh viễn.")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    sessionManager.clearSession();
                    navigateToWelcome();
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void navigateToWelcome() {
        Intent intent = new Intent(requireActivity(), WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}