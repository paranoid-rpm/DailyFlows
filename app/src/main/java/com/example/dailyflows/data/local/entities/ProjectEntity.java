package com.example.dailyflows.data.local.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "projects")
public class ProjectEntity {
    @PrimaryKey
    @NonNull
    public String id;

    @NonNull
    public String name;

    // hex color string, e.g. "#1A73E8"
    @NonNull
    public String color;
}
