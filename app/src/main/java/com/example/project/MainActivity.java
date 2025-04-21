package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import com.google.android.material.navigation.NavigationView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private ListView listViewTasks;
    private TaskAdapter taskAdapter;
    private Map<String, ArrayList<String>> tasksByDate = new HashMap<>();
    private String selectedDate = "";
    private int selectedPosition = -1;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        userId = auth.getCurrentUser().getUid();

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
            } else if (id == R.id.nav_exit) {
                new AlertDialog.Builder(this)
                        .setTitle("Выход")
                        .setMessage("Вы уверены, что хотите выйти?")
                        .setPositiveButton("Да", (dialog, which) -> {
                            FirebaseAuth.getInstance().signOut();
                            startActivity(new Intent(MainActivity.this, LoginActivity.class));
                            finish();
                        })
                        .setNegativeButton("Отмена", null)
                        .show();
            }else if (id == R.id.nav_archive) {
                startActivity(new Intent(this, ArchiveActivity.class));
            }

            drawerLayout.closeDrawers();
            return true;
        });

        CalendarView calendarView = findViewById(R.id.calendarView);
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
            Toast.makeText(MainActivity.this, "Выбрана дата: " + selectedDate, Toast.LENGTH_SHORT).show();
            updateTaskList();
        });

        Button btnAddWorkout = findViewById(R.id.btnAddWorkout);
        btnAddWorkout.setOnClickListener(v -> showAddTaskDialog());

        listViewTasks = findViewById(R.id.listViewTasks);
        taskAdapter = new TaskAdapter(this, new ArrayList<>(), selectedDate, userId);
        listViewTasks.setAdapter(taskAdapter);


        registerForContextMenu(listViewTasks);
        listViewTasks.setOnItemLongClickListener((parent, view, position, id) -> {
            selectedPosition = position;
            return false;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    private void updateTaskList() {
        if (selectedDate.isEmpty()) return;

        db.collection("users")
                .document(userId)
                .collection("workouts")
                .document(selectedDate)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> tasks = (List<String>) documentSnapshot.get("tasks");
                        if (tasks != null) {
                            tasksByDate.put(selectedDate, new ArrayList<>(tasks));
                            taskAdapter = new TaskAdapter(this, new ArrayList<>(tasks), selectedDate, userId);
                            listViewTasks.setAdapter(taskAdapter);
                        }
                    } else {
                        taskAdapter = new TaskAdapter(this, new ArrayList<>(), selectedDate, userId);
                        listViewTasks.setAdapter(taskAdapter);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show());
    }


    private void showAddTaskDialog() {
        if (selectedDate.isEmpty()) {
            Toast.makeText(this, "Сначала выберите дату!", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Добавить упражнение");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText inputExercise = new EditText(this);
        inputExercise.setHint("Введите упражнение");
        layout.addView(inputExercise);

        final EditText inputSets = new EditText(this);
        inputSets.setHint("Введите количество подходов");
        inputSets.setInputType(InputType.TYPE_CLASS_NUMBER);
        layout.addView(inputSets);

        builder.setView(layout);

        builder.setPositiveButton("Добавить", (dialog, which) -> {
            String exercise = inputExercise.getText().toString().trim();
            String sets = inputSets.getText().toString().trim();

            if (!exercise.isEmpty() && !sets.isEmpty()) {
                String fullTask = exercise + " - " + sets + " подходов";
                addTaskToList(fullTask);
            } else {
                Toast.makeText(this, "Введите все данные!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void addTaskToList(String task) {
        ArrayList<String> tasks = tasksByDate.getOrDefault(selectedDate, new ArrayList<>());
        tasks.add(task);
        tasksByDate.put(selectedDate, tasks);

        Map<String, Object> data = new HashMap<>();
        data.put("tasks", tasks);

        db.collection("users")
                .document(userId)
                .collection("workouts")
                .document(selectedDate)
                .set(data)
                .addOnSuccessListener(aVoid -> updateTaskList())
                .addOnFailureListener(e -> Toast.makeText(this, "Ошибка при сохранении", Toast.LENGTH_SHORT).show());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.menu_edit) {
            showEditTaskDialog();
            return true;
        } else if (itemId == R.id.menu_delete) {
            if (selectedPosition != -1) {
                ArrayList<String> tasks = tasksByDate.get(selectedDate);
                if (tasks != null) {
                    tasks.remove(selectedPosition);
                    tasksByDate.put(selectedDate, tasks);
                    updateTaskList();

                    db.collection("users")
                            .document(userId)
                            .collection("workouts")
                            .document(selectedDate)
                            .update("tasks", tasks)
                            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Тренировка удалена!", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(this, "Ошибка при удалении", Toast.LENGTH_SHORT).show());
                }
            }
            return true;
        } else if (itemId == R.id.menu_done) {
            if (selectedPosition != -1) {
                ArrayList<String> tasks = tasksByDate.get(selectedDate);
                if (tasks != null) {
                    String currentTask = tasks.get(selectedPosition);
                    if (!currentTask.startsWith("✔")) {
                        currentTask = "✔ " + currentTask;
                        tasks.set(selectedPosition, currentTask);
                    }
                    tasksByDate.put(selectedDate, tasks);
                    updateTaskList();

                    db.collection("users")
                            .document(userId)
                            .collection("workouts")
                            .document(selectedDate)
                            .update("tasks", tasks)
                            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Задача завершена!", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(this, "Ошибка при завершении", Toast.LENGTH_SHORT).show());
                }
            }
            return true;
        } else if (itemId == R.id.menu_settings) {
            Toast.makeText(this, "Открываем настройки...", Toast.LENGTH_SHORT).show();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.task_context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        selectedPosition = info.position;
        int itemId = item.getItemId();

        if (itemId == R.id.menu_edit) {
            showEditTaskDialog();
            return true;
        } else if (itemId == R.id.menu_delete) {
            ArrayList<String> tasks = tasksByDate.get(selectedDate);
            if (tasks != null) {
                tasks.remove(selectedPosition);
                tasksByDate.put(selectedDate, tasks);
                updateTaskList();

                db.collection("users")
                        .document(userId)
                        .collection("workouts")
                        .document(selectedDate)
                        .update("tasks", tasks)
                        .addOnSuccessListener(aVoid -> Toast.makeText(this, "Удалено!", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(this, "Ошибка при удалении", Toast.LENGTH_SHORT).show());
            }
            return true;
        } else if (itemId == R.id.menu_done) {
            ArrayList<String> tasks = tasksByDate.get(selectedDate);
            if (tasks != null) {
                String currentTask = tasks.get(selectedPosition);
                if (!currentTask.startsWith("✔")) {
                    currentTask = "✔ " + currentTask;
                    tasks.set(selectedPosition, currentTask);
                }
                tasksByDate.put(selectedDate, tasks);
                updateTaskList();

                db.collection("users")
                        .document(userId)
                        .collection("workouts")
                        .document(selectedDate)
                        .update("tasks", tasks)
                        .addOnSuccessListener(aVoid -> Toast.makeText(this, "Завершено!", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(this, "Ошибка при обновлении", Toast.LENGTH_SHORT).show());
            }
            return true;
        } else {
            return super.onContextItemSelected(item);
        }
    }

    private void showEditTaskDialog() {
        if (selectedPosition == -1) {
            Toast.makeText(this, "Выберите тренировку для редактирования", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayList<String> tasks = tasksByDate.get(selectedDate);
        if (tasks == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Редактировать тренировку");

        final EditText input = new EditText(this);
        input.setText(tasks.get(selectedPosition));
        builder.setView(input);

        builder.setPositiveButton("Сохранить", (dialog, which) -> {
            String updatedTask = input.getText().toString().trim();
            tasks.set(selectedPosition, updatedTask);
            tasksByDate.put(selectedDate, tasks);
            updateTaskList();

            db.collection("users")
                    .document(userId)
                    .collection("workouts")
                    .document(selectedDate)
                    .update("tasks", tasks)
                    .addOnSuccessListener(aVoid -> Toast.makeText(MainActivity.this, "Тренировка обновлена!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Ошибка при обновлении", Toast.LENGTH_SHORT).show());
        });

        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}