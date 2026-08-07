package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.CompanyHolidayEntity
import com.example.data.model.UserRole
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyHolidayCalendarDialog(
    holidays: List<CompanyHolidayEntity>,
    userRole: UserRole,
    onDismiss: () -> Unit,
    onAddHoliday: (title: String, date: String, type: String, isPaid: Boolean, desc: String) -> Unit,
    onDeleteHoliday: (CompanyHolidayEntity) -> Unit
) {
    var selectedMonthFilter by remember { mutableStateOf("All 2026") }
    var searchQuery by remember { mutableStateOf("") }
    var showAddHolidayForm by remember { mutableStateOf(false) }

    // New Holiday Form State
    var newTitle by remember { mutableStateOf("") }
    var newDate by remember { mutableStateOf("2026-08-28") }
    var newType by remember { mutableStateOf("National Holiday") }
    var newIsPaid by remember { mutableStateOf(true) }
    var newDesc by remember { mutableStateOf("") }
    var formError by remember { mutableStateOf<String?>(null) }

    val monthOptions = listOf(
        "All 2026", "2026-01", "2026-02", "2026-03", "2026-04",
        "2026-05", "2026-06", "2026-07", "2026-08", "2026-09",
        "2026-10", "2026-11", "2026-12"
    )

    val holidayTypes = listOf(
        "National Holiday",
        "Festival Off",
        "Public Holiday",
        "Company Event",
        "Restricted Off"
    )

    val filteredHolidays = remember(holidays, selectedMonthFilter, searchQuery) {
        holidays.filter { holiday ->
            val matchesMonth = if (selectedMonthFilter == "All 2026") true else holiday.date.startsWith(selectedMonthFilter)
            val matchesQuery = searchQuery.isBlank() ||
                    holiday.title.contains(searchQuery, ignoreCase = true) ||
                    holiday.type.contains(searchQuery, ignoreCase = true) ||
                    holiday.date.contains(searchQuery, ignoreCase = true)
            matchesMonth && matchesQuery
        }
    }

    val totalPaidHolidays = remember(holidays) { holidays.count { it.isPaidHoliday } }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .testTag("company_holiday_calendar_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top Header Bar
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Company Holiday Calendar",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "2026 Non-Working Days & Salary Protection Engine",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("close_holiday_dialog_button")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    },
                    actions = {
                        if (userRole == UserRole.ADMIN) {
                            Button(
                                onClick = { showAddHolidayForm = !showAddHolidayForm },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (showAddHolidayForm) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primary,
                                    contentColor = if (showAddHolidayForm) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimary
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("toggle_add_holiday_form_button")
                            ) {
                                Icon(
                                    imageVector = if (showAddHolidayForm) Icons.Default.Close else Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (showAddHolidayForm) "Cancel" else "Add Holiday",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Salary Engine Callout Banner
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF2563EB),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.Verified,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Auto-Salary Engine Protection Active",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFF1E3A8A)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Non-working company holidays ($totalPaidHolidays paid days in 2026) are automatically counted as fully paid days in monthly salary calculations.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF1E40AF),
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }

                    // Add Holiday Admin Form (if toggled)
                    if (showAddHolidayForm && userRole == UserRole.ADMIN) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("add_holiday_form_card"),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Event,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Declare New Company Holiday",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    OutlinedTextField(
                                        value = newTitle,
                                        onValueChange = {
                                            newTitle = it
                                            formError = null
                                        },
                                        label = { Text("Holiday Name (e.g. Diwali, Foundation Day)") },
                                        singleLine = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("holiday_title_input")
                                    )

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        OutlinedTextField(
                                            value = newDate,
                                            onValueChange = {
                                                newDate = it
                                                formError = null
                                            },
                                            label = { Text("Date (YYYY-MM-DD)") },
                                            placeholder = { Text("2026-08-28") },
                                            singleLine = true,
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("holiday_date_input")
                                        )

                                        var typeExpanded by remember { mutableStateOf(false) }
                                        ExposedDropdownMenuBox(
                                            expanded = typeExpanded,
                                            onExpandedChange = { typeExpanded = !typeExpanded },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            OutlinedTextField(
                                                value = newType,
                                                onValueChange = {},
                                                readOnly = true,
                                                label = { Text("Type") },
                                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                                                modifier = Modifier.menuAnchor()
                                            )
                                            ExposedDropdownMenu(
                                                expanded = typeExpanded,
                                                onDismissRequest = { typeExpanded = false }
                                            ) {
                                                holidayTypes.forEach { type ->
                                                    DropdownMenuItem(
                                                        text = { Text(type) },
                                                        onClick = {
                                                            newType = type
                                                            typeExpanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    OutlinedTextField(
                                        value = newDesc,
                                        onValueChange = { newDesc = it },
                                        label = { Text("Description / Notes") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Checkbox(
                                            checked = newIsPaid,
                                            onCheckedChange = { newIsPaid = it },
                                            modifier = Modifier.testTag("holiday_paid_checkbox")
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Paid Non-Working Day (Counts in Salary Engine)",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                                        )
                                    }

                                    formError?.let { err ->
                                        Text(
                                            text = err,
                                            color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            if (newTitle.isBlank()) {
                                                formError = "Please enter a holiday title"
                                                return@Button
                                            }
                                            if (!newDate.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
                                                formError = "Enter date in YYYY-MM-DD format"
                                                return@Button
                                            }
                                            onAddHoliday(newTitle, newDate, newType, newIsPaid, newDesc)
                                            newTitle = ""
                                            newDesc = ""
                                            showAddHolidayForm = false
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("save_holiday_button"),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Save & Update Salary Engine")
                                    }
                                }
                            }
                        }
                    }

                    // Filters and Search Row
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                                        }
                                    }
                                },
                                placeholder = { Text("Search holiday name, type or date...") },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("holiday_search_input")
                            )

                            // Month Selector Chips
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) {
                                items(monthOptions) { monthLabel ->
                                    val isSelected = selectedMonthFilter == monthLabel
                                    val displayText = if (monthLabel == "All 2026") "All Year" else {
                                        try {
                                            val date = SimpleDateFormat("yyyy-MM", Locale.getDefault()).parse(monthLabel)
                                            SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(date ?: Date())
                                        } catch (e: Exception) { monthLabel }
                                    }
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedMonthFilter = monthLabel },
                                        label = { Text(displayText, fontSize = 12.sp) },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                        ),
                                        modifier = Modifier.testTag("filter_chip_$monthLabel")
                                    )
                                }
                            }
                        }
                    }

                    // Interactive Calendar View Header & Month Visual Box
                    item {
                        CompanyHolidayMonthCalendarView(
                            monthFilter = selectedMonthFilter,
                            holidays = filteredHolidays
                        )
                    }

                    // Holiday List Header
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Scheduled Holidays (${filteredHolidays.size})",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = "$totalPaidHolidays Paid Off Days",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // Holiday Cards List
                    if (filteredHolidays.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        Icons.Outlined.EventBusy,
                                        contentDescription = null,
                                        modifier = Modifier.size(40.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "No holidays found for selected filter.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        items(filteredHolidays, key = { it.id }) { holiday ->
                            HolidayCardItem(
                                holiday = holiday,
                                userRole = userRole,
                                onDelete = { onDeleteHoliday(holiday) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompanyHolidayMonthCalendarView(
    monthFilter: String,
    holidays: List<CompanyHolidayEntity>
) {
    val monthTitle = if (monthFilter == "All 2026") "2026 Full Year Overview" else {
        try {
            val date = SimpleDateFormat("yyyy-MM", Locale.getDefault()).parse(monthFilter)
            SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(date ?: Date())
        } catch (e: Exception) { monthFilter }
    }

    val holidayDates = remember(holidays) { holidays.map { it.date }.toSet() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = monthTitle,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LegendItem(color = Color(0xFFF97316), label = "Holiday")
                    LegendItem(color = Color(0xFF8B5CF6), label = "Sunday Off")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Calendar Grid (31 Day visual representation)
            val monthDaysCount = 31
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                val dayNames = listOf("S", "M", "T", "W", "T", "F", "S")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    dayNames.forEach { name ->
                        Text(
                            text = name,
                            modifier = Modifier.width(32.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                val yearMonthPrefix = if (monthFilter == "All 2026") "2026-08" else monthFilter
                val weeks = remember(yearMonthPrefix, holidays) {
                    val list = mutableListOf<List<Int>>()
                    var currentWeek = mutableListOf<Int>()
                    // Basic alignment for demo
                    for (d in 1..monthDaysCount) {
                        currentWeek.add(d)
                        if (currentWeek.size == 7 || d == monthDaysCount) {
                            list.add(currentWeek)
                            currentWeek = mutableListOf()
                        }
                    }
                    list
                }

                weeks.forEach { week ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        week.forEach { dayNum ->
                            val dayStr = String.format(Locale.getDefault(), "%s-%02d", yearMonthPrefix, dayNum)
                            val isHoliday = holidayDates.contains(dayStr)
                            val isSunday = (dayNum % 7 == 2) // mock sunday pattern

                            val bg = when {
                                isHoliday -> Color(0xFFFFF7ED)
                                isSunday -> Color(0xFFF5F3FF)
                                else -> Color.Transparent
                            }
                            val borderCol = when {
                                isHoliday -> Color(0xFFF97316)
                                isSunday -> Color(0xFF8B5CF6)
                                else -> Color.Transparent
                            }
                            val textCol = when {
                                isHoliday -> Color(0xFFC2410C)
                                isSunday -> Color(0xFF6D28D9)
                                else -> MaterialTheme.colorScheme.onSurface
                            }

                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(bg)
                                    .border(1.dp, borderCol, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$dayNum",
                                    fontSize = 11.sp,
                                    fontWeight = if (isHoliday || isSunday) FontWeight.Bold else FontWeight.Normal,
                                    color = textCol
                                )
                            }
                        }
                        // Fill empty week slots if necessary
                        repeat(7 - week.size) {
                            Spacer(modifier = Modifier.size(32.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
    }
}

@Composable
private fun HolidayCardItem(
    holiday: CompanyHolidayEntity,
    userRole: UserRole,
    onDelete: () -> Unit
) {
    val dateDisplay = remember(holiday.date) {
        try {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(holiday.date)
            SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(date ?: Date())
        } catch (e: Exception) { holiday.date }
    }

    val typeColor = when (holiday.type) {
        "National Holiday" -> Color(0xFFC2410C) // Orange
        "Festival Off" -> Color(0xFF0369A1) // Blue
        "Company Event" -> Color(0xFF7E22CE) // Purple
        else -> Color(0xFF15803D) // Green
    }

    val typeBg = when (holiday.type) {
        "National Holiday" -> Color(0xFFFFF7ED)
        "Festival Off" -> Color(0xFFF0F9FF)
        "Company Event" -> Color(0xFFFAF5FF)
        else -> Color(0xFFF0FDF4)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("holiday_card_${holiday.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Calendar Date Badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = typeBg,
                border = BorderStroke(1.dp, typeColor.copy(alpha = 0.3f)),
                modifier = Modifier.size(52.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = typeColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = holiday.date.takeLast(2),
                        fontWeight = FontWeight.Bold,
                        color = typeColor,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = holiday.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = dateDisplay,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (holiday.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = holiday.description,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = typeBg
                    ) {
                        Text(
                            text = holiday.type,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = typeColor
                        )
                    }

                    if (holiday.isPaidHoliday) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFDCFCE7)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF15803D),
                                    modifier = Modifier.size(10.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "Paid Day",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF15803D)
                                )
                            }
                        }
                    }
                }
            }

            if (userRole == UserRole.ADMIN) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("delete_holiday_${holiday.id}_button")
                ) {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = "Delete Holiday",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
