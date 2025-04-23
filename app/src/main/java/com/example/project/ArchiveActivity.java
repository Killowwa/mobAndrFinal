package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ArchiveActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ArchiveAdapter adapter;
    private List<String> archivedTasks;
    private FirebaseFirestore db;
    private String userId;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archive);

        Toolbar toolbar = findViewById(R.id.toolbarArchive);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawerLayoutArchive);
        navigationView = findViewById(R.id.navigationViewArchive);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
            } else if (id == R.id.nav_exit) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(ArchiveActivity.this, LoginActivity.class));
                finish();
            } else if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
            }else if (id == R.id.nav_playlist) {
                startActivity(new Intent(this, PlaylistActivity.class));
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        recyclerView = findViewById(R.id.recyclerViewArchive);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        Button btnClearArchive = findViewById(R.id.btnClearArchive);
        btnClearArchive.setOnClickListener(v -> clearArchive());

        archivedTasks = new ArrayList<>();
        adapter = new ArchiveAdapter(archivedTasks);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        loadArchivedTasks();
    }

    private void loadArchivedTasks() {
        db.collection("users")
                .document(userId)
                .collection("archived_tasks")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    archivedTasks.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String task = document.getString("task");
                        String date = document.getString("date");
                        if (task != null && date != null) {
                            archivedTasks.add(date + " — " + task);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Ошибка загрузки архива", Toast.LENGTH_SHORT).show());
    }

    private void clearArchive() {
        db.collection("users")
                .document(userId)
                .collection("archived_tasks")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        document.getReference().delete();
                    }
                    archivedTasks.clear();
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, "Архив очищен", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Ошибка при очистке архива", Toast.LENGTH_SHORT).show());
    }
}
