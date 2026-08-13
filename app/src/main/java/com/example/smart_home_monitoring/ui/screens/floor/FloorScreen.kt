package com.example.smart_home_monitoring.ui.screens.floor

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.smart_home_monitoring.data.model.DeviceStatus
import com.example.smart_home_monitoring.data.model.DeviceType
import com.example.smart_home_monitoring.data.model.SmartDevice
import com.example.smart_home_monitoring.ui.theme.SmarthomemonitoringTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloorScreen(
    floorId: String,
    onBackClick: () -> Unit
) {
    val floorName = getFloorName(floorId)
    val devices = getDevicesForFloor(floorId)

    Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = {
                    Column {
                        Text(
                            text = floorName,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${devices.size} smart devices",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    text = "Devices",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Use the switches to control devices.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            items(
                items = devices,
                key = { device -> device.id }
            ) { device ->
                DeviceCard(device = device)
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun DeviceCard(
    device: SmartDevice
) {
    var isOn by rememberSaveable(device.id) {
        androidx.compose.runtime.mutableStateOf(
            device.status == DeviceStatus.ON
        )
    }

    val currentStatus = if (isOn) {
        DeviceStatus.ON
    } else {
        DeviceStatus.OFF
    }

    val statusColor = if (isOn) {
        Color(0xFF1B7F3A)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = device.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = device.roomName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${formatDeviceType(device.type)} • $currentStatus",
                    style = MaterialTheme.typography.labelLarge,
                    color = statusColor,
                    fontWeight = FontWeight.SemiBold
                )

                if (device.maxOnDurationMinutes != null) {
                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Safety limit: ${device.maxOnDurationMinutes} minutes",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFFB45309)
                    )
                }

                if (device.type == DeviceType.MULTI_SWITCH) {
                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${device.numberOfSwitches} individual switches",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            Switch(
                checked = isOn,
                onCheckedChange = { newValue ->
                    isOn = newValue
                }
            )
        }
    }
}

private fun formatDeviceType(type: DeviceType): String {
    return when (type) {
        DeviceType.OUTLET -> "Outlet"
        DeviceType.MULTI_SWITCH -> "Multi-switch"
        DeviceType.LIGHT -> "Light"
        DeviceType.IRON -> "Safety outlet"
        DeviceType.CAMERA -> "Camera"
    }
}

private fun getFloorName(floorId: String): String {
    return when (floorId) {
        "ground_floor" -> "Ground Floor"
        "first_floor" -> "First Floor"
        "outdoor" -> "Outdoor Area"
        else -> "Unknown Floor"
    }
}

private fun getDevicesForFloor(floorId: String): List<SmartDevice> {
    return when (floorId) {
        "ground_floor" -> listOf(
            SmartDevice(
                id = "living_room_light",
                name = "Living Room Light",
                roomName = "Living Room",
                floorId = floorId,
                type = DeviceType.LIGHT,
                status = DeviceStatus.ON,
                gridRow = 1,
                gridColumn = 1
            ),
            SmartDevice(
                id = "television_outlet",
                name = "Television Outlet",
                roomName = "Living Room",
                floorId = floorId,
                type = DeviceType.OUTLET,
                status = DeviceStatus.OFF,
                gridRow = 1,
                gridColumn = 2
            ),
            SmartDevice(
                id = "kitchen_switches",
                name = "Kitchen Switch Unit",
                roomName = "Kitchen",
                floorId = floorId,
                type = DeviceType.MULTI_SWITCH,
                status = DeviceStatus.ON,
                numberOfSwitches = 3,
                gridRow = 2,
                gridColumn = 1
            ),
            SmartDevice(
                id = "clothing_iron",
                name = "Clothing Iron",
                roomName = "Utility Room",
                floorId = floorId,
                type = DeviceType.IRON,
                status = DeviceStatus.OFF,
                maxOnDurationMinutes = 15,
                gridRow = 2,
                gridColumn = 2
            )
        )

        "first_floor" -> listOf(
            SmartDevice(
                id = "bedroom_light",
                name = "Main Bedroom Light",
                roomName = "Main Bedroom",
                floorId = floorId,
                type = DeviceType.LIGHT,
                status = DeviceStatus.OFF
            ),
            SmartDevice(
                id = "study_outlet",
                name = "Study Room Outlet",
                roomName = "Study Room",
                floorId = floorId,
                type = DeviceType.OUTLET,
                status = DeviceStatus.ON
            ),
            SmartDevice(
                id = "upstairs_camera",
                name = "Hallway Camera",
                roomName = "Upstairs Hallway",
                floorId = floorId,
                type = DeviceType.CAMERA,
                status = DeviceStatus.ON
            )
        )

        "outdoor" -> listOf(
            SmartDevice(
                id = "entrance_light",
                name = "Entrance Light",
                roomName = "Front Entrance",
                floorId = floorId,
                type = DeviceType.LIGHT,
                status = DeviceStatus.ON
            ),
            SmartDevice(
                id = "front_camera",
                name = "Front Security Camera",
                roomName = "Front Entrance",
                floorId = floorId,
                type = DeviceType.CAMERA,
                status = DeviceStatus.ON
            ),
            SmartDevice(
                id = "garden_outlet",
                name = "Garden Outlet",
                roomName = "Garden",
                floorId = floorId,
                type = DeviceType.OUTLET,
                status = DeviceStatus.OFF
            )
        )

        else -> emptyList()
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun FloorScreenPreview() {
    SmarthomemonitoringTheme {
        FloorScreen(
            floorId = "ground_floor",
            onBackClick = {}
        )
    }
}