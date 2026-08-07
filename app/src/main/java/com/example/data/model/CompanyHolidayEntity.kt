package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "company_holidays")
data class CompanyHolidayEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // format: "yyyy-MM-dd" e.g. "2026-08-15"
    val title: String, // e.g. "Independence Day"
    val type: String = "National Holiday", // "National Holiday", "Festival Off", "Company Event", "Public Holiday"
    val isPaidHoliday: Boolean = true,
    val description: String = ""
)
