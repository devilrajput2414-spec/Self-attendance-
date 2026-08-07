package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class DeductionType {
    MESS_CHARGES,
    ADVANCE_SALARY,
    LWF,
    PROF_TAX,
    ESI,
    PER_EXP,
    CO_LOAN,
    TDS,
    OTHER
}

@Entity(tableName = "deduction_ledger")
data class DeductionLedgerEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val employeeId: Int,
    val type: String,
    val amount: Double,
    val date: String,
    val description: String,
    val isCleared: Boolean = false
)
