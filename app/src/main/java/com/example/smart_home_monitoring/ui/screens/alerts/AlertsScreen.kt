package com.example.smart_home_monitoring.ui.screens.alerts

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
import com.example.smart_home_monitoring.data.model.SmartAlert
import com.example.smart_home_monitoring.data.repository.AlertRepository
import com.example.smart_home_monitoring.ui.theme.SmarthomemonitoringTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(
    onBackClick: () -> Unit
) {
    val repository = remember {
        AlertRepository()
    }

    var alerts by remember {
        mutableStateOf<List<SmartAlert>>(emptyList())
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    DisposableEffect(Unit) {
        val listener = repository.observeAlerts(
            onAlertsChanged = { updatedAlerts ->
                alerts = updatedAlerts
                isLoading = false
                errorMessage = null
            },
            onError = { message ->
                errorMessage = message
                isLoading = false
            }
        )

        onDispose {
            repository.removeAlertListener(listener)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Safety Alerts",
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "${alerts.count { !it.read }} unread",
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
                        colors = CardDefaults.cardColors(
                            containerColor =
                                MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = "Firebase error: $errorMessage",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            if (!isLoading && errorMessage == null && alerts.isEmpty()) {
                item {
                    Text(
                        text = "No safety alerts have been recorded.",
                        modifier = Modifier.padding(vertical = 32.dp)
                    )
                }
            }

            items(
                items = alerts,
                key = { alert -> alert.id }
            ) { alert ->
                AlertCard(
                    alert = alert,
                    onMarkAsRead = {
                        repository.markAsRead(
                            alertId = alert.id,
                            onError = { message ->
                                errorMessage = message
                            }
                        )
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
private fun AlertCard(
    alert: SmartAlert,
    onMarkAsRead: () -> Unit
) {
    val backgroundColor = if (alert.read) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        Color(0xFFFFE4E4)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = alert.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = alert.deviceName,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = alert.message,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = formatAlertTime(alert.createdAt),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!alert.read) {
                Spacer(modifier = Modifier.height(12.dp))

                Button(onClick = onMarkAsRead) {
                    Text(text = "Mark as read")
                }
            }
        }
    }
}

private fun formatAlertTime(timestamp: Long): String {
    if (timestamp == 0L) {
        return "Unknown time"
    }

    val formatter = SimpleDateFormat(
        "dd MMM yyyy, hh:mm a",
        Locale.getDefault()
    )

    return formatter.format(Date(timestamp))
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AlertsScreenPreview() {
    SmarthomemonitoringTheme {
        AlertsScreen(onBackClick = {})
    }
}