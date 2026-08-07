package com.example.data.dao

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // Users
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: Int): UserEntity?

    @Query("SELECT * FROM users WHERE phoneNumber = :phone LIMIT 1")
    suspend fun getUserByPhoneNumber(phone: String): UserEntity?

    @Query("SELECT * FROM users WHERE role = 'EMPLOYEE'")
    fun getAllEmployees(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("DELETE FROM users WHERE email LIKE '%@company.com' OR name IN ('Alex Morgan', 'David Chen', 'Emily Watson', 'Vikram Patel', 'Sarah Jenkins')")
    suspend fun deleteSeedProfiles()

    @Query("DELETE FROM payroll_records WHERE employeeName IN ('Alex Morgan', 'David Chen', 'Emily Watson', 'Vikram Patel', 'Sarah Jenkins')")
    suspend fun deleteSeedPayroll()

    // GeoFence
    @Query("SELECT * FROM geofence_config WHERE id = 1")
    fun getGeoFenceConfigFlow(): Flow<GeoFenceConfigEntity?>

    @Query("SELECT * FROM geofence_config WHERE id = 1")
    suspend fun getGeoFenceConfig(): GeoFenceConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveGeoFenceConfig(config: GeoFenceConfigEntity)

    // Attendance
    @Query("SELECT * FROM attendance_records ORDER BY date DESC, id DESC")
    fun getAllAttendanceRecords(): Flow<List<AttendanceRecordEntity>>

    @Query("SELECT * FROM attendance_records WHERE employeeId = :employeeId ORDER BY date DESC")
    fun getAttendanceByEmployee(employeeId: Int): Flow<List<AttendanceRecordEntity>>

    @Query("SELECT * FROM attendance_records WHERE employeeId = :employeeId AND date = :date LIMIT 1")
    suspend fun getTodayAttendance(employeeId: Int, date: String): AttendanceRecordEntity?

    @Query("SELECT * FROM attendance_records WHERE status = 'MANUAL_PENDING' ORDER BY date DESC")
    fun getPendingApprovalRequests(): Flow<List<AttendanceRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(record: AttendanceRecordEntity): Long

    @Update
    suspend fun updateAttendance(record: AttendanceRecordEntity)

    // Deductions
    @Query("SELECT * FROM deduction_ledger WHERE employeeId = :employeeId ORDER BY date DESC")
    fun getDeductionsByEmployee(employeeId: Int): Flow<List<DeductionLedgerEntity>>

    @Query("SELECT * FROM deduction_ledger ORDER BY date DESC")
    fun getAllDeductions(): Flow<List<DeductionLedgerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeduction(deduction: DeductionLedgerEntity): Long

    @Query("UPDATE deduction_ledger SET isCleared = 1 WHERE employeeId = :employeeId AND isCleared = 0")
    suspend fun markDeductionsClearedForEmployee(employeeId: Int)

    // Payroll
    @Query("SELECT * FROM payroll_records ORDER BY id DESC")
    fun getAllPayrollRecords(): Flow<List<PayrollRecordEntity>>

    @Query("SELECT * FROM payroll_records WHERE employeeId = :employeeId ORDER BY id DESC")
    fun getPayrollByEmployee(employeeId: Int): Flow<List<PayrollRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayroll(payroll: PayrollRecordEntity): Long

    // Company Holidays
    @Query("SELECT * FROM company_holidays ORDER BY date ASC")
    fun getAllCompanyHolidays(): Flow<List<CompanyHolidayEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompanyHoliday(holiday: CompanyHolidayEntity): Long

    @Delete
    suspend fun deleteCompanyHoliday(holiday: CompanyHolidayEntity)

    @Query("SELECT * FROM company_holidays WHERE date LIKE :monthPrefix || '%'")
    suspend fun getHolidaysForMonth(monthPrefix: String): List<CompanyHolidayEntity>
}
