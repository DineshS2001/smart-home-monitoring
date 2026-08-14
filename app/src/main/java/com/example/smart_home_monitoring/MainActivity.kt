package com.example.smart_home_monitoring

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.smart_home_monitoring.data.repository.FirebaseRepository
import com.example.smart_home_monitoring.ui.screens.alerts.AlertsScreen
import com.example.smart_home_monitoring.ui.screens.dashboard.DashboardScreen
import com.example.smart_home_monitoring.ui.screens.floor.FloorScreen
import com.example.smart_home_monitoring.ui.theme.SmarthomemonitoringTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val firebaseRepository = FirebaseRepository()
        firebaseRepository.initializeSampleData()

        enableEdgeToEdge()

        setContent {
            SmarthomemonitoringTheme {
                var selectedFloorId by rememberSaveable {
                    mutableStateOf<String?>(null)
                }

                var showAlerts by rememberSaveable {
                    mutableStateOf(false)
                }

                Surface(
                    color = MaterialTheme.colorScheme.background
                ) {
                    when {
                        showAlerts -> {
                            BackHandler {
                                showAlerts = false
                            }

                            AlertsScreen(
                                onBackClick = {
                                    showAlerts = false
                                }
                            )
                        }

                        selectedFloorId == null -> {
                            DashboardScreen(
                                onFloorClick = { floorId ->
                                    selectedFloorId = floorId
                                },
                                onAlertsClick = {
                                    showAlerts = true
                                }
                            )
                        }

                        else -> {
                            BackHandler {
                                selectedFloorId = null
                            }

                            FloorScreen(
                                floorId = selectedFloorId!!,
                                onBackClick = {
                                    selectedFloorId = null
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}