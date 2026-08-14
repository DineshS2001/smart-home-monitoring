package com.example.smart_home_monitoring.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smart_home_monitoring.data.model.DeviceStatus
import com.example.smart_home_monitoring.data.model.SmartDevice
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CameraMockCard(
    device: SmartDevice,
    onStatusChange: (DeviceStatus) -> Unit
) {
    val isOnline = device.status == DeviceStatus.ON

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
                }

                Switch(
                    checked = isOnline,
                    enabled =
                        device.status != DeviceStatus.ERROR &&
                                device.status != DeviceStatus.DISCONNECTED,
                    onCheckedChange = { checked ->
                        onStatusChange(
                            if (checked) {
                                DeviceStatus.ON
                            } else {
                                DeviceStatus.OFF
                            }
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            MockCameraView(
                isOnline = isOnline,
                deviceName = device.name
            )
        }
    }
}

@Composable
private fun MockCameraView(
    isOnline: Boolean,
    deviceName: String
) {
    val backgroundBrush = if (isOnline) {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF243447),
                Color(0xFF536976),
                Color(0xFF292E49)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF353535),
                Color(0xFF1C1C1C)
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundBrush)
            .padding(14.dp)
    ) {
        if (isOnline) {
            Text(
                text = "● LIVE",
                color = Color(0xFFFF6868),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.TopStart)
            )

            Text(
                text = "MOCK CAMERA SNAPSHOT",
                color = Color.White.copy(alpha = 0.9f),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Center)
            )

            Text(
                text = deviceName,
                color = Color.White.copy(alpha = 0.75f),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.BottomStart)
            )

            Text(
                text = formatCurrentTime(),
                color = Color.White.copy(alpha = 0.75f),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.BottomEnd)
            )
        } else {
            Text(
                text = "CAMERA OFFLINE",
                color = Color.White.copy(alpha = 0.75f),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

private fun formatCurrentTime(): String {
    val formatter = SimpleDateFormat(
        "dd MMM yyyy  HH:mm:ss",
        Locale.getDefault()
    )

    return formatter.format(Date())
}