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
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.smart_home_monitoring.data.model.DeviceStatus
import com.example.smart_home_monitoring.data.model.DeviceType
import com.example.smart_home_monitoring.data.model.SmartDevice
import com.example.smart_home_monitoring.data.repository.DeviceRealtimeRepository
import com.example.smart_home_monitoring.data.repository.FloorRepository
import com.example.smart_home_monitoring.ui.theme.SmarthomemonitoringTheme
import com.example.smart_home_monitoring.ui.components.FloorGrid
import com.example.smart_home_monitoring.ui.components.CameraMockCard
import com.example.smart_home_monitoring.ui.components.LightScheduleCard
import com.example.smart_home_monitoring.ui.components.AddDeviceDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloorScreen(
    floorId: String,
    onBackClick: () -> Unit
) {
    val repository = remember {
        DeviceRealtimeRepository()
    }

    val floorRepository = remember {
        FloorRepository()
    }

    var devices by remember(floorId) {
        mutableStateOf<List<SmartDevice>>(emptyList())
    }

    var isLoading by remember(floorId) {
        mutableStateOf(true)
    }

    var errorMessage by remember(floorId) {
        mutableStateOf<String?>(null)
    }

    var floorName by remember(floorId) {
        mutableStateOf("Floor")
    }

    var showAddDeviceDialog by remember(floorId) {
        mutableStateOf(false)
    }

    var deviceToDelete by remember(floorId) {
        mutableStateOf<SmartDevice?>(null)
    }

    DisposableEffect(floorId) {
        val deviceListener = repository.observeDevicesForFloor(
            floorId = floorId,
            onDevicesChanged = { updatedDevices ->
                devices = updatedDevices
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
                floorName = floors.firstOrNull { it.id == floorId }?.name
                    ?: "Floor"
            },
            onError = { message ->
                errorMessage = message
            }
        )

        onDispose {
            repository.removeDeviceListener(deviceListener)
            floorRepository.removeFloorListener(floorListener)
        }
    }

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
                    text = "Changes are synchronized with Firebase.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { showAddDeviceDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Add Appliance")
                }
            }

            if (!isLoading && devices.isNotEmpty()) {
                item {
                    FloorGrid(
                        devices = devices,
                        onDeviceClick = { selectedDevice ->
                            val isControllable =
                                selectedDevice.status != DeviceStatus.ERROR &&
                                        selectedDevice.status !=
                                        DeviceStatus.DISCONNECTED

                            if (isControllable) {
                                if (
                                    selectedDevice.type ==
                                    DeviceType.MULTI_SWITCH
                                ) {
                                    val anySwitchOn =
                                        selectedDevice.switchStates.values.any {
                                            it
                                        }

                                    repository.updateAllSwitches(
                                        device = selectedDevice,
                                        isOn = !anySwitchOn,
                                        onError = { message ->
                                            errorMessage = message
                                        }
                                    )
                                } else {
                                    val newStatus =
                                        if (
                                            selectedDevice.status ==
                                            DeviceStatus.ON
                                        ) {
                                            DeviceStatus.OFF
                                        } else {
                                            DeviceStatus.ON
                                        }

                                    repository.updateDeviceStatus(
                                        device = selectedDevice,
                                        status = newStatus,
                                        onError = { message ->
                                            errorMessage = message
                                        }
                                    )
                                }
                            }
                        }
                    )
                }
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
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = "Firebase error: $errorMessage",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            if (!isLoading && errorMessage == null && devices.isEmpty()) {
                item {
                    Text(
                        text = "No devices were found for this floor.",
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                }
            }

            items(
                items = devices,
                key = { device -> device.id }
            ) { device ->
                Column {
                    when (device.type) {
                        DeviceType.LIGHT -> {
                            LightScheduleCard(
                                device = device,
                                onPowerChange = { newStatus ->
                                    repository.updateDeviceStatus(
                                        device = device,
                                        status = newStatus,
                                        onError = { message ->
                                            errorMessage = message
                                        }
                                    )
                                },
                                onScheduleChange = {
                                        enabled,
                                        startHour,
                                        endHour ->

                                    repository.updateLightSchedule(
                                        deviceId = device.id,
                                        enabled = enabled,
                                        startHour = startHour,
                                        endHour = endHour,
                                        onError = { message ->
                                            errorMessage = message
                                        }
                                    )
                                }
                            )
                        }
                        DeviceType.CAMERA -> {
                            CameraMockCard(
                                device = device,
                                onStatusChange = { newStatus ->
                                    repository.updateDeviceStatus(
                                        device = device,
                                        status = newStatus,
                                        onError = { message ->
                                            errorMessage = message
                                        }
                                    )
                                }
                            )
                        }

                        DeviceType.MULTI_SWITCH -> {
                            MultiSwitchCard(
                                device = device,
                                onMasterSwitchChange = { isOn ->
                                    repository.updateAllSwitches(
                                        device = device,
                                        isOn = isOn,
                                        onError = { message ->
                                            errorMessage = message
                                        }
                                    )
                                },
                                onIndividualSwitchChange = { switchKey, isOn ->
                                    repository.updateIndividualSwitch(
                                        device = device,
                                        switchKey = switchKey,
                                        isOn = isOn,
                                        onError = { message ->
                                            errorMessage = message
                                        }
                                    )
                                }
                            )
                        }

                        else -> {
                            DeviceCard(
                                device = device,
                                onStatusChange = { newStatus ->
                                    repository.updateDeviceStatus(
                                        device = device,
                                        status = newStatus,
                                        onError = { message ->
                                            errorMessage = message
                                        }
                                    )
                                }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { deviceToDelete = device }
                        ) {
                            Text(text = "Remove appliance")
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showAddDeviceDialog) {
        AddDeviceDialog(
            onDismiss = { showAddDeviceDialog = false },
            onSave = {
                    name,
                    roomName,
                    type,
                    gridRow,
                    gridColumn,
                    maxOnDurationMinutes,
                    numberOfSwitches ->

                repository.addDevice(
                    floorId = floorId,
                    name = name,
                    roomName = roomName,
                    type = type,
                    gridRow = gridRow,
                    gridColumn = gridColumn,
                    maxOnDurationMinutes = maxOnDurationMinutes,
                    numberOfSwitches = numberOfSwitches,
                    onSuccess = {
                        showAddDeviceDialog = false
                        errorMessage = null
                    },
                    onError = { message ->
                        errorMessage = message
                    }
                )
            }
        )
    }

    deviceToDelete?.let { device ->
        AlertDialog(
            onDismissRequest = { deviceToDelete = null },
            title = { Text(text = "Remove ${device.name}?") },
            text = {
                Text(
                    text = "This appliance and its Firebase data will be deleted."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        repository.deleteDevice(
                            deviceId = device.id,
                            onSuccess = {
                                deviceToDelete = null
                                errorMessage = null
                            },
                            onError = { message ->
                                errorMessage = message
                            }
                        )
                    }
                ) {
                    Text(text = "Remove")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { deviceToDelete = null }
                ) {
                    Text(text = "Cancel")
                }
            }
        )
    }
}

@Composable
private fun DeviceCard(
    device: SmartDevice,
    onStatusChange: (DeviceStatus) -> Unit
) {
    val isOn = device.status == DeviceStatus.ON

    val canControl =
        device.status != DeviceStatus.ERROR &&
                device.status != DeviceStatus.DISCONNECTED

    val statusColor = when (device.status) {
        DeviceStatus.ON -> Color(0xFF1B7F3A)
        DeviceStatus.OFF -> MaterialTheme.colorScheme.onSurfaceVariant
        DeviceStatus.ERROR -> MaterialTheme.colorScheme.error
        DeviceStatus.DISCONNECTED -> Color(0xFFB45309)
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
                    text = "${formatDeviceType(device.type)} • ${device.status}",
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

                if (
                    device.type == DeviceType.IRON &&
                    device.status == DeviceStatus.ON &&
                    device.turnedOnAt != null
                ) {
                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Safety timer is active",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
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
                enabled = canControl,
                onCheckedChange = { checked ->
                    val newStatus = if (checked) {
                        DeviceStatus.ON
                    } else {
                        DeviceStatus.OFF
                    }

                    onStatusChange(newStatus)
                }
            )
        }
    }
}

@Composable
private fun MultiSwitchCard(
    device: SmartDevice,
    onMasterSwitchChange: (Boolean) -> Unit,
    onIndividualSwitchChange: (String, Boolean) -> Unit
) {
    val isMasterOn = device.switchStates.values.any { it }

    Card(
        modifier = Modifier.fillMaxWidth(),
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
            Row(
                modifier = Modifier.fillMaxWidth(),
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

                    Text(
                        text = device.roomName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Master • ${device.status}",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isMasterOn) {
                            Color(0xFF1B7F3A)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }

                Switch(
                    checked = isMasterOn,
                    onCheckedChange = onMasterSwitchChange
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            for (number in 1..device.numberOfSwitches) {
                val switchKey = "switch_$number"
                val isSwitchOn = device.switchStates[switchKey] ?: false

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Switch $number",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Switch(
                        checked = isSwitchOn,
                        onCheckedChange = { checked ->
                            onIndividualSwitchChange(
                                switchKey,
                                checked
                            )
                        }
                    )
                }
            }
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