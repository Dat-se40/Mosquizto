package com.example.mosquizto.Adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mosquizto.Dto.response.CollectionReportResponse;
import com.example.mosquizto.Dto.response.ShareCollectionResponse;
import com.example.mosquizto.R;
import com.example.mosquizto.Services.SessionManager;
import com.example.mosquizto.Util.NotificationWrapper;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_INVITE = 1;
    private static final int TYPE_REPORT = 2;

    private List<NotificationWrapper> notifications;
    private OnNotificationActionListener listener;

    private SessionManager sessionManager ;
    public interface OnNotificationActionListener {
        void onAcceptInvite(ShareCollectionResponse invite, int position);
        void onDenyInvite(ShareCollectionResponse invite, int position);
        void onDismissReport(CollectionReportResponse report, int position);
    }

    public NotificationAdapter(List<NotificationWrapper> notifications, OnNotificationActionListener listener) {
        this.notifications = notifications;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return notifications.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_INVITE) {
            View view = inflater.inflate(R.layout.item_notification_invite, parent, false);
            return new InviteViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_notification_report, parent, false);
            return new ReportViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        NotificationWrapper item = notifications.get(position);
        Context context = holder.itemView.getContext();
        
        // Lấy mã màu accent dưới dạng Hex để dùng trong HTML
        int colorInt = ContextCompat.getColor(context, R.color.noti_accent_color);

        if (holder.getItemViewType() == TYPE_INVITE) {
            InviteViewHolder inviteHolder = (InviteViewHolder) holder;
            ShareCollectionResponse invite = (ShareCollectionResponse) item;
            String username = invite.getInviterUsername();
            String title = invite.getTitle();
            String role = invite.getCollectionRole().name();

            String text = context.getString(
                    R.string.invite_message,
                    username,
                    title,
                    role
            );

            SpannableStringBuilder ssb = new SpannableStringBuilder(text);
            // username
            applyBoldAndColor(ssb, text, username, colorInt);
            // title
            applyBoldAndColor(ssb, text, title, colorInt);
            // role
            applyBold(ssb, text, role);

            inviteHolder.tvInviteContent.setText(ssb);
            inviteHolder.btnAccept.setOnClickListener(v -> listener.onAcceptInvite(invite, position));
            inviteHolder.btnDeny.setOnClickListener(v -> listener.onDenyInvite(invite, position));

        } else if (holder.getItemViewType() == TYPE_REPORT) {
            ReportViewHolder reportHolder = (ReportViewHolder) holder;
            CollectionReportResponse report = (CollectionReportResponse) item;
            String collectionTitle = sessionManager.getCollectionTitle(report.getCollectionId());

            String reason = report.getReason();
            String description = report.getDescription();

            String text = context.getString(
                    R.string.report_message,
                    collectionTitle,
                    reason,
                    description
            );

            SpannableStringBuilder ssb = new SpannableStringBuilder(text);
            // Collection name
            applyBoldAndColor(ssb, text, collectionTitle, colorInt);
            // Reason
            applyBold(ssb, text, reason);
            // Description
            applyBold(ssb, text, description);
            reportHolder.tvReportContent.setText(ssb);
            reportHolder.btnDismiss.setOnClickListener(v -> listener.onDismissReport(report, position));
        }
    }

    @Override
    public int getItemCount() {
        return notifications != null ? notifications.size() : 0;
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public void updateData(List<NotificationWrapper> newNotifications) {
        this.notifications = newNotifications;
        notifyDataSetChanged();
    }
    private void applyBold(
            SpannableStringBuilder ssb,
            String fullText,
            String target
    ) {
        int start = fullText.indexOf(target);

        if (start >= 0) {
            ssb.setSpan(
                    new StyleSpan(Typeface.BOLD),
                    start,
                    start + target.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }
    }
    private void applyBoldAndColor(
            SpannableStringBuilder ssb,
            String fullText,
            String target,
            int color
    ) {
        int start = fullText.indexOf(target);

        if (start >= 0) {
            ssb.setSpan(
                    new StyleSpan(Typeface.BOLD),
                    start,
                    start + target.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );

            ssb.setSpan(
                    new ForegroundColorSpan(color),
                    start,
                    start + target.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }
    }
    static class InviteViewHolder extends RecyclerView.ViewHolder {
        TextView tvInviteContent, tvTime;
        Button btnAccept, btnDeny;
        InviteViewHolder(View itemView) {
            super(itemView);
            tvInviteContent = itemView.findViewById(R.id.tvInviteContent);
            tvTime = itemView.findViewById(R.id.tvTime);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnDeny = itemView.findViewById(R.id.btnDeny);
        }
    }

    static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView tvReportContent, btnDismiss;
        ReportViewHolder(View itemView) {
            super(itemView);
            tvReportContent = itemView.findViewById(R.id.tvReportContent);
            btnDismiss = itemView.findViewById(R.id.btnDismiss);
        }
    }
}
