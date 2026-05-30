package com.example.mosquizto.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mosquizto.Dto.response.CollectionItemResponse;
import com.example.mosquizto.Event.ButtonSenderEvent;
import com.example.mosquizto.Event.OnDetailItemClickedListener;
import com.example.mosquizto.Models.TermItemUIModel;
import com.example.mosquizto.R;

import java.util.ArrayList;
import java.util.List;

public class TermListAdapter extends RecyclerView.Adapter<TermListAdapter.ViewHolder> {

    private List<TermItemUIModel> items = new ArrayList<>();
    private OnDetailItemClickedListener onDetailItemClickedListener;

    public TermListAdapter(List<TermItemUIModel> items) {
        if (items != null) this.items = items;
    }

    public void updateData(List<TermItemUIModel> newItems) {
        this.items = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    // Hàm này để Activity lấy list data gốc (để truyền sang GameActivity)
    public List<CollectionItemResponse> getOriginalItems() {
        List<CollectionItemResponse> original = new ArrayList<>();
        for (TermItemUIModel model : items) {
            original.add(model.getItemData());
        }
        return original;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_term_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TermItemUIModel uiModel = items.get(position);
        CollectionItemResponse item = uiModel.getItemData();

        holder.tvTerm.setText(item.getTerm());
        holder.tvDefinition.setText(item.getDefinition());

        // Cập nhật icon sao dựa trên UI State (thay tên icon của bạn vào nhé)
        if (uiModel.isStarred()) {
            holder.btnStar.setImageResource(R.drawable.ic_star);
        } else {
            holder.btnStar.setImageResource(R.drawable.ic_star_outline);
        }

        if(onDetailItemClickedListener != null) {
            holder.btnStar.setOnClickListener(v -> onDetailItemClickedListener.OnItemClicked(
                    new ButtonSenderEvent(holder.btnStar, item) // Vẫn trả về data gốc để gọi API cho dễ
            ));
        }
    }

    public void SetOnDetailItemClickedListener(OnDetailItemClickedListener listener)
    {
        this.onDetailItemClickedListener = listener ;
    }
    @Override
    public int getItemCount() {
        return items.size();
    }

    // --- CÁC HÀM TIỆN ÍCH CHO UI STATE ---

    public void updateStarState(Integer id, boolean isStarred) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getItemData().getId().equals(id)) {
                items.get(i).setStarred(isStarred);
                notifyItemChanged(i); // Báo RecyclerView vẽ lại đúng dòng này
                break;
            }
        }
    }

    public boolean isItemStarred(Integer id) {
        for (TermItemUIModel model : items) {
            if (model.getItemData().getId().equals(id)) return model.isStarred();
        }
        return false;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTerm, tvDefinition;
        ImageButton btnStar;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTerm = itemView.findViewById(R.id.tvTermItem);
            tvDefinition = itemView.findViewById(R.id.tvDefinitionItem);
            btnStar = itemView.findViewById(R.id.btnStar) ;
        }
    }
}
