package com.example.mosquizto.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mosquizto.Dto.response.FolderSummaryResponse;
import com.example.mosquizto.R;

import java.util.List;

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.ViewHolder> {
    private List<FolderSummaryResponse> folders;
    private final OnFolderClickedListener listener;

    // Callback để Fragment tự xử lý khi user bấm vào folder.
    public interface OnFolderClickedListener {
        void onFolderClicked(FolderSummaryResponse folder);
    }

    public FolderAdapter(List<FolderSummaryResponse> folders, OnFolderClickedListener listener) {
        this.folders = folders;
        this.listener = listener;
    }

    public void setFolders(List<FolderSummaryResponse> folders) {
        // Nhận list mới từ API rồi refresh RecyclerView.
        this.folders = folders;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_folder, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FolderSummaryResponse folder = folders.get(position);
        // Bind dữ liệu folder vào từng dòng UI.
        holder.tvFolderName.setText(folder.getName());

        if (folder.getName() != null && !folder.getName().isEmpty()) {
            holder.tvCreatorName.setText("by " + folder.getName());
        } else {
            holder.tvCreatorName.setText("by anonymous");
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFolderClicked(folder);
            }
        });

        if (holder.btnFolderMore != null) {
            holder.btnFolderMore.setOnClickListener(v -> {
                // Thêm logic hiển thị PopupMenu xử lý Xóa/Sửa ở đây nếu cần
            });
        }
    }

    @Override
    public int getItemCount() {
        return folders == null ? 0 : folders.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFolderName;
        TextView tvCreatorName;
        ImageView btnFolderMore;

        ViewHolder(View itemView) {
            super(itemView);
            tvFolderName = itemView.findViewById(R.id.tvFolderName);
            tvCreatorName = itemView.findViewById(R.id.tvCreatorName);
            btnFolderMore = itemView.findViewById(R.id.btnFolderMore);
        }
    }
}
