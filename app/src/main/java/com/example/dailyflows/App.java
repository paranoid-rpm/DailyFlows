package ru.yourname.dailyflow;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

import ru.yourname.dailyflow.workers.WeatherSyncWorker;

public class App extends Application {

    public static final String UNIQUE_WORK_WEATHER = "weather_sync";

    @Override
    public void onCreate() {
        super.onCreate();

        // Следовать системной теме (Day/Night)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        scheduleWeatherSync();
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
