package com.example.smart_home_monitoring.data.repository

import com.example.smart_home_monitoring.data.model.DeviceStatus
import com.example.smart_home_monitoring.data.model.Floor
import com.example.smart_home_monitoring.data.model.SmartDevice
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FloorRepository {

    private val homeReference = FirebaseDatabase
        .getInstance(DATABASE_URL)
        .getReference("smartHome")

    private val floorsReference = homeReference.child("floors")

    fun observeFloors(
        onFloorsChanged: (List<Floor>) -> Unit,
        onError: (String) -> Unit
    ): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val devicesByFloor = snapshot.child("devices")
                    .children
                    .mapNotNull { it.toFloorDevice() }
                    .groupBy { it.floorId }

                val floors = snapshot.child("floors")
                    .children
                    .mapNotNull { floorSnapshot ->
                        val id = floorSnapshot.child("id")
                            .getValue(String::class.java)
                            ?: floorSnapshot.key
                            ?: return@mapNotNull null

                        Floor(
                            id = id,
                            name = floorSnapshot.child("name")
                                .getValue(String::class.java)
                                ?: "Unnamed Floor",
                            description = floorSnapshot.child("description")
                                .getValue(String::class.java)
                                ?: "",
                            imageName = floorSnapshot.child("imageName")
                                .getValue(String::class.java)
                                ?: "",
                            devices = devicesByFloor[id].orEmpty()
                        )
                    }
                    .sortedBy { it.name.lowercase() }

                onFloorsChanged(floors)
            }

            override fun onCancelled(error: DatabaseError) {
                onError(error.message)
            }
        }

        homeReference.addValueEventListener(listener)
        return listener
    }

    fun removeFloorListener(listener: ValueEventListener) {
        homeReference.removeEventListener(listener)
    }

    fun addFloor(
        name: String,
        description: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val id = floorsReference.push().key
        if (id == null) {
            onError("Unable to create a floor ID")
            return
        }

        floorsReference.child(id).setValue(
            mapOf(
                "id" to id,
                "name" to name.trim(),
                "description" to description.trim()
            )
        ).addOnSuccessListener {
            onSuccess()
        }.addOnFailureListener { error ->
            onError(error.message ?: "Unable to add floor")
        }
    }

    fun updateFloor(
        floorId: String,
        name: String,
        description: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        floorsReference.child(floorId).updateChildren(
            mapOf(
                "name" to name.trim(),
                "description" to description.trim()
            )
        ).addOnSuccessListener {
            onSuccess()
        }.addOnFailureListener { error ->
            onError(error.message ?: "Unable to update floor")
        }
    }

    fun deleteFloor(
        floorId: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        floorsReference.child(floorId).removeValue()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { error ->
                onError(error.message ?: "Unable to delete floor")
            }
    }

    private fun DataSnapshot.toFloorDevice(): SmartDevice? {
        val floorId = child("floorId").getValue(String::class.java)
            ?: return null
        val statusText = child("status").getValue(String::class.java) ?: "OFF"
        val status = runCatching { DeviceStatus.valueOf(statusText) }
            .getOrDefault(DeviceStatus.ERROR)

        return SmartDevice(
            id = child("id").getValue(String::class.java) ?: key.orEmpty(),
            name = child("name").getValue(String::class.java) ?: "Unknown Device",
            floorId = floorId,
            status = status
        )
    }

    companion object {
        private const val DATABASE_URL =
            "https://smart-home-monitoring-36dc7-default-rtdb.asia-southeast1.firebasedatabase.app"
    }
}
