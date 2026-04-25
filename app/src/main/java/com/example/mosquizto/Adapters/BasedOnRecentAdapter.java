package com.example.mosquizto.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mosquizto.Models.Collection;
import com.example.mosquizto.R;

import java.util.List;

public class BasedOnRecentAdapter extends RecyclerView.Adapter<BasedOnRecentAdapter.ViewHolder> {

    private List<Collection> collections;

    public BasedOnRecentAdapter(List<Collection> collections) {
        this.collections = collections;
    }
    public void setCollections(List<Collection> collections) {
        this.collections = collections;
        notifyDataSetChanged(); // Báo cho RecyclerView biết dữ liệu đã thay đổi để vẽ lại UI
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Gọi file item_based_on_recent.xml đã tạo ở Phần 2
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_based_on_recent, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Collection item = collections.get(position);
        holder.tvTitle.setText(item.getTitle());

        String author = (item.getCreatedBy() != null) ? item.getCreatedBy().getUsername() : "Unknown";
        holder.tvDetails.setText(item.getCount() + " cards • by " + author);
    }

    @Override
    public int getItemCount() {
        return collections == null ? 0 : collections.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDetails;

        ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDetails = itemView.findViewById(R.id.tvDetails);
        }
    }
}