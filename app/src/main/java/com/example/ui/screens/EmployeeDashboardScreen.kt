package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PayrollRecordEntity
import com.example.ui.MainUiState
import com.example.ui.components.AnnualLeaveTrackerCard
import com.example.ui.components.GeoFenceStatusCard
import com.example.ui.components.MainSummaryCards
import com.example.ui.theme.*
import com.example.util.PayrollEngine

@Composable
fun EmployeeDashboardScreen(
    state: MainUiState,
    pagerState: PagerState,
    onPunchInClick: () -> Unit,
    onOpenSimulatorClick: () -> Unit,
    onManualPunchClick: () -> Unit,
    onOpenGeoFenceConfig: () -> Unit,
    onRefreshGpsClick: () -> Unit,
    onOpenManualAttendanceDialog: (String) -> Unit,
    onSelectPayslip: (PayrollRecordEntity) -> Unit,
    onAddWorkerClick: () -> Unit = {},
    onEditWorkerClick: () -> Unit = {},
    onGenerateInstantPayslip: () -> Unit = {},
    onOpenHolidayCalendar: () -> Unit = {},
    onAddDeductionClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    HorizontalPager(
        state = pagerState,
        modifier = modifier.fillMaxSize()
    ) { pageIndex ->
        when (pageIndex) {
            0 -> PunchInTabContent(
                state = state,
                onPunchInClick = onPunchInClick,
                onOpenSimulatorClick = onOpenSimulatorClick,
                onManualPunchClick = onManualPunchClick,
                onOpenGeoFenceConfig = onOpenGeoFenceConfig,
                onRefreshGpsClick = onRefreshGpsClick,
                onAddWorkerClick = onAddWorkerClick,
                onEditWorkerClick = onEditWorkerClick
            )
            1 -> AttendanceLogTabContent(
                state = state,
                onOpenManualAttendanceDialog = onOpenManualAttendanceDialog,
                onOpenHolidayCalendar = onOpenHolidayCalendar
            )
            2 -> PayrollSnapshotTabContent(
                state = state,
                onGenerateInstantPayslip = onGenerateInstantPayslip,
                onAddDeductionClick = onAddDeductionClick
            )
            3 -> PayslipsTabContent(
                state = state,
                onSelectPayslip = onSelectPayslip,
                onGenerateInstantPayslip = onGenerateInstantPayslip
            )
        }
    }
}

// ==========================================
// TAB 0: PUNCH-IN & GEOFENCE RADAR
// ==========================================
@Composable
fun PunchInTabContent(
    state: MainUiState,
    onPunchInClick: () -> Unit,
    onOpenSimulatorClick: () -> Unit,
    onManualPunchClick: () -> Unit,
    onOpenGeoFenceConfig: () -> Unit,
    onRefreshGpsClick: () -> Unit,
    onAddWorkerClick: () -> Unit = {},
    onEditWorkerClick: () -> Unit = {}
) {
    val employee = state.selectedEmployee

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp)
    ) {
        // Profile Welcome Card
        item {
            if (employee != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("employee_profile_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Blue100,
                            modifier = Modifier
                                .size(50.dp)
                                .border(2.dp, Blue500, CircleShape)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = employee.name.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString("").uppercase(),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Blue600
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Self Attendance Portal",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = Slate400
                            )
                            Text(
                                text = employee.name,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = Slate900
                            )
                            Text(
                                text = "${employee.designation} • ${employee.department}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate500
                            )
                        }

                        IconButton(
                            onClick = onEditWorkerClick,
                            modifier = Modifier.testTag("edit_profile_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Profile",
                                tint = Blue600,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Emerald50
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Emerald600)
                                )
                                Text(
                                    text = "ACTIVE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    ),
                                    color = Emerald600
                                )
                            }
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("no_employee_profile_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = null,
                            modifier = Modifier.size(36.dp),
                            tint = Blue600
                        )
                        Text(
                            text = "Create Worker Profile",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Slate900
                        )
                        Text(
                            text = "Add your name and details to start marking GPS radar attendance and tracking your records.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate600,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Button(
                            onClick = onAddWorkerClick,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Blue600),
                            modifier = Modifier.testTag("add_worker_profile_card_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("+ Add Worker Profile", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Summary Cards: Total Hours & Estimated Payroll Payout
        item {
            MainSummaryCards(state = state)
        }

        // GeoFence Punch Radar Card
        item {
            GeoFenceStatusCard(
                config = state.geoFenceConfig,
                currentLat = state.currentLat,
                currentLng = state.currentLng,
                distanceMeters = state.distanceToOfficeMeters,
                isInside = state.isInsideGeoFence,
                todayRecord = state.todayAttendance,
                onPunchInClick = onPunchInClick,
                onOpenSimulatorClick = onOpenSimulatorClick,
                onManualPunchClick = onManualPunchClick,
                onOpenGeoFenceConfig = onOpenGeoFenceConfig,
                onRefreshGpsClick = onRefreshGpsClick
            )
        }

        // Today's Clock Log Summary
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("today_summary_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "TODAY'S CLOCK SUMMARY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = Slate400
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Punch In Time",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate500
                            )
                            Text(
                                text = state.todayAttendance?.punchInTime ?: "Not Clocked In",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (state.todayAttendance != null) Slate900 else Slate400
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Punch Out Time",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate500
                            )
                            Text(
                                text = state.todayAttendance?.punchOutTime ?: "--:--",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Slate900
                            )
                        }
                    }

                    HorizontalDivider(color = Slate200)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Attendance Status:",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate500
                        )

                        val status = state.todayAttendance?.status ?: "PENDING"
                        val (bgColor, textColor) = when (status) {
                            "PRESENT" -> Emerald50 to Emerald600
                            "MANUAL_PENDING" -> Amber100 to Amber600
                            else -> Slate100 to Slate600
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = bgColor
                        ) {
                            Text(
                                text = status,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = textColor
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// TAB 1: ATTENDANCE LOG & CALENDAR
// ==========================================
@Composable
fun AttendanceLogTabContent(
    state: MainUiState,
    onOpenManualAttendanceDialog: (String) -> Unit,
    onOpenHolidayCalendar: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp)
    ) {
        item {
            AttendanceCalendarView(
                records = state.employeeAttendance,
                companyHolidays = state.companyHolidays,
                weeklyOffDay = state.geoFenceConfig.weeklyOffDay,
                onDateClick = { date -> onOpenManualAttendanceDialog(date) },
                onOpenHolidayCalendar = onOpenHolidayCalendar
            )
        }

        item {
            AnnualLeaveTrackerCard(
                attendanceRecords = state.employeeAttendance,
                monthlySalary = state.selectedEmployee?.basicSalary ?: 0.0,
                annualQuotaDays = 24,
                onRequestLeaveClick = {
                    val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                    onOpenManualAttendanceDialog(today)
                }
            )
        }

        item {
            Text(
                text = "ATTENDANCE HISTORY (${state.employeeAttendance.size} RECORDS)",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = Slate400,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        if (state.employeeAttendance.isEmpty()) {
            item {
                Text(
                    text = "No attendance logs found for current month.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate500
                )
            }
        } else {
            items(state.employeeAttendance) { record ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("attendance_item_${record.id}"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (record.status == "PRESENT") Emerald50 else Amber100,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (record.status == "PRESENT") Icons.Default.CheckCircle else Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = if (record.status == "PRESENT") Emerald600 else Amber600,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = record.date,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Slate900
                                )
                                Text(
                                    text = "In: ${record.punchInTime} • Out: ${record.punchOutTime ?: "Active"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate500
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (record.status == "PRESENT") Emerald50 else Amber100
                        ) {
                            Text(
                                text = record.status,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (record.status == "PRESENT") Emerald600 else Amber600
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// TAB 2: PAYROLL SNAPSHOT & LEDGER
// ==========================================
@Composable
fun PayrollSnapshotTabContent(
    state: MainUiState,
    onGenerateInstantPayslip: () -> Unit = {},
    onAddDeductionClick: () -> Unit = {}
) {
    val employee = state.selectedEmployee

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp)
    ) {
        item {
            employee?.let { emp ->
                val presentCount = state.employeeAttendance.count { it.status == "PRESENT" }.toDouble().let {
                    if (it == 0.0) 22.0 else it
                }
                val advanceTotal = state.employeeDeductions.filter { (it.type == "ADVANCE_SALARY" || it.type == "Advance") && !it.isCleared }.sumOf { it.amount }
                val messTotal = state.employeeDeductions.filter { (it.type == "MESS_CHARGES" || it.type == "Mess") && !it.isCleared }.sumOf { it.amount }
                val lwfTotal = state.employeeDeductions.filter { it.type == "LWF" && !it.isCleared }.sumOf { it.amount }
                val esiTotal = state.employeeDeductions.filter { (it.type == "ESI" || it.type == "E.S.I.") && !it.isCleared }.sumOf { it.amount }
                val coLoanTotal = state.employeeDeductions.filter { (it.type == "CO_LOAN" || it.type == "Co Loan") && !it.isCleared }.sumOf { it.amount }
                val tdsTotal = state.employeeDeductions.filter { (it.type == "TDS" || it.type == "T.D.S. (I.T.)") && !it.isCleared }.sumOf { it.amount }
                val perExpTotal = state.employeeDeductions.filter { (it.type == "PER_EXP" || it.type == "OTHER" || it.type == "Per. Exp") && !it.isCleared }.sumOf { it.amount }

                val calculation = PayrollEngine.calculateSalary(
                    basicSalary = emp.basicSalary,
                    totalWorkingDays = 22,
                    daysPresent = presentCount,
                    pfPercentage = emp.pfPercentage,
                    ptFixed = emp.professionalTax,
                    messCharges = messTotal,
                    advanceBalance = advanceTotal,
                    lwfCharges = lwfTotal,
                    esiCharges = esiTotal,
                    coLoanInstallment = coLoanTotal,
                    tdsAmount = tdsTotal,
                    perExpAmount = perExpTotal,
                    isFullMonth = true
                )

                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "ESTIMATED PAYROLL (1 MONTH)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = Slate400
                        )
                        Text(
                            text = "August 2026",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Blue600
                        )
                    }

                    // Dark Slate Net Payable Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("salary_breakdown_card"),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = Slate900),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Estimated Net Payable (1 Month)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate400
                                )

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White.copy(alpha = 0.10f)
                                ) {
                                    Text(
                                        text = "FULL MONTH",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        ),
                                        color = Color.White
                                    )
                                }
                            }

                            Text(
                                text = PayrollEngine.formatCurrency(calculation.netPayable),
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = Color.White
                            )

                            HorizontalDivider(
                                color = Color.White.copy(alpha = 0.12f),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "PAYROLL CYCLE",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = Slate400
                                    )
                                    Text(
                                        text = "1 Full Month (Standard)",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "MONTHLY BASIC / GROSS",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = Slate400
                                    )
                                    Text(
                                        text = PayrollEngine.formatCurrency(calculation.grossEarnings),
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    // Action Buttons Row (Generate & Add Manual Deduction)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onGenerateInstantPayslip,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("instant_generate_payslip_btn"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald600)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("⚡ Generate Slip", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        OutlinedButton(
                            onClick = onAddDeductionClick,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("add_manual_deduction_btn"),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Blue600),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Blue600)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddCircle,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("➕ Add Deduction", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    // Deductions Ledger Tracker Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "DEDUCTIONS LEDGER",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = Slate400
                            )

                            DeductionRowItem(
                                badgeText = "PF",
                                badgeBg = Orange50,
                                badgeTextTint = Orange600,
                                title = "Provident Fund (${emp.pfPercentage}%)",
                                amountFormatted = "- ${PayrollEngine.formatCurrency(calculation.pfDeduction)}"
                            )

                            DeductionRowItem(
                                badgeText = "PT",
                                badgeBg = Rose100,
                                badgeTextTint = Rose600,
                                title = "Professional Tax (Fixed)",
                                amountFormatted = "- ${PayrollEngine.formatCurrency(calculation.ptDeduction)}"
                            )

                            if (calculation.messDeduction > 0) {
                                DeductionRowItem(
                                    badgeText = "MS",
                                    badgeBg = Blue50,
                                    badgeTextTint = Blue600,
                                    title = "Mess / Cafeteria Charges",
                                    amountFormatted = "- ${PayrollEngine.formatCurrency(calculation.messDeduction)}"
                                )
                            }

                            if (calculation.advanceRecovery > 0) {
                                DeductionRowItem(
                                    badgeText = "AV",
                                    badgeBg = Purple50,
                                    badgeTextTint = Purple600,
                                    title = "Advance Salary Recovery",
                                    amountFormatted = "- ${PayrollEngine.formatCurrency(calculation.advanceRecovery)}"
                                )
                            }

                            if (calculation.lwfDeduction > 0) {
                                DeductionRowItem(
                                    badgeText = "LW",
                                    badgeBg = Orange50,
                                    badgeTextTint = Orange600,
                                    title = "Labour Welfare Fund (LWF)",
                                    amountFormatted = "- ${PayrollEngine.formatCurrency(calculation.lwfDeduction)}"
                                )
                            }

                            if (calculation.esiDeduction > 0) {
                                DeductionRowItem(
                                    badgeText = "ES",
                                    badgeBg = Emerald100,
                                    badgeTextTint = Emerald600,
                                    title = "Employees' State Insurance (ESI)",
                                    amountFormatted = "- ${PayrollEngine.formatCurrency(calculation.esiDeduction)}"
                                )
                            }

                            if (calculation.coLoanDeduction > 0) {
                                DeductionRowItem(
                                    badgeText = "LN",
                                    badgeBg = Blue50,
                                    badgeTextTint = Blue600,
                                    title = "Company Loan Recovery",
                                    amountFormatted = "- ${PayrollEngine.formatCurrency(calculation.coLoanDeduction)}"
                                )
                            }

                            if (calculation.tdsDeduction > 0) {
                                DeductionRowItem(
                                    badgeText = "TX",
                                    badgeBg = Rose100,
                                    badgeTextTint = Rose600,
                                    title = "T.D.S. Income Tax",
                                    amountFormatted = "- ${PayrollEngine.formatCurrency(calculation.tdsDeduction)}"
                                )
                            }

                            if (calculation.perExpDeduction > 0) {
                                DeductionRowItem(
                                    badgeText = "PX",
                                    badgeBg = Purple50,
                                    badgeTextTint = Purple600,
                                    title = "Personal Expense / Other",
                                    amountFormatted = "- ${PayrollEngine.formatCurrency(calculation.perExpDeduction)}"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// TAB 3: ISSUED PAYSLIPS
// ==========================================
@Composable
fun PayslipsTabContent(
    state: MainUiState,
    onSelectPayslip: (PayrollRecordEntity) -> Unit,
    onGenerateInstantPayslip: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp)
    ) {
        item {
            Text(
                text = "ISSUED PAYSLIPS (${state.employeePayrolls.size})",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = Slate400
            )
        }

        if (state.employeePayrolls.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("empty_payslips_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            modifier = Modifier.size(36.dp),
                            tint = Blue600
                        )
                        Text(
                            text = "Instant Salary Slip Generator",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Slate900
                        )
                        Text(
                            text = "Even if you worked just 1 day, click below to instantly generate and view your official salary slip now.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate600,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Button(
                            onClick = onGenerateInstantPayslip,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                            modifier = Modifier.testTag("generate_instant_payslip_empty_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("⚡ Generate Instant Salary Slip Now", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            items(state.employeePayrolls) { payroll ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("payslip_item_${payroll.id}"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Payslip - ${payroll.month}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Slate900
                            )
                            Text(
                                text = "Net Deposit: ${PayrollEngine.formatCurrency(payroll.netPayable)}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Emerald600
                                )
                            )
                        }

                        Button(
                            onClick = { onSelectPayslip(payroll) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Blue600),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("view_payslip_btn_${payroll.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "View Slip", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DeductionRowItem(
    badgeText: String,
    badgeBg: Color,
    badgeTextTint: Color,
    title: String,
    amountFormatted: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = badgeBg,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = badgeTextTint
                    )
                }
            }

            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = Slate700
            )
        }

        Text(
            text = amountFormatted,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Rose600
            )
        )
    }
}
