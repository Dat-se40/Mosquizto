package com.example.mosquizto.Adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mosquizto.Dto.response.CollectionReportResponse;
import com.example.mosquizto.Dto.response.FollowNotificationResponse;
import com.example.mosquizto.Dto.response.ShareCollectionResponse;
import com.example.mosquizto.Dto.response.UserReportResponse;
import com.example.mosquizto.R;
import com.example.mosquizto.Services.SessionManager;
import com.example.mosquizto.Util.AvatarImageHelper;
import com.example.mosquizto.Util.CollectionRole;
import com.example.mosquizto.Util.NotificationWrapper;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "NotificationAdapter";
    private static final int TYPE_INVITE = 1;
    private static final int TYPE_REPORT = 2;
    private static final int TYPE_FOLLOW = FollowNotificationResponse.VIEW_TYPE;
    private static final int TYPE_USER_REPORT = UserReportResponse.VIEW_TYPE;

    private List<NotificationWrapper> notifications;
    private OnNotificationActionListener listener;
    private SessionManager sessionManager;

    public interface OnNotificationActionListener {
        void onAcceptInvite(ShareCollectionResponse invite, int position);
        void onDenyInvite(ShareCollectionResponse invite, int position);
        void onDismissReport(CollectionReportResponse report, int position);
        void onDismissUserReport(UserReportResponse report, int position);
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
        switch (viewType) {
            case TYPE_REPORT:
                return new ReportViewHolder(inflater.inflate(R.layout.item_notification_report, parent, false));
            case TYPE_FOLLOW:
                return new FollowViewHolder(inflater.inflate(R.layout.item_notification_follow, parent, false));
            case TYPE_USER_REPORT:
                return new UserReportViewHolder(inflater.inflate(R.layout.item_notification_user_report, parent, false));
            case TYPE_INVITE:
            default:
                return new InviteViewHolder(inflater.inflate(R.layout.item_notification_invite, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (notifications == null || position >= notifications.size()) return;
        NotificationWrapper item = notifications.get(position);
        Context context = holder.itemView.getContext();
        int colorInt = ContextCompat.getColor(context, R.color.noti_accent_color);

        if (item.getType() == TYPE_FOLLOW) {
            holder.itemView.setClickable(false);
            holder.itemView.setFocusable(false);
            holder.itemView.setOnClickListener(null);
        } else if (item.getType() == TYPE_USER_REPORT) {
            holder.itemView.setClickable(false);
            holder.itemView.setFocusable(false);
            holder.itemView.setOnClickListener(null);
        } else {
            holder.itemView.setClickable(true);
            holder.itemView.setFocusable(true);
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(item);
            });
        }

        if (holder instanceof InviteViewHolder && item instanceof ShareCollectionResponse) {
            bindInvite((InviteViewHolder) holder, (ShareCollectionResponse) item, context, colorInt, position);
        } else if (holder instanceof ReportViewHolder && item instanceof CollectionReportResponse) {
            bindCollectionReport((ReportViewHolder) holder, (CollectionReportResponse) item, context, colorInt, position);
        } else if (holder instanceof FollowViewHolder && item instanceof FollowNotificationResponse) {
            bindFollow((FollowViewHolder) holder, (FollowNotificationResponse) item, context, colorInt);
        } else if (holder instanceof UserReportViewHolder && item instanceof UserReportResponse) {
            bindUserReport((UserReportViewHolder) holder, (UserReportResponse) item, context, colorInt, position);
        }
    }

    private void bindInvite(InviteViewHolder inviteHolder, ShareCollectionResponse invite, Context context, int colorInt, int position) {
        String username = invite.getInviterUsername() != null ? invite.getInviterUsername() : "Someone";
        String title = invite.getTitle() != null ? invite.getTitle() : context.getString(R.string.fallback_study_set_name);
        CollectionRole collectionRole = invite.getCollectionRole();
        String role = (collectionRole != null) ? collectionRole.name() : "VIEWER";

        String text = context.getString(R.string.invite_message, username, title, role);
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
    }

    private void bindCollectionReport(ReportViewHolder reportHolder, CollectionReportResponse report, Context context, int colorInt, int position) {
        String collectionTitle = context.getString(R.string.unknown_collection);
        Integer collectionId = report.getCollectionId();
        if (collectionId == null) {
            Log.w(TAG, "report title: collectionId=null, reportId=" + report.getId());
        } else if (sessionManager != null) {
            String cachedTitle = sessionManager.getCollectionTitle(collectionId);
            if (cachedTitle != null && !cachedTitle.trim().isEmpty()) {
                collectionTitle = cachedTitle;
            }
        }

        String reason = report.getReason() != null ? report.getReason() : "No reason";
        String description = report.getDescription() != null ? report.getDescription() : "";
        String text = context.getString(R.string.report_message, collectionTitle, reason, description);

        SpannableStringBuilder ssb = new SpannableStringBuilder(text);
        applyBoldAndColor(ssb, text, collectionTitle, colorInt);
        applyBold(ssb, text, reason);
        applyBold(ssb, text, description);

        reportHolder.tvReportContent.setText(ssb);
        reportHolder.btnDismiss.setOnClickListener(v -> {
            if (listener != null) listener.onDismissReport(report, position);
        });
    }

    private void bindFollow(FollowViewHolder followHolder, FollowNotificationResponse follow, Context context, int colorInt) {
        String displayName = follow.getDisplayName();
        String text = context.getString(R.string.follow_notification_message, displayName);

        SpannableStringBuilder ssb = new SpannableStringBuilder(text);
        applyBoldAndColor(ssb, text, displayName, colorInt);
        followHolder.tvFollowContent.setText(ssb);

        if (follow.getFollowedAt() != null && !follow.getFollowedAt().isEmpty()) {
            followHolder.tvFollowTime.setVisibility(View.VISIBLE);
            followHolder.tvFollowTime.setText(follow.getFollowedAt());
        } else {
            followHolder.tvFollowTime.setVisibility(View.GONE);
        }

        AvatarImageHelper.loadInto(followHolder.ivFollowerAvatar, resolveFollowerAvatar(follow));
    }

    private String resolveFollowerAvatar(FollowNotificationResponse follow) {
        if (!TextUtils.isEmpty(follow.getFollowerImgUri())) {
            return follow.getFollowerImgUri();
        }
        if (sessionManager != null && follow.getFollowerId() != null) {
            return sessionManager.getUserAvatar(follow.getFollowerId());
        }
        return null;
    }

    private void bindUserReport(UserReportViewHolder reportHolder, UserReportResponse report, Context context, int colorInt, int position) {
        String reason = report.getReason() != null ? report.getReason() : "No reason";
        String description = report.getDescription() != null ? report.getDescription() : "";
        String text = context.getString(R.string.user_report_message, reason, description);

        SpannableStringBuilder ssb = new SpannableStringBuilder(text);
        applyBold(ssb, text, reason);
        applyBold(ssb, text, description);
        reportHolder.tvUserReportContent.setText(ssb);

        reportHolder.btnDismissUserReport.setOnClickListener(v -> {
            if (listener != null) listener.onDismissUserReport(report, position);
        });
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
            ssb.setSpan(new StyleSpan(Typeface.BOLD), start, start + target.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private void applyBoldAndColor(SpannableStringBuilder ssb, String fullText, String target, int color) {
        if (target == null || target.isEmpty()) return;
        int start = fullText.indexOf(target);
        if (start >= 0) {
            ssb.setSpan(new StyleSpan(Typeface.BOLD), start, start + target.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ssb.setSpan(new ForegroundColorSpan(color), start, start + target.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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

    static class FollowViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFollowerAvatar;
        TextView tvFollowContent, tvFollowTime;

        FollowViewHolder(View itemView) {
            super(itemView);
            ivFollowerAvatar = itemView.findViewById(R.id.ivFollowerAvatar);
            tvFollowContent = itemView.findViewById(R.id.tvFollowContent);
            tvFollowTime = itemView.findViewById(R.id.tvFollowTime);
        }
    }

    static class UserReportViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserReportContent, btnDismissUserReport;

        UserReportViewHolder(View itemView) {
            super(itemView);
            tvUserReportContent = itemView.findViewById(R.id.tvUserReportContent);
            btnDismissUserReport = itemView.findViewById(R.id.btnDismissUserReport);
        }
    }
}
