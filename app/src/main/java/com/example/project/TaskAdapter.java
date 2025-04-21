package com.example.project;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskAdapter extends BaseAdapter {
    private Context context;
    private List<String> tasks;
    private String selectedDate;
    private FirebaseFirestore db;
    private String userId;

    public TaskAdapter(Context context, List<String> tasks, String selectedDate, String userId) {
        this.context = context;
        this.tasks = tasks;
        this.selectedDate = selectedDate;
        this.db = FirebaseFirestore.getInstance();
        this.userId = userId;
    }

    @Override
    public int getCount() {
        return tasks.size();
    }

    @Override
    public Object getItem(int position) {
        return tasks.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.task_item, parent, false);
        }

        TextView taskText = convertView.findViewById(R.id.taskText);
        ImageButton archiveBtn = convertView.findViewById(R.id.btnArchive);

        String task = tasks.get(position);
        taskText.setText(task);

        if (task.startsWith("✔")) {
            archiveBtn.setVisibility(View.VISIBLE);
        } else {
            archiveBtn.setVisibility(View.GONE);
        }

        archiveBtn.setOnClickListener(v -> {
            archiveTask(task, () -> {
                tasks.remove(position);
                notifyDataSetChanged();
                Toast.makeText(context, "Задача архивирована", Toast.LENGTH_SHORT).show();
            });
        });

        return convertView;
    }

    private void archiveTask(String task, Runnable onSuccess) {
        Map<String, Object> data = new HashMap<>();
        data.put("task", task);
        data.put("date", selectedDate);

        db.collection("users")
                .document(userId)
                .collection("archived_tasks")
                .add(data)
                .addOnSuccessListener(archivedRef -> {
                    // Удаление из workouts
                    db.collection("users")
                            .document(userId)
                            .collection("workouts")
                            .document(selectedDate)
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                List<String> workoutTasks = (List<String>) documentSnapshot.get("tasks");
                                if (workoutTasks != null && workoutTasks.contains(task)) {
                                    workoutTasks.remove(task);
                                    Map<String, Object> update = new HashMap<>();
                                    update.put("tasks", workoutTasks);

                                    db.collection("users")
                                            .document(userId)
                                            .collection("workouts")
                                            .document(selectedDate)
                                            .update(update)
                                            .addOnSuccessListener(aVoid -> onSuccess.run())
                                            .addOnFailureListener(e -> Toast.makeText(context, "Ошибка при удалении", Toast.LENGTH_SHORT).show());
                                }
                            });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Ошибка архивации", Toast.LENGTH_SHORT).show());
    }
}
