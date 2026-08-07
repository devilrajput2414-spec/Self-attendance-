package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "geofence_config")
data class GeoFenceConfigEntity(
    @PrimaryKey val id: Int = 1,
    val officeName: String = "Tech Hub Headquarters",
    val latitude: Double = 37.4220,
    val longitude: Double = -122.0841,
    val radiusMeters: Float = 50.0f,
    val autoPunchEnabled: Boolean = true,
    val weeklyOffDay: String = "SUNDAY"
)
