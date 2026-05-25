package com.ammad.billcalculator.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "readings",
    indices = [Index(value = ["serviceNumber"])],
)
data class ReadingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val serviceNumber: String,
    val reading: Long,
    val cost: String,
    val timestamp: Long,
)
