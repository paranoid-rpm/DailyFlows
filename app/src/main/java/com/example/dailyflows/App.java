package com.example.dailyflows;

import android.app.Application;
import android.content.res.Configuration;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

import com.example.dailyflows.workers.WeatherSyncWorker;
import com.example.dailyflows.util.PrefsUtil;

public class App extends Application {

    public static final String UNIQUE_WORK_WEATHER = "weather_sync";

    @Override
    public void onCreate() {
        super.onCreate();

        applyTheme();
        applyFontScale();
        scheduleWeatherSync();
    }

    private void applyTheme() {
        int theme = PrefsUtil.getTheme(this);
        switch (theme) {
            case 1:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case 2:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }

    private void applyFontScale() {
        float scale = PrefsUtil.getFontScale(this);
        Configuration config = getResources().getConfiguration();
        config.fontScale = scale;
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    private void scheduleWeatherSync() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                WeatherSyncWorker.class,
                6, TimeUnit.HOURS
        ).setConstraints(constraints).build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                UNIQUE_WORK_WEATHER,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
        );
    }
}
