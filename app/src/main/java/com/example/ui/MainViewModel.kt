package com.example.ui

import android.annotation.SuppressLint
import android.app.Application
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.*
import com.example.data.repository.WorkplaceRepository
import com.example.util.AttendanceNotificationHelper
import com.example.util.GeoFenceUtils
import com.example.util.PayrollEngine
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class MainUiState(
    val isDarkMode: Boolean = false,
    val currentRole: UserRole = UserRole.EMPLOYEE,
    val selectedEmployee: UserEntity? = null,
    val allEmployees: List<UserEntity> = emptyList(),
    val geoFenceConfig: GeoFenceConfigEntity = GeoFenceConfigEntity(),
    // Simulated or Real GPS location
    val currentLat: Double = 37.4220,
    val currentLng: Double = -122.0841,
    val distanceToOfficeMeters: Float = 0f,
    val isInsideGeoFence: Boolean = true,
    // Attendance data
    val employeeAttendance: List<AttendanceRecordEntity> = emptyList(),
    val pendingApprovalRequests: List<AttendanceRecordEntity> = emptyList(),
    val todayAttendance: AttendanceRecordEntity? = null,
    // Deductions & Payroll
    val employeeDeductions: List<DeductionLedgerEntity> = emptyList(),
    val employeePayrolls: List<PayrollRecordEntity> = emptyList(),
    val allPayrolls: List<PayrollRecordEntity> = emptyList(),
    val companyHolidays: List<CompanyHolidayEntity> = emptyList(),
    // UI Dialogs
    val showLocationSimulator: Boolean = false,
    val showManualPunchDialog: Boolean = false,
    val showGeoFenceConfigDialog: Boolean = false,
    val showAddEmployeeDialog: Boolean = false,
    val showAddDeductionDialog: Boolean = false,
    val showManualAttendanceDialog: Boolean = false,
    val showHolidayCalendarDialog: Boolean = false,
    val selectedDateForManualEntry: String = "2026-08-01",
    val selectedPayslipForPreview: PayrollRecordEntity? = null,
    val snackbarMessage: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = WorkplaceRepository(db.appDao())

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initializeDefaultDataIfEmpty()
            observeData()
        }
    }

    private fun observeData() {
        viewModelScope.launch {
            // GeoFence Config
            repository.geoFenceConfigFlow.filterNotNull().collect { config ->
                val current = _uiState.value
                val dist = GeoFenceUtils.calculateDistanceMeters(
                    current.currentLat, current.currentLng,
                    config.latitude, config.longitude
                )
                val inside = dist <= config.radiusMeters

                _uiState.update {
                    it.copy(
                        geoFenceConfig = config,
                        distanceToOfficeMeters = dist,
                        isInsideGeoFence = inside
                    )
                }
            }
        }

        viewModelScope.launch {
            repository.allEmployees.collect { employees ->
                _uiState.update { state ->
                    val selected = if (state.selectedEmployee != null && employees.any { it.id == state.selectedEmployee.id }) {
                        state.selectedEmployee
                    } else {
                        employees.firstOrNull()
                    }
                    state.copy(
                        allEmployees = employees,
                        selectedEmployee = selected
                    )
                }
                _uiState.value.selectedEmployee?.let { loadEmployeeDetails(it.id) }
            }
        }

        viewModelScope.launch {
            repository.pendingApprovalRequests.collect { pending ->
                _uiState.update { it.copy(pendingApprovalRequests = pending) }
            }
        }

        viewModelScope.launch {
            repository.allPayrollRecords.collect { payrolls ->
                _uiState.update { it.copy(allPayrolls = payrolls) }
            }
        }

        viewModelScope.launch {
            repository.companyHolidays.collect { holidays ->
                _uiState.update { it.copy(companyHolidays = holidays) }
            }
        }
    }

    fun switchRole(role: UserRole) {
        _uiState.update { it.copy(currentRole = role) }
    }

    fun selectEmployee(employee: UserEntity) {
        _uiState.update { it.copy(selectedEmployee = employee) }
        loadEmployeeDetails(employee.id)
    }

    private fun loadEmployeeDetails(employeeId: Int) {
        viewModelScope.launch {
            repository.getAttendanceByEmployee(employeeId).collect { attendance ->
                val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                val todayRec = attendance.find { it.date == todayStr }
                _uiState.update {
                    it.copy(
                        employeeAttendance = attendance,
                        todayAttendance = todayRec
                    )
                }
            }
        }

        viewModelScope.launch {
            repository.getDeductionsByEmployee(employeeId).collect { deductions ->
                _uiState.update { it.copy(employeeDeductions = deductions) }
            }
        }

        viewModelScope.launch {
            repository.getPayrollByEmployee(employeeId).collect { payrolls ->
                _uiState.update { it.copy(employeePayrolls = payrolls) }
            }
        }
    }

    /**
     * Update Location in Simulator (for testing Geo-fencing dynamically)
     */
    fun updateSimulatedLocation(lat: Double, lng: Double) {
        val config = _uiState.value.geoFenceConfig
        val dist = GeoFenceUtils.calculateDistanceMeters(lat, lng, config.latitude, config.longitude)
        val inside = dist <= config.radiusMeters

        _uiState.update {
            it.copy(
                currentLat = lat,
                currentLng = lng,
                distanceToOfficeMeters = dist,
                isInsideGeoFence = inside
            )
        }

        // Auto Punch-In if inside and enabled, or show arrival notification reminder
        if (inside) {
            _uiState.value.selectedEmployee?.let { emp ->
                if (_uiState.value.todayAttendance?.punchInTime == null) {
                    if (config.autoPunchEnabled) {
                        performAutoPunchIn(emp.id)
                    } else {
                        AttendanceNotificationHelper.showArrivalReminderNotification(getApplication(), config.officeName)
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startLiveGpsGeofencing(context: android.content.Context) {
        if (!GeoFenceUtils.hasLocationPermissions(context)) {
            _uiState.update { it.copy(snackbarMessage = "Location permissions required for geofence tracking.") }
            return
        }
        viewModelScope.launch {
            val location = GeoFenceUtils.getCurrentLocation(context)
            if (location != null) {
                updateSimulatedLocation(location.latitude, location.longitude)
            }
            try {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                val locationRequest = LocationRequest.Builder(
                    Priority.PRIORITY_HIGH_ACCURACY, 15000L
                ).setMinUpdateIntervalMillis(5000L).build()

                val locationCallback = object : LocationCallback() {
                    override fun onLocationResult(result: LocationResult) {
                        result.lastLocation?.let { loc ->
                            updateSimulatedLocation(loc.latitude, loc.longitude)
                        }
                    }
                }
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun refreshLiveGps(context: android.content.Context) {
        if (!GeoFenceUtils.hasLocationPermissions(context)) {
            _uiState.update { it.copy(snackbarMessage = "Please grant location permissions.") }
            return
        }
        viewModelScope.launch {
            val location = GeoFenceUtils.getCurrentLocation(context)
            if (location != null) {
                updateSimulatedLocation(location.latitude, location.longitude)
                _uiState.update {
                    it.copy(snackbarMessage = "GPS Position Updated (${String.format(java.util.Locale.getDefault(), "%.4f, %.4f", location.latitude, location.longitude)})")
                }
            } else {
                _uiState.update { it.copy(snackbarMessage = "Unable to fetch GPS location. Check device GPS settings.") }
            }
        }
    }

    private fun performAutoPunchIn(employeeId: Int) {
        viewModelScope.launch {
            val state = _uiState.value
            val record = repository.processGeoFencePunch(
                employeeId = employeeId,
                currentLat = state.currentLat,
                currentLng = state.currentLng,
                isAuto = true
            )
            _uiState.update {
                it.copy(
                    todayAttendance = record,
                    snackbarMessage = "Auto Punch-In recorded at ${record.punchInTime} (Inside Geo-fence)"
                )
            }
        }
    }

    fun triggerManualPunch() {
        val emp = _uiState.value.selectedEmployee ?: return
        viewModelScope.launch {
            val state = _uiState.value
            val record = repository.processGeoFencePunch(
                employeeId = emp.id,
                currentLat = state.currentLat,
                currentLng = state.currentLng,
                isAuto = false
            )
            _uiState.update {
                it.copy(
                    todayAttendance = record,
                    snackbarMessage = "Attendance updated at ${record.punchInTime}"
                )
            }
        }
    }

    fun submitManualApprovalRequest(reason: String) {
        val emp = _uiState.value.selectedEmployee ?: return
        viewModelScope.launch {
            val state = _uiState.value
            val record = repository.submitManualPunchIn(
                employeeId = emp.id,
                currentLat = state.currentLat,
                currentLng = state.currentLng,
                reason = reason
            )
            _uiState.update {
                it.copy(
                    showManualPunchDialog = false,
                    snackbarMessage = "Manual approval request submitted to HR."
                )
            }
        }
    }

    fun approveOrRejectRequest(record: AttendanceRecordEntity, approve: Boolean) {
        viewModelScope.launch {
            val updated = record.copy(
                status = if (approve) AttendanceStatus.PRESENT.name else AttendanceStatus.REJECTED.name,
                approvedByAdmin = approve
            )
            repository.updateAttendanceRecord(updated)
            val msg = if (approve) "Approved manual punch-in for employee #${record.employeeId}" else "Rejected attendance request"
            _uiState.update { it.copy(snackbarMessage = msg) }
        }
    }

    fun saveGeoFenceSettings(officeName: String, lat: Double, lng: Double, radius: Float, autoPunch: Boolean, weeklyOffDay: String) {
        viewModelScope.launch {
            val newConfig = GeoFenceConfigEntity(
                id = 1,
                officeName = officeName,
                latitude = lat,
                longitude = lng,
                radiusMeters = radius,
                autoPunchEnabled = autoPunch,
                weeklyOffDay = weeklyOffDay
            )
            repository.saveGeoFenceConfig(newConfig)
            _uiState.update {
                it.copy(
                    showGeoFenceConfigDialog = false,
                    snackbarMessage = "Radar & Weekly Off settings updated successfully."
                )
            }
        }
    }

    fun saveEmployee(name: String, email: String, designation: String, dept: String, salary: Double, pf: Double, pt: Double, customEmployeeId: String, pfUanNo: String) {
        viewModelScope.launch {
            val finalEmail = if (email.isBlank()) {
                name.lowercase(java.util.Locale.ROOT).replace(" ", ".").replace(Regex("[^a-z0-9.]"), "") + "@workplace.local"
            } else email

            val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())

            val currentSelected = _uiState.value.selectedEmployee
            if (currentSelected != null) {
                val updatedUser = currentSelected.copy(
                    name = name,
                    email = finalEmail,
                    designation = designation.ifBlank { "Worker / Staff" },
                    department = dept.ifBlank { "General" },
                    basicSalary = salary,
                    pfPercentage = pf,
                    professionalTax = pt,
                    customEmployeeId = customEmployeeId,
                    pfUanNo = pfUanNo.ifBlank { "102293272082" }
                )
                repository.updateUser(updatedUser)
                _uiState.update {
                    it.copy(
                        selectedEmployee = updatedUser,
                        showAddEmployeeDialog = false,
                        snackbarMessage = "Worker profile updated successfully."
                    )
                }
                loadEmployeeDetails(updatedUser.id)
            } else {
                val newUser = UserEntity(
                    name = name,
                    email = finalEmail,
                    role = UserRole.EMPLOYEE.name,
                    designation = designation.ifBlank { "Worker / Staff" },
                    department = dept.ifBlank { "General" },
                    basicSalary = salary,
                    pfPercentage = pf,
                    professionalTax = pt,
                    joiningDate = todayStr,
                    customEmployeeId = customEmployeeId,
                    pfUanNo = pfUanNo.ifBlank { "102293272082" }
                )
                val insertedId = repository.insertUser(newUser)
                val createdUser = newUser.copy(id = insertedId.toInt())

                _uiState.update {
                    it.copy(
                        selectedEmployee = createdUser,
                        showAddEmployeeDialog = false,
                        snackbarMessage = "Worker profile '$name' created successfully."
                    )
                }
                loadEmployeeDetails(createdUser.id)
            }
        }
    }

    fun addDeductionOrAdvance(employeeId: Int, type: DeductionType, amount: Double, desc: String) {
        viewModelScope.launch {
            val dateStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            val deduction = DeductionLedgerEntity(
                employeeId = employeeId,
                type = type.name,
                amount = amount,
                date = dateStr,
                description = desc,
                isCleared = false
            )
            repository.addDeduction(deduction)
            _uiState.update {
                it.copy(
                    showAddDeductionDialog = false,
                    snackbarMessage = "Deduction/Advance entry recorded."
                )
            }
        }
    }

    fun setShowHolidayCalendarDialog(show: Boolean) {
        _uiState.update { it.copy(showHolidayCalendarDialog = show) }
    }

    fun addCompanyHoliday(title: String, date: String, type: String, isPaid: Boolean, desc: String) {
        viewModelScope.launch {
            val holiday = CompanyHolidayEntity(
                date = date,
                title = title,
                type = type,
                isPaidHoliday = isPaid,
                description = desc
            )
            repository.addCompanyHoliday(holiday)
            _uiState.update {
                it.copy(snackbarMessage = "Company Holiday '$title' added ($date)")
            }
        }
    }

    fun deleteCompanyHoliday(holiday: CompanyHolidayEntity) {
        viewModelScope.launch {
            repository.deleteCompanyHoliday(holiday)
            _uiState.update {
                it.copy(snackbarMessage = "Company Holiday '${holiday.title}' removed.")
            }
        }
    }

    fun generateMonthlyPayrollForAll(month: String, workingDays: Int) {
        viewModelScope.launch {
            val employees = repository.allEmployees.first()
            val holidays = _uiState.value.companyHolidays
            val paidHolidaysCount = holidays.filter { it.isPaidHoliday }.size.toDouble()
            var count = 0
            employees.forEach { emp ->
                val deductions = _uiState.value.employeeDeductions
                val advanceTotal = deductions.filter { it.type == DeductionType.ADVANCE_SALARY.name && !it.isCleared }.sumOf { it.amount }
                val messTotal = deductions.filter { it.type == DeductionType.MESS_CHARGES.name && !it.isCleared }.sumOf { it.amount }
                val lwfTotal = deductions.filter { it.type == DeductionType.LWF.name && !it.isCleared }.sumOf { it.amount }
                val esiTotal = deductions.filter { it.type == DeductionType.ESI.name && !it.isCleared }.sumOf { it.amount }
                val coLoanTotal = deductions.filter { it.type == DeductionType.CO_LOAN.name && !it.isCleared }.sumOf { it.amount }
                val tdsTotal = deductions.filter { it.type == DeductionType.TDS.name && !it.isCleared }.sumOf { it.amount }
                val perExpTotal = deductions.filter { (it.type == DeductionType.PER_EXP.name || it.type == DeductionType.OTHER.name) && !it.isCleared }.sumOf { it.amount }

                // Calculate present days from records
                val attendance = repository.getAttendanceByEmployee(emp.id).first()
                val daysPresent = attendance.count { it.status == AttendanceStatus.PRESENT.name }.toDouble().let {
                    if (it == 0.0) 20.0 else it // Default to realistic present days if newly added
                }

                repository.generatePayrollForEmployee(
                    employee = emp,
                    month = month,
                    totalWorkingDays = workingDays,
                    daysPresent = daysPresent,
                    paidHolidays = paidHolidaysCount,
                    messCharges = messTotal,
                    advanceBalance = advanceTotal,
                    lwfCharges = lwfTotal,
                    esiCharges = esiTotal,
                    coLoanInstallment = coLoanTotal,
                    tdsAmount = tdsTotal,
                    perExpAmount = perExpTotal
                )
                count++
            }

            _uiState.update {
                it.copy(
                    snackbarMessage = "Monthly Payroll generated for $count employees ($month)."
                )
            }
        }
    }

    fun setShowManualAttendanceDialog(show: Boolean, date: String = "") {
        val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        val dateToSet = if (date.isBlank()) todayStr else date
        _uiState.update {
            it.copy(
                showManualAttendanceDialog = show,
                selectedDateForManualEntry = dateToSet
            )
        }
    }

    fun saveManualAttendanceOrLeave(
        date: String,
        status: String,
        punchInTime: String?,
        punchOutTime: String?,
        leaveType: String?,
        note: String?
    ) {
        viewModelScope.launch {
            val emp = _uiState.value.selectedEmployee ?: return@launch
            repository.saveManualAttendanceOrLeave(
                employeeId = emp.id,
                date = date,
                status = status,
                punchInTime = punchInTime,
                punchOutTime = punchOutTime,
                leaveType = leaveType,
                note = note,
                approvedByAdmin = true
            )
            val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            val updatedToday = if (date == todayStr) repository.getTodayAttendance(emp.id, todayStr) else _uiState.value.todayAttendance
            _uiState.update {
                it.copy(
                    todayAttendance = updatedToday ?: it.todayAttendance,
                    showManualAttendanceDialog = false,
                    snackbarMessage = "Attendance record saved for $date ($status)"
                )
            }
        }
    }

    fun setShowLocationSimulator(show: Boolean) {
        _uiState.update { it.copy(showLocationSimulator = show) }
    }

    fun setShowManualPunchDialog(show: Boolean) {
        _uiState.update { it.copy(showManualPunchDialog = show) }
    }

    fun setShowGeoFenceConfigDialog(show: Boolean) {
        _uiState.update { it.copy(showGeoFenceConfigDialog = show) }
    }

    fun setShowAddEmployeeDialog(show: Boolean) {
        _uiState.update { it.copy(showAddEmployeeDialog = show) }
    }

    fun setShowAddDeductionDialog(show: Boolean) {
        _uiState.update { it.copy(showAddDeductionDialog = show) }
    }

    fun generateInstantPayslipForSelectedEmployee(month: String = "August 2026") {
        viewModelScope.launch {
            val emp = _uiState.value.selectedEmployee ?: return@launch
            val attendance = repository.getAttendanceByEmployee(emp.id).first()
            val presentCount = attendance.count { it.status == AttendanceStatus.PRESENT.name }.toDouble().let {
                if (it == 0.0) 1.0 else it
            }

            val deductions = _uiState.value.employeeDeductions
            val advanceTotal = deductions.filter { it.type == DeductionType.ADVANCE_SALARY.name && !it.isCleared }.sumOf { it.amount }
            val messTotal = deductions.filter { it.type == DeductionType.MESS_CHARGES.name && !it.isCleared }.sumOf { it.amount }
            val lwfTotal = deductions.filter { it.type == DeductionType.LWF.name && !it.isCleared }.sumOf { it.amount }
            val esiTotal = deductions.filter { it.type == DeductionType.ESI.name && !it.isCleared }.sumOf { it.amount }
            val coLoanTotal = deductions.filter { it.type == DeductionType.CO_LOAN.name && !it.isCleared }.sumOf { it.amount }
            val tdsTotal = deductions.filter { it.type == DeductionType.TDS.name && !it.isCleared }.sumOf { it.amount }
            val perExpTotal = deductions.filter { (it.type == DeductionType.PER_EXP.name || it.type == DeductionType.OTHER.name) && !it.isCleared }.sumOf { it.amount }

            val holidays = _uiState.value.companyHolidays
            val paidHolidaysCount = holidays.filter { it.isPaidHoliday }.size.toDouble()

            val generatedRecord = repository.generatePayrollForEmployee(
                employee = emp,
                month = month,
                totalWorkingDays = 23,
                daysPresent = presentCount,
                paidHolidays = paidHolidaysCount,
                messCharges = messTotal,
                advanceBalance = advanceTotal,
                lwfCharges = lwfTotal,
                esiCharges = esiTotal,
                coLoanInstallment = coLoanTotal,
                tdsAmount = tdsTotal,
                perExpAmount = perExpTotal
            )

            _uiState.update {
                it.copy(
                    selectedPayslipForPreview = generatedRecord,
                    snackbarMessage = "Instant Salary Slip generated for ${emp.name} ($month)!"
                )
            }
        }
    }

    fun selectPayslipForPreview(payroll: PayrollRecordEntity?) {
        _uiState.update { it.copy(selectedPayslipForPreview = payroll) }
    }

    fun toggleDarkMode() {
        _uiState.update { it.copy(isDarkMode = !it.isDarkMode) }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}
