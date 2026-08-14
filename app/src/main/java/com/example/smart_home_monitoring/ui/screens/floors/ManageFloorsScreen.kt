package com.example.smart_home_monitoring.ui.screens.floors

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smart_home_monitoring.data.model.Floor
import com.example.smart_home_monitoring.data.repository.FloorRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageFloorsScreen(
    onBackClick: () -> Unit
) {
    val repository = remember { FloorRepository() }
    var floors by remember { mutableStateOf<List<Floor>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var editorFloor by remember { mutableStateOf<Floor?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var floorToDelete by remember { mutableStateOf<Floor?>(null) }

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Manage Floors", fontWeight = FontWeight.Bold)
                        Text(
                            "${floors.size} floor plans",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                },
                navigationIcon = {
                    TextButton(onClick = onBackClick) { Text("Back") }
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
                Text(
                    "Create floor plans and update their names and descriptions.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = {
                        editorFloor = null
                        showEditor = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add New Floor")
                }
            }

            if (isLoading) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalArrangement = Arrangement.Center
                    ) { CircularProgressIndicator() }
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
                            "Firebase error: $errorMessage",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            items(floors, key = { it.id }) { floor ->
                FloorManagementCard(
                    floor = floor,
                    onEdit = {
                        editorFloor = floor
                        showEditor = true
                    },
                    onDelete = {
                        if (floor.totalDeviceCount > 0) {
                            errorMessage =
                                "Move or remove the ${floor.totalDeviceCount} devices on " +
                                        "${floor.name} before deleting it."
                        } else {
                            floorToDelete = floor
                        }
                    }
                )
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    if (showEditor) {
        FloorEditorDialog(
            floor = editorFloor,
            onDismiss = { showEditor = false },
            onSave = { name, description ->
                val existingFloor = editorFloor
                if (existingFloor == null) {
                    repository.addFloor(
                        name = name,
                        description = description,
                        onSuccess = { showEditor = false },
                        onError = { errorMessage = it }
                    )
                } else {
                    repository.updateFloor(
                        floorId = existingFloor.id,
                        name = name,
                        description = description,
                        onSuccess = { showEditor = false },
                        onError = { errorMessage = it }
                    )
                }
            }
        )
    }

    floorToDelete?.let { floor ->
        AlertDialog(
            onDismissRequest = { floorToDelete = null },
            title = { Text("Delete ${floor.name}?") },
            text = { Text("This empty floor plan will be removed from Firebase.") },
            confirmButton = {
                Button(
                    onClick = {
                        repository.deleteFloor(
                            floorId = floor.id,
                            onSuccess = { floorToDelete = null },
                            onError = { errorMessage = it }
                        )
                    }
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { floorToDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun FloorManagementCard(
    floor: Floor,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(
                floor.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                floor.description.ifBlank { "No description" },
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "${floor.totalDeviceCount} devices",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onEdit) { Text("Edit") }
                TextButton(onClick = onDelete) { Text("Delete") }
            }
        }
    }
}

@Composable
private fun FloorEditorDialog(
    floor: Floor?,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by remember(floor?.id) { mutableStateOf(floor?.name.orEmpty()) }
    var description by remember(floor?.id) {
        mutableStateOf(floor?.description.orEmpty())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (floor == null) "Add Floor" else "Edit Floor") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Floor name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                enabled = name.isNotBlank(),
                onClick = { onSave(name.trim(), description.trim()) }
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
