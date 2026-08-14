package com.example.smart_home_monitoring.data.repository

import android.util.Log
import com.google.firebase.database.FirebaseDatabase

class FirebaseRepository {

    private val database = FirebaseDatabase.getInstance(DATABASE_URL)
    private val homeReference = database.getReference("smartHome")

    fun initializeSampleData() {
        homeReference.get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.exists()) {
                    uploadSampleData()
                } else {
                    Log.d(TAG, "Firebase sample data already exists")
                }
            }
            .addOnFailureListener { error ->
                Log.e(TAG, "Failed to check Firebase data", error)
            }
    }

    private fun uploadSampleData() {
        val sampleData = mapOf(
            "floors" to mapOf(
                "ground_floor" to mapOf(
                    "id" to "ground_floor",
                    "name" to "Ground Floor",
                    "description" to "Living room, kitchen and garage"
                ),
                "first_floor" to mapOf(
                    "id" to "first_floor",
                    "name" to "First Floor",
                    "description" to "Bedrooms and study room"
                ),
                "outdoor" to mapOf(
                    "id" to "outdoor",
                    "name" to "Outdoor Area",
                    "description" to "Garden, entrance and security"
                )
            ),
            "devices" to mapOf(
                "living_room_light" to deviceData(
                    id = "living_room_light",
                    name = "Living Room Light",
                    roomName = "Living Room",
                    floorId = "ground_floor",
                    type = "LIGHT",
                    status = "ON",
                    gridRow = 1,
                    gridColumn = 1
                ),
                "television_outlet" to deviceData(
                    id = "television_outlet",
                    name = "Television Outlet",
                    roomName = "Living Room",
                    floorId = "ground_floor",
                    type = "OUTLET",
                    status = "OFF",
                    gridRow = 1,
                    gridColumn = 2
                ),
                "kitchen_switches" to deviceData(
                    id = "kitchen_switches",
                    name = "Kitchen Switch Unit",
                    roomName = "Kitchen",
                    floorId = "ground_floor",
                    type = "MULTI_SWITCH",
                    status = "ON",
                    gridRow = 2,
                    gridColumn = 1,
                    numberOfSwitches = 3
                ),
                "clothing_iron" to deviceData(
                    id = "clothing_iron",
                    name = "Clothing Iron",
                    roomName = "Utility Room",
                    floorId = "ground_floor",
                    type = "IRON",
                    status = "OFF",
                    gridRow = 2,
                    gridColumn = 2,
                    maxOnDurationMinutes = 15
                ),
                "bedroom_light" to deviceData(
                    id = "bedroom_light",
                    name = "Main Bedroom Light",
                    roomName = "Main Bedroom",
                    floorId = "first_floor",
                    type = "LIGHT",
                    status = "OFF"
                ),
                "study_outlet" to deviceData(
                    id = "study_outlet",
                    name = "Study Room Outlet",
                    roomName = "Study Room",
                    floorId = "first_floor",
                    type = "OUTLET",
                    status = "ON"
                ),
                "upstairs_camera" to deviceData(
                    id = "upstairs_camera",
                    name = "Hallway Camera",
                    roomName = "Upstairs Hallway",
                    floorId = "first_floor",
                    type = "CAMERA",
                    status = "ON"
                ),
                "entrance_light" to deviceData(
                    id = "entrance_light",
                    name = "Entrance Light",
                    roomName = "Front Entrance",
                    floorId = "outdoor",
                    type = "LIGHT",
                    status = "ON"
                ),
                "front_camera" to deviceData(
                    id = "front_camera",
                    name = "Front Security Camera",
                    roomName = "Front Entrance",
                    floorId = "outdoor",
                    type = "CAMERA",
                    status = "ON"
                ),
                "garden_outlet" to deviceData(
                    id = "garden_outlet",
                    name = "Garden Outlet",
                    roomName = "Garden",
                    floorId = "outdoor",
                    type = "OUTLET",
                    status = "OFF"
                )
            )
        )

        homeReference.setValue(sampleData)
            .addOnSuccessListener {
                Log.d(TAG, "Firebase sample data uploaded successfully")
            }
            .addOnFailureListener { error ->
                Log.e(TAG, "Failed to upload Firebase sample data", error)
            }
    }

    private fun deviceData(
        id: String,
        name: String,
        roomName: String,
        floorId: String,
        type: String,
        status: String,
        gridRow: Int = 0,
        gridColumn: Int = 0,
        maxOnDurationMinutes: Int? = null,
        numberOfSwitches: Int = 1
    ): Map<String, Any> {
        val data = mutableMapOf<String, Any>(
            "id" to id,
            "name" to name,
            "roomName" to roomName,
            "floorId" to floorId,
            "type" to type,
            "status" to status,
            "gridRow" to gridRow,
            "gridColumn" to gridColumn,
            "numberOfSwitches" to numberOfSwitches
        )

        if (maxOnDurationMinutes != null) {
            data["maxOnDurationMinutes"] = maxOnDurationMinutes
        }

        return data
    }

    companion object {
        private const val TAG = "FirebaseRepository"

        private const val DATABASE_URL =
            "https://smart-home-monitoring-36dc7-default-rtdb.asia-southeast1.firebasedatabase.app"
    }
}