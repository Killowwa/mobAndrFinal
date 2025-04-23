package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private TextView textEmail, textDate, textPassword;
    private Button btnTogglePassword;
    private boolean isPasswordVisible = false;
    private RecyclerView recyclerView;
    private ArchiveAdapter adapter;
    private ArrayList<String> archiveList = new ArrayList<>();

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String userId;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = findViewById(R.id.toolbarProfile);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawerLayoutProfile);
        navigationView = findViewById(R.id.navigationViewProfile);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
            } else if (id == R.id.nav_exit) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                finish();
            } else if (id == R.id.nav_archive) {
                startActivity(new Intent(this, ArchiveActivity.class));
            }else if (id == R.id.nav_playlist) {
                startActivity(new Intent(this, PlaylistActivity.class));
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        userId = auth.getCurrentUser().getUid();

        textEmail = findViewById(R.id.textEmail);
        textDate = findViewById(R.id.textDate);
        textPassword = findViewById(R.id.textPassword);
        btnTogglePassword = findViewById(R.id.btnTogglePassword);




        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String email = user.getEmail();
            long createdTimestamp = user.getMetadata() != null ? user.getMetadata().getCreationTimestamp() : 0;

            String formattedDate = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
                    .format(new Date(createdTimestamp));

            textEmail.setText("Логин: " + email);
            textDate.setText("Дата регистрации: " + formattedDate);
            textPassword.setText("Пароль: ********");

            btnTogglePassword.setOnClickListener(v -> {
                if (isPasswordVisible) {
                    textPassword.setText("Пароль: ********");
                    btnTogglePassword.setText("Показать пароль");
                } else {
                    textPassword.setText("Пароль: 12345678"); // Тестовый вариант
                    btnTogglePassword.setText("Скрыть пароль");
                }
                isPasswordVisible = !isPasswordVisible;
            });
        }
    }
}
