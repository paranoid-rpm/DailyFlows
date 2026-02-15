package com.example.dailyflows.data.repo;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.example.dailyflows.data.local.AppDatabase;
import com.example.dailyflows.data.local.entities.TaskEntity;
import com.example.dailyflows.util.NotificationUtil;

public class TaskRepository {

    private final AppDatabase db;
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public TaskRepository(Context context) {
        db = AppDatabase.get(context);
    }

    public LiveData<List<TaskEntity>> observeAll() {
        return db.taskDao().observeAll();
    }

    public LiveData<List<TaskEntity>> observeOpen() {
        return db.taskDao().observeOpen();
    }

    public void upsert(TaskEntity task, Context context, Runnable onComplete) {
        io.execute(() -> {
            if (task.id == null) task.id = UUID.randomUUID().toString();

            long now = System.currentTimeMillis();
            if (task.createdAtMillis == 0) task.createdAtMillis = now;
            task.updatedAtMillis = now;

            db.taskDao().upsert(task);

            if (!task.done && task.dueAtMillis > 0) {
                NotificationUtil.scheduleReminderWork(context, task.id, task.title, task.dueAtMillis);
            } else {
                NotificationUtil.cancelReminderWork(context, task.id);
            }

            if (onComplete != null) {
                mainHandler.post(onComplete);
            }
        });
    }

    public void setDone(String taskId, boolean done, Context context) {
        io.execute(() -> {
            TaskEntity t = db.taskDao().getById(taskId);
            if (t == null) return;
            t.done = done;
            t.updatedAtMillis = System.currentTimeMillis();
            db.taskDao().upsert(t);
            if (done) NotificationUtil.cancelReminderWork(context, taskId);
        });
    }

    public List<TaskEntity> getAllNow() {
        return db.taskDao().getAllNow();
    }
}
