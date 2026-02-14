package ru.yourname.dailyflow.data.repo;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

public class WeatherRepository {

    private static final String PREFS = "weather_cache";
    private static final String KEY_TEXT = "text";
    private static final String KEY_AT = "at";

    public static void save(Context context, String text) {
        SharedPreferences sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        sp.edit()
                .putString(KEY_TEXT, text)
                .putLong(KEY_AT, System.currentTimeMillis())
                .apply();
    }

    @Nullable
    public static String getText(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_TEXT, null);
    }

    public static long getAt(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getLong(KEY_AT, 0);
    }
}
