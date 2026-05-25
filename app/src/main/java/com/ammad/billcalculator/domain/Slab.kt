package com.ammad.billcalculator.domain

import java.math.BigDecimal

data class Slab(
    val upperBound: Long?,
    val ratePerUnit: BigDecimal,
)
