package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceRecordEntity
import com.example.data.model.UserEntity
import com.example.ui.MainUiState
import com.example.ui.components.MainSummaryCards
import com.example.ui.theme.*
import com.example.util.PayrollEngine

@Composable
fun AdminDashboardScreen(
    state: MainUiState,
    onOpenGeoFenceConfig: () -> Unit,
    onApproveRequest: (AttendanceRecordEntity, Boolean) -> Unit,
    onAddEmployeeClick: () -> Unit,
    onAddDeductionClick: (UserEntity) -> Unit,
    onGeneratePayrollClick: () -> Unit,
    onSelectEmployee: (UserEntity) -> Unit,
    onOpenHolidayCalendar: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp)
    ) {
        // Admin Top Summary Cards Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AdminStatCard(
                    title = "Total Staff",
                    value = "${state.allEmployees.size}",
                    icon = Icons.Default.Groups,
                    containerColor = Blue50,
                    contentColor = Blue600,
                    modifier = Modifier.weight(1f)
                )

                AdminStatCard(
                    title = "Inside Workplace",
                    value = "${state.allEmployees.size}",
                    icon = Icons.Default.Place,
                    containerColor = Emerald50,
                    contentColor = Emerald600,
                    modifier = Modifier.weight(1f)
                )

                AdminStatCard(
                    title = "Pending Approvals",
                    value = "${state.pendingApprovalRequests.size}",
                    icon = Icons.Default.PendingActions,
                    containerColor = Amber100,
                    contentColor = Amber600,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Current Month Total Hours & Estimated Payout Cards
        item {
            MainSummaryCards(state = state)
        }

        // Workplace Geo-Fence Monitor Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_geofence_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = Blue600
                            )
                            Text(
                                text = "Geo-Fence Perimeter Settings",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Slate900
                            )
                        }

                        Button(
                            onClick = onOpenGeoFenceConfig,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Blue600),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("edit_geofence_params_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.EditLocation,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Configure Radius", style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    HorizontalDivider(color = Slate200)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Office: ${state.geoFenceConfig.officeName}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = Slate900
                            )
                            Text(
                                text = "GPS: ${state.geoFenceConfig.latitude}, ${state.geoFenceConfig.longitude}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate500
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Blue50
                        ) {
                            Text(
                                text = "Radius: ${state.geoFenceConfig.radiusMeters.toInt()}m",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = Blue600
                            )
                        }
                    }
                }
            }
        }

        // Pending Offsite Attendance Approval Requests Queue
        if (state.pendingApprovalRequests.isNotEmpty()) {
            item {
                Text(
                    text = "PENDING OFFSITE REQUESTS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = Slate400
                )
            }

            items(state.pendingApprovalRequests) { req ->
                val emp = state.allEmployees.find { it.id == req.employeeId }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("approval_req_${req.id}"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = emp?.name ?: "Employee #${req.employeeId}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Slate900
                                )
                                Text(
                                    text = "Date: ${req.date} at ${req.punchInTime}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate500
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Amber100
                            ) {
                                Text(
                                    text = "Offsite (${req.distanceFromOfficeMeters?.toInt() ?: 0}m)",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Amber600
                                )
                            }
                        }

                        req.manualReason?.let { reason ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Slate50,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "\"$reason\"",
                                    modifier = Modifier.padding(10.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate700
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onApproveRequest(req, true) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("approve_btn_${req.id}"),
                                colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Approve", fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { onApproveRequest(req, false) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("reject_btn_${req.id}"),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Rose100),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Reject", color = Rose600, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // 1-Click Monthly Payroll Generator Header & Action
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("payroll_generator_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "1-Click Payroll Batch Engine",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "Batch calculate August 2026 gross salaries, PF, PT, Mess & Advance recoveries.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate400
                        )
                    }

                    Button(
                        onClick = onGeneratePayrollClick,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Blue600),
                        modifier = Modifier.testTag("generate_payroll_batch_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Generate All", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Company Holiday Calendar Admin Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_holiday_calendar_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Amber100.copy(alpha = 0.5f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Amber600.copy(alpha = 0.3f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Amber600,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Event,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Company Holiday Calendar",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Slate900
                            )
                            Text(
                                text = "${state.companyHolidays.size} non-working days active in salary engine",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate700
                            )
                        }
                    }

                    Button(
                        onClick = onOpenHolidayCalendar,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Amber600),
                        modifier = Modifier.testTag("admin_manage_holidays_btn")
                    ) {
                        Text("Manage", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Employee Directory Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "EMPLOYEE ROSTER (${state.allEmployees.size})",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = Slate400
                )

                Button(
                    onClick = onAddEmployeeClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Blue600),
                    modifier = Modifier.testTag("admin_add_employee_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Staff", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        items(state.allEmployees) { emp ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("employee_roster_card_${emp.id}"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = emp.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Slate900
                            )
                            Text(
                                text = "${emp.designation} • ${emp.department}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate500
                            )
                        }

                        Text(
                            text = PayrollEngine.formatCurrency(emp.basicSalary) + "/mo",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = Slate900
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onAddDeductionClick(emp) },
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("add_deduction_btn_${emp.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PriceChange,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Slate700
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Deduction/Advance", style = MaterialTheme.typography.labelSmall, color = Slate700)
                        }

                        Button(
                            onClick = { onSelectEmployee(emp) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Blue600),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("view_emp_dashboard_btn_${emp.id}")
                        ) {
                            Text("View Dashboard", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = containerColor
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = contentColor
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = Slate600
            )
        }
    }
}
