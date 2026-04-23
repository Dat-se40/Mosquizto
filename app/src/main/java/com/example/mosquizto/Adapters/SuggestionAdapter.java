package com.example.mosquizto.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mosquizto.R;

import java.util.List;

public class SuggestionAdapter extends RecyclerView.Adapter<SuggestionAdapter.ViewHolder> {

    public interface OnSuggestionClickListener {
        void onSuggestionClick(String text);
        void onFillClick(String text);
    }

    private List<String> suggestions;
    private OnSuggestionClickListener listener;

    public SuggestionAdapter(List<String> suggestions, OnSuggestionClickListener listener) {
        this.suggestions = suggestions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_suggestion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String text = suggestions.get(position);

        holder.tvSuggestionText.setText(text);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onSuggestionClick(text);
        });

        holder.ivFill.setOnClickListener(v -> {
            if (listener != null) listener.onFillClick(text);
        });
    }

    @Override
    public int getItemCount() {
        return suggestions != null ? suggestions.size() : 0;
    }

    public void updateData(List<String> newSuggestions) {
        this.suggestions = newSuggestions;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSuggestionText;
        ImageView ivFill;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSuggestionText = itemView.findViewById(R.id.tvSuggestionText);
            ivFill = itemView.findViewById(R.id.ivFill);
        }
    }
}