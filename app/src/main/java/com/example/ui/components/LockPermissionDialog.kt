package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SettingsAccessibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.DarkHudBg
import com.example.ui.theme.HudBorder
import com.example.ui.theme.HudSurface
import com.example.ui.theme.StarkGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoidBlack

@Composable
fun LockPermissionDialog(
    onEnableAccessibility: () -> Unit,
    onEnableDeviceAdmin: () -> Unit,
    onSimulateLock: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(DarkHudBg)
                .border(1.5.dp, ArcCyan.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(ArcCyan.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = ArcCyan,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "SCREEN LOCK PERMISSION",
                            style = MaterialTheme.typography.titleMedium,
                            color = ArcCyan
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = StarkGold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Android requires a one-time permission for Dev AI to lock the screen via voice command or automation routine:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Option 1: Accessibility (Recommended)
                Button(
                    onClick = {
                        onEnableAccessibility()
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("enable_accessibility_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ArcCyan)
                ) {
                    Icon(Icons.Default.SettingsAccessibility, contentDescription = null, tint = VoidBlack)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "1. Enable Accessibility (Recommended)",
                        color = VoidBlack,
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Option 2: Device Admin
                OutlinedButton(
                    onClick = {
                        onEnableDeviceAdmin()
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("enable_device_admin_button"),
                    shape = RoundedCornerShape(12.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(HudBorder))
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = StarkGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "2. Enable Device Admin",
                        color = StarkGold,
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Option 3: Simulate Standby
                OutlinedButton(
                    onClick = {
                        onSimulateLock()
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("simulate_standby_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "3. Instant In-App Screen Standby",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
