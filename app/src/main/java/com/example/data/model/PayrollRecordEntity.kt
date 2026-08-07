package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payroll_records")
data class PayrollRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val employeeId: Int,
    val employeeName: String,
    val designation: String,
    val department: String = "General",
    val month: String, // e.g., "May 2026"
    val monthDays: Int = 31,
    val totalWorkingDays: Int = 23,
    val daysPresent: Double = 23.0,
    val holidayDays: Int = 0,
    val offDays: Int = 5,
    val casualLeave: Int = 2,
    val paidLeave: Int = 0,
    val sickLeave: Int = 0,
    val leaveWithoutPay: Int = 1,
    val basicSalary: Double,
    val hraAmount: Double = 0.0,
    val specialAllowance: Double = 0.0,
    val conveyanceAllowance: Double = 0.0,
    val grossEarnings: Double,
    val pfDeduction: Double,
    val ptDeduction: Double,
    val messDeduction: Double = 0.0,
    val advanceRecovery: Double = 0.0,
    val lwfDeduction: Double = 0.0,
    val esiDeduction: Double = 0.0,
    val coLoanDeduction: Double = 0.0,
    val tdsDeduction: Double = 0.0,
    val perExpDeduction: Double = 0.0,
    val totalDeductions: Double,
    val netPayable: Double,
    val generatedDate: String,
    val status: String = "PAID",
    val pfUanNo: String = "102293272082",
    val customEmployeeId: String = ""
)
