package com.example.smart_home_monitoring.ui.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.smart_home_monitoring.data.model.Floor
import com.example.smart_home_monitoring.data.model.DeviceStatus
import com.example.smart_home_monitoring.data.repository.FloorRepository
import com.example.smart_home_monitoring.ui.theme.SmarthomemonitoringTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onFloorClick: (String) -> Unit = {},
    onAlertsClick: () -> Unit = {},
    onReportsClick: () -> Unit = {},
    onManageFloorsClick: () -> Unit = {}
) {
    val repository = remember { FloorRepository() }
    var floors by remember { mutableStateOf<List<Floor>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        val listener = repository.observeFloors(
            onFloorsChanged = { updatedFloors ->
                floors = updatedFloors
                isLoading = false
                errorMessage = null
            },
            onError = { message ->
                errorMessage = message
                isLoading = false
            }
        )

        onDispose { repository.removeFloorListener(listener) }
    }

    val totalDevices = floors.sumOf { floor ->
        floor.devices.count { device ->
            device.status != DeviceStatus.ERROR &&
                    device.status != DeviceStatus.DISCONNECTED
        }
    }
    val activeDevices = floors.sumOf { it.activeDeviceCount }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Smart Home",
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Monitoring & Control",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "System Overview",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SummaryCard(
                        modifier = Modifier.weight(1f),
                        title = "Online",
                        value = totalDevices.toString(),
                        backgroundColor = Color(0xFFE1F5E7)
                    )

                    SummaryCard(
                        modifier = Modifier.weight(1f),
                        title = "Active",
                        value = activeDevices.toString(),
                        backgroundColor = Color(0xFFE3F2FD)
                    )

                    SummaryCard(
                        modifier = Modifier.weight(1f),
                        title = "Warnings",
                        value = "0",
                        backgroundColor = Color(0xFFFFF3E0)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onAlertsClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "View Safety Alerts")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onReportsClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View Usage Reports")
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Your Floors",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onManageFloorsClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Add or Manage Floors")
                }
            }

            if (isLoading) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            if (errorMessage != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = "Firebase error: $errorMessage",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            if (!isLoading && errorMessage == null && floors.isEmpty()) {
                item {
                    Text(
                        text = "No floors yet. Tap Add or Manage Floors to create one.",
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            }

            items(
                items = floors,
                key = { floor ->
                    floor.id
                }
            ) { floor ->
                FloorCard(
                    floor = floor,
                    onClick = {
                        onFloorClick(floor.id)
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SummaryCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    backgroundColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1D2A35)
            )

            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF455A64)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FloorCard(
    floor: Floor,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Text(
                text = floor.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = floor.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text =
                    "${floor.activeDeviceCount} active • " +
                            "${floor.totalDeviceCount} devices",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun DashboardScreenPreview() {
    SmarthomemonitoringTheme {
        DashboardScreen()
    }
}