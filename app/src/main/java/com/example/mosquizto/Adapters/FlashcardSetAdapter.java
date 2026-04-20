package com.example.mosquizto.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.mosquizto.Models.Collection;
import com.example.mosquizto.R;
import de.hdodenhof.circleimageview.CircleImageView;
import java.util.List;

public class FlashcardSetAdapter extends RecyclerView.Adapter<FlashcardSetAdapter.ViewHolder> {
    private List<Collection> list;

    public FlashcardSetAdapter(List<Collection> list) { this.list = list; }

    public void setData(List<Collection> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_flashcard_set_library, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Collection item = list.get(position);
        holder.tvTitle.setText(item.getTitle());
        holder.tvCount.setText(item.getCount() + " terms");

        if (item.getCreatedBy() != null) {
            holder.tvAuthor.setText(item.getCreatedBy().getUsername());
            // Load avatar người tạo (nếu có URL)
            Glide.with(holder.itemView.getContext())
                    .load(item.getCreatedBy().getAvatarUrl()) // Đảm bảo Model User có avatarUrl
                    .placeholder(R.drawable.ic_default_avatar)
                    .into(holder.imgAvatar);
        }
    }

    @Override
    public int getItemCount() { return list == null ? 0 : list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCount, tvAuthor;
        CircleImageView imgAvatar;
        public ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvSetTitle);
            tvCount = itemView.findViewById(R.id.tvTermCount);
            tvAuthor = itemView.findViewById(R.id.tvCreatorName);
            imgAvatar = itemView.findViewById(R.id.imgCreatorAvatar);
        }
    }
}