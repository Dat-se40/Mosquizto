package com.example.mosquizto.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mosquizto.Dto.response.CollectionItemResponse;
import com.example.mosquizto.R;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class EditCardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_FOOTER = 2;

    private final List<CollectionItemResponse> items;
    private final List<Integer> deletedItemIds = new ArrayList<>();
    
    private String title = "";
    private String description = "";
    private final OnAddCardClickListener addCardClickListener;

    public interface OnAddCardClickListener {
        void onAddCardClick();
    }

    public EditCardAdapter(List<CollectionItemResponse> items, OnAddCardClickListener listener) {
        this.items = items;
        this.addCardClickListener = listener;
        setHasStableIds(true); 
    }

    public void setHeaderData(String title, String description) {
        this.title = title != null ? title : "";
        this.description = description != null ? description : "";
        notifyItemChanged(0);
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) return TYPE_HEADER;
        if (position == items.size() + 1) return TYPE_FOOTER;
        return TYPE_ITEM;
    }

    @Override
    public long getItemId(int position) {
        if (position == 0) return -1;
        if (position == items.size() + 1) return -2;
        CollectionItemResponse item = items.get(position - 1);
        return item.getId() != null ? item.getId() : item.hashCode();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            return new HeaderViewHolder(inflater.inflate(R.layout.header_edit_collection, parent, false));
        } else if (viewType == TYPE_FOOTER) {
            return new FooterViewHolder(inflater.inflate(R.layout.footer_edit_collection, parent, false));
        } else {
            return new EditCardViewHolder(inflater.inflate(R.layout.item_edit_card, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            HeaderViewHolder h = (HeaderViewHolder) holder;
            h.etTitle.setText(title);
            h.etDescription.setText(description);

            h.etTitle.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) title = h.etTitle.getText().toString();
            });
            h.etDescription.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) description = h.etDescription.getText().toString();
            });

        } else if (holder instanceof EditCardViewHolder) {
            int itemPos = position - 1;
            CollectionItemResponse item = items.get(itemPos);
            EditCardViewHolder h = (EditCardViewHolder) holder;
            
            h.tvIndex.setText(String.valueOf(position));
            h.etTerm.setText(item.getTerm());
            h.etDefinition.setText(item.getDefinition());

            h.etTerm.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) item.setTerm(h.etTerm.getText().toString());
            });
            
            h.etDefinition.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) item.setDefinition(h.etDefinition.getText().toString());
            });

            h.btnDelete.setOnClickListener(v -> {
                int currentPos = holder.getBindingAdapterPosition();
                if (currentPos != RecyclerView.NO_POSITION) {
                    int actualItemPos = currentPos - 1;
                    CollectionItemResponse removedItem = items.get(actualItemPos);
                    if (removedItem.getId() != null) deletedItemIds.add(removedItem.getId());
                    items.remove(actualItemPos);
                    notifyItemRemoved(currentPos);
                    notifyItemRangeChanged(currentPos, getItemCount() - currentPos);
                }
            });
        } else if (holder instanceof FooterViewHolder) {
            ((FooterViewHolder) holder).btnAddCard.setOnClickListener(v -> {
                if (addCardClickListener != null) addCardClickListener.onAddCardClick();
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size() + 2;
    }

    public List<Integer> getDeletedItemIds() { return deletedItemIds; }
    public List<CollectionItemResponse> getItems() { return items; }

    static class EditCardViewHolder extends RecyclerView.ViewHolder {
        TextView tvIndex;
        ImageButton btnDelete;
        TextInputEditText etTerm, etDefinition;
        public EditCardViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIndex = itemView.findViewById(R.id.tv_index);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            etTerm = itemView.findViewById(R.id.et_term);
            etDefinition = itemView.findViewById(R.id.et_definition);
        }
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        EditText etTitle, etDescription;
        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            etTitle = itemView.findViewById(R.id.et_title);
            etDescription = itemView.findViewById(R.id.et_description);
        }
    }

    static class FooterViewHolder extends RecyclerView.ViewHolder {
        Button btnAddCard;
        public FooterViewHolder(@NonNull View itemView) {
            super(itemView);
            btnAddCard = itemView.findViewById(R.id.btn_add_card);
        }
    }
}