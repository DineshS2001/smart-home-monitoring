package com.example.smart_home_monitoring.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smart_home_monitoring.data.model.DeviceStatus
import com.example.smart_home_monitoring.data.model.SmartDevice

@Composable
fun FloorGrid(
    devices: List<SmartDevice>,
    onDeviceClick: (SmartDevice) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF1EDF8)
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Text(
                text = "Interactive Floor Grid",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Tap a mapped device to control it",
                modifier = Modifier.padding(
                    top = 2.dp,
                    bottom = 12.dp
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            for (rowNumber in 1..2) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (columnNumber in 1..2) {
                        val device = devices.firstOrNull { item ->
                            item.gridRow == rowNumber &&
                                    item.gridColumn == columnNumber
                        }

                        GridCell(
                            modifier = Modifier.weight(1f),
                            device = device,
                            rowNumber = rowNumber,
                            columnNumber = columnNumber,
                            onDeviceClick = onDeviceClick
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GridCell(
    modifier: Modifier,
    device: SmartDevice?,
    rowNumber: Int,
    columnNumber: Int,
    onDeviceClick: (SmartDevice) -> Unit
) {
    if (device == null) {
        Card(
            modifier = modifier.height(112.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE4E1E8)
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = "Empty Area",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Row $rowNumber • Column $columnNumber",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        return
    }

    val backgroundColor = when (device.status) {
        DeviceStatus.ON -> Color(0xFFDDF5E5)
        DeviceStatus.OFF -> Color(0xFFFFFFFF)
        DeviceStatus.ERROR -> Color(0xFFFFDEDE)
        DeviceStatus.DISCONNECTED -> Color(0xFFFFEBC7)
    }

    val statusColor = when (device.status) {
        DeviceStatus.ON -> Color(0xFF187038)
        DeviceStatus.OFF ->
            MaterialTheme.colorScheme.onSurfaceVariant

        DeviceStatus.ERROR -> MaterialTheme.colorScheme.error
        DeviceStatus.DISCONNECTED -> Color(0xFF9A5B00)
    }

    Card(
        modifier = modifier.height(112.dp),
        onClick = {
            onDeviceClick(device)
        },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = device.name,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 2
            )

            Text(
                text = device.roomName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )

            Text(
                text = device.status.name,
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.labelMedium,
                color = statusColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}