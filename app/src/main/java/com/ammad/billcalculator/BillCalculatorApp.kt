package com.ammad.billcalculator

import android.app.Application
import android.util.Log
import com.ammad.billcalculator.data.BillDatabase
import com.ammad.billcalculator.data.ReadingRepository
import com.ammad.billcalculator.domain.SlabConfig

class BillCalculatorApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(BillDatabase.get(this))
        SlabConfig.SLABS.size.let { Log.i(TAG, "App started with $it slab(s)") }
    }

    class AppContainer(database: BillDatabase) {
        val readingRepository: ReadingRepository = ReadingRepository(database.readingDao())
    }

    companion object {
        private const val TAG = "BillCalculatorApp"
    }
}
