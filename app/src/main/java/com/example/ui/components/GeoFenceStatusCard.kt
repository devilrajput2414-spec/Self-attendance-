package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.model.GeoFenceConfigEntity
import com.example.ui.theme.*
import com.example.util.GeoFenceUtils

@Composable
fun GeoFenceStatusCard(
    config: GeoFenceConfigEntity,
    currentLat: Double,
    currentLng: Double,
    distanceMeters: Float,
    isInside: Boolean,
    todayRecord: AttendanceRecordEntity?,
    onPunchInClick: () -> Unit,
    onOpenSimulatorClick: () -> Unit,
    onManualPunchClick: () -> Unit,
    onOpenGeoFenceConfig: () -> Unit,
    onRefreshGpsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusBgColor by animateColorAsState(
        if (isInside) Emerald50 else Amber100,
        label = "statusBg"
    )
    val statusTextColor by animateColorAsState(
        if (isInside) Emerald600 else Amber600,
        label = "statusText"
    )
    val radarCenterColor by animateColorAsState(
        if (isInside) Blue600 else Amber600,
        label = "radarColor"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("geofence_status_card"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Row: Title, Radius Pill, & Customize Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "GEO-FENCE RADAR",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = Slate400
                    )
                    Text(
                        text = "${config.officeName} (${config.radiusMeters.toInt()}m)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Slate900
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onOpenGeoFenceConfig,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("customize_geofence_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Customize GeoFence",
                            tint = Blue600
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = statusBgColor
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(statusTextColor)
                            )
                            Text(
                                text = if (isInside) "INSIDE" else "OUTSIDE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 10.sp
                                ),
                                color = statusTextColor
                            )
                        }
                    }
                }
            }

            // Central Visual Radar Indicator
            Box(
                modifier = Modifier
                    .size(112.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer ring
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(if (isInside) Blue100.copy(alpha = 0.35f) else Amber100.copy(alpha = 0.35f))
                )
                // Middle ring
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(if (isInside) Blue200.copy(alpha = 0.45f) else Amber100.copy(alpha = 0.60f))
                )
                // Core Circle Icon
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    color = radarCenterColor,
                    shadowElevation = 6.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            // Main Status Headline & Description
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = if (isInside) "Workplace Location Verified" else "Outside Geo-Fence Radius",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Slate900
                )

                val clockInTime = todayRecord?.punchInTime
                val descText = if (isInside) {
                    if (clockInTime != null) "Inside ${config.officeName} boundary (${config.radiusMeters.toInt()}m). Clocked in at $clockInTime."
                    else "Inside ${config.officeName} boundary (${config.radiusMeters.toInt()}m). Ready to punch in."
                } else {
                    "You are ${GeoFenceUtils.formatDistance(distanceMeters)} away from ${config.officeName}. Change location or request approval."
                }

                Text(
                    text = descText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate500,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }

            // Punch Status Time Pills
            todayRecord?.let { record ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Slate50,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Punch In",
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate400
                            )
                            Text(
                                text = record.punchInTime ?: "--:--",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = Slate900
                            )
                        }
                        VerticalDivider(modifier = Modifier.height(28.dp), color = Slate200)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Punch Out",
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate400
                            )
                            Text(
                                text = record.punchOutTime ?: "--:--",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = Slate900
                            )
                        }
                        VerticalDivider(modifier = Modifier.height(28.dp), color = Slate200)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Status",
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate400
                            )
                            Text(
                                text = record.status,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (record.status == "PRESENT") Emerald600 else Amber600
                                )
                            )
                        }
                    }
                }
            }

            // Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = onRefreshGpsClick,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 10.dp),
                    modifier = Modifier.testTag("refresh_live_gps_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Emerald600
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(text = "Live GPS", style = MaterialTheme.typography.labelMedium)
                }

                OutlinedButton(
                    onClick = onOpenSimulatorClick,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 10.dp),
                    modifier = Modifier.testTag("open_gps_simulator_btn")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.EditLocationAlt,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(text = "Simulator", style = MaterialTheme.typography.labelMedium)
                }

                OutlinedButton(
                    onClick = onOpenGeoFenceConfig,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                    modifier = Modifier.testTag("quick_geofence_custom_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.PinDrop,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Blue600
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Manual Radar", style = MaterialTheme.typography.labelMedium, color = Blue600)
                }

                if (isInside) {
                    Button(
                        onClick = onPunchInClick,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("punch_in_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (todayRecord?.punchInTime == null) "Punch In" else "Punch Out",
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Button(
                        onClick = onManualPunchClick,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("manual_punch_request_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = Amber600),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Rule,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Offsite Punch",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
