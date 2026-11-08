package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.StarkGold
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SimulatedLockOverlay(
    isLocked: Boolean,
    onUnlock: () -> Unit
) {
    AnimatedVisibility(
        visible = isLocked,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        var currentTime by remember { mutableStateOf("") }
        var currentDate by remember { mutableStateOf("") }

        LaunchedEffect(isLocked) {
            while (isLocked) {
                val now = Date()
                currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(now)
                currentDate = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(now)
                delay(1000)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onUnlock
                )
                .testTag("simulated_lock_overlay"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(ArcCyan.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Device Locked",
                        tint = ArcCyan,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = currentTime,
                    style = MaterialTheme.typography.displayLarge,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = currentDate,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "DEV AI SECURED // SCREEN LOCKED",
                    style = MaterialTheme.typography.labelLarge,
                    color = StarkGold
                )

                Spacer(modifier = Modifier.height(48.dp))

                Text(
                    text = "Tap anywhere to wake up Dev AI",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary.copy(alpha = 0.7f)
                )
            }
        }
    }
}
