package com.brokencoders.narisuraksha.ui.screens

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brokencoders.narisuraksha.core.Constants
import com.brokencoders.narisuraksha.data.SosEventEntity
import com.brokencoders.narisuraksha.ui.theme.EmergencyRed
import com.brokencoders.narisuraksha.ui.theme.GuardianBlue
import com.brokencoders.narisuraksha.ui.theme.SafeGreen
import com.brokencoders.narisuraksha.ui.theme.TextMuted
import com.brokencoders.narisuraksha.ui.theme.TextPrimary
import com.brokencoders.narisuraksha.ui.theme.TextSecondary
import com.brokencoders.narisuraksha.ui.viewmodels.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onBack: () -> Unit
) {
    val events by viewModel.events.collectAsState()
    val playingAudioPath by viewModel.currentlyPlayingAudioPath.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    val filteredEvents = remember(events, selectedTab) {
        when (selectedTab) {
            1 -> events.filter { it.eventType == SosEventEntity.TYPE_SENT }
            2 -> events.filter { it.eventType == SosEventEntity.TYPE_RECEIVED }
            else -> events
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "SOS Incident History",
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
                actions = {
                    if (events.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearAllHistory() }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Clear All",
                                tint = TextMuted
                            )
                        }
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
            // Tabs: All, Sent, Received
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF181818),
                contentColor = TextPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = EmergencyRed
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("All (${events.size})", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("My Broadcasts", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Received", fontWeight = FontWeight.SemiBold) }
                )
            }

            if (filteredEvents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.NearMe,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Incident Records",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextSecondary
                        )
                        Text(
                            text = "Recorded SOS broadcasts and received beacons will appear here offline.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredEvents, key = { it.id }) { event ->
                        SosHistoryCard(
                            event = event,
                            isPlayingAudio = playingAudioPath == event.audioPath,
                            onPlayAudio = { event.audioPath?.let { viewModel.playAudio(it) } },
                            onDelete = { viewModel.deleteEvent(event) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SosHistoryCard(
    event: SosEventEntity,
    isPlayingAudio: Boolean,
    onPlayAudio: () -> Unit,
    onDelete: () -> Unit
) {
    val isSent = event.eventType == SosEventEntity.TYPE_SENT
    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    val dateText = dateFormat.format(Date(event.createdAt))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (isSent) EmergencyRed.copy(alpha = 0.2f) else GuardianBlue.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isSent) Icons.Default.Warning else Icons.Default.NearMe,
                            contentDescription = null,
                            tint = if (isSent) EmergencyRed else GuardianBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isSent) "SOS BROADCASTED" else "BEACON RECEIVED",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            ),
                            color = if (isSent) EmergencyRed else GuardianBlue
                        )
                        Text(
                            text = dateText,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Location coordinates info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Location: ",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = TextSecondary
                )
                Text(
                    text = if (event.lat != null && event.lon != null) {
                        "${String.format("%.4f", event.lat)}, ${String.format("%.4f", event.lon)}"
                    } else {
                        "Offline / GPS Unavailable"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimary
                )
            }

            if (event.rssi != null) {
                val bucket = Constants.getDistanceBucket(event.rssi)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Distance: ",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = TextSecondary
                    )
                    Text(
                        text = "${bucket.approxDistanceText} (${event.rssi} dBm)",
                        style = MaterialTheme.typography.bodySmall,
                        color = SafeGreen
                    )
                }
            }

            // Audio Evidence Player
            if (event.audioPath != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onPlayAudio,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPlayingAudio) SafeGreen else Color(0xFF2C2C2C),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(42.dp)
                ) {
                    Icon(
                        imageVector = if (isPlayingAudio) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isPlayingAudio) "Playing Audio Evidence..." else "Play Audio Evidence",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = if (isPlayingAudio) Icons.Default.GraphicEq else Icons.Default.Mic,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
