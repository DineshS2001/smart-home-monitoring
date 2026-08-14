package com.example.smart_home_monitoring.data.repository

import com.example.smart_home_monitoring.data.model.DeviceStatus
import com.example.smart_home_monitoring.data.model.DeviceType
import com.example.smart_home_monitoring.data.model.SmartDevice
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DeviceRealtimeRepository {

    private val database = FirebaseDatabase.getInstance(DATABASE_URL)
    private val devicesReference =
        database.getReference("smartHome").child("devices")

    fun observeDevicesForFloor(
        floorId: String,
        onDevicesChanged: (List<SmartDevice>) -> Unit,
        onError: (String) -> Unit
    ): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val devices = snapshot.children
                    .mapNotNull { deviceSnapshot ->
                        deviceSnapshot.toSmartDevice()
                    }
                    .filter { device ->
                        device.floorId == floorId
                    }
                    .sortedBy { device ->
                        device.name
                    }

                onDevicesChanged(devices)
            }

            override fun onCancelled(error: DatabaseError) {
                onError(error.message)
            }
        }

        devicesReference.addValueEventListener(listener)
        return listener
    }

    fun removeDeviceListener(listener: ValueEventListener) {
        devicesReference.removeEventListener(listener)
    }

    fun updateDeviceStatus(
        deviceId: String,
        status: DeviceStatus,
        onError: (String) -> Unit = {}
    ) {
        devicesReference
            .child(deviceId)
            .child("status")
            .setValue(status.name)
            .addOnFailureListener { error ->
                onError(error.message ?: "Unable to update device")
            }
    }

    private fun DataSnapshot.toSmartDevice(): SmartDevice? {
        val id = child("id").getValue(String::class.java)
            ?: key
            ?: return null

        val typeText =
            child("type").getValue(String::class.java) ?: "OUTLET"

        val statusText =
            child("status").getValue(String::class.java) ?: "OFF"

        val type = runCatching {
            DeviceType.valueOf(typeText)
        }.getOrDefault(DeviceType.OUTLET)

        val status = runCatching {
            DeviceStatus.valueOf(statusText)
        }.getOrDefault(DeviceStatus.ERROR)

        return SmartDevice(
            id = id,
            name = child("name").getValue(String::class.java) ?: "Unknown Device",
            roomName = child("roomName").getValue(String::class.java) ?: "",
            floorId = child("floorId").getValue(String::class.java) ?: "",
            type = type,
            status = status,
            gridRow = child("gridRow").getValue(Long::class.java)?.toInt() ?: 0,
            gridColumn = child("gridColumn").getValue(Long::class.java)?.toInt() ?: 0,
            maxOnDurationMinutes = child("maxOnDurationMinutes")
                .getValue(Long::class.java)
                ?.toInt(),
            numberOfSwitches = child("numberOfSwitches")
                .getValue(Long::class.java)
                ?.toInt()
                ?: 1
        )
    }

    companion object {
        private const val DATABASE_URL =
            "https://smart-home-monitoring-36dc7-default-rtdb.asia-southeast1.firebasedatabase.app"
    }
}