package com.example.project;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ArchiveAdapter extends RecyclerView.Adapter<ArchiveAdapter.ViewHolder> {

    private final List<String> archivedTasks;

    public ArchiveAdapter(List<String> archivedTasks) {
        this.archivedTasks = archivedTasks;
    }

    @NonNull
    @Override
    public ArchiveAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_archive_task, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArchiveAdapter.ViewHolder holder, int position) {
        String task = archivedTasks.get(position);
        holder.taskText.setText(task);
    }

    @Override
    public int getItemCount() {
        return archivedTasks.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView taskText;

        public ViewHolder(View itemView) {
            super(itemView);
            taskText = itemView.findViewById(R.id.textTask);
        }
    }
}
