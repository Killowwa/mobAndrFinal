package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class PlaylistActivity extends AppCompatActivity {

    private ArrayList<String> musicNames = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private String selectedTrackName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        EditText inputMusicName = findViewById(R.id.inputMusicUrl);
        Button btnAddTrack = findViewById(R.id.btnAddTrack);
        Button btnStartMusic = findViewById(R.id.btnStartMusic);
        Button btnStopMusic = findViewById(R.id.btnStopMusic);
        ListView listViewTracks = findViewById(R.id.listViewTracks);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, musicNames);
        listViewTracks.setAdapter(adapter);

        // ➕ Добавление трека, если найден в папке raw
        btnAddTrack.setOnClickListener(v -> {
            String name = inputMusicName.getText().toString().trim();

            // Получаем ресурс по имени
            int resId = getResources().getIdentifier(name, "raw", getPackageName());

            if (resId != 0) {
                musicNames.add(name);
                adapter.notifyDataSetChanged();
                inputMusicName.setText("");
            } else {
                Toast.makeText(this, "Файл не найден в res/raw", Toast.LENGTH_SHORT).show();
            }
        });

        // 📌 Сохраняем выбранный трек
        listViewTracks.setOnItemClickListener((parent, view, position, id) -> {
            selectedTrackName = musicNames.get(position);
            Toast.makeText(this, "Выбрано: " + selectedTrackName, Toast.LENGTH_SHORT).show();
        });

        // ▶️ Воспроизведение по кнопке
        btnStartMusic.setOnClickListener(v -> {
            if (selectedTrackName != null) {
                int resId = getResources().getIdentifier(selectedTrackName, "raw", getPackageName());
                if (resId != 0) {
                    Intent intent = new Intent(this, MusicService.class);
                    intent.putExtra(MusicService.EXTRA_RES_ID, resId);
                    ContextCompat.startForegroundService(this, intent);
                    Toast.makeText(this, "Воспроизведение: " + selectedTrackName, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Файл не найден в res/raw", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Выберите трек из списка", Toast.LENGTH_SHORT).show();
            }
        });

        // ⏹ Остановка музыки
        btnStopMusic.setOnClickListener(v -> {
            Intent stopIntent = new Intent(this, MusicService.class);
            stopService(stopIntent);
            Toast.makeText(this, "Музыка остановлена", Toast.LENGTH_SHORT).show();
        });
    }
}
