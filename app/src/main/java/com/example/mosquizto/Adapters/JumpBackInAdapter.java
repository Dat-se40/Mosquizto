package com.example.mosquizto.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mosquizto.R;
import com.example.mosquizto.Dto.response.StudySessionResponse;
import java.util.List;

public class JumpBackInAdapter extends RecyclerView.Adapter<JumpBackInAdapter.ViewHolder> {

    private List<StudySessionResponse> sessions;

    public JumpBackInAdapter(List<StudySessionResponse> sessions) {
        this.sessions = sessions;
    }

    public void setSessions(List<StudySessionResponse> sessions) {
        this.sessions = sessions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_jump_back_in, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StudySessionResponse item = sessions.get(position);
        
        holder.tvTitle.setText(item.getCollectionName());
        
        int totalCorrect = item.getTotalCorrect() != null ? item.getTotalCorrect() : 0;
        int totalCount = item.getCollectionCount() != null && item.getCollectionCount() > 0 ? item.getCollectionCount() : 1;
        
        int progress = (totalCorrect * 100) / totalCount;
        
        holder.progressBar.setProgress(progress);
        holder.tvProgressText.setText(progress + "% hoàn thành");
    }

    @Override
    public int getItemCount() {
        return sessions == null ? 0 : sessions.size();
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
