package com.example.mosquizto.Adapters;

import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mosquizto.Dto.response.SearchCollectionResultItem;
import com.example.mosquizto.R;
import com.example.mosquizto.Dto.response.CollectionResponse;
import com.example.mosquizto.Dto.response.UserResponse;
import com.example.mosquizto.Util.SearchResultWrapper;
import com.squareup.picasso.Picasso;

import java.util.List;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder> {
    public static final int TYPE_COLLECTION = 1;
    public static final int TYPE_USER = 2;

    public interface OnResultClickListener {
        void onResultClick(SearchResultWrapper item);
        void onMoreClick(SearchResultWrapper item);
    }

    private List<SearchResultWrapper> results;
    private OnResultClickListener listener;

    public SearchResultAdapter(List<SearchResultWrapper> results, OnResultClickListener listener) {
        this.results = results;
        this.listener = listener;
    }

    // BƯỚC 1: Lấy đúng type của item
    @Override
    public int getItemViewType(int position) {
        return results.get(position).getType();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Vì dùng chung 1 layout nên không cần if/else ở đây
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_result, parent, false);
        return new ViewHolder(view);
    }

    // BƯỚC 2: Rẽ nhánh để gán dữ liệu phù hợp
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SearchResultWrapper item = results.get(position);
        int viewType = getItemViewType(position);

        if (viewType == TYPE_COLLECTION) {
            SearchCollectionResultItem collection = (SearchCollectionResultItem) item;

            holder.tvTitle.setText(collection.getTitle());

            // Subtitle cho Collection: Tên user • Số lượng thuật ngữ
            String countInfo = (collection.getCount() != null ? collection.getCount() : 0) + " thuật ngữ";
            String subtitle = collection.getCreatedByUsername() + " • " + countInfo;
            holder.tvSubtitle.setText(subtitle);

            holder.ivThumbnail.setImageResource(R.drawable.ic_study_set); // Icon học phần
            holder.ivMore.setVisibility(View.VISIBLE); // Hiện nút 3 chấm

        } else if (viewType == TYPE_USER) {
            UserResponse user = (UserResponse) item;

            // Title cho User: Ưu tiên FullName, nếu null thì dùng Username
            String title = (user.getFullName() != null && !user.getFullName().isEmpty())
                    ? user.getFullName()
                    : user.getUsername();
            holder.tvTitle.setText(title);

            // Subtitle cho User: @username
            holder.tvSubtitle.setText("@" + user.getUsername());

            holder.ivMore.setVisibility(View.GONE); // Giả sử tìm user thì không cần nút 3 chấm
            var thumbnail = holder.ivThumbnail ;
            String imgUri = user.getImgUri() ;
            if (imgUri != null && !imgUri.isEmpty()) {
                holder.flThumbnail.setBackground(null);
                Picasso.get()
                        .load(imgUri)
                        .placeholder(R.drawable.ic_default_avatar)
                        .error(R.drawable.ic_default_avatar)
                        .into(thumbnail);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    thumbnail.setImageTintBlendMode(null);
                }
            } else {
                thumbnail.setImageResource(R.drawable.ic_default_avatar);
            }
        }

        // Xử lý click sự kiện
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

    public void updateData(List<SearchResultWrapper> newResults) {
        this.results = newResults;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumbnail;
        TextView tvTitle;
        TextView tvSubtitle;
        ImageView ivMore;
        FrameLayout flThumbnail ;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumbnail = itemView.findViewById(R.id.ivThumbnail);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvSubtitle = itemView.findViewById(R.id.tvSubtitle);
            ivMore = itemView.findViewById(R.id.ivMore);
            flThumbnail = itemView.findViewById(R.id.flThumbnail) ;
        }
    }
}