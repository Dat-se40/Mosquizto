package com.example.mosquizto.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mosquizto.R;
import com.example.mosquizto.Dto.response.SearchResultItem;

import java.util.List;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder> {

    public interface OnResultClickListener {
        void onResultClick(SearchResultItem item);
        void onMoreClick(SearchResultItem item);
    }

    private List<SearchResultItem> results;
    private OnResultClickListener listener;

    public SearchResultAdapter(List<SearchResultItem> results, OnResultClickListener listener) {
        this.results = results;
        this.listener = listener;
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
        SearchResultItem item = results.get(position);

        holder.tvTitle.setText(item.getTitle());
        holder.tvSubtitle.setText(item.getSubtitle());

        // TODO: Load thumbnail bằng Glide/Picasso nếu có URL
        // Glide.with(holder.itemView).load(item.getThumbnailUrl()).into(holder.ivThumbnail);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onResultClick(item);
        });

        holder.ivMore.setOnClickListener(v -> {
            if (listener != null) listener.onMoreClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return results != null ? results.size() : 0;
    }

    public void updateData(List<SearchResultItem> newResults) {
        this.results = newResults;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumbnail;
        TextView tvTitle;
        TextView tvSubtitle;
        ImageView ivMore;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumbnail = itemView.findViewById(R.id.ivThumbnail);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvSubtitle = itemView.findViewById(R.id.tvSubtitle);
            ivMore = itemView.findViewById(R.id.ivMore);
        }
    }
}