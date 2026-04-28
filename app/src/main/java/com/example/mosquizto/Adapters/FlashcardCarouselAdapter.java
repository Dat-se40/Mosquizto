package com.example.mosquizto.Adapters;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mosquizto.Dto.response.CollectionItemResponse;
import com.example.mosquizto.R;
import java.util.List;

public class FlashcardCarouselAdapter extends RecyclerView.Adapter<FlashcardCarouselAdapter.ViewHolder> {

    private List<CollectionItemResponse> items;
    private Context context;

    public FlashcardCarouselAdapter(Context context, List<CollectionItemResponse> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_flashcard_carousel, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CollectionItemResponse item = items.get(position);
        holder.tvTerm.setText(item.getTerm());
        holder.tvDefinition.setText(item.getDefinition());

        // Thay đổi camera distance để tránh bị cắt ảnh khi xoay 3D
        float scale = context.getResources().getDisplayMetrics().density;
        holder.cardFront.setCameraDistance(8000 * scale);
        holder.cardBack.setCameraDistance(8000 * scale);

        // Click để lật thẻ
        holder.itemView.setOnClickListener(v -> {
            if (holder.isFront) {
                flipCard(holder.cardFront, holder.cardBack);
            } else {
                flipCard(holder.cardBack, holder.cardFront);
            }
            holder.isFront = !holder.isFront;
        });
    }

    private void flipCard(View visibleView, View invisibleView) {
        // Animation lật mượt mà (Có thể định nghĩa trong res/animator)
        visibleView.animate().rotationY(90f).setDuration(150).withEndAction(() -> {
            visibleView.setVisibility(View.INVISIBLE);
            invisibleView.setVisibility(View.VISIBLE);
            invisibleView.setRotationY(-90f);
            invisibleView.animate().rotationY(0f).setDuration(150).start();
        }).start();
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        View cardFront, cardBack;
        TextView tvTerm, tvDefinition;
        boolean isFront = true;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardFront = itemView.findViewById(R.id.cardFront);
            cardBack = itemView.findViewById(R.id.cardBack);
            tvTerm = itemView.findViewById(R.id.tvTerm);
            tvDefinition = itemView.findViewById(R.id.tvDefinition);
        }
    }
}