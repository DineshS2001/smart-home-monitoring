package com.example.smart_home_monitoring.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smart_home_monitoring.data.model.DeviceStatus
import com.example.smart_home_monitoring.data.model.SmartDevice
import java.util.Locale

@Composable
fun LightScheduleCard(
    device: SmartDevice,
    onPowerChange: (DeviceStatus) -> Unit,
    onScheduleChange: (
        enabled: Boolean,
        startHour: Int,
        endHour: Int
    ) -> Unit
) {
    val isOn = device.status == DeviceStatus.ON

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = device.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = device.roomName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = when (device.status) {
                            DeviceStatus.ON -> "Light • ON"
                            DeviceStatus.OFF -> "Light • OFF"
                            DeviceStatus.ERROR -> "Light • ERROR"
                            DeviceStatus.DISCONNECTED -> "Light • DISCONNECTED"
                        },
                        modifier = Modifier.padding(top = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = when (device.status) {
                            DeviceStatus.ON -> Color(0xFF1B7F3A)
                            DeviceStatus.ERROR -> Color(0xFFD32F2F)
                            DeviceStatus.DISCONNECTED -> Color(0xFFF57C00)
                            DeviceStatus.OFF ->
                                MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }

                Switch(
                    checked = isOn,
                    enabled =
                        device.status != DeviceStatus.ERROR &&
                                device.status !=
                                DeviceStatus.DISCONNECTED,
                    onCheckedChange = { checked ->
                        onPowerChange(
                            if (checked) {
                                DeviceStatus.ON
                            } else {
                                DeviceStatus.OFF
                            }
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Automatic schedule",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = if (device.scheduleEnabled) {
                            "Schedule enabled"
                        } else {
                            "Schedule disabled"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = device.scheduleEnabled,
                    onCheckedChange = { enabled ->
                        onScheduleChange(
                            enabled,
                            device.scheduleStartHour,
                            device.scheduleEndHour
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HourSelector(
                    modifier = Modifier.weight(1f),
                    title = "Turn ON",
                    hour = device.scheduleStartHour,
                    enabled = device.scheduleEnabled,
                    onHourChange = { newHour ->
                        onScheduleChange(
                            device.scheduleEnabled,
                            newHour,
                            device.scheduleEndHour
                        )
                    }
                )

                HourSelector(
                    modifier = Modifier.weight(1f),
                    title = "Turn OFF",
                    hour = device.scheduleEndHour,
                    enabled = device.scheduleEnabled,
                    onHourChange = { newHour ->
                        onScheduleChange(
                            device.scheduleEnabled,
                            device.scheduleStartHour,
                            newHour
                        )
                    }
                )
            }

            if (device.scheduleEnabled) {
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text =
                        "Active from " +
                                formatHour(device.scheduleStartHour) +
                                " to " +
                                formatHour(device.scheduleEndHour),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun HourSelector(
    modifier: Modifier,
    title: String,
    hour: Int,
    enabled: Boolean,
    onHourChange: (Int) -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium
            )

            Text(
                text = formatHour(hour),
                modifier = Modifier.padding(vertical = 4.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row {
                TextButton(
                    enabled = enabled,
                    onClick = {
                        onHourChange((hour + 23) % 24)
                    }
                ) {
                    Text(text = "−")
                }

                TextButton(
                    enabled = enabled,
                    onClick = {
                        onHourChange((hour + 1) % 24)
                    }
                ) {
                    Text(text = "+")
                }
            }
        }
    }
}

private fun formatHour(hour: Int): String {
    return String.format(Locale.US, "%02d:00", hour)
}