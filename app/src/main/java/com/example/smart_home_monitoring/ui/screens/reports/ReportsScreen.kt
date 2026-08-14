package com.example.smart_home_monitoring.ui.screens.reports

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.smart_home_monitoring.data.model.UsageEvent
import com.example.smart_home_monitoring.data.repository.UsageRepository
import com.example.smart_home_monitoring.data.repository.FloorRepository
import com.example.smart_home_monitoring.ui.theme.SmarthomemonitoringTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    onBackClick: () -> Unit
) {
    val repository = remember {
        UsageRepository()
    }

    val floorRepository = remember {
        FloorRepository()
    }

    var events by remember {
        mutableStateOf<List<UsageEvent>>(emptyList())
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    var floorNames by remember {
        mutableStateOf<Map<String, String>>(emptyMap())
    }

    DisposableEffect(Unit) {
        val usageListener = repository.observeUsageEvents(
            onEventsChanged = { updatedEvents ->
                events = updatedEvents
                isLoading = false
                errorMessage = null
            },
            onError = { message ->
                errorMessage = message
                isLoading = false
            }
        )

        val floorListener = floorRepository.observeFloors(
            onFloorsChanged = { floors ->
                floorNames = floors.associate { floor ->
                    floor.id to floor.name
                }
            },
            onError = { message ->
                errorMessage = message
            }
        )

        onDispose {
            repository.removeUsageListener(usageListener)
            floorRepository.removeFloorListener(floorListener)
        }
    }

    val turnOnCount = events.count { event ->
        event.newStatus == "ON"
    }

    val turnOffCount = events.count { event ->
        event.newStatus == "OFF"
    }

    val safetyDeviceEvents = events.count { event ->
        event.deviceType == "IRON"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Usage Reports",
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "${events.size} recent events",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                },
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Text(text = "Back")
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
                    text = "Activity Summary",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ReportSummaryCard(
                        modifier = Modifier.weight(1f),
                        value = turnOnCount.toString(),
                        label = "Turned ON",
                        color = Color(0xFFE1F5E7)
                    )

                    ReportSummaryCard(
                        modifier = Modifier.weight(1f),
                        value = turnOffCount.toString(),
                        label = "Turned OFF",
                        color = Color(0xFFE3F2FD)
                    )

                    ReportSummaryCard(
                        modifier = Modifier.weight(1f),
                        value = safetyDeviceEvents.toString(),
                        label = "Iron events",
                        color = Color(0xFFFFF0D8)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Recent Device Activity",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            if (isLoading) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            if (errorMessage != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor =
                                MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = "Firebase error: $errorMessage",
                            modifier = Modifier.padding(16.dp),
                            color =
                                MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            if (!isLoading && errorMessage == null && events.isEmpty()) {
                item {
                    Text(
                        text = "No device activity has been recorded.",
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                }
            }

            items(
                items = events,
                key = { event ->
                    event.id
                }
            ) { event ->
                UsageEventCard(
                    event = event,
                    floorName = floorNames[event.floorId]
                        ?: "Deleted or unavailable floor"
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ReportSummaryCard(
    modifier: Modifier,
    value: String,
    label: String,
    color: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1D2A35)
            )

            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF455A64)
            )
        }
    }
}

@Composable
private fun UsageEventCard(
    event: UsageEvent,
    floorName: String
) {
    val statusColor = when (event.newStatus) {
        "ON" -> Color(0xFF1B7F3A)
        "OFF" -> MaterialTheme.colorScheme.primary
        "ERROR" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = event.deviceName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text =
                    "${event.previousStatus} → ${event.newStatus}",
                style = MaterialTheme.typography.labelLarge,
                color = statusColor,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text =
                    "${formatDeviceType(event.deviceType)} • " +
                            floorName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = formatEventTime(event.changedAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatDeviceType(type: String): String {
    return when (type) {
        "OUTLET" -> "Outlet"
        "MULTI_SWITCH" -> "Multi-switch"
        "LIGHT" -> "Light"
        "IRON" -> "Safety outlet"
        "CAMERA" -> "Camera"
        else -> "Device"
    }
}

private fun formatEventTime(timestamp: Long): String {
    if (timestamp == 0L) {
        return "Unknown time"
    }

    val formatter = SimpleDateFormat(
        "dd MMM yyyy, hh:mm:ss a",
        Locale.getDefault()
    )

    return formatter.format(Date(timestamp))
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ReportsScreenPreview() {
    SmarthomemonitoringTheme {
        ReportsScreen(onBackClick = {})
    }
}