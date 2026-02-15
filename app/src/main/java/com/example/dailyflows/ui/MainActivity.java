package com.example.dailyflows.ui;

import android.os.Bundle;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.example.dailyflows.R;
import com.example.dailyflows.ui.agenda.AgendaFragment;
import com.example.dailyflows.ui.settings.SettingsFragment;
import com.example.dailyflows.util.PrefsUtil;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        applySelectedTheme();
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

    private void applySelectedTheme() {
        int style = PrefsUtil.getStyle(this);
        switch (style) {
            case 1:
                setTheme(R.style.Theme_DailyFlow_AppleGlass);
                break;
            case 2:
                setTheme(R.style.Theme_DailyFlow_PixelMin);
                break;
            case 3:
                setTheme(R.style.Theme_DailyFlow_Cyberpunk);
                break;
            case 4:
                setTheme(R.style.Theme_DailyFlow_Sunset);
                break;
            case 5:
                setTheme(R.style.Theme_DailyFlow_Ocean);
                break;
            case 6:
                setTheme(R.style.Theme_DailyFlow_Forest);
                break;
            case 7:
                setTheme(R.style.Theme_DailyFlow_MonoDark);
                break;
            case 8:
                setTheme(R.style.Theme_DailyFlow_Candy);
                break;
            default:
                setTheme(R.style.Theme_DailyFlow);
                break;
        }
    }

    private void open(Fragment f) {
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left,
                        R.anim.slide_in_left,
                        R.anim.slide_out_right
                )
                .replace(R.id.container, f)
                .commit();
    }
}
