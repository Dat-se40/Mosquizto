package com.example.mosquizto.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mosquizto.R;
import com.example.mosquizto.Models.Collection;
import java.util.List;

public class JumpBackInAdapter extends RecyclerView.Adapter<JumpBackInAdapter.ViewHolder> {

    private List<Collection> collections;

    public JumpBackInAdapter(List<Collection> collections) {
        this.collections = collections;
    }
    public void setCollections(List<Collection> collections) {
        this.collections = collections;
        notifyDataSetChanged(); // Báo cho RecyclerView biết dữ liệu đã thay đổi để vẽ lại UI
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_jump_back_in, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Collection item = collections.get(position);
        holder.tvTitle.setText(item.getTitle());
        holder.progressBar.setProgress(item.getProgress());
        holder.tvProgressText.setText(item.getProgress() + "% of questions completed");
    }

    @Override
    public int getItemCount() {
        return collections == null ? 0 : collections.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvProgressText;
        ProgressBar progressBar;

        ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvJumpTitle);
            tvProgressText = itemView.findViewById(R.id.tvProgressText);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }
}