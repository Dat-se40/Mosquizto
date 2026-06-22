package com.example.mosquizto.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mosquizto.Dto.response.CollectionResponse;
import com.example.mosquizto.Event.OnItemCollectionClickedListener;
import com.example.mosquizto.R;
import com.example.mosquizto.Util.AvatarImageHelper;

import java.util.List;

public class FlashcardSetAdapter extends RecyclerView.Adapter<FlashcardSetAdapter.ViewHolder> {

    public interface OnCollectionDeleteListener {
        void onDelete(CollectionResponse item, int position);
    }

    private final Context context;
    private List<CollectionResponse> collectionList;
    private final OnItemCollectionClickedListener onCollectionClickListener;
    private OnCollectionDeleteListener deleteListener;
    private String currentUsername;

    public FlashcardSetAdapter(Context context, List<CollectionResponse> collectionList,
                               OnItemCollectionClickedListener onCollectionClickListener) {
        this.context = context;
        this.collectionList = collectionList;
        this.onCollectionClickListener = onCollectionClickListener;
    }

    public void setCurrentUsername(String username) {
        this.currentUsername = username;
        notifyDataSetChanged();
    }

    public void setDeleteListener(OnCollectionDeleteListener deleteListener) {
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_flashcard_set_library, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CollectionResponse collection = collectionList.get(position);

        holder.tvTitle.setText(collection.getTitle() != null ? collection.getTitle() : context.getString(R.string.unknown_collection));

        int itemCount = collection.getCount() != null ? collection.getCount() : 0;
        holder.tvItemCount.setText(holder.itemView.getContext().getResources()
                .getQuantityString(R.plurals.term_count, itemCount, itemCount));

        holder.itemView.setOnClickListener(v -> {
            if (onCollectionClickListener != null) {
                onCollectionClickListener.OnItemClicked(collection);
            }
        });

        holder.tvUsername.setText(collection.getUserName() != null
                ? collection.getUserName()
                : "Unknown user");
        AvatarImageHelper.loadInto(holder.imgAvatar, collection.getAuthorImgUri());

        boolean isOwner = isOwnedByCurrentUser(collection);
        if (holder.ivAction != null) {
            if (deleteListener != null) {
                holder.ivAction.setVisibility(View.VISIBLE);
                holder.ivAction.setOnClickListener(v -> {
                    PopupMenu popupMenu = new PopupMenu(holder.itemView.getContext(), holder.ivAction);
                    popupMenu.getMenu().add(R.string.delete);
                    popupMenu.setOnMenuItemClickListener(item -> {
                        if (deleteListener != null) {
                            deleteListener.onDelete(collection, holder.getBindingAdapterPosition());
                        }
                        return true;
                    });
                    popupMenu.show();
                });
            } else {
                holder.ivAction.setVisibility(View.GONE);
                holder.ivAction.setOnClickListener(null);
            }
        }
    }

    private boolean isOwnedByCurrentUser(CollectionResponse collection) {
        if (currentUsername == null || collection.getUserName() == null) {
            return false;
        }
        return currentUsername.equalsIgnoreCase(collection.getUserName());
    }

    @Override
    public int getItemCount() {
        return collectionList != null ? collectionList.size() : 0;
    }

    public void setCollectionList(List<CollectionResponse> newList) {
        this.collectionList = newList;
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (collectionList == null || position < 0 || position >= collectionList.size()) return;
        collectionList.remove(position);
        notifyItemRemoved(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemCount, tvTitle, tvUsername;
        ImageView imgAvatar;
        ImageButton ivAction;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemCount = itemView.findViewById(R.id.tv_item_count);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvUsername = itemView.findViewById(R.id.tv_username);
            imgAvatar = itemView.findViewById(R.id.img_avatar);
            ivAction = itemView.findViewById(R.id.ivAction);
        }
    }
}
