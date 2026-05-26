# Bill Calculator

Android app that calculates an electricity bill from a meter reading against a configurable slab table.

## What it does

- Enter a customer service number (exactly 10 alphanumeric characters) and the current meter reading (positive whole number).
- Tap **Submit** to compute the consumption (difference from the customer's previous reading, or the reading itself for a new customer) and price it against the slab table.
- Tap **Save** to persist the reading and cost to the customer's history (Room database).
- The result panel shows the bill summary plus the customer's last 3 saved readings.

## Validation

- Service number must match `^[A-Za-z0-9]{10}$`. Anything else surfaces an inline error and disables Submit.
- Reading must be a non-negative whole number. Negative values are accepted at the field level so the validator can flag "Meter reading cannot be negative"; the Submit button stays disabled while the error is visible.
  <img width="180" height="400" alt="WhatsApp Image 2026-05-26 at 12 42 17 PM" src="https://github.com/user-attachments/assets/9ea44a16-b0ce-420d-a2f0-eee25113589e" />

- A reading lower than the customer's previous reading shows a snackbar warning ("New reading is lower than the previous reading…") and skips the calculation.
  <img width="180" height="400" alt="WhatsApp Image 2026-05-26 at 12 42 17 PM-2" src="https://github.com/user-attachments/assets/a4067804-f7c1-4660-a906-869a9bdcd958" />


## Tech stack

- Kotlin 2.2 + Jetpack Compose + Material 3
- Single Activity, MVVM (`BillViewModel` + `StateFlow<BillUiState>`)
- Room (KSP) for history persistence
- `BigDecimal` for currency math (slab rates can be decimal)
- No DI framework — small manual `AppContainer` in `BillCalculatorApp`

## Build and run

```
./gradlew assembleDebug
```

Open in Android Studio (Giraffe or newer) and run on an emulator or device with API 28+.

## Configuring the slab table

The slab table is intentionally **code-configurable**, so the reviewer changes one Kotlin file and rebuilds. There is no slab configuration screen.

**File:** `app/src/main/java/com/ammad/billcalculator/domain/SlabConfig.kt`

Edit the `SLABS` list. Each `Slab(upperBound, ratePerUnit)`:

- `upperBound: Long?` — inclusive upper unit count for this slab. The **last** slab must use `null` to mark "unbounded" (it captures all consumption above the previous slab's bound).
- `ratePerUnit: BigDecimal` — currency-per-unit rate. Use a `BigDecimal` string literal (`BigDecimal("5.50")`).

Example matching the spec illustration:

```kotlin
val SLABS: List<Slab> = listOf(
    Slab(upperBound = 100L,  ratePerUnit = BigDecimal("5")),   // 1–100 units @ 5/unit
    Slab(upperBound = 500L,  ratePerUnit = BigDecimal("8")),   // 101–500 units @ 8/unit
    Slab(upperBound = null,  ratePerUnit = BigDecimal("10")),  // > 500 units @ 10/unit
)
```

Rules (enforced at app launch by `SlabConfig.validate`, which throws `IllegalStateException` on any violation):

1. The list must be non-empty.
2. Bounded slabs are listed in ascending order of `upperBound`.
3. At most one slab has `upperBound = null`, and if present it must be the last entry.
4. All `ratePerUnit` values are non-negative.

After editing `SLABS`, rebuild and re-run the app.

## Persistence

Room database at `bill-calculator.db` with a single `readings` table indexed by `serviceNumber`. The DAO exposes one insert and a "last N by service number" query. Clearing app data resets history.

## Logging

`android.util.Log` with per-file `TAG`s. The Application logs the loaded slab summary on launch (handy when verifying a reviewer's slab edit). The ViewModel logs validation rejections (`Log.w`), submit/save lifecycle events (`Log.d`/`Log.i`), and unexpected failures (`Log.e`) — exceptions are caught at the coroutine boundary and surfaced as a snackbar.

## Screenshots

- No history for a given service number
  <img width="720" height="1600" alt="WhatsApp Image 2026-05-26 at 12 42 17 PM-3" src="https://github.com/user-attachments/assets/baa06f97-8929-4177-97ca-b40a13357ebd" />

- Reading saved
  <img width="720" height="1600" alt="WhatsApp Image 2026-05-26 at 12 42 18 PM" src="https://github.com/user-attachments/assets/ddeea7af-1e81-406d-80f1-9a996d0d7451" />

- Last 3 readings for a given service number, displayed in a history table
  <img width="720" height="1600" alt="WhatsApp Image 2026-05-26 at 12 42 18 PM-3" src="https://github.com/user-attachments/assets/5e96f34b-4a5e-4fe0-b830-24beab87d96f" />
<img width="720" height="1600" alt="WhatsApp Image 2026-05-26 at 12 42 18 PM-2" src="https://github.com/user-attachments/assets/33265143-f2f0-4b2b-b21b-62a6ea997546" />

