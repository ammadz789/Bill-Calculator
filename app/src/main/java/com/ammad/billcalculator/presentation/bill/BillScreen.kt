package com.ammad.billcalculator.presentation.bill

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ammad.billcalculator.R
import com.ammad.billcalculator.domain.Reading
import com.ammad.billcalculator.util.Validators
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val currencyFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.US).apply {
    minimumFractionDigits = 2
    maximumFractionDigits = 2
}

private val dateFormat: SimpleDateFormat =
    SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

@Composable
fun BillScreenHost(
    viewModel: BillViewModel = viewModel(factory = BillViewModel.Factory),
) {
    val state by viewModel.state.collectAsState()
    BillScreen(
        state = state,
        onServiceNumberChange = viewModel::onServiceNumberChange,
        onReadingChange = viewModel::onReadingChange,
        onSubmit = viewModel::onSubmit,
        onSave = viewModel::onSave,
        onCancel = viewModel::onCancel,
        onSnackbarShown = viewModel::onSnackbarShown,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillScreen(
    state: BillUiState,
    onServiceNumberChange: (String) -> Unit,
    onReadingChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    onSnackbarShown: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.snackbarMessage) {
        val message = state.snackbarMessage
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            onSnackbarShown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.electricity_bill_calculator)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            EntryCard(
                state = state,
                onServiceNumberChange = onServiceNumberChange,
                onReadingChange = onReadingChange,
                onSubmit = onSubmit,
            )
            state.result?.let { result ->
                ResultCard(result = result, onSave = onSave, onCancel = onCancel, isSubmitting = state.isSubmitting)
            }
        }
    }
}

@Composable
private fun EntryCard(
    state: BillUiState,
    onServiceNumberChange: (String) -> Unit,
    onReadingChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Bolt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(0.dp))
                Text(
                    stringResource(R.string.enter_meter_reading),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            OutlinedTextField(
                value = state.serviceNumber,
                onValueChange = onServiceNumberChange,
                label = { Text(stringResource(R.string.service_number)) },
                singleLine = true,
                isError = state.serviceNumberError != null,
                supportingText = {
                    val msg = state.serviceNumberError
                        ?: "Exactly ${Validators.SERVICE_NUMBER_LENGTH} alphanumeric characters"
                    Text(msg)
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    keyboardType = KeyboardType.Ascii,
                    imeAction = ImeAction.Next,
                ),
                modifier = Modifier.fillMaxWidth(),
                enabled = state.result == null,
            )
            OutlinedTextField(
                value = state.currentReading,
                onValueChange = onReadingChange,
                label = { Text(stringResource(R.string.current_reading_units)) },
                singleLine = true,
                isError = state.currentReadingError != null,
                supportingText = state.currentReadingError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done,
                ),
                modifier = Modifier.fillMaxWidth(),
                enabled = state.result == null,
            )
            Button(
                onClick = onSubmit,
                enabled = state.canSubmit,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(20.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(stringResource(R.string.submit))
                }
            }
        }
    }
}

@Composable
private fun ResultCard(
    result: CalculationResult,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    isSubmitting: Boolean,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(R.string.bill_summary), style = MaterialTheme.typography.titleMedium)
            SummaryRow(
                label = stringResource(R.string.previous_reading),
                value = result.previousReading?.toString() ?: stringResource(R.string.none),
            )
            SummaryRow(label = stringResource(R.string.current_reading), value = result.currentReading.toString())
            SummaryRow(label = stringResource(R.string.consumption), value = "${result.consumption} units")
            HorizontalDivider()
            SummaryRow(
                label = stringResource(R.string.total_cost),
                value = currencyFormat.format(result.cost),
                emphasize = true,
            )

            if (result.history.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "Last ${result.history.size} reading(s) for ${result.serviceNumber}",
                    style = MaterialTheme.typography.titleSmall,
                )
                HistoryTable(history = result.history)
            } else {
                Text(
                    "No previous readings for ${result.serviceNumber}.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                    enabled = !isSubmitting,
                ) { Text(stringResource(R.string.cancel)) }
                Button(
                    onClick = onSave,
                    modifier = Modifier.weight(1f),
                    enabled = !isSubmitting,
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(20.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(stringResource(R.string.save))
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, emphasize: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = if (emphasize) MaterialTheme.typography.titleMedium
            else MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = value,
            style = if (emphasize) MaterialTheme.typography.titleMedium
            else MaterialTheme.typography.bodyMedium,
            fontWeight = if (emphasize) FontWeight.Bold else FontWeight.Normal,
        )
    }
}

@Composable
private fun HistoryTable(history: List<Reading>) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            HistoryRow(
                date = stringResource(R.string.date),
                reading = stringResource(R.string.reading),
                cost = stringResource(R.string.cost),
                header = true,
            )
            HorizontalDivider()
            history.forEach { row ->
                HistoryRow(
                    date = dateFormat.format(Date(row.timestamp)),
                    reading = row.reading.toString(),
                    cost = currencyFormat.format(row.cost),
                    header = false,
                )
            }
        }
    }
}

@Composable
private fun HistoryRow(date: String, reading: String, cost: String, header: Boolean) {
    val style = if (header) MaterialTheme.typography.labelMedium
    else MaterialTheme.typography.bodySmall
    val weight = if (header) FontWeight.Bold else FontWeight.Normal
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Text(date, style = style, fontWeight = weight, modifier = Modifier.weight(2f))
        Text(
            reading,
            style = style,
            fontWeight = weight,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f),
        )
        Text(
            cost,
            style = style,
            fontWeight = weight,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.2f),
        )
    }
}