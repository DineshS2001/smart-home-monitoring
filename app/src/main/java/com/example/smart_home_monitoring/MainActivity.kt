package com.example.smart_home_monitoring

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.smart_home_monitoring.ui.screens.dashboard.DashboardScreen
import com.example.smart_home_monitoring.ui.theme.SmarthomemonitoringTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            SmarthomemonitoringTheme {
                Surface(
                    color = MaterialTheme.colorScheme.background
                ) {
                    DashboardScreen()
                }
            }
        }
    }
}