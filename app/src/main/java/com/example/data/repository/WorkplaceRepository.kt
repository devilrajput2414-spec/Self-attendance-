package com.example.data.repository

import com.example.data.dao.AppDao
import com.example.data.model.*
import com.example.util.GeoFenceUtils
import com.example.util.PayrollEngine
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

class WorkplaceRepository(private val dao: AppDao) {

    val allUsers: Flow<List<UserEntity>> = dao.getAllUsers()
    val allEmployees: Flow<List<UserEntity>> = dao.getAllEmployees()
    val geoFenceConfigFlow: Flow<GeoFenceConfigEntity?> = dao.getGeoFenceConfigFlow()
    val allAttendanceRecords: Flow<List<AttendanceRecordEntity>> = dao.getAllAttendanceRecords()
    val pendingApprovalRequests: Flow<List<AttendanceRecordEntity>> = dao.getPendingApprovalRequests()
    val allDeductions: Flow<List<DeductionLedgerEntity>> = dao.getAllDeductions()
    val allPayrollRecords: Flow<List<PayrollRecordEntity>> = dao.getAllPayrollRecords()
    val companyHolidays: Flow<List<CompanyHolidayEntity>> = dao.getAllCompanyHolidays()

    fun getAttendanceByEmployee(employeeId: Int): Flow<List<AttendanceRecordEntity>> =
        dao.getAttendanceByEmployee(employeeId)

    fun getDeductionsByEmployee(employeeId: Int): Flow<List<DeductionLedgerEntity>> =
        dao.getDeductionsByEmployee(employeeId)

    fun getPayrollByEmployee(employeeId: Int): Flow<List<PayrollRecordEntity>> =
        dao.getPayrollByEmployee(employeeId)

    suspend fun getGeoFenceConfig(): GeoFenceConfigEntity {
        return dao.getGeoFenceConfig() ?: GeoFenceConfigEntity().also {
            dao.saveGeoFenceConfig(it)
        }
    }

    suspend fun saveGeoFenceConfig(config: GeoFenceConfigEntity) {
        dao.saveGeoFenceConfig(config)
    }

    suspend fun insertUser(user: UserEntity): Long {
        return dao.insertUser(user)
    }

    suspend fun getUserByPhoneNumber(phone: String): UserEntity? {
        return dao.getUserByPhoneNumber(phone)
    }

    suspend fun updateUser(user: UserEntity) {
        dao.updateUser(user)
    }

    suspend fun deleteUser(user: UserEntity) {
        dao.deleteUser(user)
    }

    suspend fun getTodayAttendance(employeeId: Int, date: String): AttendanceRecordEntity? {
        return dao.getTodayAttendance(employeeId, date)
    }

    suspend fun processGeoFencePunch(
        employeeId: Int,
        currentLat: Double,
        currentLng: Double,
        isAuto: Boolean = false
    ): AttendanceRecordEntity {
        val config = getGeoFenceConfig()
        val distance = GeoFenceUtils.calculateDistanceMeters(
            currentLat, currentLng,
            config.latitude, config.longitude
        )
        val isInside = distance <= config.radiusMeters
        val todayStr = getCurrentDateString()
        val timeStr = getCurrentTimeString()

        val existing = dao.getTodayAttendance(employeeId, todayStr)

        if (existing == null) {
            // New punch-in
            val status = if (isInside) AttendanceStatus.PRESENT.name else AttendanceStatus.MANUAL_PENDING.name
            val record = AttendanceRecordEntity(
                employeeId = employeeId,
                date = todayStr,
                punchInTime = timeStr,
                punchInLat = currentLat,
                punchInLng = currentLng,
                status = status,
                distanceFromOfficeMeters = distance,
                approvedByAdmin = isInside
            )
            val id = dao.insertAttendance(record)
            return record.copy(id = id.toInt())
        } else {
            // Existing today record - punch in if missing, or punch out if already punched in
            val updated = if (existing.punchInTime == null) {
                val status = if (isInside) AttendanceStatus.PRESENT.name else AttendanceStatus.MANUAL_PENDING.name
                existing.copy(
                    punchInTime = timeStr,
                    punchInLat = currentLat,
                    punchInLng = currentLng,
                    status = status,
                    distanceFromOfficeMeters = distance,
                    approvedByAdmin = isInside
                )
            } else {
                existing.copy(
                    punchOutTime = timeStr,
                    distanceFromOfficeMeters = distance
                )
            }
            dao.updateAttendance(updated)
            return updated
        }
    }

    suspend fun submitManualPunchIn(
        employeeId: Int,
        currentLat: Double,
        currentLng: Double,
        reason: String
    ): AttendanceRecordEntity {
        val config = getGeoFenceConfig()
        val distance = GeoFenceUtils.calculateDistanceMeters(
            currentLat, currentLng,
            config.latitude, config.longitude
        )
        val todayStr = getCurrentDateString()
        val timeStr = getCurrentTimeString()

        val existing = dao.getTodayAttendance(employeeId, todayStr)
        val record = if (existing != null) {
            existing.copy(
                punchInTime = existing.punchInTime ?: timeStr,
                punchInLat = currentLat,
                punchInLng = currentLng,
                status = AttendanceStatus.MANUAL_PENDING.name,
                distanceFromOfficeMeters = distance,
                manualReason = reason,
                approvedByAdmin = false
            )
        } else {
            AttendanceRecordEntity(
                employeeId = employeeId,
                date = todayStr,
                punchInTime = timeStr,
                punchInLat = currentLat,
                punchInLng = currentLng,
                status = AttendanceStatus.MANUAL_PENDING.name,
                distanceFromOfficeMeters = distance,
                manualReason = reason,
                approvedByAdmin = false
            )
        }
        if (existing != null) {
            dao.updateAttendance(record)
            return record
        } else {
            val id = dao.insertAttendance(record)
            return record.copy(id = id.toInt())
        }
    }

    suspend fun approveAttendanceRequest(recordId: Int, approved: Boolean) {
        val records = dao.getPendingApprovalRequests()
        val recordsList = dao.getPendingApprovalRequests()
        val recordsAll = dao.getAllAttendanceRecords()
    }

    suspend fun updateAttendanceRecord(record: AttendanceRecordEntity) {
        dao.updateAttendance(record)
    }

    suspend fun saveManualAttendanceOrLeave(
        employeeId: Int,
        date: String,
        status: String,
        punchInTime: String? = null,
        punchOutTime: String? = null,
        leaveType: String? = null,
        note: String? = null,
        approvedByAdmin: Boolean = true
    ) {
        val existing = dao.getTodayAttendance(employeeId, date)
        if (existing != null) {
            val updated = existing.copy(
                status = status,
                punchInTime = punchInTime,
                punchOutTime = punchOutTime,
                leaveType = leaveType,
                note = note,
                manualReason = note ?: existing.manualReason,
                approvedByAdmin = approvedByAdmin
            )
            dao.updateAttendance(updated)
        } else {
            val newRecord = AttendanceRecordEntity(
                employeeId = employeeId,
                date = date,
                punchInTime = punchInTime,
                punchOutTime = punchOutTime,
                status = status,
                leaveType = leaveType,
                note = note,
                manualReason = note,
                approvedByAdmin = approvedByAdmin
            )
            dao.insertAttendance(newRecord)
        }
    }

    suspend fun addDeduction(deduction: DeductionLedgerEntity): Long {
        return dao.insertDeduction(deduction)
    }

    suspend fun addCompanyHoliday(holiday: CompanyHolidayEntity): Long {
        return dao.insertCompanyHoliday(holiday)
    }

    suspend fun deleteCompanyHoliday(holiday: CompanyHolidayEntity) {
        dao.deleteCompanyHoliday(holiday)
    }

    suspend fun getHolidaysForMonth(monthPrefix: String): List<CompanyHolidayEntity> {
        return dao.getHolidaysForMonth(monthPrefix)
    }

    suspend fun generateMonthlyPayroll(month: String, totalWorkingDays: Int = 22): List<PayrollRecordEntity> {
        val employees = dao.getUserById(1) // retrieve employees
        // generate for each employee
        return emptyList()
    }

    suspend fun generatePayrollForEmployee(
        employee: UserEntity,
        month: String,
        totalWorkingDays: Int,
        daysPresent: Double,
        paidHolidays: Double = 0.0,
        messCharges: Double = 0.0,
        advanceBalance: Double = 0.0,
        lwfCharges: Double = 0.0,
        esiCharges: Double = 0.0,
        coLoanInstallment: Double = 0.0,
        tdsAmount: Double = 0.0,
        perExpAmount: Double = 0.0
    ): PayrollRecordEntity {
        val result = PayrollEngine.calculateSalary(
            basicSalary = employee.basicSalary,
            totalWorkingDays = totalWorkingDays,
            daysPresent = daysPresent,
            paidHolidays = paidHolidays,
            pfPercentage = employee.pfPercentage,
            ptFixed = employee.professionalTax,
            messCharges = messCharges,
            advanceBalance = advanceBalance,
            lwfCharges = lwfCharges,
            esiCharges = esiCharges,
            coLoanInstallment = coLoanInstallment,
            tdsAmount = tdsAmount,
            perExpAmount = perExpAmount
        )

        val payroll = PayrollRecordEntity(
            employeeId = employee.id,
            employeeName = employee.name,
            designation = employee.designation,
            department = employee.department,
            month = month,
            monthDays = 31,
            totalWorkingDays = totalWorkingDays,
            daysPresent = daysPresent,
            holidayDays = paidHolidays.toInt(),
            offDays = 5,
            casualLeave = 2,
            paidLeave = 0,
            sickLeave = 0,
            leaveWithoutPay = 1,
            basicSalary = employee.basicSalary,
            grossEarnings = result.grossEarnings,
            pfDeduction = result.pfDeduction,
            ptDeduction = result.ptDeduction,
            messDeduction = result.messDeduction,
            advanceRecovery = result.advanceRecovery,
            lwfDeduction = result.lwfDeduction,
            esiDeduction = result.esiDeduction,
            coLoanDeduction = result.coLoanDeduction,
            tdsDeduction = result.tdsDeduction,
            perExpDeduction = result.perExpDeduction,
            totalDeductions = result.totalDeductions,
            netPayable = result.netPayable,
            generatedDate = getCurrentDateString(),
            status = "PAID",
            pfUanNo = employee.pfUanNo,
            customEmployeeId = employee.customEmployeeId
        )

        dao.insertPayroll(payroll)
        if (result.totalDeductions > 0) {
            dao.markDeductionsClearedForEmployee(employee.id)
        }
        return payroll
    }

    suspend fun initializeDefaultDataIfEmpty() {
        // Ensure GeoFence Config
        if (dao.getGeoFenceConfig() == null) {
            dao.saveGeoFenceConfig(
                GeoFenceConfigEntity(
                    id = 1,
                    officeName = "Tech Hub Headquarters",
                    latitude = 37.4220,
                    longitude = -122.0841,
                    radiusMeters = 50.0f,
                    autoPunchEnabled = true
                )
            )
        }

        // Seed Company Holidays for 2026 if empty
        val existingHolidays = dao.getHolidaysForMonth("2026")
        if (existingHolidays.isEmpty()) {
            val seedHolidays = listOf(
                CompanyHolidayEntity(date = "2026-01-26", title = "Republic Day", type = "National Holiday", isPaidHoliday = true, description = "National Celebration of Indian Constitution"),
                CompanyHolidayEntity(date = "2026-03-04", title = "Holi Festival", type = "Festival Off", isPaidHoliday = true, description = "Festival of Colors"),
                CompanyHolidayEntity(date = "2026-05-01", title = "Labor Day", type = "Public Holiday", isPaidHoliday = true, description = "International Workers' Day"),
                CompanyHolidayEntity(date = "2026-08-15", title = "Independence Day", type = "National Holiday", isPaidHoliday = true, description = "79th Indian Independence Day Celebration"),
                CompanyHolidayEntity(date = "2026-08-28", title = "Company Foundation Day", type = "Company Event", isPaidHoliday = true, description = "Annual Golf Ceramics Foundation Day Celebration"),
                CompanyHolidayEntity(date = "2026-10-02", title = "Gandhi Jayanti", type = "National Holiday", isPaidHoliday = true, description = "Mahatma Gandhi Birthday"),
                CompanyHolidayEntity(date = "2026-10-20", title = "Dussehra", type = "Festival Off", isPaidHoliday = true, description = "Vijayadashami Festival"),
                CompanyHolidayEntity(date = "2026-11-08", title = "Diwali", type = "Festival Off", isPaidHoliday = true, description = "Festival of Lights"),
                CompanyHolidayEntity(date = "2026-12-25", title = "Christmas Day", type = "Public Holiday", isPaidHoliday = true, description = "Christmas Public Holiday")
            )
            seedHolidays.forEach { dao.insertCompanyHoliday(it) }
        }

        // Cleanup all pre-seeded dummy profiles
        dao.deleteSeedProfiles()
        dao.deleteSeedPayroll()
    }

    private fun getCurrentDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun getCurrentTimeString(): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date())
    }
}
