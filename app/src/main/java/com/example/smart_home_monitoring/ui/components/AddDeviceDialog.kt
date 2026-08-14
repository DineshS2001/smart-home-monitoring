package com.example.smart_home_monitoring.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smart_home_monitoring.data.model.DeviceType

@Composable
fun AddDeviceDialog(
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        roomName: String,
        type: DeviceType,
        gridRow: Int,
        gridColumn: Int,
        maxOnDurationMinutes: Int,
        numberOfSwitches: Int
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var roomName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(DeviceType.OUTLET) }
    var gridRowText by remember { mutableStateOf("1") }
    var gridColumnText by remember { mutableStateOf("1") }
    var safetyMinutesText by remember { mutableStateOf("15") }
    var switchCountText by remember { mutableStateOf("3") }
    var validationMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Appliance") },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Appliance name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = roomName,
                    onValueChange = { roomName = it },
                    label = { Text("Room name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    "Appliance type",
                    fontWeight = FontWeight.Bold
                )

                DeviceType.entries.forEach { type ->
                    val selected = selectedType == type
                    if (selected) {
                        Button(
                            onClick = { selectedType = type },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(deviceTypeLabel(type)) }
                    } else {
                        OutlinedButton(
                            onClick = { selectedType = type },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(deviceTypeLabel(type)) }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = gridRowText,
                        onValueChange = { gridRowText = it.filter(Char::isDigit) },
                        label = { Text("Grid row") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = gridColumnText,
                        onValueChange = {
                            gridColumnText = it.filter(Char::isDigit)
                        },
                        label = { Text("Grid column") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (selectedType == DeviceType.IRON) {
                    OutlinedTextField(
                        value = safetyMinutesText,
                        onValueChange = {
                            safetyMinutesText = it.filter(Char::isDigit)
                        },
                        label = { Text("Safety limit in minutes") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (selectedType == DeviceType.MULTI_SWITCH) {
                    OutlinedTextField(
                        value = switchCountText,
                        onValueChange = {
                            switchCountText = it.filter(Char::isDigit)
                        },
                        label = { Text("Number of switches") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                validationMessage?.let { message ->
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val gridRow = gridRowText.toIntOrNull()
                    val gridColumn = gridColumnText.toIntOrNull()
                    val safetyMinutes = safetyMinutesText.toIntOrNull()
                    val switchCount = switchCountText.toIntOrNull()

                    validationMessage = when {
                        name.isBlank() -> "Enter an appliance name."
                        roomName.isBlank() -> "Enter a room name."
                        gridRow == null || gridRow !in 1..2 ->
                            "Grid row must be 1 or 2."
                        gridColumn == null || gridColumn !in 1..2 ->
                            "Grid column must be 1 or 2."
                        selectedType == DeviceType.IRON &&
                                (safetyMinutes == null || safetyMinutes < 1) ->
                            "Enter a valid safety limit."
                        selectedType == DeviceType.MULTI_SWITCH &&
                                (switchCount == null || switchCount !in 1..8) ->
                            "Switch count must be between 1 and 8."
                        else -> null
                    }

                    if (validationMessage == null) {
                        onSave(
                            name.trim(),
                            roomName.trim(),
                            selectedType,
                            gridRow!!,
                            gridColumn!!,
                            safetyMinutes ?: 15,
                            switchCount ?: 3
                        )
                    }
                }
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

private fun deviceTypeLabel(type: DeviceType): String {
    return when (type) {
        DeviceType.OUTLET -> "Outlet"
        DeviceType.MULTI_SWITCH -> "Multi-switch"
        DeviceType.LIGHT -> "Light"
        DeviceType.IRON -> "Iron / Safety appliance"
        DeviceType.CAMERA -> "Camera"
    }
}