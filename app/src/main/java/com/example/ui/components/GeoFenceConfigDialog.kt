package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GeoFenceConfigEntity
import com.example.ui.theme.*
import java.util.Locale

data class OfficeRadarPreset(
    val name: String,
    val address: String,
    val lat: Double,
    val lng: Double,
    val category: String
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GeoFenceConfigDialog(
    currentConfig: GeoFenceConfigEntity,
    onSaveConfig: (officeName: String, lat: Double, lng: Double, radius: Float, autoPunch: Boolean, weeklyOffDay: String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var officeName by remember { mutableStateOf(currentConfig.officeName) }
    var latText by remember { mutableStateOf(currentConfig.latitude.toString()) }
    var lngText by remember { mutableStateOf(currentConfig.longitude.toString()) }
    var radiusText by remember { mutableStateOf(currentConfig.radiusMeters.toInt().toString()) }
    var autoPunch by remember { mutableStateOf(currentConfig.autoPunchEnabled) }
    var weeklyOffDay by remember { mutableStateOf(currentConfig.weeklyOffDay) }

    val radarPresets = remember {
        listOf(
            OfficeRadarPreset("HQ Main Tech Park", "Electronic City Phase 1, Bangalore", 12.9716, 77.5946, "Headquarters"),
            OfficeRadarPreset("Connaught Place Hub", "Connaught Place, New Delhi", 28.6139, 77.2090, "North Hub"),
            OfficeRadarPreset("Bandra Business Center", "BKC, Mumbai, Maharashtra", 19.0596, 72.8656, "West Hub"),
            OfficeRadarPreset("Hinjewadi IT Phase 1", "Hinjewadi, Pune, Maharashtra", 18.5912, 73.7389, "Tech Center"),
            OfficeRadarPreset("Gachibowli Financial Hub", "Financial District, Hyderabad", 17.4401, 78.3489, "South Hub"),
            OfficeRadarPreset("MG Road Corporate Tower", "MG Road, Chennai, Tamil Nadu", 13.0827, 80.2707, "East Hub"),
            OfficeRadarPreset("Ahmedabad Corporate Center", "SG Highway, Ahmedabad, Gujarat", 23.0225, 72.5714, "West Hub"),
            OfficeRadarPreset("Client Project Site Alpha", "Custom Sector 4, Industrial Zone", 12.9352, 77.6245, "Field Site")
        )
    }

    val filteredPresets = remember(searchQuery) {
        if (searchQuery.isBlank()) radarPresets
        else radarPresets.filter {
            it.name.contains(searchQuery, true) ||
            it.address.contains(searchQuery, true) ||
            it.category.contains(searchQuery, true)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Radar,
                    contentDescription = null,
                    tint = Blue600
                )
                Text("GPS Radar & Office Search", style = MaterialTheme.typography.titleLarge)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Search your office location or pick a corporate hub to set up the active attendance GPS radar. Workers will record attendance automatically inside this radius.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate600
                )

                // Search Bar for Office Location
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search Office Location / Hub / City") },
                    placeholder = { Text("e.g. Bangalore, Mumbai, Tech Park") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Blue600) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Slate400)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("office_search_field"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // Presets / Search Results List
                if (filteredPresets.isNotEmpty()) {
                    Text(
                        text = "SUGGESTED OFFICE HUBS & PRESETS",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Slate400
                    )
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.heightIn(max = 200.dp).verticalScroll(rememberScrollState())
                    ) {
                        filteredPresets.forEach { preset ->
                            val isSelected = officeName == preset.name
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        officeName = preset.name
                                        latText = preset.lat.toString()
                                        lngText = preset.lng.toString()
                                    }
                                    .testTag("preset_hub_${preset.name.lowercase().replace(" ", "_")}"),
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) Blue50 else Slate50,
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, Blue600) else null
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text(text = "🏢", fontSize = 14.sp)
                                            Text(
                                                text = preset.name,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                color = Slate900
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = preset.address,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Slate500
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isSelected) Blue600 else Slate200
                                    ) {
                                        Text(
                                            text = if (isSelected) "Active" else "Select",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (isSelected) Color.White else Slate700
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Active Radar Visual Preview Box
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Blue50,
                    modifier = Modifier.fillMaxWidth(),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Blue200)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ACTIVE RADAR TARGET",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Blue600
                            )
                            Surface(shape = CircleShape, color = Emerald100) {
                                Text(
                                    text = "${radiusText.ifBlank { "50" }}m Radius",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Emerald600
                                )
                            }
                        }

                        // Mini Radar Animation Circle
                        Box(
                            modifier = Modifier.size(72.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(modifier = Modifier.fillMaxSize().clip(CircleShape).background(Blue100.copy(alpha = 0.5f)))
                            Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(Blue200.copy(alpha = 0.7f)))
                            Surface(modifier = Modifier.size(28.dp), shape = CircleShape, color = Blue600) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.MyLocation, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        Text(
                            text = officeName.ifBlank { "Unnamed Office Radar" },
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Slate900
                        )
                        Text(
                            text = "Lat: ${latText.ifBlank { "0.0" }}, Lng: ${lngText.ifBlank { "0.0" }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate600
                        )
                    }
                }

                // Open Google Maps Button for precise coordinate verification
                OutlinedButton(
                    onClick = {
                        val lat = latText.toDoubleOrNull() ?: 12.9716
                        val lng = lngText.toDoubleOrNull() ?: 77.5946
                        val gmmIntentUri = Uri.parse("geo:$lat,$lng?q=$lat,$lng(${Uri.encode(officeName)})")
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                            setPackage("com.google.android.apps.maps")
                        }
                        try {
                            context.startActivity(mapIntent)
                        } catch (e: Exception) {
                            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lng"))
                            context.startActivity(webIntent)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("open_google_maps_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Blue600)
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Blue600
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Open in Google Maps for Exact GPS", fontWeight = FontWeight.Bold, color = Blue600)
                }

                OutlinedTextField(
                    value = officeName,
                    onValueChange = { officeName = it },
                    label = { Text("Radar / Location Name") },
                    modifier = Modifier.fillMaxWidth().testTag("geofence_office_name_field"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = latText,
                        onValueChange = { latText = it },
                        label = { Text("Latitude") },
                        modifier = Modifier.weight(1f).testTag("geofence_lat_field"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = lngText,
                        onValueChange = { lngText = it },
                        label = { Text("Longitude") },
                        modifier = Modifier.weight(1f).testTag("geofence_lng_field"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                OutlinedTextField(
                    value = radiusText,
                    onValueChange = { radiusText = it },
                    label = { Text("Radar Radius Threshold (Meters)") },
                    modifier = Modifier.fillMaxWidth().testTag("geofence_radius_field"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp)
                )

                // Quick Radius Presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("30", "50", "100", "250", "500").forEach { preset ->
                        FilterChip(
                            selected = radiusText == preset,
                            onClick = { radiusText = preset },
                            label = { Text("${preset}m") },
                            modifier = Modifier.testTag("radius_preset_$preset")
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Blue50,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Auto-Attendance in Radar",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = Blue600
                            )
                            Text(
                                text = "Automatically record punch-in when entering this radar",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate700
                            )
                        }
                        Switch(
                            checked = autoPunch,
                            onCheckedChange = { autoPunch = it },
                            modifier = Modifier.testTag("auto_punch_switch")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "SELECT YOUR WEEKLY OFF DAY",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Slate400
                )
                val daysOfWeek = listOf("SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY")
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    daysOfWeek.forEach { day ->
                        val isSelected = weeklyOffDay.uppercase() == day
                        FilterChip(
                            selected = isSelected,
                            onClick = { weeklyOffDay = day },
                            label = {
                                Text(
                                    text = day.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            modifier = Modifier.testTag("weekly_off_chip_$day")
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val lat = latText.toDoubleOrNull() ?: currentConfig.latitude
                    val lng = lngText.toDoubleOrNull() ?: currentConfig.longitude
                    val radius = radiusText.toFloatOrNull() ?: currentConfig.radiusMeters
                    onSaveConfig(officeName, lat, lng, radius, autoPunch, weeklyOffDay)
                },
                modifier = Modifier.testTag("save_geofence_settings_btn"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Blue600)
            ) {
                Icon(Icons.Default.Radar, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Set Radar & Save", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
