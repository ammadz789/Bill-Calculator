package com.ammad.billcalculator.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ReadingDao {

    @Insert
    suspend fun insert(reading: ReadingEntity): Long

    @Query("SELECT * FROM readings WHERE serviceNumber = :serviceNumber " +
            "ORDER BY timestamp DESC, id DESC LIMIT :limit"
    )
    suspend fun lastN(serviceNumber: String, limit: Int): List<ReadingEntity>
}
