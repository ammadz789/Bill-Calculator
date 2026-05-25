package com.ammad.billcalculator.domain

import java.math.BigDecimal

object SlabCalculator {
    fun calculateCost(consumption: Long, slabs: List<Slab>): BigDecimal {
        SlabConfig.validate(slabs)
        if (consumption <= 0L) return BigDecimal.ZERO

        var remaining = consumption
        var previousBound = 0L
        var total = BigDecimal.ZERO

        for (slab in slabs) {
            if (remaining <= 0L) break
            val unitsInSlab: Long = if (slab.upperBound == null) {
                remaining
            } else {
                val capacity = slab.upperBound - previousBound
                minOf(remaining, capacity)
            }
            total = total.add(slab.ratePerUnit.multiply(BigDecimal.valueOf(unitsInSlab)))
            remaining -= unitsInSlab
            if (slab.upperBound != null) previousBound = slab.upperBound
        }
        return total
    }
}
