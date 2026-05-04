package com.example.mosquizto.Adapters;

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
        String author = (item.getUserName() != null) ? item.getUserName() : "Me";
        holder.tvDetails.setText(item.getCount() + " thẻ • bởi " + author);

        boolean isSelected = selectedIds.contains(item.getId());

        // Đổi UI của nút Action (Từ ic_more sang add/check)
        if (isSelected) {
            holder.ivAction.setImageResource(R.drawable.ic_check);
            holder.ivAction.setColorFilter(android.graphics.Color.parseColor("#4255FF")); // Xanh Quizlet
        } else {
            holder.ivAction.setImageResource(R.drawable.ic_add);
            holder.ivAction.setColorFilter(android.graphics.Color.parseColor("#586380")); // Xám
        }

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
            ivAction = itemView.findViewById(R.id.ivAction); // Yêu cầu bạn đã gắn ID này
        }
    }
}