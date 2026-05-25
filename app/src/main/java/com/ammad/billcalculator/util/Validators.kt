package com.ammad.billcalculator.util

object Validators {

    const val SERVICE_NUMBER_LENGTH = 10
    private val SERVICE_NUMBER_REGEX = Regex("^[A-Za-z0-9]{$SERVICE_NUMBER_LENGTH}$")

    const val ERROR_SERVICE_EMPTY = "Service number is required"
    const val ERROR_SERVICE_FORMAT = "Service number must be exactly 10 alphanumeric characters"
    const val ERROR_READING_EMPTY = "Meter reading is required"
    const val ERROR_READING_NOT_NUMERIC = "Meter reading must be a whole number"
    const val ERROR_READING_NEGATIVE = "Meter reading cannot be negative"

    fun validateServiceNumber(input: String): String? = when {
        input.isEmpty() -> ERROR_SERVICE_EMPTY
        !SERVICE_NUMBER_REGEX.matches(input) -> ERROR_SERVICE_FORMAT
        else -> null
    }

    fun validateReading(input: String): ReadingResult = when {
        input.isEmpty() -> ReadingResult.Invalid(ERROR_READING_EMPTY)
        else -> {
            val parsed = input.toLongOrNull()
            when {
                parsed == null -> ReadingResult.Invalid(ERROR_READING_NOT_NUMERIC)
                parsed < 0L -> ReadingResult.Invalid(ERROR_READING_NEGATIVE)
                else -> ReadingResult.Valid(parsed)
            }
        }
    }

    sealed interface ReadingResult {
        data class Valid(val value: Long) : ReadingResult
        data class Invalid(val message: String) : ReadingResult
    }
}
