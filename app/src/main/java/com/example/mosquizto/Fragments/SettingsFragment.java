package com.example.mosquizto.Fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mosquizto.Activities.WelcomeActivity;
import com.example.mosquizto.Models.User;
import com.example.mosquizto.Network.WebSocketManager;
import com.example.mosquizto.R;
import com.example.mosquizto.Services.LocalCacheClearManager;
import com.example.mosquizto.Services.LogoutManager;
import com.example.mosquizto.Services.SessionManager;
import com.example.mosquizto.Util.AboutDialogHelper;
import com.example.mosquizto.Util.LocaleManager;
import com.example.mosquizto.Util.ThemeManager;
import com.google.android.material.switchmaterial.SwitchMaterial;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SettingsFragment extends Fragment {

    @Inject
    SessionManager sessionManager;

    @Inject
    WebSocketManager webSocketManager;

    @Inject
    LogoutManager logoutManager;

    @Inject
    LocalCacheClearManager localCacheClearManager;

    private AlertDialog progressDialog;

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
            if (tvUsername != null) tvUsername.setText(currentUser.getUsername());
            if (tvEmail    != null) tvEmail.setText(currentUser.getEmail());
        }

        // Back button
        View btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> requireActivity().finish());
        }

        // Dark Mode
        SwitchMaterial switchDarkMode = view.findViewById(R.id.switchDarkMode);
        if (switchDarkMode != null) {
            switchDarkMode.setChecked(ThemeManager.isDarkMode(requireContext()));
            switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) ->
                    ThemeManager.setDarkMode(requireContext(), isChecked));
        }

        // Other Switches (Using SwitchMaterial to match XML)
        //SwitchMaterial switchOffline      = view.findViewById(R.id.switchOffline);
        SwitchMaterial switchNotification = view.findViewById(R.id.switchNotification);
        //SwitchMaterial switchSound        = view.findViewById(R.id.switchSound);
        //SwitchMaterial switchHaptic       = view.findViewById(R.id.switchHaptic);

        // Push notifications
        if (switchNotification != null) {
            switchNotification.setChecked(webSocketManager.isPushNotificationEnabled());
            switchNotification.setOnCheckedChangeListener((buttonView, isChecked) ->
                    webSocketManager.setPushNotificationEnabled(isChecked));
        }

        TextView tvLanguageValue = view.findViewById(R.id.tvLanguageValue);
        View itemLanguage = view.findViewById(R.id.itemLanguage);
        if (tvLanguageValue != null) {
            tvLanguageValue.setText(LocaleManager.getLanguageDisplayName(
                    requireContext(), LocaleManager.getLanguageTag(requireContext())));
        }
        if (itemLanguage != null) {
            itemLanguage.setOnClickListener(v -> showLanguageDialog(tvLanguageValue));
        }

        //if (switchSound  != null) switchSound.setChecked(true);
        //if (switchHaptic != null) switchHaptic.setChecked(true);

        View itemStorage = view.findViewById(R.id.itemStorage);
        if (itemStorage != null) {
            itemStorage.setOnClickListener(v -> showClearCacheDialog());
        }

        // Xóa tài khoản
        View btnDeleteAccount = view.findViewById(R.id.btnDeleteAccount);
        if (btnDeleteAccount != null) {
            btnDeleteAccount.setOnClickListener(v -> showDeleteDialog());
        }

        // ── About: dialogs ───────────────────────────────────────────

        // 1. Chính sách quyền riêng tư
        View itemPrivacy = view.findViewById(R.id.itemPrivacy);
        if (itemPrivacy != null) {
            itemPrivacy.setOnClickListener(v ->
                    AboutDialogHelper.showPrivacyPolicy(requireContext()));
        }

        // 2. Điều khoản dịch vụ
        View itemTerms = view.findViewById(R.id.itemTerms);
        if (itemTerms != null) {
            itemTerms.setOnClickListener(v ->
                    AboutDialogHelper.showTermsOfService(requireContext()));
        }

        // 3. Giấy phép mã nguồn mở
        View itemOpenSource = view.findViewById(R.id.itemOpenSource);
        if (itemOpenSource != null) {
            itemOpenSource.setOnClickListener(v ->
                    AboutDialogHelper.showOpenSourceLicenses(requireContext()));
        }

        // 4. Trung tâm hỗ trợ
        View itemSupport = view.findViewById(R.id.itemSupport);
        if (itemSupport != null) {
            itemSupport.setOnClickListener(v ->
                    AboutDialogHelper.showSupportCenter(requireContext()));
        }
    }

    // ── Dialogs tài khoản ─────────────────────────────────────────────

    private void showLanguageDialog(TextView tvLanguageValue) {
        String currentTag = LocaleManager.getLanguageTag(requireContext());
        String[] labels = new String[]{
                getString(R.string.language_name_english),
                getString(R.string.language_name_vietnamese)
        };
        String[] tags = new String[]{LocaleManager.LANG_EN, LocaleManager.LANG_VI};
        int checkedItem = LocaleManager.LANG_VI.equals(currentTag) ? 1 : 0;

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.settings_language)
                .setSingleChoiceItems(labels, checkedItem, (dialog, which) -> {
                    String selectedTag = tags[which];
                    if (!selectedTag.equals(currentTag)) {
                        LocaleManager.setLanguage(requireContext(), selectedTag);
                        if (tvLanguageValue != null) {
                            tvLanguageValue.setText(LocaleManager.getLanguageDisplayName(
                                    requireContext(), selectedTag));
                        }
                        requireActivity().recreate();
                    }
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showClearCacheDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.settings_clear_cache_dialog_title)
                .setMessage(R.string.settings_clear_cache_dialog_message)
                .setPositiveButton(R.string.settings_clear_cache_confirm, (dialog, which) -> performClearLocalCache())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void performClearLocalCache() {
        if (!isAdded()) return;

        View progressView = getLayoutInflater().inflate(R.layout.dialog_clear_cache_progress, null);
        progressDialog = new AlertDialog.Builder(requireContext())
                .setView(progressView)
                .setCancelable(false)
                .create();
        progressDialog.show();

        localCacheClearManager.clearAllLocalData(success -> {
            if (!isAdded()) return;
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            navigateToWelcome();
        });
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc muốn đăng xuất không?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    logoutManager.logout();
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
                    logoutManager.logout();
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

    @Override
    public void onDestroyView() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        progressDialog = null;
        super.onDestroyView();
    }
}