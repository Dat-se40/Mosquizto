package com.example.mosquizto.Adapters;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mosquizto.Dto.request.CollectionItemRequest;
import com.example.mosquizto.R;
import java.util.List;

public class CreateCardAdapter extends RecyclerView.Adapter<CreateCardAdapter.CardViewHolder> {

    private final List<CollectionItemRequest> items;

    public CreateCardAdapter(List<CollectionItemRequest> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_create_card, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        CollectionItemRequest item = items.get(position);

        // Gỡ bỏ TextWatcher cũ để tránh việc cập nhật nhầm vị trí khi cuộn
        holder.clearWatchers();

        holder.etTerm.setText(item.getTerm());
        holder.etDefinition.setText(item.getDefinition());

        // Lắng nghe thay đổi của Thuật ngữ
        holder.termWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                item.setTerm(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        };

        // Lắng nghe thay đổi của Định nghĩa
        holder.definitionWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                item.setDefinition(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        };

        holder.etTerm.addTextChangedListener(holder.termWatcher);
        holder.etDefinition.addTextChangedListener(holder.definitionWatcher);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class CardViewHolder extends RecyclerView.ViewHolder {
        EditText etTerm, etDefinition;
        TextWatcher termWatcher, definitionWatcher;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            etTerm = itemView.findViewById(R.id.et_term);
            etDefinition = itemView.findViewById(R.id.et_definition);
        }

        public void clearWatchers() {
            if (termWatcher != null) etTerm.removeTextChangedListener(termWatcher);
            if (definitionWatcher != null) etDefinition.removeTextChangedListener(definitionWatcher);
        }
    }
}