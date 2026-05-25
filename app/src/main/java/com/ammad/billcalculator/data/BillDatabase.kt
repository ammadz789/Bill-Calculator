package com.ammad.billcalculator.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ReadingEntity::class], version = 1, exportSchema = false)
abstract class BillDatabase : RoomDatabase() {

    abstract fun readingDao(): ReadingDao

    companion object {
        @Volatile
        private var instance: BillDatabase? = null

        fun get(context: Context): BillDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    BillDatabase::class.java,
                    "bill-calculator.db",
                ).build().also { instance = it }
            }
    }
}
