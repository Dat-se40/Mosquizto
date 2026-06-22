package com.example.mosquizto.Adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mosquizto.Dto.response.CollectionResponse;
import com.example.mosquizto.R;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SelectCollectionAdapter extends RecyclerView.Adapter<SelectCollectionAdapter.ViewHolder> {
    private List<CollectionResponse> collections;
    private Set<Integer> selectedIds = new HashSet<>();
    private OnSelectionChangedListener listener;

    public interface OnSelectionChangedListener {
        void onSelectionChanged(int selectedCount);
    }

    public SelectCollectionAdapter(List<CollectionResponse> collections, OnSelectionChangedListener listener) {
        this.collections = collections;
        this.listener = listener;
    }

    public Set<Integer> getSelectedIds() {
        return selectedIds;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recent_collection, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CollectionResponse item = collections.get(position);
        holder.tvTitle.setText(item.getTitle());

        String author = item.getUserName() != null ? item.getUserName() : "Me";
        int count = item.getCount() != null ? item.getCount() : 0;
        holder.tvDetails.setText(holder.itemView.getContext().getString(
                R.string.tvCollectionDetails, count, author));

        boolean isSelected = selectedIds.contains(item.getId());
        holder.ivAction.setImageResource(isSelected ? R.drawable.ic_check : R.drawable.ic_add);
        holder.ivAction.setColorFilter(Color.parseColor(isSelected ? "#4255FF" : "#586380"));

        holder.itemView.setOnClickListener(v -> {
            if (isSelected) {
                selectedIds.remove(item.getId());
            } else {
                selectedIds.add(item.getId());
            }
            notifyItemChanged(position);
            if (listener != null) listener.onSelectionChanged(selectedIds.size());
        });
    }

    @Override
    public int getItemCount() {
        return collections == null ? 0 : collections.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDetails;
        ImageView ivAction;

        ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvCollectionTitle);
            tvDetails = itemView.findViewById(R.id.tvCollectionDetails);
            ivAction = itemView.findViewById(R.id.ivAction);
        }
    }
}
