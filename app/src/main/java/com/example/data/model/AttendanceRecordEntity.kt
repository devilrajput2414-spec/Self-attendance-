package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AttendanceStatus {
    PRESENT,
    ABSENT,
    LEAVE,
    WEEKLY_OFF,
    HOLIDAY,
    HALF_DAY,
    MANUAL_PENDING,
    REJECTED
}

@Entity(tableName = "attendance_records")
data class AttendanceRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val employeeId: Int,
    val date: String, // e.g., "2026-08-01"
    val punchInTime: String? = null,
    val punchOutTime: String? = null,
    val punchInLat: Double? = null,
    val punchInLng: Double? = null,
    val status: String = AttendanceStatus.PRESENT.name,
    val distanceFromOfficeMeters: Float? = null,
    val manualReason: String? = null,
    val leaveType: String? = null, // e.g. "Casual Leave", "Sick Leave", "Paid Leave", "National Holiday", "Sunday Off"
    val note: String? = null,
    val approvedByAdmin: Boolean = false
)
