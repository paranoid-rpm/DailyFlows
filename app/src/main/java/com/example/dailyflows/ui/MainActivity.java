package com.example.dailyflows.ui;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.example.dailyflows.R;
import com.example.dailyflows.ui.agenda.AgendaFragment;
import com.example.dailyflows.ui.settings.SettingsFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView nav = findViewById(R.id.bottomNav);
        nav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_agenda) {
                open(new AgendaFragment());
                return true;
            } else if (item.getItemId() == R.id.nav_settings) {
                open(new SettingsFragment());
                return true;
            }
            return false;
        });

        if (savedInstanceState == null) {
            nav.setSelectedItemId(R.id.nav_agenda);
        }
    }

    private void open(Fragment f) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, f)
                .commit();
    }
}
