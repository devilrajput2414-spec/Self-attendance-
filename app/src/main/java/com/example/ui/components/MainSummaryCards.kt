package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainUiState
import com.example.ui.theme.*
import com.example.util.PayrollEngine

@Composable
fun MainSummaryCards(
    state: MainUiState,
    modifier: Modifier = Modifier
) {
    val employee = state.selectedEmployee
    val basicSalary = employee?.basicSalary ?: 25000.0
    val presentCount = remember(state.employeeAttendance) {
        state.employeeAttendance.count { it.status == "PRESENT" || it.status == "HALF_DAY" }
    }
    val fullPresentCount = remember(state.employeeAttendance) {
        state.employeeAttendance.count { it.status == "PRESENT" }.toDouble().let { if (it == 0.0) 1.0 else it }
    }
    
    // Total Hours Calculation
    val totalHoursWorked = remember(state.employeeAttendance) {
        state.employeeAttendance.sumOf { record ->
            when (record.status) {
                "PRESENT" -> 8.0
                "HALF_DAY" -> 4.0
                else -> 0.0
            }
        }
    }

    // Estimated Payroll Calculation
    val paidHolidaysCount = remember(state.companyHolidays) {
        state.companyHolidays.count { it.isPaidHoliday }.toDouble()
    }
    val messTotal = remember(state.employeeDeductions) {
        state.employeeDeductions.filter { it.type == "MESS_CHARGES" && !it.isCleared }.sumOf { it.amount }
    }
    val advanceTotal = remember(state.employeeDeductions) {
        state.employeeDeductions.filter { it.type == "ADVANCE_SALARY" && !it.isCleared }.sumOf { it.amount }
    }

    val payrollCalc = remember(basicSalary, fullPresentCount, paidHolidaysCount, messTotal, advanceTotal) {
        PayrollEngine.calculateSalary(
            basicSalary = basicSalary,
            totalWorkingDays = 23,
            daysPresent = fullPresentCount,
            paidHolidays = paidHolidaysCount,
            pfPercentage = employee?.pfPercentage ?: 12.0,
            ptFixed = employee?.professionalTax ?: 200.0,
            messCharges = messTotal,
            advanceBalance = advanceTotal
        )
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Card 1: Current Month Total Hours Card
        Card(
            modifier = Modifier
                .weight(1f)
                .testTag("total_hours_summary_card"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            border = BorderStroke(1.dp, CardBorder)
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
                    Surface(
                        shape = CircleShape,
                        color = Blue50,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = Blue600,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Blue50
                    ) {
                        Text(
                            text = "AUG 2026",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            color = Blue600
                        )
                    }
                }

                Column {
                    Text(
                        text = "Total Hours Worked",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${String.format("%.1f", totalHoursWorked)} hrs",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = Slate900
                    )
                }

                HorizontalDivider(color = Slate100)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Emerald600,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "$presentCount days logged",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate600
                    )
                }
            }
        }

        // Card 2: Estimated Payroll Payout Card
        Card(
            modifier = Modifier
                .weight(1f)
                .testTag("estimated_payroll_payout_card"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.15f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = null,
                                tint = Color(0xFF34D399), // Emerald 400
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.10f)
                    ) {
                        Text(
                            text = "ESTIMATED",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            color = Color(0xFF34D399)
                        )
                    }
                }

                Column {
                    Text(
                        text = "Estimated Payout",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate400
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = PayrollEngine.formatCurrency(payrollCalc.netPayable),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = Color.White
                    )
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.12f))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Gross ${PayrollEngine.formatCurrency(payrollCalc.grossEarnings)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate400
                    )
                }
            }
        }
    }
}
