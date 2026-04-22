package com.example.mosquizto.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mosquizto.R;
import com.example.mosquizto.model.RecentSearchItem;

import java.util.List;

public class RecentSearchAdapter extends RecyclerView.Adapter<RecentSearchAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(String text);
        void onArrowClick(String text);
    }

    private List<RecentSearchItem> items;
    private OnItemClickListener listener;

    public RecentSearchAdapter(List<RecentSearchItem> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recent_search, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecentSearchItem item = items.get(position);

        holder.tvRecentText.setText(item.getText());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item.getText());
        });

        holder.ivArrow.setOnClickListener(v -> {
            if (listener != null) listener.onArrowClick(item.getText());
        });
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public void updateData(List<RecentSearchItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRecentText;
        ImageView ivArrow;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRecentText = itemView.findViewById(R.id.tvRecentText);
            ivArrow = itemView.findViewById(R.id.ivArrow);
        }
    }
}