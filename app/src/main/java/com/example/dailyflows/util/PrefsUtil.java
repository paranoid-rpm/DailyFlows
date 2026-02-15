package com.example.dailyflows.util;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefsUtil {

    private static final String PREFS = "app_settings";

    // Theme: 0=system, 1=day, 2=night
    private static final String KEY_THEME = "theme";
    // Style: 0=standard, 1=apple_glass, 2=pixel_minimalism
    private static final String KEY_STYLE = "style";
    // Font scale: 1.0f default
    private static final String KEY_FONT_SCALE = "font_scale";

    public static int getTheme(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY_THEME, 0);
    }

    public static void setTheme(Context context, int theme) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putInt(KEY_THEME, theme).apply();
    }

    public static int getStyle(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY_STYLE, 0);
    }

    public static void setStyle(Context context, int style) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putInt(KEY_STYLE, style).apply();
    }

    public static float getFontScale(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getFloat(KEY_FONT_SCALE, 1.0f);
    }

    public static void setFontScale(Context context, float scale) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putFloat(KEY_FONT_SCALE, scale).apply();
    }
}
