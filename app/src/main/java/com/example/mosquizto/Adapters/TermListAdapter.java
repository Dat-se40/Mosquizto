package com.example.mosquizto.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mosquizto.Dto.response.CollectionItemResponse;
import com.example.mosquizto.R;

import java.util.ArrayList;
import java.util.List;

public class TermListAdapter extends RecyclerView.Adapter<TermListAdapter.ViewHolder> {

    private List<CollectionItemResponse> items = new ArrayList<>();

    public TermListAdapter(List<CollectionItemResponse> items) {
        if (items != null) {
            this.items = items;
        }
    }

    public void updateData(List<CollectionItemResponse> newItems) {
        this.items = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_term_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CollectionItemResponse item = items.get(position);
        holder.tvTerm.setText(item.getTerm());
        holder.tvDefinition.setText(item.getDefinition());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTerm, tvDefinition;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTerm = itemView.findViewById(R.id.tvTermItem);
            tvDefinition = itemView.findViewById(R.id.tvDefinitionItem);
        }
    }
}
