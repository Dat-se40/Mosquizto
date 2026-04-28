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

public class RecentAdapter extends RecyclerView.Adapter<RecentAdapter.ViewHolder> {

    private List<CollectionResponse> collections;
    private OnItemCollectionClickedListener listener;

    public RecentAdapter(List<CollectionResponse> collections, OnItemCollectionClickedListener listener) {
        this.collections = collections;
        this.listener = listener;
    }

    public void setCollections(List<CollectionResponse> collections) {
        this.collections = collections;
        notifyDataSetChanged();
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

        String author = (item.getUserName() != null) ? item.getUserName() : "Quizlet";
        holder.tvDetails.setText(item.getCount() + " thẻ • bởi " + author);


        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.OnItemClicked(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return collections == null ? 0 : collections.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDetails;

        ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvCollectionTitle);
            tvDetails = itemView.findViewById(R.id.tvCollectionDetails);
        }
    }
}
