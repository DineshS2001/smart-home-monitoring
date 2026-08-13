package com.example.smart_home_monitoring.data.model

data class SmartDevice(
    val id: String = "",
    val name: String = "",
    val roomName: String = "",
    val floorId: String = "",
    val type: DeviceType = DeviceType.OUTLET,
    val status: DeviceStatus = DeviceStatus.OFF,
    val gridRow: Int = 0,
    val gridColumn: Int = 0,
    val maxOnDurationMinutes: Int? = null,
    val numberOfSwitches: Int = 1
)