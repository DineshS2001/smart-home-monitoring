package com.example.smart_home_monitoring.data.repository

import com.example.smart_home_monitoring.data.model.UsageEvent
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UsageRepository {

    private val database = FirebaseDatabase.getInstance(DATABASE_URL)

    private val usageEventsReference =
        database.getReference("smartHome").child("usageEvents")

    fun observeUsageEvents(
        onEventsChanged: (List<UsageEvent>) -> Unit,
        onError: (String) -> Unit
    ): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val events = snapshot.children
                    .mapNotNull { eventSnapshot ->
                        eventSnapshot.toUsageEvent()
                    }
                    .sortedByDescending { event ->
                        event.changedAt
                    }
                    .take(100)

                onEventsChanged(events)
            }

            override fun onCancelled(error: DatabaseError) {
                onError(error.message)
            }
        }

        usageEventsReference.addValueEventListener(listener)
        return listener
    }

    fun removeUsageListener(listener: ValueEventListener) {
        usageEventsReference.removeEventListener(listener)
    }

    private fun DataSnapshot.toUsageEvent(): UsageEvent? {
        val eventId = key ?: return null

        return UsageEvent(
            id = eventId,
            deviceId =
                child("deviceId").getValue(String::class.java) ?: "",
            deviceName =
                child("deviceName").getValue(String::class.java)
                    ?: "Unknown Device",
            deviceType =
                child("deviceType").getValue(String::class.java) ?: "",
            floorId =
                child("floorId").getValue(String::class.java) ?: "",
            previousStatus =
                child("previousStatus").getValue(String::class.java)
                    ?: "",
            newStatus =
                child("newStatus").getValue(String::class.java) ?: "",
            changedAt =
                child("changedAt").getValue(Long::class.java) ?: 0L
        )
    }

    companion object {
        private const val DATABASE_URL =
            "https://smart-home-monitoring-36dc7-default-rtdb." +
                    "asia-southeast1.firebasedatabase.app"
    }
}