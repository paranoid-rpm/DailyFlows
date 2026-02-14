package ru.yourname.dailyflow.data.local.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "tasks",
        indices = {
                @Index("dueAtMillis"),
                @Index("done"),
                @Index("projectId")
        }
)
public class TaskEntity {
    @PrimaryKey
    @NonNull
    public String id;

    @NonNull
    public String title;

    public String note;

    // 0 = нет дедлайна
    public long dueAtMillis;

    public boolean done;

    // 0..3
    public int priority;

    // nullable
    public String projectId;

    public long createdAtMillis;
    public long updatedAtMillis;
}
