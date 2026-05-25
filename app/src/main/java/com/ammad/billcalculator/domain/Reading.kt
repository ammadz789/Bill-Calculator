package com.ammad.billcalculator.domain

import java.math.BigDecimal

data class Reading(
    val id: Long,
    val serviceNumber: String,
    val reading: Long,
    val cost: BigDecimal,
    val timestamp: Long,
)
