package com.ammad.billcalculator.domain

import android.util.Log
import java.math.BigDecimal

/**
 * To reconfigure: edit [SLABS] below and rebuild. Constraints:
 *   - Slabs are ordered ascending by [Slab.upperBound].
 *   - At most one slab has a `null` `upperBound` — and if present, it must be the last entry.
 *   - All [Slab.ratePerUnit] values must be non-negative.
 */
object SlabConfig {

    private const val TAG = "SlabConfig"

    val SLABS: List<Slab> = listOf(
        Slab(upperBound = 100L, ratePerUnit = BigDecimal("5")),
        Slab(upperBound = 500L, ratePerUnit = BigDecimal("8")),
        Slab(upperBound = null, ratePerUnit = BigDecimal("10")),
    )

    init {
        validate(SLABS)
        Log.i(TAG, "Loaded ${SLABS.size} slab(s): ${SLABS.joinToString { "<=${it.upperBound ?: "∞"}@${it.ratePerUnit}" }}")
    }

    fun validate(slabs: List<Slab>) {
        check(slabs.isNotEmpty()) { "Slab table must not be empty" }
        var previousBound: Long? = null
        slabs.forEachIndexed { index, slab ->
            check(slab.ratePerUnit.signum() >= 0) {
                "Slab[$index] rate must be non-negative (was ${slab.ratePerUnit})"
            }
            val bound = slab.upperBound
            if (bound != null) {
                check(bound > 0) { "Slab[$index] upperBound must be positive (was $bound)" }
                if (previousBound != null) {
                    check(bound > previousBound!!) {
                        "Slab[$index] upperBound ($bound) must exceed previous ($previousBound)"
                    }
                }
                previousBound = bound
            } else {
                check(index == slabs.lastIndex) {
                    "Unbounded slab (upperBound=null) must be the last entry, found at index $index"
                }
            }
        }
    }
}
