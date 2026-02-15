package com.example.dailyflows.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import com.example.dailyflows.data.local.entities.TaskEntity;

@Dao
public interface TaskDao {

    @Query("SELECT * FROM tasks ORDER BY done ASC, dueAtMillis ASC, updatedAtMillis DESC")
    LiveData<List<TaskEntity>> observeAll();

    @Query("SELECT * FROM tasks WHERE done = 0 ORDER BY dueAtMillis ASC, updatedAtMillis DESC")
    LiveData<List<TaskEntity>> observeOpen();

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    TaskEntity getById(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(TaskEntity task);

    @Delete
    void delete(TaskEntity task);

    @Query("DELETE FROM tasks")
    void deleteAll();

    @Query("SELECT * FROM tasks")
    List<TaskEntity> getAllNow();
}
