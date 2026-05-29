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

public class RecentAdapter extends RecyclerView.Adapter<RecentAdapter.ViewHolder> {

    private List<CollectionResponse> collections;
    private OnItemCollectionClickedListener listener;
    private OnItemOptionsClickedListener optionsListener;

    public interface OnItemOptionsClickedListener {
        void onOptionsClicked(CollectionResponse item, int position);
    }

    public RecentAdapter(List<CollectionResponse> collections, OnItemCollectionClickedListener listener) {
        this(collections, listener, null);
    }

    public RecentAdapter(List<CollectionResponse> collections, OnItemCollectionClickedListener listener, OnItemOptionsClickedListener optionsListener) {
        this.collections = collections;
        this.listener = listener;
        this.optionsListener = optionsListener;
    }

    public void setCollections(List<CollectionResponse> collections) {
        this.collections = collections;
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        collections.remove(position);
        notifyItemRemoved(position);
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

        String author = item.getUserName() != null ? item.getUserName() : "Quizlet";
        int count = item.getCount() != null ? item.getCount() : 0;
        if (count == 0)
        {
            SharedPreferences sharedPref = holder.itemView.getContext().getSharedPreferences("MosquiztoCache", Context.MODE_PRIVATE);
            String key = "COLLECTION_COUNT_" + item.getId();
            count = sharedPref.getInt(key, 0);
        }
        holder.tvDetails.setText(count + " thẻ • bởi " + author);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.OnItemClicked(item);
            }
        });

        holder.ivAction.setOnClickListener(v -> {
            if (optionsListener != null) {
                optionsListener.onOptionsClicked(item, position);
            }
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
