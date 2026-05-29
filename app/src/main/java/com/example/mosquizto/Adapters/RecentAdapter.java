package com.example.mosquizto.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mosquizto.Dto.response.CollectionResponse;
import com.example.mosquizto.Event.OnItemCollectionClickedListener;
import com.example.mosquizto.R;

import java.util.List;

public class RecentAdapter extends RecyclerView.Adapter<RecentAdapter.ViewHolder> {

    private List<CollectionResponse> collections;
    private OnItemCollectionClickedListener listener;
    private OnItemOptionsClickedListener optionsListener;
    private OnCollectionActionListener actionListener;

    // Cập nhật Interface: Chia thành các hành động cụ thể
    public interface OnCollectionActionListener {
        void onEdit(CollectionResponse item, int position);
        void onShare(CollectionResponse item, int position);
        void onDelete(CollectionResponse item, int position);
    }
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
            if (actionListener != null) {
                // Khởi tạo PopupMenu
                PopupMenu popupMenu = new PopupMenu(holder.itemView.getContext(), holder.ivAction);
                popupMenu.inflate(R.menu.menu_collection_options); // Trỏ tới file XML menu bạn đã tạo

                // Lắng nghe sự kiện chọn menu
                popupMenu.setOnMenuItemClickListener(menuItem -> {
                    int itemId = menuItem.getItemId();
                    if (itemId == R.id.action_edit) {
                        actionListener.onEdit(item, position);
                        return true;
                    } else if (itemId == R.id.action_share) {
                        actionListener.onShare(item, position);
                        return true;
                    } else if (itemId == R.id.action_delete) {
                        actionListener.onDelete(item, position);
                        return true;
                    }
                    return false;
                });

                // Hiển thị menu
                popupMenu.show();
            }
        });
    }
    public void SetOnCloclickListener(OnCollectionActionListener listener)
    {
        this.actionListener = listener;
    }
    @Override
    public int getItemCount() {
        return collections == null ? 0 : collections.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDetails;
        ImageButton ivAction;

        ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvCollectionTitle);
            tvDetails = itemView.findViewById(R.id.tvCollectionDetails);
            ivAction = itemView.findViewById(R.id.ivAction);
        }
    }
}
