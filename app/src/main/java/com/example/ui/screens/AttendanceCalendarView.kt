package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceRecordEntity
import com.example.data.model.CompanyHolidayEntity
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AttendanceCalendarView(
    records: List<AttendanceRecordEntity>,
    companyHolidays: List<CompanyHolidayEntity> = emptyList(),
    weeklyOffDay: String = "SUNDAY",
    onDateClick: (String) -> Unit,
    onOpenHolidayCalendar: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // Calendar state for selected year and month (0-indexed month)
    var selectedCalendar by remember {
        mutableStateOf(Calendar.getInstance().apply {
            set(2026, Calendar.AUGUST, 1)
        })
    }

    val year = selectedCalendar.get(Calendar.YEAR)
    val month = selectedCalendar.get(Calendar.MONTH) // 0 to 11

    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    val monthTitle = monthFormat.format(selectedCalendar.time)

    // Filter records and holidays for selected month prefix (e.g., "2026-08")
    val monthPrefix = String.format(Locale.getDefault(), "%d-%02d", year, month + 1)
    
    val monthRecords = remember(records, monthPrefix) {
        records.filter { it.date.startsWith(monthPrefix) }
    }
    val presentCount = monthRecords.count { it.status == "PRESENT" || it.status == "HALF_DAY" }
    val leaveCount = monthRecords.count { it.status == "LEAVE" }
    val holidayCount = companyHolidays.count { it.date.startsWith(monthPrefix) }

    // Calculate days in month and starting day offset (Monday start)
    val daysInMonthData = remember(year, month) {
        val cal = Calendar.getInstance().apply {
            set(year, month, 1)
        }
        val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        // DAY_OF_WEEK: Sunday=1, Monday=2, ..., Saturday=7
        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        // Offset for Monday start: Monday=0, Tuesday=1, ..., Sunday=6
        val offset = (firstDayOfWeek - Calendar.MONDAY + 7) % 7
        
        val list = mutableListOf<Int?>()
        repeat(offset) {
            list.add(null)
        }
        for (d in 1..maxDays) {
            list.add(d)
        }
        list
    }

    val holidayMap = remember(companyHolidays) {
        companyHolidays.associateBy { it.date }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("attendance_calendar_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header with Month Title and Previous/Next Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Blue50,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = Blue600,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = monthTitle,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Slate900,
                            modifier = Modifier.testTag("attendance_month_title")
                        )
                        Text(
                            text = "Tap any date to mark manual / leave",
                            style = MaterialTheme.typography.labelSmall,
                            color = Slate500
                        )
                    }
                }

                // Month navigation (< and >)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = {
                            selectedCalendar = (selectedCalendar.clone() as Calendar).apply {
                                add(Calendar.MONTH, -1)
                            }
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("prev_month_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "Previous Month",
                            tint = Slate700
                        )
                    }
                    IconButton(
                        onClick = {
                            selectedCalendar = (selectedCalendar.clone() as Calendar).apply {
                                add(Calendar.MONTH, 1)
                            }
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("next_month_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Next Month",
                            tint = Slate700
                        )
                    }
                }
            }

            // Quick Month Selection Filter Chips (Jan - Dec 2026)
            val monthsList = remember {
                val cal = Calendar.getInstance().apply { set(2026, Calendar.JANUARY, 1) }
                val list = mutableListOf<Calendar>()
                for (m in 0..11) {
                    list.add((cal.clone() as Calendar).apply { set(Calendar.MONTH, m) })
                }
                list
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(vertical = 2.dp)
            ) {
                items(monthsList) { calItem ->
                    val isSelected = calItem.get(Calendar.YEAR) == year && calItem.get(Calendar.MONTH) == month
                    val label = SimpleDateFormat("MMM", Locale.getDefault()).format(calItem.time)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedCalendar = (calItem.clone() as Calendar)
                        },
                        label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        shape = RoundedCornerShape(10.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Blue600,
                            selectedLabelColor = Color.White,
                            containerColor = Slate100,
                            labelColor = Slate700
                        ),
                        modifier = Modifier.testTag("month_chip_$label")
                    )
                }
            }

            // Monthly Attendance Summary Statistics Card
            val presentCount = monthRecords.count { it.status == "PRESENT" }
            val halfDayCount = monthRecords.count { it.status == "HALF_DAY" }
            val leaveCount = monthRecords.count { it.status == "LEAVE" }
            val absentCount = monthRecords.count { it.status == "ABSENT" }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Slate50,
                modifier = Modifier.fillMaxWidth().testTag("attendance_summary_stats_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AttendanceStatItem("Present", "$presentCount", Emerald600, Emerald50)
                    AttendanceStatItem("Half Day", "$halfDayCount", Blue600, Blue50)
                    AttendanceStatItem("Leave", "$leaveCount", Blue600, Blue50)
                    AttendanceStatItem("Absent", "$absentCount", Rose600, Rose100)
                }
            }

            // Action Buttons Row (Holidays & Leave/Off)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (onOpenHolidayCalendar != null) {
                        Button(
                            onClick = onOpenHolidayCalendar,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Amber100, contentColor = Amber600),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("open_holiday_calendar_btn")
                        ) {
                            Text(text = "📅 Holidays", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }

                    Button(
                        onClick = {
                            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                            onDateClick(todayStr)
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Blue50, contentColor = Blue600),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("manual_mark_leave_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(text = "Leave/Off", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }

            // Days of week header
            val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                daysOfWeek.forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Slate400,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Grid of days
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                userScrollEnabled = false
            ) {
                items(daysInMonthData) { dayNum ->
                    if (dayNum == null) {
                        Box(modifier = Modifier.aspectRatio(1f))
                    } else {
                        val dayStr = String.format(Locale.getDefault(), "%d-%02d-%02d", year, month + 1, dayNum)
                        val rec = monthRecords.find { it.date == dayStr }
                        val isCompanyHoliday = holidayMap.containsKey(dayStr)

                        val status = when {
                            rec != null -> rec.status
                            isCompanyHoliday -> "HOLIDAY"
                            isWeeklyOff(year, month, dayNum, weeklyOffDay) -> "WEEKLY_OFF"
                            else -> ""
                        }

                        val (bgColor, textColor) = when (status) {
                            "PRESENT" -> Emerald50 to Emerald600
                            "HALF_DAY" -> Emerald50 to Blue600
                            "LEAVE" -> Blue50 to Blue600
                            "WEEKLY_OFF" -> Slate100 to Slate600
                            "HOLIDAY" -> Amber100 to Amber600
                            "ABSENT" -> Rose100 to Rose600
                            "MANUAL_PENDING" -> Amber100 to Amber600
                            else -> Slate50 to Slate500
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = bgColor,
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clickable { onDateClick(dayStr) }
                                .testTag("calendar_day_$dayStr")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$dayNum",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (rec != null || isCompanyHoliday) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 11.sp
                                        ),
                                        color = textColor
                                    )
                                    if (status.isNotBlank()) {
                                        val badgeSymbol = when (status) {
                                            "PRESENT" -> "•"
                                            "HALF_DAY" -> "½"
                                            "LEAVE" -> "L"
                                            "WEEKLY_OFF" -> "OFF"
                                            "HOLIDAY" -> "H"
                                            "ABSENT" -> "A"
                                            else -> "P"
                                        }
                                        Text(
                                            text = badgeSymbol,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = textColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Legend Footer
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                LegendPill("🟢 Present", Emerald50, Emerald600)
                LegendPill("🔵 Leave", Blue50, Blue600)
                LegendPill("⚪ Weekly Off", Slate100, Slate600)
                LegendPill("🟠 Holiday", Amber100, Amber600)
                LegendPill("🔴 Absent", Rose100, Rose600)
            }
        }
    }
}

private fun isWeeklyOff(year: Int, month: Int, day: Int, weeklyOffDay: String): Boolean {
    val cal = Calendar.getInstance().apply {
        set(year, month, day)
    }
    val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
    val target = when (weeklyOffDay.uppercase()) {
        "MONDAY" -> Calendar.MONDAY
        "TUESDAY" -> Calendar.TUESDAY
        "WEDNESDAY" -> Calendar.WEDNESDAY
        "THURSDAY" -> Calendar.THURSDAY
        "FRIDAY" -> Calendar.FRIDAY
        "SATURDAY" -> Calendar.SATURDAY
        else -> Calendar.SUNDAY
    }
    return dayOfWeek == target
}

@Composable
private fun LegendPill(text: String, bg: Color, fg: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bg
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
            color = fg
        )
    }
}

@Composable
private fun AttendanceStatItem(label: String, value: String, fg: Color, bg: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = bg
        ) {
            Text(
                text = value,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp),
                color = fg
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = Slate600
        )
    }
}
