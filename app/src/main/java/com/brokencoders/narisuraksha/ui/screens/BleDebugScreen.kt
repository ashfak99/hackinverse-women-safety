package com.brokencoders.narisuraksha.ui.screens

import android.os.Build
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brokencoders.narisuraksha.core.PermissionHelper
import com.brokencoders.narisuraksha.ui.theme.EmergencyRed
import com.brokencoders.narisuraksha.ui.theme.GuardianBlue
import com.brokencoders.narisuraksha.ui.theme.SafeGreen
import com.brokencoders.narisuraksha.ui.theme.TextMuted
import com.brokencoders.narisuraksha.ui.theme.TextPrimary
import com.brokencoders.narisuraksha.ui.theme.TextSecondary
import com.brokencoders.narisuraksha.ui.theme.VigilanceAmber
import com.brokencoders.narisuraksha.ui.viewmodels.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BleDebugScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    val isAdvertising by viewModel.bleDiagnostics.isAdvertising.collectAsState()
    val isScanning by viewModel.bleDiagnostics.isScanning.collectAsState()
    val packetsSent by viewModel.bleDiagnostics.packetsSentCount.collectAsState()
    val packetsReceived by viewModel.bleDiagnostics.packetsReceivedCount.collectAsState()
    val acksSent by viewModel.bleDiagnostics.acksSentCount.collectAsState()
    val acksReceived by viewModel.bleDiagnostics.acksReceivedCount.collectAsState()
    val rejectedPackets by viewModel.bleDiagnostics.rejectedPacketsCount.collectAsState()
    val debugScanMode by viewModel.bleDiagnostics.debugScanModeEnabled.collectAsState()
    val logs by viewModel.bleDiagnostics.logs.collectAsState()
    val isTestBroadcasting by viewModel.isTestBroadcasting.collectAsState()
    val shakeDiag by viewModel.shakeDiagnostics.collectAsState()

    // Hardware status
    val btAvailable = PermissionHelper.isBluetoothAvailable(context)
    val btEnabled = PermissionHelper.isBluetoothEnabled(context)
    val bleSupported = PermissionHelper.isBleSupported(context)
    val advSupported = PermissionHelper.isAdvertiserSupported(context)

    // Permission status
    val scanGranted = PermissionHelper.hasBluetoothScanPermission(context)
    val advGranted = PermissionHelper.hasBluetoothAdvertisePermission(context)
    val connGranted = PermissionHelper.hasBluetoothConnectPermission(context)
    val locGranted = PermissionHelper.hasLocationPermission(context)

    val deviceId = viewModel.anonymousDeviceId
    val deviceIdHex = String.format("0x%04X", (deviceId.toInt() and 0xFFFF))

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.BugReport,
                            contentDescription = null,
                            tint = VigilanceAmber,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "BLE Diagnostics & Test Mode",
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
                // Banner
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(VigilanceAmber.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Radio,
                                contentDescription = null,
                                tint = VigilanceAmber,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Anonymous Device ID: $deviceIdHex (#$deviceId)",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = TextPrimary
                            )
                            Text(
                                text = "Service UUID: 0xFDE1 • AD Payload: 17 Bytes (Legacy 31B Compliant)",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                    }
                }
            }

            // Radio & Permission Status Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF181818)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "RADIO & HARDWARE CAPABILITIES",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = GuardianBlue
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        StatusRow("Bluetooth Adapter", if (btEnabled) "ON" else "OFF", btEnabled)
                        StatusRow("BLE Radio Supported", if (bleSupported) "YES" else "NO", bleSupported)
                        StatusRow("BLE Advertiser Supported", if (advSupported) "YES" else "NO", advSupported)
                        StatusRow("BLE Scanner Supported", if (btAvailable) "YES" else "NO", btAvailable)

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "RUNTIME PERMISSIONS",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = GuardianBlue
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        StatusRow("BLUETOOTH_SCAN", if (scanGranted) "Granted" else "Denied", scanGranted)
                        StatusRow("BLUETOOTH_ADVERTISE", if (advGranted) "Granted" else "Denied", advGranted)
                        StatusRow("BLUETOOTH_CONNECT", if (connGranted) "Granted" else "Denied", connGranted)
                        StatusRow("ACCESS_FINE_LOCATION", if (locGranted) "Granted" else "Denied", locGranted)
                    }
                }
            }

            // Live Radio State & Telemetry Counters
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF181818)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "LIVE RADIO STATE",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = SafeGreen
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        StatusRow("Advertising", if (isAdvertising) "ACTIVE" else "INACTIVE", isAdvertising)
                        StatusRow("Scanning", if (isScanning) "ACTIVE" else "INACTIVE", isScanning)

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "PACKET TELEMETRY COUNTERS",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = SafeGreen
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            CounterBadge("Packets Sent", packetsSent, GuardianBlue)
                            CounterBadge("Packets Recv", packetsReceived, SafeGreen)
                            CounterBadge("ACKs Sent", acksSent, VigilanceAmber)
                            CounterBadge("ACKs Recv", acksReceived, SafeGreen)
                        }
                        if (rejectedPackets > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Rejected / Cooldown Packets: $rejectedPackets",
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                color = TextMuted
                            )
                        }
                    }
                }
            }

            // Shake Detector Diagnostics Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF181818)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "SHAKE DETECTOR DIAGNOSTICS",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = VigilanceAmber
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        StatusRow("Shake Detection", if (shakeDiag.isListening) "ON" else "OFF", shakeDiag.isListening)
                        StatusRow("Sensor Available", if (shakeDiag.isSensorAvailable) "YES" else "NO", shakeDiag.isSensorAvailable)
                        StatusRow(
                            "Current Acceleration",
                            String.format(java.util.Locale.US, "%.2f g (thresh: %.1fg)", shakeDiag.currentGForce, 2.7f),
                            shakeDiag.currentGForce >= 2.7f
                        )
                        StatusRow(
                            "Peak Acceleration",
                            String.format(java.util.Locale.US, "%.2f g", shakeDiag.peakGForce),
                            shakeDiag.peakGForce >= 2.7f
                        )
                        StatusRow("Spike Count", "${shakeDiag.spikeCount} / 3 spikes", shakeDiag.spikeCount > 0)
                        StatusRow("Cooldown Status", if (shakeDiag.isCooldownActive) "ACTIVE (3s)" else "READY", !shakeDiag.isCooldownActive)

                        val lastSpikeStr = if (shakeDiag.lastSpikeTimestamp > 0L) {
                            java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(shakeDiag.lastSpikeTimestamp))
                        } else "None"
                        StatusRow("Last Spike Timestamp", lastSpikeStr, shakeDiag.lastSpikeTimestamp > 0L)
                    }
                }
            }

            // Test Broadcast & Actions Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "REAL-DEVICE DIAGNOSTIC ACTIONS",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = VigilanceAmber
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Test Broadcast Button
                        Button(
                            onClick = {
                                if (isTestBroadcasting) {
                                    viewModel.stopTestBroadcast()
                                } else {
                                    viewModel.startTestBroadcast()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isTestBroadcasting) EmergencyRed else VigilanceAmber
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Icon(
                                imageVector = if (isTestBroadcasting) Icons.Default.Clear else Icons.Default.Radio,
                                contentDescription = null,
                                tint = if (isTestBroadcasting) Color.White else Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isTestBroadcasting) "STOP TEST BROADCAST" else "START TEST BROADCAST (10s)",
                                fontWeight = FontWeight.Bold,
                                color = if (isTestBroadcasting) Color.White else Color.Black,
                                fontSize = 13.sp
                            )
                        }

                        Text(
                            text = "Broadcasts a 10s beacon with FLAG_TEST. Will be received by Phone B in diagnostics without triggering real sirens.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            modifier = Modifier.padding(top = 6.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Debug scan mode toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Debug Scan Mode (Software Filtering)",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Bypasses strict OEM hardware filters to guarantee discovery across Qualcomm/MediaTek",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )
                            }
                            Switch(
                                checked = debugScanMode,
                                onCheckedChange = { viewModel.toggleDebugScanMode(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = SafeGreen,
                                    checkedTrackColor = SafeGreen.copy(alpha = 0.3f)
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { viewModel.restartBleScan() },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Restart Scanner", fontSize = 12.sp)
                            }
                            OutlinedButton(
                                onClick = { viewModel.bleDiagnostics.clearLogs() },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Clear Logs", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Real-Time Event Logs Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LIVE BLE LOGCAT EVENTS (${logs.size})",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextSecondary
                    )
                }
            }

            if (logs.isEmpty()) {
                item {
                    Text(
                        text = "No BLE events recorded yet. Start a test broadcast or tap 'Restart Scanner'.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            } else {
                items(logs, key = { it.id }) { log ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF141414)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = log.tag,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    color = if (log.isError) EmergencyRed else if (log.tag.contains("ACK")) VigilanceAmber else SafeGreen
                                )
                                Text(
                                    text = log.timeFormatted,
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                    color = TextMuted
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = log.message,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = TextPrimary
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun StatusRow(label: String, value: String, isOk: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                ),
                color = if (isOk) SafeGreen else EmergencyRed
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = if (isOk) Icons.Default.CheckCircle else Icons.Default.Error,
                contentDescription = null,
                tint = if (isOk) SafeGreen else EmergencyRed,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun CounterBadge(label: String, count: Int, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF222222))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            ),
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            color = TextMuted
        )
    }
}
