package com.ammad.billcalculator.presentation.bill

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ammad.billcalculator.BillCalculatorApp
import com.ammad.billcalculator.data.ReadingRepository
import com.ammad.billcalculator.domain.SlabCalculator
import com.ammad.billcalculator.domain.SlabConfig
import com.ammad.billcalculator.util.Validators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BillViewModel(
    private val repo: ReadingRepository,
    private val nowMillis: () -> Long = System::currentTimeMillis,
) : ViewModel() {

    private val _state = MutableStateFlow(BillUiState())
    val state: StateFlow<BillUiState> = _state.asStateFlow()

    fun onServiceNumberChange(value: String) {
        val trimmed = value.trim()
        _state.update {
            it.copy(
                serviceNumber = trimmed,
                serviceNumberError = if (trimmed.isEmpty()) null
                else Validators.validateServiceNumber(trimmed),
            )
        }
    }

    fun onReadingChange(value: String) {
        val digitsOnly = value.filter { it.isDigit() }
        _state.update {
            it.copy(
                currentReading = digitsOnly,
                currentReadingError = when {
                    digitsOnly.isEmpty() -> null
                    digitsOnly.toLongOrNull() == null -> Validators.ERROR_READING_NOT_NUMERIC
                    else -> null
                },
            )
        }
    }

    fun onSubmit() {
        val current = _state.value
        val svcError = Validators.validateServiceNumber(current.serviceNumber)
        val readingResult = Validators.validateReading(current.currentReading)
        val readingError = (readingResult as? Validators.ReadingResult.Invalid)?.message

        if (svcError != null || readingError != null) {
            _state.update {
                it.copy(
                    serviceNumberError = svcError,
                    currentReadingError = readingError,
                )
            }
            Log.w(TAG, "Submit blocked by validation: svc=$svcError reading=$readingError")
            return
        }

        val reading = (readingResult as Validators.ReadingResult.Valid).value
        val serviceNumber = current.serviceNumber

        _state.update { it.copy(isSubmitting = true) }
        viewModelScope.launch {
            try {
                val history = repo.lastN(serviceNumber, HISTORY_LIMIT)
                val previous = history.firstOrNull()?.reading
                if (previous != null && reading < previous) {
                    Log.w(TAG, "Reading $reading less than previous $previous for $serviceNumber")
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            snackbarMessage = "New reading ($reading) is lower than the previous " +
                                    "reading ($previous). Readings must not decrease.",
                        )
                    }
                    return@launch
                }
                val consumption = if (previous == null) reading else reading - previous
                val cost = SlabCalculator.calculateCost(consumption, SlabConfig.SLABS)
                Log.d(
                    TAG,
                    "Computed: svc=$serviceNumber reading=$reading prev=$previous " +
                            "consumption=$consumption cost=$cost",
                )
                _state.update {
                    it.copy(
                        isSubmitting = false,
                        result = CalculationResult(
                            serviceNumber = serviceNumber,
                            currentReading = reading,
                            previousReading = previous,
                            consumption = consumption,
                            cost = cost,
                            history = history,
                        ),
                    )
                }
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to compute bill", t)
                _state.update {
                    it.copy(
                        isSubmitting = false,
                        snackbarMessage = "Something went wrong calculating the bill. Please try again.",
                    )
                }
            }
        }
    }

    fun onSave() {
        val result = _state.value.result ?: return
        _state.update { it.copy(isSubmitting = true) }
        viewModelScope.launch {
            try {
                repo.insert(
                    serviceNumber = result.serviceNumber,
                    reading = result.currentReading,
                    cost = result.cost,
                    timestamp = nowMillis(),
                )
                Log.i(TAG, "Saved reading for ${result.serviceNumber}: ${result.currentReading}")
                _state.value = BillUiState(snackbarMessage = "Reading saved.")
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to save reading", t)
                _state.update {
                    it.copy(
                        isSubmitting = false,
                        snackbarMessage = "Could not save the reading. Please try again.",
                    )
                }
            }
        }
    }

    fun onCancel() {
        _state.update { it.copy(result = null) }
    }

    fun onSnackbarShown() {
        _state.update { it.copy(snackbarMessage = null) }
    }

    companion object {
        private const val TAG = "BillViewModel"
        private const val HISTORY_LIMIT = 3

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as BillCalculatorApp
                BillViewModel(app.container.readingRepository)
            }
        }
    }
}
