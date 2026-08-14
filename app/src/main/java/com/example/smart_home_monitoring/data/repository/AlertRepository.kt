package com.example.smart_home_monitoring.data.repository

import com.example.smart_home_monitoring.data.model.SmartAlert
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AlertRepository {

    private val database = FirebaseDatabase.getInstance(DATABASE_URL)
    private val alertsReference =
        database.getReference("smartHome").child("alerts")

    fun observeAlerts(
        onAlertsChanged: (List<SmartAlert>) -> Unit,
        onError: (String) -> Unit
    ): ValueEventListener {
        val query = alertsReference
            .orderByChild("createdAt")
            .limitToLast(50)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val alerts = snapshot.children
                    .mapNotNull { alertSnapshot ->
                        alertSnapshot.toSmartAlert()
                    }
                    .sortedByDescending { alert ->
                        alert.createdAt
                    }

                onAlertsChanged(alerts)
            }

            override fun onCancelled(error: DatabaseError) {
                onError(error.message)
            }
        }

        query.addValueEventListener(listener)
        return listener
    }

    fun removeAlertListener(listener: ValueEventListener) {
        alertsReference.removeEventListener(listener)
    }

    fun markAsRead(
        alertId: String,
        onError: (String) -> Unit = {}
    ) {
        alertsReference
            .child(alertId)
            .child("read")
            .setValue(true)
            .addOnFailureListener { error ->
                onError(error.message ?: "Unable to update alert")
            }
    }

    private fun DataSnapshot.toSmartAlert(): SmartAlert? {
        val alertId = key ?: return null

        return SmartAlert(
            id = alertId,
            deviceId = child("deviceId").getValue(String::class.java) ?: "",
            deviceName = child("deviceName").getValue(String::class.java) ?: "",
            type = child("type").getValue(String::class.java) ?: "",
            title = child("title").getValue(String::class.java) ?: "Alert",
            message = child("message").getValue(String::class.java) ?: "",
            createdAt = child("createdAt").getValue(Long::class.java) ?: 0L,
            read = child("read").getValue(Boolean::class.java) ?: false
        )
    }

    companion object {
        private const val DATABASE_URL =
            "https://smart-home-monitoring-36dc7-default-rtdb.asia-southeast1.firebasedatabase.app"
    }
}