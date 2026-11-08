package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.StarkGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoidBlack

@Composable
fun QuickMessageDialog(
    initialText: String = "",
    onSendMessage: (platform: String, phoneNumber: String?, message: String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedPlatform by remember { mutableStateOf("whatsapp") }
    var phoneNumber by remember { mutableStateOf("") }
    var messageText by remember { mutableStateOf(initialText.ifBlank { "Hello! Sent via Dev AI Assistant." }) }

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
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(NeonGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Message,
                                contentDescription = null,
                                tint = NeonGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "QUICK MESSAGING // DEV AI",
                            style = MaterialTheme.typography.titleMedium,
                            color = ArcCyan
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("close_message_dialog")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = StarkGold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Platform Selector (WhatsApp vs SMS)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val isWhatsApp = selectedPlatform == "whatsapp"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isWhatsApp) NeonGreen.copy(alpha = 0.2f) else HudSurface)
                            .border(1.dp, if (isWhatsApp) NeonGreen else HudBorder, RoundedCornerShape(12.dp))
                            .clickable { selectedPlatform = "whatsapp" }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "WhatsApp 💬",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isWhatsApp) NeonGreen else TextSecondary
                        )
                    }

                    val isSms = selectedPlatform == "sms"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSms) ArcCyan.copy(alpha = 0.2f) else HudSurface)
                            .border(1.dp, if (isSms) ArcCyan else HudBorder, RoundedCornerShape(12.dp))
                            .clickable { selectedPlatform = "sms" }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Direct SMS ✉️",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isSms) ArcCyan else TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Phone number (Optional)
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("message_phone_field"),
                    label = { Text("Phone Number (Optional)", color = TextSecondary) },
                    placeholder = { Text("+91 98765 43210 or leave blank", color = TextSecondary) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ArcCyan,
                        unfocusedBorderColor = HudBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = HudSurface,
                        unfocusedContainerColor = HudSurface
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Message Text
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .testTag("message_body_field"),
                    label = { Text("Message Body", color = TextSecondary) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ArcCyan,
                        unfocusedBorderColor = HudBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = HudSurface,
                        unfocusedContainerColor = HudSurface
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Send Button
                Button(
                    onClick = {
                        onSendMessage(selectedPlatform, phoneNumber.ifBlank { null }, messageText)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("send_message_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedPlatform == "whatsapp") NeonGreen else ArcCyan
                    )
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, tint = VoidBlack)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (selectedPlatform == "whatsapp") "SEND ON WHATSAPP" else "DISPATCH SMS",
                        color = VoidBlack,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}
