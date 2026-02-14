package ru.yourname.dailyflow.data.repo;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ru.yourname.dailyflow.data.local.AppDatabase;
import ru.yourname.dailyflow.data.local.entities.ProjectEntity;
import ru.yourname.dailyflow.data.local.entities.TaskEntity;

public class BackupRepository {

    public static class BackupData {
        public List<TaskEntity> tasks;
        public List<ProjectEntity> projects;
        public long exportedAtMillis;
        public int version = 1;
    }

    private final AppDatabase db;
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public BackupRepository(Context context) {
        db = AppDatabase.get(context);
    }

    public void exportTo(@NonNull Context context, @NonNull Uri uri, @NonNull Runnable onSuccess, @NonNull Runnable onError) {
        io.execute(() -> {
            try {
                BackupData data = new BackupData();
                data.tasks = db.taskDao().getAllNow();
                data.projects = db.projectDao().getAllNow();
                data.exportedAtMillis = System.currentTimeMillis();

                String json = gson.toJson(data);

                ContentResolver cr = context.getContentResolver();
                try (OutputStream os = cr.openOutputStream(uri, "w")) {
                    if (os == null) throw new IllegalStateException("OutputStream is null");
                    os.write(json.getBytes());
                    os.flush();
                }

                onSuccess.run();
            } catch (Throwable t) {
                onError.run();
            }
        });
    }

    public void importFrom(@NonNull Context context, @NonNull Uri uri, @NonNull Runnable onSuccess, @NonNull Runnable onError) {
        io.execute(() -> {
            try {
                ContentResolver cr = context.getContentResolver();
                StringBuilder sb = new StringBuilder();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(cr.openInputStream(uri)))) {
                    String line;
                    while ((line = br.readLine()) != null) sb.append(line).append('\n');
                }

                BackupData data = gson.fromJson(sb.toString(), BackupData.class);
                if (data == null) throw new IllegalStateException("BackupData null");

                db.runInTransaction(() -> {
                    db.taskDao().deleteAll();
                    db.projectDao().deleteAll();

                    if (data.projects != null) {
                        for (ProjectEntity p : data.projects) db.projectDao().upsert(p);
                    }
                    if (data.tasks != null) {
                        for (TaskEntity t : data.tasks) db.taskDao().upsert(t);
                    }
                });

                onSuccess.run();
            } catch (Throwable t) {
                onError.run();
            }
        });
    }
}
