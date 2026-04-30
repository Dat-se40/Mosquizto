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
import java.util.List;
import com.bumptech.glide.Glide;
public class FlashcardSetAdapter extends RecyclerView.Adapter<FlashcardSetAdapter.ViewHolder> {

    private Context context;
    private List<Collection> collectionList;

    private OnItemCollectionClickedListener onCollectionClickListener;
    public FlashcardSetAdapter(Context context, List<Collection> collectionList , OnItemCollectionClickedListener onCollectionClickListener) {
        this.context = context;
        this.collectionList = collectionList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_flashcard_set_library, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Collection collection = collectionList.get(position);

        holder.tvTitle.setText(collection.getTitle() != null ? collection.getTitle() : "Untitled");

        // Cập nhật số lượng term. Đảm bảo trong Model Collection của bạn có trường itemCount
        int itemCount = collection.getCount();
        holder.tvItemCount.setText(itemCount + (itemCount > 1 ? " terms" : " term"));
        holder.itemView.setOnClickListener(v -> onCollectionClickListener.OnItemClicked(Collection.toResponse(collection) ));
        // Cập nhật tên User
        if (collection.getCreatedBy() != null) {
            // 1. Gán tên
            holder.tvUsername.setText(collection.getCreatedBy().getUsername() != null
                    ? collection.getCreatedBy().getUsername()
                    : "Unknown user");

            // 2. Load Avatar từ URL
            String avatarUrl = collection.getCreatedBy().getAvatarUrl(); // Gọi đúng hàm getAvatarUrl() bạn đã tạo ở Model User

            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                Glide.with(context)
                        .load(avatarUrl)
                        .placeholder(R.drawable.ic_default_avatar) // Ảnh hiển thị trong lúc chờ load
                        .error(R.drawable.ic_default_avatar)       // Ảnh hiển thị nếu load URL bị lỗi
                        .into(holder.imgAvatar);
            } else {
                // Nếu user không có avatar URL thì set ảnh mặc định
                holder.imgAvatar.setImageResource(R.drawable.ic_default_avatar);
            }
        } else {
            holder.tvUsername.setText("Unknown user");
            holder.imgAvatar.setImageResource(R.drawable.ic_default_avatar);
        }
    }

    @Override
    public int getItemCount() {
        return collectionList != null ? collectionList.size() : 0;
    }

    public void setCollectionList(List<Collection> newList) {
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