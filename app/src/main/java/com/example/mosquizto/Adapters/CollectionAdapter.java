package com.example.mosquizto.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mosquizto.R;
import com.example.mosquizto.Models.Collection;
import java.util.ArrayList;
import java.util.List;

public class CollectionAdapter extends RecyclerView.Adapter<CollectionAdapter.ViewHolder> {

    private List<Collection> collections = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Collection collection);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setCollections(List<Collection> collections) {
        this.collections = collections;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Sử dụng layout item_recent_collection đã tạo ở trên
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recent_collection, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Collection collection = collections.get(position);

        holder.tvTitle.setText(collection.getTitle());

        String author = (collection.getCreatedBy() != null) ? collection.getCreatedBy().getUsername() : "Unknown";
        int count = collection.getCount();
        holder.tvDetails.setText(holder.itemView.getContext().getString(
                R.string.tvCollectionDetails, count, author));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(collection);
        });
    }

    @Override
    public int getItemCount() {
        return collections != null ? collections.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvDetails;

        ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvCollectionTitle);
            tvDetails = itemView.findViewById(R.id.tvCollectionDetails);
        }
    }
}