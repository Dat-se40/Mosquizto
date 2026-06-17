package com.example.mosquizto.Adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
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
import com.example.mosquizto.Util.CollectionRole;
import com.example.mosquizto.Util.NotificationWrapper;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "NotificationAdapter";
    private static final int TYPE_INVITE = 1;
    private static final int TYPE_REPORT = 2;

    private List<NotificationWrapper> notifications;
    private OnNotificationActionListener listener;

    private SessionManager sessionManager;

    public interface OnNotificationActionListener {
        void onAcceptInvite(ShareCollectionResponse invite, int position);
        void onDenyInvite(ShareCollectionResponse invite, int position);
        void onDismissReport(CollectionReportResponse report, int position);
        void onItemClick(NotificationWrapper item);
    }

    public NotificationAdapter(List<NotificationWrapper> notifications, SessionManager sessionManager, OnNotificationActionListener listener) {
        this.notifications = notifications;
        this.sessionManager = sessionManager;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        if (notifications == null || position >= notifications.size()) return TYPE_INVITE;
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
        if (notifications == null || position >= notifications.size()) return;
        NotificationWrapper item = notifications.get(position);
        Context context = holder.itemView.getContext();

        int colorInt = ContextCompat.getColor(context, R.color.noti_accent_color);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });

        // TODO: dùng vistor pattern hoặc statery pattern nếu sau này mở rộng nhiều loại thông báo hơn nữa
        if (holder.getItemViewType() == TYPE_INVITE && item instanceof ShareCollectionResponse) {
            InviteViewHolder inviteHolder = (InviteViewHolder) holder;
            ShareCollectionResponse invite = (ShareCollectionResponse) item;
            
            String username = invite.getInviterUsername() != null ? invite.getInviterUsername() : "Someone";
            String title = invite.getTitle() != null ? invite.getTitle() : "a collection";
            
            CollectionRole collectionRole = invite.getCollectionRole();
            String role = (collectionRole != null) ? collectionRole.name() : "VIEWER";

            String text = context.getString(
                    R.string.invite_message,
                    username,
                    title,
                    role
            );

            SpannableStringBuilder ssb = new SpannableStringBuilder(text);
            applyBoldAndColor(ssb, text, username, colorInt);
            applyBoldAndColor(ssb, text, title, colorInt);
            applyBold(ssb, text, role);

            inviteHolder.tvInviteContent.setText(ssb);
            inviteHolder.btnAccept.setOnClickListener(v -> {
                if (listener != null) listener.onAcceptInvite(invite, position);
            });
            inviteHolder.btnDeny.setOnClickListener(v -> {
                if (listener != null) listener.onDenyInvite(invite, position);
            });

        } else if (holder.getItemViewType() == TYPE_REPORT && item instanceof CollectionReportResponse) {
            ReportViewHolder reportHolder = (ReportViewHolder) holder;
            CollectionReportResponse report = (CollectionReportResponse) item;

            String collectionTitle = "Unknown";
            Integer collectionId = report.getCollectionId();
            if (collectionId == null) {
                Log.w(TAG, "report title: collectionId=null, reportId=" + report.getId()
                        + " -> using fallback");
            } else if (sessionManager == null) {
                Log.w(TAG, "report title: sessionManager=null, collectionId=" + collectionId
                        + " -> using fallback");
            } else {
                String cachedTitle = sessionManager.getCollectionTitle(collectionId);
                if (cachedTitle == null || cachedTitle.trim().isEmpty()) {
                    Log.w(TAG, "report title: cache miss, collectionId=" + collectionId
                            + ", reportId=" + report.getId() + " -> using fallback");
                } else {
                    collectionTitle = cachedTitle;
                    Log.d(TAG, "report title: cache hit, collectionId=" + collectionId
                            + " -> \"" + cachedTitle + "\"");
                }
            }

            String reason = report.getReason() != null ? report.getReason() : "No reason";
            String description = report.getDescription() != null ? report.getDescription() : "";

            String text = context.getString(
                    R.string.report_message,
                    collectionTitle,
                    reason,
                    description
            );

            SpannableStringBuilder ssb = new SpannableStringBuilder(text);
            applyBoldAndColor(ssb, text, collectionTitle, colorInt);
            applyBold(ssb, text, reason);
            applyBold(ssb, text, description);
            
            reportHolder.tvReportContent.setText(ssb);
            reportHolder.btnDismiss.setOnClickListener(v -> {
                if (listener != null) listener.onDismissReport(report, position);
            });
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

    private void applyBold(SpannableStringBuilder ssb, String fullText, String target) {
        if (target == null || target.isEmpty()) return;
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

    private void applyBoldAndColor(SpannableStringBuilder ssb, String fullText, String target, int color) {
        if (target == null || target.isEmpty()) return;
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
