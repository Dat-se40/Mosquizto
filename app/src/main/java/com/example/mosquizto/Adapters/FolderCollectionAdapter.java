package com.example.mosquizto.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mosquizto.Dto.response.CollectionResponse;
import com.example.mosquizto.Event.OnItemCollectionClickedListener;
import com.example.mosquizto.R;

import java.util.List;

public class FolderCollectionAdapter extends RecyclerView.Adapter<FolderCollectionAdapter.ViewHolder> {

    private List<CollectionResponse> collections;
    private final OnItemCollectionClickedListener listener;
    private final RecentAdapter.OnItemOptionsClickedListener optionsListener;

    public FolderCollectionAdapter(List<CollectionResponse> collections,
                                   OnItemCollectionClickedListener listener,
                                   RecentAdapter.OnItemOptionsClickedListener optionsListener) {
        this.collections = collections;
        this.listener = listener;
        this.optionsListener = optionsListener;
    }

    public void removeItem(int position) {
        if (collections == null || position < 0 || position >= collections.size()) {
            return;
        }
        collections.remove(position);
        notifyItemRemoved(position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CollectionResponse item = collections.get(position);
        holder.tvTitle.setText(item.getTitle() != null ? item.getTitle() : "");

        String author = item.getUserName();
        int count = item.getCount() != null ? item.getCount() : 0;
        if (item.getId() != null) {
            SharedPreferences sessionPref = holder.itemView.getContext()
                    .getSharedPreferences("MosquiztoSession", Context.MODE_PRIVATE);
            SharedPreferences cachePref = holder.itemView.getContext()
                    .getSharedPreferences("MosquiztoCache", Context.MODE_PRIVATE);
            if (author == null || author.isEmpty()) {
                author = sessionPref.getString("COLLECTION_AUTHOR_" + item.getId(), null);
            }
            if (count == 0) {
                count = sessionPref.getInt("COLLECTION_COUNT_" + item.getId(), 0);
            }
            if (count == 0) {
                count = cachePref.getInt("COLLECTION_COUNT_" + item.getId(), 0);
            }
        }
        if (author == null || author.isEmpty()) {
            author = "Unknown";
        }
        String countLabel = holder.itemView.getContext().getResources()
                .getQuantityString(R.plurals.term_count, count, count);
        holder.tvSubtitle.setText(author + " • " + countLabel);

        holder.ivThumbnail.setImageResource(R.drawable.ic_study_set);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.OnItemClicked(item);
            }
        });

        holder.ivMore.setVisibility(optionsListener != null ? View.VISIBLE : View.GONE);
        holder.ivMore.setOnClickListener(v -> {
            if (optionsListener != null) {
                optionsListener.onOptionsClicked(item, holder.getBindingAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return collections == null ? 0 : collections.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvSubtitle;
        ImageView ivMore;
        ImageView ivThumbnail;

        ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvSubtitle = itemView.findViewById(R.id.tvSubtitle);
            ivMore = itemView.findViewById(R.id.ivMore);
            ivThumbnail = itemView.findViewById(R.id.ivThumbnail);
        }
    }
}
