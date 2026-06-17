package com.example.mosquizto.Util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.example.mosquizto.R;

public class AboutDialogHelper {

    public static void showPrivacyPolicy(Context context) {
        showScrollableDialog(
                context,
                context.getString(R.string.about_privacy_policy_title),
                context.getString(R.string.about_privacy_policy_content)
        );
    }

    public static void showTermsOfService(Context context) {
        showScrollableDialog(
                context,
                context.getString(R.string.about_terms_title),
                context.getString(R.string.about_terms_content)
        );
    }

    public static void showOpenSourceLicenses(Context context) {
        showScrollableDialog(
                context,
                context.getString(R.string.about_oss_title),
                context.getString(R.string.about_oss_content)
        );
    }

    public static void showSupportCenter(Context context) {
        showScrollableDialog(
                context,
                context.getString(R.string.about_support_title),
                context.getString(R.string.about_support_content, context.getString(R.string.about_app_version))
        );
    }

    private static void showScrollableDialog(Context context, String title, String htmlContent) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AboutDialogTheme);

        View dialogView = LayoutInflater.from(context)
                .inflate(R.layout.dialog_about_content, null);

        TextView tvTitle = dialogView.findViewById(R.id.dialogTitle);
        TextView tvContent = dialogView.findViewById(R.id.dialogContent);

        tvTitle.setText(title);
        tvContent.setText(Html.fromHtml(htmlContent, Html.FROM_HTML_MODE_LEGACY));

        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        ImageButton btnClose = dialogView.findViewById(R.id.btnCloseDialog);
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dialog.dismiss());
        }

        TextView btnDismiss = dialogView.findViewById(R.id.btnDismissDialog);
        if (btnDismiss != null) {
            btnDismiss.setOnClickListener(v -> dialog.dismiss());
        }

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        dialog.show();
    }
}
