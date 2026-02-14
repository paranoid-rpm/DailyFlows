package com.example.dailyflows.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import com.example.dailyflows.data.local.entities.ProjectEntity;

@Dao
public interface ProjectDao {

    @Query("SELECT * FROM projects ORDER BY name ASC")
    LiveData<List<ProjectEntity>> observeAll();

    @Query("SELECT * FROM projects")
    List<ProjectEntity> getAllNow();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(ProjectEntity project);

    @Query("DELETE FROM projects")
    void deleteAll();
}
