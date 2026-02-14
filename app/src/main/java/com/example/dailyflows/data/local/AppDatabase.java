package com.example.dailyflows.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.dailyflows.data.local.entities.ProjectEntity;
import com.example.dailyflows.data.local.entities.TaskEntity;

@Database(
        entities = {TaskEntity.class, ProjectEntity.class},
        version = 1,
        exportSchema = true
)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract TaskDao taskDao();

    public abstract ProjectDao projectDao();

    public static AppDatabase get(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "dailyflow.db"
                    ).build();
                }
            }
        }
        return INSTANCE;
    }
}
