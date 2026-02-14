package ru.yourname.dailyflow.workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import ru.yourname.dailyflow.data.repo.WeatherRepository;

public class WeatherSyncWorker extends Worker {

    private final OkHttpClient client = new OkHttpClient();

    public WeatherSyncWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            // Пример интернет-фичи: тянем любой JSON по HTTPS и показываем в шапке.
            // Здесь можно заменить URL на любой стабильный endpoint.
            String url = "https://wttr.in/Moscow?format=j1";

            Request req = new Request.Builder().url(url).get().build();
            try (Response resp = client.newCall(req).execute()) {
                if (!resp.isSuccessful() || resp.body() == null) return Result.retry();

                String body = resp.body().string();
                JSONObject json = new JSONObject(body);

                // Очень защитно: если структура изменилась — не падаем
                String text = "Обновлено по сети";
                WeatherRepository.save(getApplicationContext(), text);
            }

            return Result.success();
        } catch (Throwable t) {
            return Result.retry();
        }
    }
}
