package com.example.mosquizto.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mosquizto.Event.OnItemCollectionClickedListener;
import com.example.mosquizto.R;
import com.example.mosquizto.Dto.response.CollectionResponse;
import java.util.List;

public class BasedOnRecentAdapter extends RecyclerView.Adapter<BasedOnRecentAdapter.ViewHolder> {

    private List<CollectionResponse> collections;

    private OnItemCollectionClickedListener itemCollectionClickedListener ;
    public BasedOnRecentAdapter(List<CollectionResponse> collections) {
        this.collections = collections;
    }
    public void setCollections(List<CollectionResponse> collections) {
        this.collections = collections;
        notifyDataSetChanged();
        this.itemCollectionClickedListener = itemCollectionClickedListener ;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_based_on_recent, parent, false);
        return new ViewHolder(view);
    }
    public void setItemCollectionClickedListener(OnItemCollectionClickedListener onItemCollectionClickedListener)
    {
        this.itemCollectionClickedListener = onItemCollectionClickedListener ;
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CollectionResponse item = collections.get(position);
        if (item != null) {
            if (holder.tvTitle != null) {
                holder.tvTitle.setText(item.getTitle());
            }

            if (holder.tvDetails != null) {
                String author = (item.getUserName() != null) ? item.getUserName() : "Quizlet";
                int count = item.getCount() != null ? item.getCount() : 0;
                holder.tvDetails.setText(holder.itemView.getContext().getString(
                        R.string.tvCollectionDetails, count, author));
            }
            holder.itemView.setOnClickListener(v -> {
                if (itemCollectionClickedListener != null) {
                    itemCollectionClickedListener.OnItemClicked(item);
                }
            });
        }
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
