package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AttendanceStatus
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ManualAttendanceDialog(
    initialDate: String,
    onSave: (
        date: String,
        status: String,
        punchInTime: String?,
        punchOutTime: String?,
        leaveType: String?,
        note: String?
    ) -> Unit,
    onDismiss: () -> Unit
) {
    var dateText by remember { mutableStateOf(initialDate) }
    var selectedStatus by remember { mutableStateOf(AttendanceStatus.PRESENT.name) }
    var leaveType by remember { mutableStateOf("Casual Leave") }
    var holidayType by remember { mutableStateOf("Public Holiday") }
    var punchInTime by remember { mutableStateOf("09:00 AM") }
    var punchOutTime by remember { mutableStateOf("06:00 PM") }
    var noteText by remember { mutableStateOf("") }

    val leaveTypes = listOf("Casual Leave", "Sick Leave", "Paid Leave", "Earned Leave", "Maternity/Paternity Leave")
    val holidayTypes = listOf("Public Holiday", "National Holiday", "Festival Off", "Company Event")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.EditCalendar,
                    contentDescription = null,
                    tint = Blue600
                )
                Text("Manual Attendance & Leave Mark", style = MaterialTheme.typography.titleLarge)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Date Input
                OutlinedTextField(
                    value = dateText,
                    onValueChange = { dateText = it },
                    label = { Text("Date (YYYY-MM-DD)") },
                    leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("manual_date_input"),
                    singleLine = true
                )

                // Select Status Category
                Text(
                    text = "SELECT STATUS CATEGORY",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Slate400
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatusChip(
                        label = "🟢 Present",
                        isSelected = selectedStatus == AttendanceStatus.PRESENT.name,
                        onClick = { selectedStatus = AttendanceStatus.PRESENT.name }
                    )
                    StatusChip(
                        label = "🌗 Half Day",
                        isSelected = selectedStatus == AttendanceStatus.HALF_DAY.name,
                        onClick = { selectedStatus = AttendanceStatus.HALF_DAY.name }
                    )
                    StatusChip(
                        label = "🔵 Leave",
                        isSelected = selectedStatus == AttendanceStatus.LEAVE.name,
                        onClick = { selectedStatus = AttendanceStatus.LEAVE.name }
                    )
                    StatusChip(
                        label = "⚪ Weekly Off",
                        isSelected = selectedStatus == AttendanceStatus.WEEKLY_OFF.name,
                        onClick = { selectedStatus = AttendanceStatus.WEEKLY_OFF.name }
                    )
                    StatusChip(
                        label = "🟠 Holiday",
                        isSelected = selectedStatus == AttendanceStatus.HOLIDAY.name,
                        onClick = { selectedStatus = AttendanceStatus.HOLIDAY.name }
                    )
                    StatusChip(
                        label = "🔴 Absent",
                        isSelected = selectedStatus == AttendanceStatus.ABSENT.name,
                        onClick = { selectedStatus = AttendanceStatus.ABSENT.name }
                    )
                }

                // Dynamic options based on selection
                when (selectedStatus) {
                    AttendanceStatus.PRESENT.name, AttendanceStatus.HALF_DAY.name -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = punchInTime,
                                onValueChange = { punchInTime = it },
                                label = { Text("Punch In Time") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("punch_in_time_input"),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = punchOutTime,
                                onValueChange = { punchOutTime = it },
                                label = { Text("Punch Out Time") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("punch_out_time_input"),
                                singleLine = true
                            )
                        }
                    }

                    AttendanceStatus.LEAVE.name -> {
                        Text(
                            text = "LEAVE TYPE",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Slate400
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            leaveTypes.forEach { lType ->
                                FilterChip(
                                    selected = leaveType == lType,
                                    onClick = { leaveType = lType },
                                    label = { Text(lType) }
                                )
                            }
                        }
                    }

                    AttendanceStatus.HOLIDAY.name -> {
                        Text(
                            text = "HOLIDAY TYPE",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Slate400
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            holidayTypes.forEach { hType ->
                                FilterChip(
                                    selected = holidayType == hType,
                                    onClick = { holidayType = hType },
                                    label = { Text(hType) }
                                )
                            }
                        }
                    }
                }

                // Note / Remarks
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Note / Reason (Optional)") },
                    placeholder = { Text("e.g. Annual leave approved, Sunday shift") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("manual_note_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalLeaveType = when (selectedStatus) {
                        AttendanceStatus.LEAVE.name -> leaveType
                        AttendanceStatus.HOLIDAY.name -> holidayType
                        AttendanceStatus.WEEKLY_OFF.name -> "Weekly Off (Sunday)"
                        else -> null
                    }
                    val inTime = if (selectedStatus == AttendanceStatus.PRESENT.name || selectedStatus == AttendanceStatus.HALF_DAY.name) punchInTime else null
                    val outTime = if (selectedStatus == AttendanceStatus.PRESENT.name || selectedStatus == AttendanceStatus.HALF_DAY.name) punchOutTime else null

                    onSave(
                        dateText,
                        selectedStatus,
                        inTime,
                        outTime,
                        finalLeaveType,
                        noteText.ifBlank { null }
                    )
                    onDismiss()
                },
                modifier = Modifier.testTag("save_manual_attendance_btn"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Blue600)
            ) {
                Text("Save Attendance", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_manual_attendance_btn")
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun StatusChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) Blue600 else Slate100,
        contentColor = if (isSelected) Color.White else Slate700
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
        )
    }
}
