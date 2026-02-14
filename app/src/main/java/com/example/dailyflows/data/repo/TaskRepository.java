package ru.yourname.dailyflow.data.repo;

import android.content.Context;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ru.yourname.dailyflow.data.local.AppDatabase;
import ru.yourname.dailyflow.data.local.entities.TaskEntity;
import ru.yourname.dailyflow.util.NotificationUtil;

public class TaskRepository {

    private final AppDatabase db;
    private final ExecutorService io = Executors.newSingleThreadExecutor();

    public TaskRepository(Context context) {
        db = AppDatabase.get(context);
    }

    public LiveData<List<TaskEntity>> observeAll() {
        return db.taskDao().observeAll();
    }

    public LiveData<List<TaskEntity>> observeOpen() {
        return db.taskDao().observeOpen();
    }

    public void setDone(String taskId, boolean done) {
        io.execute(() -> {
            TaskEntity t = db.taskDao().getById(taskId);
            if (t == null) return;
            t.done = done;
            t.updatedAtMillis = System.currentTimeMillis();
            db.taskDao().upsert(t);

            if (done) {
                NotificationUtil.cancelReminderWork(AppDatabase.get(null), null, taskId); // no-op safety
            }
        });
    }

    public void upsert(TaskEntity task, Context context) {
        io.execute(() -> {
            if (task.id == null) task.id = UUID.randomUUID().toString();
            long now = System.currentTimeMillis();
            if (task.createdAtMillis == 0) task.createdAtMillis = now;
            task.updatedAtMillis = now;

            db.taskDao().upsert(task);

            // планируем напоминание, если есть дедлайн и задача не выполнена
            if (!task.done && task.dueAtMillis > 0) {
                NotificationUtil.scheduleReminderWork(context, task.id, task.title, task.dueAtMillis);
            } else {
                NotificationUtil.cancelReminderWork(context, task.id);
            }
        });
    }

    public void deleteAll(Runnable onDone) {
        io.execute(() -> {
            db.taskDao().deleteAll();
            if (onDone != null) onDone.run();
        });
    }

    public List<TaskEntity> getAllNow() {
        return db.taskDao().getAllNow();
    }
}
