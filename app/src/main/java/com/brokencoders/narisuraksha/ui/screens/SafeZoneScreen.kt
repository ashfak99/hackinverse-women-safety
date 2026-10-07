package com.brokencoders.narisuraksha.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Train
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brokencoders.narisuraksha.ui.theme.EmergencyRed
import com.brokencoders.narisuraksha.ui.theme.EmergencyRedDark
import com.brokencoders.narisuraksha.ui.theme.GuardianBlue
import com.brokencoders.narisuraksha.ui.theme.SafeGreen
import com.brokencoders.narisuraksha.ui.theme.TextMuted
import com.brokencoders.narisuraksha.ui.theme.TextPrimary
import com.brokencoders.narisuraksha.ui.theme.TextSecondary
import com.brokencoders.narisuraksha.ui.theme.VigilanceAmber

data class SafeZone(
    val id: String,
    val name: String,
    val type: SafeZoneType,
    val address: String,
    val lat: Double,
    val lon: Double,
    val emergencyContact: String
)

enum class SafeZoneType(val label: String, val icon: ImageVector, val color: Color) {
    POLICE("Police Station", Icons.Default.LocalPolice, GuardianBlue),
    WOMEN_HELP_DESK("24x7 Women Desk", Icons.Default.Security, EmergencyRed),
    HOSPITAL("Hospital / Emergency", Icons.Default.LocalHospital, SafeGreen),
    METRO_SECURITY("Metro Station Guard", Icons.Default.Train, VigilanceAmber)
}

val sampleSafeZones = listOf(
    SafeZone(
        id = "1",
        name = "Central Women Police Station & 24x7 Help Desk",
        type = SafeZoneType.WOMEN_HELP_DESK,
        address = "Sector 12, Police Lines (24x7 Armed Guard)",
        lat = 28.6139,
        lon = 77.2090,
        emergencyContact = "1091"
    ),
    SafeZone(
        id = "2",
        name = "City Police Headquarters",
        type = SafeZoneType.POLICE,
        address = "Ashoka Road, Civil Lines",
        lat = 28.6190,
        lon = 77.2150,
        emergencyContact = "112"
    ),
    SafeZone(
        id = "3",
        name = "Civil Hospital - Trauma & Emergency Center",
        type = SafeZoneType.HOSPITAL,
        address = "Medical Enclave, Ring Road",
        lat = 28.6050,
        lon = 77.2200,
        emergencyContact = "102"
    ),
    SafeZone(
        id = "4",
        name = "Central Metro Station Security Post",
        type = SafeZoneType.METRO_SECURITY,
        address = "Gate 2, CISF Security Booth",
        lat = 28.6250,
        lon = 77.2180,
        emergencyContact = "155370"
    ),
    SafeZone(
        id = "5",
        name = "One Stop Centre (Sakhi) for Women",
        type = SafeZoneType.WOMEN_HELP_DESK,
        address = "District Social Welfare Complex",
        lat = 28.6300,
        lon = 77.2250,
        emergencyContact = "181"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafeZoneScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var selectedFilter by remember { mutableStateOf<SafeZoneType?>(null) }

    val filteredZones = remember(selectedFilter) {
        if (selectedFilter == null) sampleSafeZones
        else sampleSafeZones.filter { it.type == selectedFilter }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Offline Safe Zones",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF181818))
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Emergency Helplines quick bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF181818))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EmergencyDialChip("112 All Emergency", "112", context)
                EmergencyDialChip("1091 Women Helpline", "1091", context)
            }

            // Category Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == null,
                    onClick = { selectedFilter = null },
                    label = { Text("All") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = EmergencyRed,
                        selectedLabelColor = Color.White
                    )
                )
                FilterChip(
                    selected = selectedFilter == SafeZoneType.WOMEN_HELP_DESK,
                    onClick = {
                        selectedFilter = if (selectedFilter == SafeZoneType.WOMEN_HELP_DESK) null else SafeZoneType.WOMEN_HELP_DESK
                    },
                    label = { Text("Women Desks") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = EmergencyRed,
                        selectedLabelColor = Color.White
                    )
                )
                FilterChip(
                    selected = selectedFilter == SafeZoneType.POLICE,
                    onClick = {
                        selectedFilter = if (selectedFilter == SafeZoneType.POLICE) null else SafeZoneType.POLICE
                    },
                    label = { Text("Police") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GuardianBlue,
                        selectedLabelColor = Color.White
                    )
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredZones, key = { it.id }) { zone ->
                    SafeZoneCard(zone = zone, context = context)
                }
            }
        }
    }
}

@Composable
private fun EmergencyDialChip(title: String, number: String, context: Context) {
    Button(
        onClick = {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
            context.startActivity(intent)
        },
        colors = ButtonDefaults.buttonColors(containerColor = EmergencyRedDark),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.height(36.dp)
    ) {
        Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SafeZoneCard(zone: SafeZone, context: Context) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(zone.type.color.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = zone.type.icon,
                        contentDescription = null,
                        tint = zone.type.color,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = zone.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Text(
                        text = zone.type.label,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = zone.type.color
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = zone.address,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        val geoUri = Uri.parse("geo:${zone.lat},${zone.lon}?q=${zone.lat},${zone.lon}(${Uri.encode(zone.name)})")
                        val mapIntent = Intent(Intent.ACTION_VIEW, geoUri)
                        if (mapIntent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(mapIntent)
                        } else {
                            Toast.makeText(context, "No offline map app installed", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2C)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Navigate", fontSize = 13.sp)
                }

                Button(
                    onClick = {
                        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${zone.emergencyContact}"))
                        context.startActivity(dialIntent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SafeGreen),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Call ${zone.emergencyContact}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
