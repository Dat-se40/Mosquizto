package com.example.mosquizto.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mosquizto.Dto.response.CollectionResponse;
import com.example.mosquizto.Event.OnItemCollectionClickedListener;
import com.example.mosquizto.Models.Collection;
import com.example.mosquizto.R;
import com.example.mosquizto.Util.AvatarImageHelper;
import java.util.List;
public class FlashcardSetAdapter extends RecyclerView.Adapter<FlashcardSetAdapter.ViewHolder> {

    private Context context;
    private List<CollectionResponse> collectionList;

    private OnItemCollectionClickedListener onCollectionClickListener;
    public FlashcardSetAdapter(Context context, List<CollectionResponse> collectionList , OnItemCollectionClickedListener onCollectionClickListener) {
        this.context = context;
        this.collectionList = collectionList;
        this.onCollectionClickListener = onCollectionClickListener;
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

        holder.tvTitle.setText(collection.getTitle() != null ? collection.getTitle() : "Untitled");

        // Cập nhật số lượng term. Đảm bảo trong Model Collection của bạn có trường itemCount
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
    }

    @Override
    public int getItemCount() {
        return collectionList != null ? collectionList.size() : 0;
    }

    public void setCollectionList(List<CollectionResponse> newList) {
        this.collectionList = newList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemCount, tvTitle, tvUsername;
        ImageView imgAvatar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemCount = itemView.findViewById(R.id.tv_item_count);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvUsername = itemView.findViewById(R.id.tv_username);
            imgAvatar = itemView.findViewById(R.id.img_avatar);
        }
    }
}