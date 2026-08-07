package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole {
    ADMIN, EMPLOYEE
}

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val email: String,
    val role: String = UserRole.EMPLOYEE.name,
    val designation: String,
    val department: String,
    val basicSalary: Double,
    val pfPercentage: Double = 12.0,
    val professionalTax: Double = 200.0,
    val joiningDate: String = "2024-01-15",
    val phoneNumber: String = "",
    val customEmployeeId: String = "",
    val pfUanNo: String = "102293272082"
)
