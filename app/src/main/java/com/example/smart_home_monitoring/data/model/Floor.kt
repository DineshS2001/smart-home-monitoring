package com.example.smart_home_monitoring.data.model

data class Floor(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val imageName: String = "",
    val devices: List<SmartDevice> = emptyList()
) {
    val activeDeviceCount: Int
        get() = devices.count { device ->
            device.status == DeviceStatus.ON
        }

    val totalDeviceCount: Int
        get() = devices.size
}