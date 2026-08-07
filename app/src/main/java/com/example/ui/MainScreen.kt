package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.UserRole
import com.example.ui.components.*
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.EmployeeDashboardScreen
import com.example.ui.theme.*
import kotlinx.coroutines.launch

private data class NavTabItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val context = androidx.compose.ui.platform.LocalContext.current

    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.any { it }) {
            viewModel.startLiveGpsGeofencing(context)
        }
    }

    LaunchedEffect(Unit) {
        if (com.example.util.GeoFenceUtils.hasLocationPermissions(context)) {
            viewModel.startLiveGpsGeofencing(context)
        } else {
            permissionLauncher.launch(com.example.util.GeoFenceUtils.getRequiredLocationPermissions())
        }
    }

    val pagerState = rememberPagerState(pageCount = { 4 })
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    val navItems = remember {
        listOf(
            NavTabItem("Punch-In", Icons.Default.Home, Icons.Outlined.Home),
            NavTabItem("Attendance", Icons.Default.CalendarMonth, Icons.Outlined.CalendarMonth),
            NavTabItem("Payroll", Icons.Default.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet),
            NavTabItem("Payslips", Icons.Default.ReceiptLong, Icons.Outlined.ReceiptLong)
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Blue600
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier
                                    .padding(6.dp)
                                    .size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Self Attendance",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black)
                            )
                            Text(
                                text = "WorkPulse GPS Portal",
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate500
                            )
                        }
                    }
                },
                actions = {
                    // Dark Mode / Light Mode Theme Toggle
                    IconButton(
                        onClick = { viewModel.toggleDarkMode() },
                        modifier = Modifier.testTag("theme_switcher_btn")
                    ) {
                        Icon(
                            imageVector = if (state.isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Theme",
                            tint = if (state.isDarkMode) Amber600 else Slate700
                        )
                    }

                    // Employee Profile Selection Dropdown & Add Worker Action
                    var expandedMenu by remember { mutableStateOf(false) }

                    Box(modifier = Modifier.padding(end = 8.dp)) {
                        OutlinedButton(
                            onClick = { expandedMenu = true },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("select_employee_profile_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Blue600
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = state.selectedEmployee?.name ?: "+ Add Worker",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Slate900
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = Slate500
                            )
                        }

                        DropdownMenu(
                            expanded = expandedMenu,
                            onDismissRequest = { expandedMenu = false }
                        ) {
                            state.allEmployees.forEach { emp ->
                                DropdownMenuItem(
                                    text = { Text("${emp.name} (${emp.designation})") },
                                    onClick = {
                                        viewModel.selectEmployee(emp)
                                        expandedMenu = false
                                    },
                                    modifier = Modifier.testTag("profile_item_${emp.id}")
                                )
                            }
                            if (state.allEmployees.isNotEmpty()) {
                                HorizontalDivider()
                            }
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PersonAdd,
                                            contentDescription = null,
                                            tint = Blue600,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text("+ Add Worker Profile", fontWeight = FontWeight.Bold, color = Blue600)
                                    }
                                },
                                onClick = {
                                    expandedMenu = false
                                    viewModel.setShowAddEmployeeDialog(true)
                                },
                                modifier = Modifier.testTag("add_worker_profile_menu_item")
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                navItems.forEachIndexed { index, item ->
                    val selected = pagerState.currentPage == index
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label
                            )
                        },
                        label = {
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Blue600,
                            selectedTextColor = Blue600,
                            indicatorColor = Blue50,
                            unselectedIconColor = Slate400,
                            unselectedTextColor = Slate500
                        ),
                        modifier = Modifier.testTag("nav_tab_${item.label.lowercase()}")
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            EmployeeDashboardScreen(
                state = state,
                pagerState = pagerState,
                onPunchInClick = { viewModel.triggerManualPunch() },
                onOpenSimulatorClick = { viewModel.setShowLocationSimulator(true) },
                onManualPunchClick = { viewModel.setShowManualPunchDialog(true) },
                onOpenGeoFenceConfig = { viewModel.setShowGeoFenceConfigDialog(true) },
                onRefreshGpsClick = { viewModel.refreshLiveGps(context) },
                onOpenManualAttendanceDialog = { date -> viewModel.setShowManualAttendanceDialog(true, date) },
                onSelectPayslip = { viewModel.selectPayslipForPreview(it) },
                onAddWorkerClick = { viewModel.setShowAddEmployeeDialog(true) },
                onEditWorkerClick = { viewModel.setShowAddEmployeeDialog(true) },
                onGenerateInstantPayslip = { viewModel.generateInstantPayslipForSelectedEmployee() },
                onOpenHolidayCalendar = { viewModel.setShowHolidayCalendarDialog(true) },
                onAddDeductionClick = { viewModel.setShowAddDeductionDialog(true) }
            )

            if (state.showAddDeductionDialog) {
                DeductionManagementDialog(
                    employeeName = state.selectedEmployee?.name ?: "Worker",
                    onSaveDeduction = { type, amount, desc ->
                        state.selectedEmployee?.let { emp ->
                            viewModel.addDeductionOrAdvance(emp.id, type, amount, desc)
                        }
                    },
                    onDismiss = { viewModel.setShowAddDeductionDialog(false) }
                )
            }

            // Company Holiday Calendar Dialog
            if (state.showHolidayCalendarDialog) {
                CompanyHolidayCalendarDialog(
                    holidays = state.companyHolidays,
                    userRole = state.currentRole,
                    onDismiss = { viewModel.setShowHolidayCalendarDialog(false) },
                    onAddHoliday = { title, date, type, isPaid, desc ->
                        viewModel.addCompanyHoliday(title, date, type, isPaid, desc)
                    },
                    onDeleteHoliday = { holiday ->
                        viewModel.deleteCompanyHoliday(holiday)
                    }
                )
            }

            // Dialogs for Self Attendance
            if (state.showAddEmployeeDialog) {
                EmployeeFormDialog(
                    existingEmployee = state.selectedEmployee,
                    onSaveEmployee = { name, email, designation, dept, salary, pf, pt, customEmpId, pfUan ->
                        viewModel.saveEmployee(name, email, designation, dept, salary, pf, pt, customEmpId, pfUan)
                    },
                    onDismiss = { viewModel.setShowAddEmployeeDialog(false) }
                )
            }
            if (state.showManualAttendanceDialog) {
                com.example.ui.components.ManualAttendanceDialog(
                    initialDate = state.selectedDateForManualEntry,
                    onSave = { date, status, inTime, outTime, leaveType, note ->
                        viewModel.saveManualAttendanceOrLeave(date, status, inTime, outTime, leaveType, note)
                    },
                    onDismiss = { viewModel.setShowManualAttendanceDialog(false) }
                )
            }
            if (state.showGeoFenceConfigDialog) {
                GeoFenceConfigDialog(
                    currentConfig = state.geoFenceConfig,
                    onSaveConfig = { officeName, lat, lng, radius, autoPunch, weeklyOffDay ->
                        viewModel.saveGeoFenceSettings(officeName, lat, lng, radius, autoPunch, weeklyOffDay)
                    },
                    onDismiss = { viewModel.setShowGeoFenceConfigDialog(false) }
                )
            }

            if (state.showLocationSimulator) {
                LocationSimulatorDialog(
                    officeLat = state.geoFenceConfig.latitude,
                    officeLng = state.geoFenceConfig.longitude,
                    currentLat = state.currentLat,
                    currentLng = state.currentLng,
                    radiusMeters = state.geoFenceConfig.radiusMeters,
                    onSelectLocation = { lat, lng ->
                        viewModel.updateSimulatedLocation(lat, lng)
                    },
                    onDismiss = { viewModel.setShowLocationSimulator(false) }
                )
            }

            if (state.showManualPunchDialog) {
                var reasonText by remember { mutableStateOf("") }
                AlertDialog(
                    onDismissRequest = { viewModel.setShowManualPunchDialog(false) },
                    title = { Text("Request Offsite Punch Approval") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "You are currently outside the ${state.geoFenceConfig.officeName} radius. Enter a reason for offsite attendance approval:",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            OutlinedTextField(
                                value = reasonText,
                                onValueChange = { reasonText = it },
                                label = { Text("Reason (e.g., Client visit, Field work)") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("offsite_reason_field")
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (reasonText.isNotBlank()) {
                                    viewModel.submitManualApprovalRequest(reasonText)
                                }
                            },
                            modifier = Modifier.testTag("submit_offsite_request_btn")
                        ) {
                            Text("Submit Request")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.setShowManualPunchDialog(false) }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            state.selectedPayslipForPreview?.let { payroll ->
                PayslipDialog(
                    payroll = payroll,
                    onDownloadClick = {
                        viewModel.selectPayslipForPreview(null)
                    },
                    onDismiss = { viewModel.selectPayslipForPreview(null) }
                )
            }
        }
    }
}
