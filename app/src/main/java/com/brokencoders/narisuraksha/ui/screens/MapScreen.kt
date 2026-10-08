package com.brokencoders.narisuraksha.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brokencoders.narisuraksha.data.SafeZoneCategory
import com.brokencoders.narisuraksha.data.SafeZoneItem
import com.brokencoders.narisuraksha.data.SafeZonesRepository
import com.brokencoders.narisuraksha.ui.theme.EmergencyRed
import com.brokencoders.narisuraksha.ui.theme.GuardianBlue
import com.brokencoders.narisuraksha.ui.theme.SafeGreen
import com.brokencoders.narisuraksha.ui.theme.TextMuted
import com.brokencoders.narisuraksha.ui.theme.TextPrimary
import com.brokencoders.narisuraksha.ui.theme.TextSecondary
import com.brokencoders.narisuraksha.ui.theme.VigilanceAmber
import com.brokencoders.narisuraksha.ui.viewmodels.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val currentCoords by viewModel.currentCoordinates.collectAsState()
    val latestAlert by viewModel.latestReceivedAlert.collectAsState()

    var selectedFilter by remember { mutableStateOf<SafeZoneCategory?>(null) }
    var selectedSafeZone by remember { mutableStateOf<SafeZoneItem?>(null) }

    // Fallback baseline coordinates if GPS is disabled (Center of New Delhi / Demo baseline)
    val userLat = currentCoords?.lat ?: 28.6139
    val userLon = currentCoords?.lon ?: 77.2090
    val isGpsActive = currentCoords != null

    val safeZones = remember(currentCoords) {
        SafeZonesRepository.preCachedSafeZones.sortedBy { zone ->
            SafeZonesRepository.calculateDistanceKm(userLat, userLon, zone.lat, zone.lon)
        }
    }

    val filteredZones = remember(safeZones, selectedFilter) {
        if (selectedFilter == null) safeZones else safeZones.filter { it.category == selectedFilter }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Map, contentDescription = null, tint = GuardianBlue, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Offline Tactical Map & Radar",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshLocation() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh GPS", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF141414))
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Telemetry Header Card
                LocationTelemetryCard(
                    coords = currentCoords,
                    isGpsActive = isGpsActive,
                    onOpenExternalMap = {
                        openExternalMaps(context, userLat, userLon)
                    }
                )
            }

            // Tactical Proximity Radar Canvas
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161616)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "PROXIMITY RADAR (5 KM RANGE)",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = GuardianBlue
                            )
                            Text(
                                text = "100% OFFLINE BEARING",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = SafeGreen
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Radar View
                        TacticalRadarView(
                            userLat = userLat,
                            userLon = userLon,
                            safeZones = safeZones,
                            selectedZone = selectedSafeZone,
                            activeDistressAlert = latestAlert,
                            onSelectZone = { selectedSafeZone = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Radar Legend
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LegendItem(GuardianBlue, "You")
                            LegendItem(SafeGreen, "Hospital")
                            LegendItem(GuardianBlue, "Police")
                            LegendItem(EmergencyRed, "Women Desk")
                            if (latestAlert != null) {
                                LegendItem(Color.Red, "SOS Alert")
                            }
                        }
                    }
                }
            }

            // Selected Safe Zone Highlight (if tapped)
            selectedSafeZone?.let { zone ->
                item {
                    val distKm = SafeZonesRepository.calculateDistanceKm(userLat, userLon, zone.lat, zone.lon)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF221F28)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, GuardianBlue, RoundedCornerShape(14.dp))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "SELECTED SAFE HAVEN",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = GuardianBlue
                                )
                                Text(
                                    text = SafeZonesRepository.formatDistance(distKm),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    color = SafeGreen
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = zone.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = zone.address,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { dialPhone(context, zone.emergencyContact) },
                                    colors = ButtonDefaults.buttonColors(containerColor = SafeGreen),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).height(40.dp)
                                ) {
                                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("DIAL ${zone.emergencyContact}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                OutlinedButton(
                                    onClick = { openDirections(context, zone.lat, zone.lon, zone.name) },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).height(40.dp)
                                ) {
                                    Icon(Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("DIRECTIONS", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Filter Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == null,
                        onClick = { selectedFilter = null },
                        label = { Text("All (${safeZones.size})", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GuardianBlue,
                            selectedLabelColor = Color.White
                        )
                    )
                    SafeZoneCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = selectedFilter == cat,
                            onClick = { selectedFilter = if (selectedFilter == cat) null else cat },
                            label = { Text(cat.label.take(12), fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GuardianBlue,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // Safe Zones Directory List
            items(filteredZones, key = { it.id }) { zone ->
                val distKm = SafeZonesRepository.calculateDistanceKm(userLat, userLon, zone.lat, zone.lon)
                SafeZoneCard(
                    zone = zone,
                    distanceText = SafeZonesRepository.formatDistance(distKm),
                    isSelected = selectedSafeZone?.id == zone.id,
                    onSelect = { selectedSafeZone = zone },
                    onCall = { dialPhone(context, zone.emergencyContact) },
                    onDirections = { openDirections(context, zone.lat, zone.lon, zone.name) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun LocationTelemetryCard(
    coords: com.brokencoders.narisuraksha.capture.Coordinates?,
    isGpsActive: Boolean,
    onOpenExternalMap: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isGpsActive) SafeGreen else VigilanceAmber)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isGpsActive) "CURRENT GPS TELEMETRY" else "OFFLINE BASELINE LOCATION",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isGpsActive) SafeGreen else VigilanceAmber
                    )
                }

                Button(
                    onClick = onOpenExternalMap,
                    colors = ButtonDefaults.buttonColors(containerColor = GuardianBlue),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("OPEN IN MAPS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TelemetryParam("LATITUDE", coords?.let { String.format(Locale.US, "%.5f", it.lat) } ?: "28.61393")
                TelemetryParam("LONGITUDE", coords?.let { String.format(Locale.US, "%.5f", it.lon) } ?: "77.20902")
                TelemetryParam("ACCURACY", coords?.accuracy?.let { "±${it.toInt()} m" } ?: "±12 m")
                TelemetryParam(
                    "UPDATED",
                    coords?.timestamp?.let { SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(it)) } ?: "Cached"
                )
            }
        }
    }
}

@Composable
private fun TelemetryParam(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = TextMuted)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            ),
            color = TextPrimary
        )
    }
}

@Composable
private fun TacticalRadarView(
    userLat: Double,
    userLon: Double,
    safeZones: List<SafeZoneItem>,
    selectedZone: SafeZoneItem?,
    activeDistressAlert: com.brokencoders.narisuraksha.ble.ReceivedSos?,
    onSelectZone: (SafeZoneItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0F0F0F))
            .border(1.dp, Color(0xFF262626), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = size.height.coerceAtMost(size.width) / 2f - 24.dp.toPx()

            // Concentric range rings: 1.25km, 2.5km, 3.75km, 5km
            val ringIntervals = listOf(0.25f, 0.5f, 0.75f, 1.0f)
            val strokeDashed = Stroke(
                width = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
            )

            ringIntervals.forEach { fraction ->
                drawCircle(
                    color = Color(0xFF263238),
                    radius = maxRadius * fraction,
                    center = center,
                    style = strokeDashed
                )
            }

            // Crosshairs
            drawLine(Color(0xFF1E282D), Offset(center.x, center.y - maxRadius), Offset(center.x, center.y + maxRadius), strokeWidth = 1.dp.toPx())
            drawLine(Color(0xFF1E282D), Offset(center.x - maxRadius, center.y), Offset(center.x + maxRadius, center.y), strokeWidth = 1.dp.toPx())

            // Plot Safe Zones (max distance cap 5.0 km)
            safeZones.forEach { zone ->
                val distKm = SafeZonesRepository.calculateDistanceKm(userLat, userLon, zone.lat, zone.lon)
                val bearing = SafeZonesRepository.calculateBearing(userLat, userLon, zone.lat, zone.lon)
                val clampedDist = distKm.coerceAtMost(5.0)
                val radiusPx = (clampedDist / 5.0) * maxRadius

                // Bearing: 0 deg = North (up: -y), 90 deg = East (+x)
                val angleRad = Math.toRadians(bearing.toDouble() - 90.0)
                val targetX = center.x + (radiusPx * cos(angleRad)).toFloat()
                val targetY = center.y + (radiusPx * sin(angleRad)).toFloat()

                val pinColor = when (zone.category) {
                    SafeZoneCategory.POLICE -> GuardianBlue
                    SafeZoneCategory.HOSPITAL -> SafeGreen
                    SafeZoneCategory.WOMEN_HELP_DESK -> EmergencyRed
                    SafeZoneCategory.METRO_SECURITY -> VigilanceAmber
                }

                val isSelected = selectedZone?.id == zone.id

                // Draw beacon pin
                drawCircle(
                    color = pinColor,
                    radius = if (isSelected) 8.dp.toPx() else 5.dp.toPx(),
                    center = Offset(targetX, targetY)
                )
                if (isSelected) {
                    drawCircle(
                        color = Color.White,
                        radius = 11.dp.toPx(),
                        center = Offset(targetX, targetY),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }

            // Active distress alert pin (if alert has valid GPS)
            activeDistressAlert?.let { alert ->
                if (alert.packet.lat != 0f && alert.packet.lon != 0f) {
                    val alertDist = SafeZonesRepository.calculateDistanceKm(userLat, userLon, alert.packet.lat.toDouble(), alert.packet.lon.toDouble())
                    val alertBearing = SafeZonesRepository.calculateBearing(userLat, userLon, alert.packet.lat.toDouble(), alert.packet.lon.toDouble())
                    val radiusPx = (alertDist.coerceAtMost(5.0) / 5.0) * maxRadius
                    val angleRad = Math.toRadians(alertBearing.toDouble() - 90.0)
                    val targetX = center.x + (radiusPx * cos(angleRad)).toFloat()
                    val targetY = center.y + (radiusPx * sin(angleRad)).toFloat()

                    drawCircle(color = Color.Red, radius = 9.dp.toPx(), center = Offset(targetX, targetY))
                    drawCircle(color = Color.White, radius = 13.dp.toPx(), center = Offset(targetX, targetY), style = Stroke(width = 2.dp.toPx()))
                }
            }

            // Center User Dot
            drawCircle(color = GuardianBlue, radius = 7.dp.toPx(), center = center)
            drawCircle(color = Color.White, radius = 10.dp.toPx(), center = center, style = Stroke(width = 2.dp.toPx()))
        }

        // Cardinal directions labels
        Text("N", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = GuardianBlue), modifier = Modifier.align(Alignment.TopCenter).padding(top = 4.dp))
        Text("S", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = TextMuted), modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 4.dp))
        Text("E", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = TextMuted), modifier = Modifier.align(Alignment.CenterEnd).padding(end = 6.dp))
        Text("W", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = TextMuted), modifier = Modifier.align(Alignment.CenterStart).padding(start = 6.dp))
    }
}

@Composable
private fun SafeZoneCard(
    zone: SafeZoneItem,
    distanceText: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onCall: () -> Unit,
    onDirections: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF221E28) else Color(0xFF191919)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            when (zone.category) {
                                SafeZoneCategory.POLICE -> GuardianBlue.copy(alpha = 0.2f)
                                SafeZoneCategory.HOSPITAL -> SafeGreen.copy(alpha = 0.2f)
                                SafeZoneCategory.WOMEN_HELP_DESK -> EmergencyRed.copy(alpha = 0.2f)
                                SafeZoneCategory.METRO_SECURITY -> VigilanceAmber.copy(alpha = 0.2f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (zone.category) {
                            SafeZoneCategory.POLICE -> Icons.Default.LocalPolice
                            SafeZoneCategory.HOSPITAL -> Icons.Default.LocalHospital
                            SafeZoneCategory.WOMEN_HELP_DESK -> Icons.Default.Security
                            SafeZoneCategory.METRO_SECURITY -> Icons.Default.Train
                        },
                        contentDescription = null,
                        tint = when (zone.category) {
                            SafeZoneCategory.POLICE -> GuardianBlue
                            SafeZoneCategory.HOSPITAL -> SafeGreen
                            SafeZoneCategory.WOMEN_HELP_DESK -> EmergencyRed
                            SafeZoneCategory.METRO_SECURITY -> VigilanceAmber
                        },
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = zone.name,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Text(
                        text = zone.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = distanceText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = SafeGreen
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onCall,
                    colors = ButtonDefaults.buttonColors(containerColor = SafeGreen),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(36.dp)
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("CALL ${zone.emergencyContact}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onDirections,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(36.dp)
                ) {
                    Icon(Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("MAP", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = TextMuted)
    }
}

private fun dialPhone(context: Context, number: String) {
    try {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Could not open dialer: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

private fun openDirections(context: Context, lat: Double, lon: Double, label: String) {
    try {
        val uri = Uri.parse("geo:$lat,$lon?q=$lat,$lon(${Uri.encode(label)})")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "No map application installed.", Toast.LENGTH_SHORT).show()
    }
}

private fun openExternalMaps(context: Context, lat: Double, lon: Double) {
    try {
        val uri = Uri.parse("geo:$lat,$lon?q=$lat,$lon(My+Location)")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "No map application installed.", Toast.LENGTH_SHORT).show()
    }
}
