package com.ammad.billcalculator.presentation.bill

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ammad.billcalculator.R
import com.ammad.billcalculator.domain.Reading
import com.ammad.billcalculator.presentation.theme.MonoFamily
import com.ammad.billcalculator.util.Validators
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Currency
import java.util.Date
import java.util.Locale

private val currencyFormat: NumberFormat =
    NumberFormat.getCurrencyInstance(Locale("en", "PK")).apply {
        currency = Currency.getInstance("PKR")
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }

private val dateFormat: SimpleDateFormat =
    SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

private val PillShape = RoundedCornerShape(50)
private val CardCornerShape = RoundedCornerShape(16.dp)
private val FieldShape = RoundedCornerShape(12.dp)

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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Bolt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.size(8.dp))
                        Text(
                            text = stringResource(R.string.electricity_bill_calculator),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
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
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            EntryCard(
                state = state,
                onServiceNumberChange = onServiceNumberChange,
                onReadingChange = onReadingChange,
                onSubmit = onSubmit,
            )
            state.result?.let { result ->
                SummaryCard(result = result)
                HistorySection(result = result)
                ActionRow(
                    onSave = onSave,
                    onCancel = onCancel,
                    isSubmitting = state.isSubmitting,
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SectionCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardCornerShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) { content() }
    }
}

@Composable
private fun EntryCard(
    state: BillUiState,
    onServiceNumberChange: (String) -> Unit,
    onReadingChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    SectionCard {
        Text(
            text = stringResource(R.string.enter_meter_reading),
            style = MaterialTheme.typography.titleLarge,
        )
        Spacer(Modifier.height(20.dp))

        ThemedField(
            value = state.serviceNumber,
            onValueChange = onServiceNumberChange,
            label = stringResource(R.string.service_number),
            leadingIcon = Icons.Filled.Badge,
            isError = state.serviceNumberError != null,
            supportingText = state.serviceNumberError
                ?: "Exactly ${Validators.SERVICE_NUMBER_LENGTH} alphanumeric characters",
            enabled = state.result == null,
            keyboardOptions = KeyboardOptions(),
        )
        Spacer(Modifier.height(12.dp))
        ThemedField(
            value = state.currentReading,
            onValueChange = onReadingChange,
            label = stringResource(R.string.current_reading_units),
            leadingIcon = Icons.Filled.Speed,
            isError = state.currentReadingError != null,
            supportingText = state.currentReadingError,
            enabled = state.result == null,
            keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
        )

        Spacer(Modifier.height(20.dp))
        Button(
            onClick = onSubmit,
            enabled = state.canSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = PillShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            if (state.isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                Text(
                    text = stringResource(R.string.submit),
                    style = MaterialTheme.typography.titleSmall,
                )
            }
        }
    }
}

@Composable
private fun ThemedField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector,
    isError: Boolean,
    supportingText: String?,
    enabled: Boolean,
    keyboardOptions: KeyboardOptions,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
            )
        },
        singleLine = true,
        isError = isError,
        supportingText = supportingText?.let { { Text(it) } },
        enabled = enabled,
        keyboardOptions = keyboardOptions,
        shape = FieldShape,
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.outline,
            cursorColor = MaterialTheme.colorScheme.primary,
        ),
    )
}

@Composable
private fun SummaryCard(result: CalculationResult) {
    SectionCard {
        Text(
            text = stringResource(R.string.bill_summary),
            style = MaterialTheme.typography.titleLarge,
        )
        Spacer(Modifier.height(20.dp))
        SummaryRow(
            label = stringResource(R.string.previous_reading),
            value = result.previousReading?.toString() ?: stringResource(R.string.none),
        )
        Spacer(Modifier.height(12.dp))
        SummaryRow(
            label = stringResource(R.string.current_reading),
            value = result.currentReading.toString(),
        )
        Spacer(Modifier.height(12.dp))
        SummaryRow(
            label = stringResource(R.string.consumption),
            value = "${result.consumption} units",
        )
        Spacer(Modifier.height(16.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.total_cost),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = currencyFormat.format(result.cost),
                style = MaterialTheme.typography.headlineMedium.copy(fontFamily = MonoFamily),
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun HistorySection(result: CalculationResult) {
    if (result.history.isEmpty()) {
        SectionCard {
            Text(
                text = "No previous readings for ${result.serviceNumber}.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row {
            Text(
                text = "Last ${result.history.size} reading(s) for ",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = result.serviceNumber,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = CardCornerShape,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column {
                HistoryHeader()
                result.history.forEachIndexed { index, row ->
                    HistoryDataRow(row)
                    if (index < result.history.lastIndex) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HeaderCell(
            text = stringResource(R.string.date),
            icon = Icons.Filled.CalendarToday,
            weight = 2f,
            align = TextAlign.Start,
        )
        HeaderCell(
            text = stringResource(R.string.reading),
            icon = Icons.AutoMirrored.Filled.ShowChart,
            weight = 1.2f,
            align = TextAlign.Start,
        )
        HeaderCell(
            text = stringResource(R.string.cost),
            icon = Icons.Filled.Payments,
            weight = 1.4f,
            align = TextAlign.Start,
        )
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.HeaderCell(
    text: String,
    icon: ImageVector,
    weight: Float,
    align: TextAlign,
) {
    Row(
        modifier = Modifier
            .weight(weight)
            .padding(end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.size(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = align,
        )
    }
}

@Composable
private fun HistoryDataRow(row: Reading) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = dateFormat.format(Date(row.timestamp)),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .weight(2f)
                .padding(end = 8.dp),
        )
        Text(
            text = row.reading.toString(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
        )
        Text(
            text = currencyFormat.format(row.cost),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1.4f),
        )
    }
}

@Composable
private fun ActionRow(
    onSave: () -> Unit,
    onCancel: () -> Unit,
    isSubmitting: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Button(
            onClick = onSave,
            enabled = !isSubmitting,
            modifier = Modifier
                .weight(1f)
                .height(52.dp),
            shape = PillShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                Text(
                    text = stringResource(R.string.save),
                    style = MaterialTheme.typography.titleSmall,
                )
            }
        }
        OutlinedButton(
            onClick = onCancel,
            enabled = !isSubmitting,
            modifier = Modifier
                .weight(1f)
                .height(52.dp),
            shape = PillShape,
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
        ) {
            Text(
                text = stringResource(R.string.cancel),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}