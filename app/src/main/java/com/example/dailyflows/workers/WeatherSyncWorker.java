package com.example.dailyflows.workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import com.example.dailyflows.data.repo.WeatherRepository;

public class WeatherSyncWorker extends Worker {

    private final OkHttpClient client = new OkHttpClient();

    public WeatherSyncWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            // Simple online feature: fetch JSON and update a small "online card" text.
            String url = "https://wttr.in/Moscow?format=j1";

            Request req = new Request.Builder().url(url).get().build();
            try (Response resp = client.newCall(req).execute()) {
                if (!resp.isSuccessful() || resp.body() == null) return Result.retry();
                String body = resp.body().string();

                // Keep parsing safe; if JSON changes we still keep app stable.
                new JSONObject(body);
                WeatherRepository.save(getApplicationContext(), "Онлайн: обновлено");
            }

            return Result.success();
        } catch (Throwable t) {
            return Result.retry();
        }
    }
}
