package com.brokencoders.narisuraksha.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brokencoders.narisuraksha.ui.theme.EmergencyRed
import com.brokencoders.narisuraksha.ui.theme.EmergencyRedDark
import com.brokencoders.narisuraksha.ui.theme.EmergencyRedLight

@Composable
fun SosPulseButton(
    isBroadcasting: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "SosPulse")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PulseScale"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PulseAlpha"
    )

    Box(
        modifier = modifier.size(240.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer pulsating animated ring (shown when active or pulsing)
        Box(
            modifier = Modifier
                .size(200.dp)
                .scale(if (isBroadcasting) pulseScale else 1.15f)
                .clip(CircleShape)
                .background(
                    if (isBroadcasting) EmergencyRed.copy(alpha = pulseAlpha)
                    else EmergencyRedDark.copy(alpha = 0.2f)
                )
        )

        // Middle aura
        Box(
            modifier = Modifier
                .size(175.dp)
                .clip(CircleShape)
                .background(EmergencyRedLight.copy(alpha = 0.15f))
        )

        // Inner Action Button
        Box(
            modifier = Modifier
                .size(150.dp)
                .shadow(16.dp, CircleShape, spotColor = EmergencyRedLight)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            EmergencyRedLight,
                            EmergencyRed,
                            EmergencyRedDark
                        )
                    )
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true, radius = 75.dp, color = Color.White),
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "SOS",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isBroadcasting) "BROADCASTING" else "TAP SOS",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = if (isBroadcasting) 13.sp else 18.sp,
                        letterSpacing = 1.sp
                    ),
                    color = Color.White
                )
            }
        }
    }
}
