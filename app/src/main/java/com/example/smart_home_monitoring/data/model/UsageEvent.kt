package com.example.smart_home_monitoring.data.model

data class UsageEvent(
    val id: String = "",
    val deviceId: String = "",
    val deviceName: String = "",
    val deviceType: String = "",
    val floorId: String = "",
    val previousStatus: String = "",
    val newStatus: String = "",
    val changedAt: Long = 0L
)