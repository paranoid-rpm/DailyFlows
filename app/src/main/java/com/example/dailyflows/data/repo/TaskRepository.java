package com.example.dailyflows.data.repo;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.example.dailyflows.data.local.AppDatabase;
import com.example.dailyflows.data.local.entities.TaskEntity;
import com.example.dailyflows.util.NotificationUtil;

public class TaskRepository {

    private static final String TAG = "TaskRepository";
    
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
        Log.d(TAG, "[REPO] upsert() called - scheduling background task");
        Log.d(TAG, "[REPO] Task to save: id=" + task.id + ", title=" + task.title + ", note_length=" + (task.note != null ? task.note.length() : 0));
        
        io.execute(() -> {
            try {
                Log.d(TAG, "[REPO] Background thread started");
                
                if (task.id == null) {
                    task.id = UUID.randomUUID().toString();
                    Log.d(TAG, "[REPO] Generated new ID: " + task.id);
                }

                long now = System.currentTimeMillis();
                if (task.createdAtMillis == 0) task.createdAtMillis = now;
                task.updatedAtMillis = now;
                
                Log.d(TAG, "[REPO] Calling db.taskDao().upsert()...");
                db.taskDao().upsert(task);
                Log.d(TAG, "[REPO] ✅ Database upsert COMPLETE!");
                
                // Verify save
                TaskEntity saved = db.taskDao().getById(task.id);
                if (saved != null) {
                    Log.d(TAG, "[REPO] ✅ Verification: Task EXISTS in DB! Title: " + saved.title + ", note_length: " + (saved.note != null ? saved.note.length() : 0));
                } else {
                    Log.e(TAG, "[REPO] ❌ Verification FAILED: Task NOT FOUND in DB!");
                }

                if (!task.done && task.dueAtMillis > 0) {
                    Log.d(TAG, "[REPO] Scheduling notification...");
                    NotificationUtil.scheduleReminderWork(context, task.id, task.title, task.dueAtMillis);
                } else {
                    Log.d(TAG, "[REPO] Cancelling notification...");
                    NotificationUtil.cancelReminderWork(context, task.id);
                }

                if (onComplete != null) {
                    Log.d(TAG, "[REPO] Posting onComplete callback to main thread...");
                    mainHandler.post(onComplete);
                } else {
                    Log.d(TAG, "[REPO] No onComplete callback provided");
                }
                
                Log.d(TAG, "[REPO] === REPOSITORY SAVE COMPLETE ===");
            } catch (Exception e) {
                Log.e(TAG, "[REPO] ❌ ERROR during upsert!", e);
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
