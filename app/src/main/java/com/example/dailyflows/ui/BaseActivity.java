package com.example.dailyflows.ui;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dailyflows.util.PrefsUtil;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(applyFontScale(newBase));
    }

    private Context applyFontScale(Context context) {
        float scale = PrefsUtil.getFontScale(context);
        Configuration config = new Configuration(context.getResources().getConfiguration());
        config.fontScale = scale;
        return context.createConfigurationContext(config);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        applySelectedTheme();
        super.onCreate(savedInstanceState);
    }

    private void applySelectedTheme() {
        int style = PrefsUtil.getStyle(this);
        switch (style) {
            case 1:
                setTheme(com.example.dailyflows.R.style.Theme_DailyFlow_AppleGlass);
                break;
            case 2:
                setTheme(com.example.dailyflows.R.style.Theme_DailyFlow_PixelMin);
                break;
            case 3:
                setTheme(com.example.dailyflows.R.style.Theme_DailyFlow_Cyberpunk);
                break;
            case 4:
                setTheme(com.example.dailyflows.R.style.Theme_DailyFlow_Sunset);
                break;
            case 5:
                setTheme(com.example.dailyflows.R.style.Theme_DailyFlow_Ocean);
                break;
            case 6:
                setTheme(com.example.dailyflows.R.style.Theme_DailyFlow_Forest);
                break;
            case 7:
                setTheme(com.example.dailyflows.R.style.Theme_DailyFlow_MonoDark);
                break;
            case 8:
                setTheme(com.example.dailyflows.R.style.Theme_DailyFlow_Candy);
                break;
            default:
                setTheme(com.example.dailyflows.R.style.Theme_DailyFlow);
                break;
        }
    }
}
