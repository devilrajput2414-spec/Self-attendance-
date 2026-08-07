package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceRecordEntity
import com.example.ui.theme.*
import com.example.util.PayrollEngine

@Composable
fun AnnualLeaveTrackerCard(
    attendanceRecords: List<AttendanceRecordEntity>,
    monthlySalary: Double,
    annualQuotaDays: Int = 24,
    onRequestLeaveClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Calculate leave metrics
    val leavesTaken = attendanceRecords.count { it.status == "LEAVE" || it.status == "ABSENT" }
    val paidLeavesUsed = kotlin.math.min(leavesTaken, annualQuotaDays)
    val deductedLeaves = kotlin.math.max(0, leavesTaken - annualQuotaDays)
    val remainingPaidLeaves = kotlin.math.max(0, annualQuotaDays - leavesTaken)

    val dailyRate = if (monthlySalary > 0) monthlySalary / 30.0 else 0.0
    val totalDeductedAmount = deductedLeaves * dailyRate

    val progressFraction = (leavesTaken.toFloat() / annualQuotaDays.toFloat()).coerceIn(0f, 1f)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("annual_leave_tracker_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row
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
                        shape = RoundedCornerShape(12.dp),
                        color = Blue50,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.BeachAccess,
                                contentDescription = null,
                                tint = Blue600,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "1-YEAR LEAVE & DEDUCTION TRACKER",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = Slate400
                        )
                        Text(
                            text = "Annual Leave Quota: $annualQuotaDays Days/Year",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Slate900
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (remainingPaidLeaves > 5) Emerald50 else Amber100
                ) {
                    Text(
                        text = "$remainingPaidLeaves Days Left",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = if (remainingPaidLeaves > 5) Emerald600 else Amber600
                    )
                }
            }

            // Progress Bar
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Used $leavesTaken of $annualQuotaDays paid leaves",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = Slate600
                    )
                    Text(
                        text = "${(progressFraction * 100).toInt()}% Used",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = Blue600
                    )
                }

                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = if (deductedLeaves > 0) Rose600 else Blue600,
                    trackColor = Slate100
                )
            }

            // 4 Stats Cards Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatPill(
                    label = "Annual Quota",
                    value = "$annualQuotaDays Days",
                    bgColor = Slate50,
                    textColor = Slate900,
                    modifier = Modifier.weight(1f)
                )
                StatPill(
                    label = "Used Leaves",
                    value = "$leavesTaken Days",
                    bgColor = Blue50,
                    textColor = Blue600,
                    modifier = Modifier.weight(1f)
                )
                StatPill(
                    label = "Remaining Paid",
                    value = "$remainingPaidLeaves Days",
                    bgColor = Emerald50,
                    textColor = Emerald600,
                    modifier = Modifier.weight(1f)
                )
                StatPill(
                    label = "Unpaid/Deducted",
                    value = "$deductedLeaves Days",
                    bgColor = if (deductedLeaves > 0) Rose100 else Slate50,
                    textColor = if (deductedLeaves > 0) Rose600 else Slate600,
                    modifier = Modifier.weight(1f)
                )
            }

            // Salary Deduction Banner
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (deductedLeaves > 0) Rose100.copy(alpha = 0.5f) else Emerald50.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = if (deductedLeaves > 0) Icons.Default.MoneyOff else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (deductedLeaves > 0) Rose600 else Emerald600,
                        modifier = Modifier.size(24.dp)
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (deductedLeaves > 0) "Salary Deduction Notice" else "Leaves Within Paid Allowance",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (deductedLeaves > 0) Rose600 else Emerald600
                        )
                        Text(
                            text = if (deductedLeaves > 0) {
                                "$deductedLeaves unpaid leave(s) exceed annual limit. Estimated salary deduction: -${PayrollEngine.formatCurrency(totalDeductedAmount)}."
                            } else {
                                "All $leavesTaken leaves taken are covered under annual paid leave entitlement. No salary deduction applied."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate700
                        )
                    }
                }
            }

            // Request Time Off Button
            Button(
                onClick = onRequestLeaveClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("request_time_off_btn"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Blue600)
            ) {
                Icon(
                    imageVector = Icons.Default.EventNote,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Request Time Off / Apply Leave", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun StatPill(
    label: String,
    value: String,
    bgColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = bgColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = textColor,
                textAlign = TextAlign.Center
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = Slate500,
                textAlign = TextAlign.Center
            )
        }
    }
}
