package com.ammad.billcalculator.data

import com.ammad.billcalculator.domain.Reading
import java.math.BigDecimal

class ReadingRepository(private val dao: ReadingDao) {

    suspend fun lastN(serviceNumber: String, limit: Int): List<Reading> =
        dao.lastN(serviceNumber, limit).map { it.toDomain() }

    suspend fun insert(
        serviceNumber: String,
        reading: Long,
        cost: BigDecimal,
        timestamp: Long,
    ): Long = dao.insert(
        ReadingEntity(
            serviceNumber = serviceNumber,
            reading = reading,
            cost = cost.toPlainString(),
            timestamp = timestamp,
        )
    )

    private fun ReadingEntity.toDomain(): Reading = Reading(
        id = id,
        serviceNumber = serviceNumber,
        reading = reading,
        cost = BigDecimal(cost),
        timestamp = timestamp,
    )
}
