package com.example.dailyflows.workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.dailyflows.util.NotificationUtil;

public class ReminderWorker extends Worker {

    public static final String KEY_TASK_ID = "task_id";
    public static final String KEY_TITLE = "title";

    public ReminderWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        String taskId = getInputData().getString(KEY_TASK_ID);
        String title = getInputData().getString(KEY_TITLE);
        if (taskId == null) return Result.success();

        NotificationUtil.showReminder(getApplicationContext(), taskId, title != null ? title : "Задача");
        return Result.success();
    }
}
