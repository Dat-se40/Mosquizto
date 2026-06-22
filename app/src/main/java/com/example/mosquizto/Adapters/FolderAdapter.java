package com.example.mosquizto.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mosquizto.Dto.response.FolderSummaryResponse;
import com.example.mosquizto.R;

import java.util.List;

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.ViewHolder> {
    private List<FolderSummaryResponse> folders;
    private final OnFolderClickedListener listener;
    private OnFolderOptionsListener optionsListener;

    public interface OnFolderClickedListener {
        void onFolderClicked(FolderSummaryResponse folder);
    }

    public interface OnFolderOptionsListener {
        void onDeleteFolder(FolderSummaryResponse folder, int position);
    }

    public FolderAdapter(List<FolderSummaryResponse> folders, OnFolderClickedListener listener) {
        this.folders = folders;
        this.listener = listener;
    }

    public void setOnFolderOptionsListener(OnFolderOptionsListener optionsListener) {
        this.optionsListener = optionsListener;
    }

    public void setFolders(List<FolderSummaryResponse> folders) {
        this.folders = folders;
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (folders == null || position < 0 || position >= folders.size()) {
            return;
        }
        folders.remove(position);
        notifyItemRemoved(position);
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
        holder.tvFolderName.setText(folder.getName() != null ? folder.getName() : "");

        if (holder.tvCreatorName != null) {
            holder.tvCreatorName.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFolderClicked(folder);
            }
        });

        if (holder.btnFolderMore != null) {
            holder.btnFolderMore.setOnClickListener(v -> {
                if (optionsListener == null) {
                    return;
                }
                PopupMenu popupMenu = new PopupMenu(holder.itemView.getContext(), holder.btnFolderMore);
                popupMenu.inflate(R.menu.menu_folder_options);
                popupMenu.setOnMenuItemClickListener(menuItem -> {
                    if (menuItem.getItemId() == R.id.action_delete_folder) {
                        optionsListener.onDeleteFolder(folder, holder.getBindingAdapterPosition());
                        return true;
                    }
                    return false;
                });
                popupMenu.show();
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
