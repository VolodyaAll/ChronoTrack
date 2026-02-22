package com.sharai.chronotrack.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activities")
data class Activity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val color: Int,
    val icon: String,
    val isActive: Boolean = false,
    val isArchived: Boolean = false
) 