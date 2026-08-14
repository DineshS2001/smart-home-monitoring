package com.example.smart_home_monitoring.data.model

data class SmartAlert(
    val id: String = "",
    val deviceId: String = "",
    val deviceName: String = "",
    val type: String = "",
    val title: String = "",
    val message: String = "",
    val createdAt: Long = 0L,
    val read: Boolean = false
)