package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald600
import com.example.util.GeoFenceUtils

data class PresetLocation(
    val title: String,
    val subtitle: String,
    val lat: Double,
    val lng: Double,
    val distanceDescription: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationSimulatorDialog(
    officeLat: Double,
    officeLng: Double,
    currentLat: Double,
    currentLng: Double,
    radiusMeters: Float,
    onSelectLocation: (Double, Double) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    val presets = remember(officeLat, officeLng) {
        listOf(
            PresetLocation("At Office Desk", "Inside workplace perimeter", officeLat, officeLng, "0 meters"),
            PresetLocation("Office Entrance Gate", "Within boundary", officeLat + 0.0002, officeLng + 0.0001, "~30 meters"),
            PresetLocation("Nearby Coffee Shop", "Just outside radius", officeLat + 0.0010, officeLng + 0.0008, "~140 meters"),
            PresetLocation("Remote / Home Location", "Far outside geo-fence", officeLat + 0.0250, officeLng + 0.0200, "~3.2 km")
        )
    }

    var manualLatText by remember { mutableStateOf(currentLat.toString()) }
    var manualLngText by remember { mutableStateOf(currentLng.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.GpsFixed,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text("Radar Location & Manual GPS Setup", style = MaterialTheme.typography.titleLarge)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Quick Presets", fontWeight = FontWeight.Bold) },
                        modifier = Modifier.testTag("tab_presets")
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Manual GPS Input", fontWeight = FontWeight.Bold) },
                        modifier = Modifier.testTag("tab_manual_gps")
                    )
                }

                if (selectedTab == 0) {
                    Text(
                        text = "Select a preset location to simulate your GPS coordinates on the radar:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    presets.forEach { preset ->
                        val dist = GeoFenceUtils.calculateDistanceMeters(preset.lat, preset.lng, officeLat, officeLng)
                        val isInside = dist <= radiusMeters
                        val isCurrent = Math.abs(preset.lat - currentLat) < 0.0001 && Math.abs(preset.lng - currentLng) < 0.0001

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectLocation(preset.lat, preset.lng)
                                }
                                .testTag("preset_location_${preset.title.lowercase().replace(" ", "_")}"),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isCurrent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = if (isCurrent) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
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
                                        text = preset.title,
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = preset.subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isInside) Emerald100 else MaterialTheme.colorScheme.errorContainer
                                ) {
                                    Text(
                                        text = if (isInside) "Inside (${preset.distanceDescription})" else "Outside (${preset.distanceDescription})",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isInside) Emerald600 else MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Manual Lat & Lng Input
                    Text(
                        text = "Apne hisab se latitude aur longitude manually enter karke radar location test karein:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = manualLatText,
                        onValueChange = { manualLatText = it },
                        label = { Text("Latitude (e.g. 37.4220)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("manual_lat_field"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    OutlinedTextField(
                        value = manualLngText,
                        onValueChange = { manualLngText = it },
                        label = { Text("Longitude (e.g. -122.0841)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("manual_lng_field"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Button(
                        onClick = {
                            val lat = manualLatText.toDoubleOrNull() ?: currentLat
                            val lng = manualLngText.toDoubleOrNull() ?: currentLng
                            onSelectLocation(lat, lng)
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("apply_manual_location_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.MyLocation, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Apply Manual Location to Radar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("close_simulator_btn")
            ) {
                Text("Close")
            }
        }
    )
}
