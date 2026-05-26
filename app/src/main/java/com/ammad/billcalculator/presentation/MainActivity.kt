package com.ammad.billcalculator.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ammad.billcalculator.presentation.bill.BillScreenHost
import com.ammad.billcalculator.presentation.theme.BillCalculatorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BillCalculatorTheme {
                BillScreenHost()
            }
        }
    }
}