package ru.yourname.dailyflow.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

import ru.yourname.dailyflow.R;
import ru.yourname.dailyflow.workers.ReminderWorker;

public class NotificationUtil {

    private static final String CHANNEL_ID = "reminders";

    public static void ensureChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel ch = new NotificationChannel(
                CHANNEL_ID,
                "Напоминания",
                NotificationManager.IMPORTANCE_HIGH
        );
        nm.createNotificationChannel(ch);
    }

    public static void showReminder(Context context, String taskId, String title) {
        ensureChannel(context);

        NotificationCompat.Builder b = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Напоминание")
                .setContentText(title)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(taskId.hashCode(), b.build());
    }

    public static void scheduleReminderWork(Context context, String taskId, String title, long triggerAtMillis) {
        long now = System.currentTimeMillis();
        long delay = Math.max(0, triggerAtMillis - now);

        Data input = new Data.Builder()
                .putString(ReminderWorker.KEY_TASK_ID, taskId)
                .putString(ReminderWorker.KEY_TITLE, title)
                .build();

        OneTimeWorkRequest req = new OneTimeWorkRequest.Builder(ReminderWorker.class)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(input)
                .addTag("reminder_" + taskId)
                .build();

        WorkManager.getInstance(context).enqueueUniqueWork(
                "reminder_" + taskId,
                ExistingWorkPolicy.REPLACE,
                req
        );
    }

    public static void cancelReminderWork(Context context, String taskId) {
        WorkManager.getInstance(context).cancelUniqueWork("reminder_" + taskId);
    }
}
