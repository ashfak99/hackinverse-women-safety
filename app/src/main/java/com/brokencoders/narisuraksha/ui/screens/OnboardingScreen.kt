package com.brokencoders.narisuraksha.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
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

@SuppressLint("BatteryLife")
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    var hasAllPermissions by remember { mutableStateOf(PermissionHelper.hasAllPermissions(context)) }
    var isBatteryOptIgnored by remember { mutableStateOf(PermissionHelper.isIgnoringBatteryOptimizations(context)) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        hasAllPermissions = PermissionHelper.hasAllPermissions(context)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // App Shield Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(EmergencyRed.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Shield",
                    tint = EmergencyRed,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Nari-Suraksha",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 28.sp
                ),
                color = TextPrimary
            )

            Text(
                text = "Offline Women's Safety & BLE Guardian",
                style = MaterialTheme.typography.titleMedium,
                color = VigilanceAmber
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Works 100% offline without internet, cloud, or SIM network using Bluetooth Low Energy peer-to-peer broadcasts.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Permissions Checklist Cards
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PermissionItem(
                    title = "Bluetooth Low Energy (Nearby)",
                    subtitle = "Required to broadcast & receive offline SOS beacons from nearby phones.",
                    icon = Icons.Default.Bluetooth,
                    iconTint = GuardianBlue,
                    isGranted = PermissionHelper.hasBluetoothPermissions(context)
                )

                PermissionItem(
                    title = "GPS / Device Location",
                    subtitle = "Provides exact GPS coordinates in SOS packet without internet.",
                    icon = Icons.Default.MyLocation,
                    iconTint = EmergencyRed,
                    isGranted = PermissionHelper.hasLocationPermission(context)
                )

                PermissionItem(
                    title = "Audio Recording",
                    subtitle = "Records local audio evidence securely on device during confirmed SOS.",
                    icon = Icons.Default.Mic,
                    iconTint = SafeGreen,
                    isGranted = PermissionHelper.hasAudioPermission(context)
                )

                PermissionItem(
                    title = "High Priority Notifications",
                    subtitle = "Alerts you with sound and vibration if a woman triggers SOS nearby.",
                    icon = Icons.Default.NotificationsActive,
                    iconTint = VigilanceAmber,
                    isGranted = hasAllPermissions
                )

                PermissionItem(
                    title = "Battery Optimization Exemption",
                    subtitle = "Allows background scanner to listen for emergency alerts when screen is off.",
                    icon = Icons.Default.BatteryAlert,
                    iconTint = Color(0xFFAB47BC),
                    isGranted = isBatteryOptIgnored
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Action Buttons
            if (!hasAllPermissions) {
                Button(
                    onClick = {
                        val permissions = PermissionHelper.getRequiredPermissions().toTypedArray()
                        permissionLauncher.launch(permissions)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Text(
                        text = "GRANT PERMISSIONS",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            } else if (!isBatteryOptIgnored) {
                Button(
                    onClick = {
                        try {
                            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            try {
                                val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                context.startActivity(intent)
                            } catch (e2: Exception) {
                                // fallback
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VigilanceAmber),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Text(
                        text = "DISABLE BATTERY OPTIMIZATION",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onComplete,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (hasAllPermissions) SafeGreen else Color(0xFF333333),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Text(
                    text = "ENTER NARI-SURAKSHA",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PermissionItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    isGranted: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    ),
                    color = TextPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (isGranted) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Granted",
                    tint = SafeGreen,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
