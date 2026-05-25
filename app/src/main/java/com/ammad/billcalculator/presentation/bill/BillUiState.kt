package com.ammad.billcalculator.presentation.bill

import com.ammad.billcalculator.domain.Reading
import java.math.BigDecimal

data class BillUiState(
    val serviceNumber: String = "",
    val serviceNumberError: String? = null,
    val currentReading: String = "",
    val currentReadingError: String? = null,
    val isSubmitting: Boolean = false,
    val result: CalculationResult? = null,
    val snackbarMessage: String? = null,
) {
    val canSubmit: Boolean
        get() = !isSubmitting &&
                serviceNumber.isNotBlank() &&
                currentReading.isNotBlank() &&
                serviceNumberError == null &&
                currentReadingError == null &&
                result == null
}

data class CalculationResult(
    val serviceNumber: String,
    val currentReading: Long,
    val previousReading: Long?,
    val consumption: Long,
    val cost: BigDecimal,
    val history: List<Reading>,
)
