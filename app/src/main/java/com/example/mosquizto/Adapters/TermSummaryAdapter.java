package com.example.mosquizto.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mosquizto.Dto.response.CollectionItemResponse;
import com.example.mosquizto.Models.TermSummaryUIModel;
import com.example.mosquizto.R;

import java.util.List;

public class TermSummaryAdapter extends RecyclerView.Adapter<TermSummaryAdapter.ViewHolder> {

    private final List<TermSummaryUIModel> items;

    public TermSummaryAdapter(List<TermSummaryUIModel> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_term_summary, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TermSummaryUIModel uiModel = items.get(position);
        CollectionItemResponse item = uiModel.getItemData();

        holder.tvTerm.setText(item.getTerm());
        holder.tvDefinition.setText(item.getDefinition());

        if (uiModel.isCorrect()) {
            holder.ivStatus.setImageResource(R.drawable.ic_check);
            holder.ivStatus.setColorFilter(holder.itemView.getContext().getColor(R.color.quizlet_green));
        } else {
            holder.ivStatus.setImageResource(R.drawable.ic_close);
            holder.ivStatus.setColorFilter(holder.itemView.getContext().getColor(R.color.quizlet_red));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTerm, tvDefinition;
        ImageView ivStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTerm = itemView.findViewById(R.id.tvTermSummary);
            tvDefinition = itemView.findViewById(R.id.tvDefinitionSummary);
            ivStatus = itemView.findViewById(R.id.ivStatusIcon);
        }
    }
}
