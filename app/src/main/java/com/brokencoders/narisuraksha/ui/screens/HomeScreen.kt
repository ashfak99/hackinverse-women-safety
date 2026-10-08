package com.brokencoders.narisuraksha.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.automirrored.outlined.VolumeOff
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.outlined.FlashlightOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brokencoders.narisuraksha.ui.components.CountdownOverlay
import com.brokencoders.narisuraksha.ui.components.SettingsDialog
import com.brokencoders.narisuraksha.ui.components.SosPulseButton
import com.brokencoders.narisuraksha.ui.components.StatusBadge
import com.brokencoders.narisuraksha.ui.theme.EmergencyRed
import com.brokencoders.narisuraksha.ui.theme.EmergencyRedDark
import com.brokencoders.narisuraksha.ui.theme.GuardianBlue
import com.brokencoders.narisuraksha.ui.theme.SafeGreen
import com.brokencoders.narisuraksha.ui.theme.TextMuted
import com.brokencoders.narisuraksha.ui.theme.TextPrimary
import com.brokencoders.narisuraksha.ui.theme.TextSecondary
import com.brokencoders.narisuraksha.ui.theme.VigilanceAmber
import com.brokencoders.narisuraksha.ui.viewmodels.MainViewModel

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToHistory: () -> Unit,
    onNavigateToSafeZones: () -> Unit,
    onNavigateToDecoy: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToContacts: () -> Unit,
    onNavigateToBleDebug: () -> Unit,
    onNavigateToAlert: (senderId: Short, lat: Float, lon: Float, rssi: Int) -> Unit
) {
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.ensureGuardianScanActive()
    }

    val isCountingDown by viewModel.isCountingDown.collectAsState()
    val countdownSeconds by viewModel.countdownSeconds.collectAsState()
    val isSosActive by viewModel.isSosActive.collectAsState()
    val isShakeEnabled by viewModel.isShakeEnabled.collectAsState()
    val isScanEnabled by viewModel.isScanEnabled.collectAsState()
    val isDecoyEnabled by viewModel.isDecoyEnabled.collectAsState()
    val secretPin by viewModel.secretDecoyCode.collectAsState()
    val responderAckCount by viewModel.responderAckCount.collectAsState()
    val latestAlert by viewModel.latestReceivedAlert.collectAsState()
    val isFlashlightOn by viewModel.isFlashlightOn.collectAsState()
    val isSirenOn by viewModel.isSirenOn.collectAsState()

    var isSettingsOpen by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Nari-Suraksha",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = (-0.5).sp
                                ),
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(SafeGreen.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "OFFLINE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    ),
                                    color = SafeGreen
                                )
                            }
                        }
                        Text(
                            text = "Network gayab, fir bhi help alive.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }

                    Row {
                        IconButton(
                            onClick = onNavigateToDecoy,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFF242424))
                                .size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = "Decoy Calculator",
                                tint = Color.LightGray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = onNavigateToHistory,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFF242424))
                                .size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "History",
                                tint = Color.LightGray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = { isSettingsOpen = true },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFF242424))
                                .size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = Color.LightGray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Active Incoming Alert Banner (if received)
                latestAlert?.let { alert ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onNavigateToAlert(
                                    alert.packet.senderId,
                                    alert.packet.lat,
                                    alert.packet.lon,
                                    alert.rssi
                                )
                            },
                        colors = CardDefaults.cardColors(containerColor = EmergencyRedDark),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Alert",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "🚨 NEARBY SOS RECEIVED",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Text(
                                    text = "Tap to view estimated distance & responder map",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // System Status Badges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatusBadge(
                        title = if (isScanEnabled) "BLE Guardian On" else "BLE Scan Off",
                        isActive = isScanEnabled,
                        activeColor = SafeGreen
                    )
                    StatusBadge(
                        title = if (isShakeEnabled) "Shake Detection" else "Shake Disabled",
                        isActive = isShakeEnabled,
                        activeColor = VigilanceAmber
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Hero SOS Trigger Button
                SosPulseButton(
                    isBroadcasting = isSosActive,
                    onClick = {
                        if (isSosActive) {
                            viewModel.stopSos()
                        } else {
                            viewModel.triggerSos()
                        }
                    }
                )

                // Responder Acknowledgment Card (Option B Live Counter)
                if (isSosActive) {
                    Spacer(modifier = Modifier.height(14.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (responderAckCount > 0) Color(0xFF1B5E20) else Color(0xFF2E2E2E)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (responderAckCount > 0) Icons.Default.CheckCircle else Icons.Default.People,
                                contentDescription = null,
                                tint = if (responderAckCount > 0) Color.White else VigilanceAmber,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (responderAckCount > 0) "👥 $responderAckCount Nearby Responder(s) Alerted!" else "Broadcasting to Nearby Responders...",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Text(
                                    text = if (responderAckCount > 0) "A nearby person has acknowledged your alert." else "Distress packet transmitting peer-to-peer via BLE.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.stopSos() },
                        colors = ButtonDefaults.buttonColors(containerColor = SafeGreen),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = "I AM SAFE (STOP BROADCAST)",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Emergency Contacts Alert Workflow (Part 4)
                    val emergencyContacts by viewModel.emergencyContacts.collectAsState()
                    if (emergencyContacts.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF22171B)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "EMERGENCY CONTACTS (CALL / SMS)",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = EmergencyRed
                                    )
                                    Text(
                                        text = "CELLULAR BACKUP",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                        color = VigilanceAmber
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                emergencyContacts.take(3).forEach { contact ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(contact.name, fontWeight = FontWeight.Bold, color = TextPrimary)
                                                if (contact.isPrimary) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(3.dp))
                                                            .background(EmergencyRed.copy(alpha = 0.2f))
                                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                                    ) {
                                                        Text("PRIMARY", fontSize = 8.sp, fontWeight = FontWeight.Black, color = EmergencyRed)
                                                    }
                                                }
                                            }
                                            Text("${contact.relationship} • ${contact.phoneNumber}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Button(
                                                onClick = { viewModel.callContact(contact.phoneNumber) },
                                                colors = ButtonDefaults.buttonColors(containerColor = SafeGreen),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.height(34.dp)
                                            ) {
                                                Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(13.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("CALL", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                            OutlinedButton(
                                                onClick = { viewModel.sendEmergencySms(contact.phoneNumber) },
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.height(34.dp)
                                            ) {
                                                Text("SMS", fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Quick Emergency Helper Tools
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Emergency Deterrents",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ToolButton(
                                title = if (isSirenOn) "Stop Siren" else "Loud Siren",
                                icon = if (isSirenOn) Icons.AutoMirrored.Outlined.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                                isActive = isSirenOn,
                                activeColor = EmergencyRed,
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.toggleSiren() }
                            )
                            ToolButton(
                                title = if (isFlashlightOn) "Torch Off" else "Strobe Torch",
                                icon = if (isFlashlightOn) Icons.Outlined.FlashlightOff else Icons.Default.FlashlightOn,
                                isActive = isFlashlightOn,
                                activeColor = VigilanceAmber,
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.toggleFlashlight() }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Protection Toggles Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Offline Protection Settings",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        SettingToggleRow(
                            title = "Shake to Trigger SOS",
                            subtitle = "3 rapid shakes (2.7g) starts 3s countdown",
                            icon = Icons.Default.Vibration,
                            checked = isShakeEnabled,
                            onCheckedChange = { viewModel.setShakeDetectionEnabled(it) }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        SettingToggleRow(
                            title = "Background BLE Guardian",
                            subtitle = "Listens for nearby distress alerts even screen-off",
                            icon = Icons.Default.Sensors,
                            checked = isScanEnabled,
                            onCheckedChange = { viewModel.setGuardianScanEnabled(it) }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        SettingToggleRow(
                            title = "Decoy Calculator Mode",
                            subtitle = "Disguise app as calculator with PIN trigger (PIN: $secretPin)",
                            icon = Icons.Default.Calculate,
                            checked = isDecoyEnabled,
                            onCheckedChange = { viewModel.setDecoyModeEnabled(it) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Offline Map & Radar Shortcut
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToMap() },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(GuardianBlue.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = "Offline Map",
                                tint = GuardianBlue
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Offline Tactical Map & Radar",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = "Proximity radar, GPS coordinates & safe zones map",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Emergency Contacts Shortcut
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToContacts() },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(SafeGreen.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Emergency Contacts",
                                tint = SafeGreen
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Emergency Contacts",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = "Manage trusted contacts for direct call and emergency SMS",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Safe Zones Card Shortcut
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToSafeZones() },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(GuardianBlue.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalPolice,
                                contentDescription = "Safe Zones",
                                tint = GuardianBlue
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Offline Safe Zones & Help Desks",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = "Police stations, 24x7 women desks & hospitals",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Countdown Overlay when SOS is triggered
            CountdownOverlay(
                isCountingDown = isCountingDown,
                remainingSeconds = countdownSeconds,
                onCancel = { viewModel.cancelCountdown() }
            )

            // Settings Dialog
            if (isSettingsOpen) {
                SettingsDialog(
                    currentPin = secretPin,
                    anonymousDeviceId = viewModel.anonymousDeviceId,
                    onSavePin = { viewModel.updateSecretDecoyCode(it) },
                    onDismiss = { isSettingsOpen = false },
                    onOpenBleDebug = onNavigateToBleDebug
                )
            }
        }
    }
}

@Composable
private fun ToolButton(
    title: String,
    icon: ImageVector,
    isActive: Boolean,
    activeColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isActive) activeColor else Color(0xFF2C2C2C),
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.height(50.dp)
    ) {
        Icon(imageVector = icon, contentDescription = title, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = GuardianBlue,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = TextPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = GuardianBlue,
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color(0xFF333333)
            )
        )
    }
}
