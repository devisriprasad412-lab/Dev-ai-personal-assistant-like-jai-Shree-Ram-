package com.example.ui.screens

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.speech.VoiceState
import com.example.ui.components.AppsLauncherDialog
import com.example.ui.components.AutomationDialog
import com.example.ui.components.CustomAutomationDialog
import com.example.ui.components.JarvisArcReactorVisualizer
import com.example.ui.components.LockPermissionDialog
import com.example.ui.components.QuickMessageDialog
import com.example.ui.components.SimulatedLockOverlay
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcCyanDark
import com.example.ui.theme.DarkHudBg
import com.example.ui.theme.DivineSaffron
import com.example.ui.theme.HudBorder
import com.example.ui.theme.HudSurface
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed
import com.example.ui.theme.StarkGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoidBlack
import com.example.ui.viewmodel.ChatMessage
import com.example.ui.viewmodel.JarvisViewModel
import com.example.ui.viewmodel.Sender

@Composable
fun JarvisMainScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context as? Activity

    var textInput by remember { mutableStateOf("") }
    val chatListState = rememberLazyListState()

    // Auto-scroll chat to bottom when new messages arrive
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            chatListState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(VoidBlack)
    ) {
        // Futuristic Cybernetic Background
        Image(
            painter = painterResource(id = R.drawable.jarvis_hud_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.22f
        )

        // Gradient lighting overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            VoidBlack.copy(alpha = 0.88f),
                            DarkHudBg.copy(alpha = 0.75f),
                            VoidBlack.copy(alpha = 0.95f)
                        )
                    )
                )
        )

        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                BottomCommandBar(
                    textInput = textInput,
                    onTextChange = { textInput = it },
                    onSend = {
                        if (textInput.isNotBlank()) {
                            viewModel.processCommand(textInput)
                            textInput = ""
                        }
                    },
                    voiceState = uiState.voiceState,
                    onToggleVoice = {
                        if (uiState.voiceState == VoiceState.LISTENING) {
                            viewModel.stopListening()
                        } else {
                            viewModel.startListening()
                        }
                    },
                    onQuickChipClick = { phrase ->
                        viewModel.processCommand(phrase)
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top HUD Bar (English labels + Dev AI branding + Telugu Voice Indicator)
                TopHudBar(
                    batteryPercent = uiState.batteryInfo.levelPercent,
                    isCharging = uiState.batteryInfo.isCharging,
                    isTorchOn = uiState.isTorchOn,
                    onToggleTorch = { viewModel.toggleTorch() },
                    onResetChat = { viewModel.resetConversation() }
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Continuous Live Conversation Toggle Pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (uiState.isContinuousMode) NeonGreen.copy(alpha = 0.2f) else HudSurface)
                        .border(1.dp, if (uiState.isContinuousMode) NeonGreen else HudBorder, RoundedCornerShape(16.dp))
                        .clickable { viewModel.toggleContinuousConversationMode() }
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                        .testTag("continuous_voice_toggle"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Hearing,
                        contentDescription = null,
                        tint = if (uiState.isContinuousMode) NeonGreen else ArcCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (uiState.isContinuousMode) "LIVE CONVERSATION: ON" else "HANDS-FREE MODE: OFF",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (uiState.isContinuousMode) NeonGreen else TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Arc Reactor Centerpiece with Interactive Pulsing Glow
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    JarvisArcReactorVisualizer(
                        voiceState = uiState.voiceState,
                        soundLevel = uiState.soundLevel,
                        onClick = {
                            if (uiState.voiceState == VoiceState.LISTENING) {
                                viewModel.stopListening()
                            } else {
                                viewModel.startListening()
                            }
                        }
                    )
                }

                // Reactor Status Subtitle in English
                val statusText = when (uiState.voiceState) {
                    VoiceState.LISTENING -> "LISTENING... SPEAK IN TELUGU OR ENGLISH"
                    VoiceState.SPEAKING -> "DEV AI SPEAKING IN TELUGU..."
                    VoiceState.PROCESSING -> "PROCESSING WITH GEMINI AI..."
                    VoiceState.ERROR -> "SYSTEM RE-CALIBRATING..."
                    VoiceState.IDLE -> "TAP REACTOR TO COMMAND DEV AI"
                }

                val statusColor = when (uiState.voiceState) {
                    VoiceState.LISTENING -> NeonGreen
                    VoiceState.SPEAKING -> ArcCyan
                    VoiceState.PROCESSING -> StarkGold
                    VoiceState.ERROR -> NeonRed
                    VoiceState.IDLE -> ArcCyan.copy(alpha = 0.85f)
                }

                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor,
                    modifier = Modifier.padding(top = 4.dp)
                )

                // Real-time Speech Recognizer Partial Output Display
                if (uiState.lastRecognizedText.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(ArcCyan.copy(alpha = 0.12f))
                            .border(1.dp, ArcCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "🎙️ \"${uiState.lastRecognizedText}\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = StarkGold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action Deck Cards in English (Jai Shree Ram, Screen Lock, Wake Screen, Messaging, Apps, Automation)
                QuickActionDeck(
                    onJaiShreeRam = { viewModel.speakJaiShreeRam() },
                    onLockScreen = { viewModel.lockScreen() },
                    onWakeScreen = { viewModel.wakeScreen() },
                    onOpenMessage = { viewModel.setQuickMessageDialogVisible(true) },
                    onOpenApps = { viewModel.setAppsDialogVisible(true) },
                    onOpenAutomation = { viewModel.setAutomationDialogVisible(true) },
                    appsCount = uiState.installedApps.size
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Chat / Interaction Log
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(HudSurface.copy(alpha = 0.7f))
                        .border(1.dp, HudBorder, RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    LazyColumn(
                        state = chatListState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.messages, key = { it.id }) { msg ->
                            ChatMessageCard(
                                message = msg,
                                onSpeakAgain = { viewModel.speakMessage(msg.text) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Dialogs
        if (uiState.showAppsDialog) {
            AppsLauncherDialog(
                apps = uiState.installedApps,
                onLaunchApp = { pkg -> viewModel.launchAppByPackage(pkg) },
                onDismiss = { viewModel.setAppsDialogVisible(false) }
            )
        }

        if (uiState.showAutomationDialog) {
            AutomationDialog(
                routines = uiState.routines,
                onExecuteRoutine = { routine -> viewModel.executeRoutine(routine) },
                onAddNewTask = {
                    viewModel.setAutomationDialogVisible(false)
                    viewModel.setCustomAutomationDialogVisible(true)
                },
                onDismiss = { viewModel.setAutomationDialogVisible(false) }
            )
        }

        if (uiState.showCustomAutomationDialog) {
            CustomAutomationDialog(
                onSaveRoutine = { routine -> viewModel.addCustomRoutine(routine) },
                onDismiss = { viewModel.setCustomAutomationDialogVisible(false) }
            )
        }

        if (uiState.showQuickMessageDialog) {
            QuickMessageDialog(
                initialText = uiState.initialMessageText,
                onSendMessage = { platform, phoneNumber, message ->
                    if (platform == "sms") {
                        viewModel.sendSmsMessage(phoneNumber, message)
                    } else {
                        viewModel.sendWhatsAppMessage(phoneNumber, message)
                    }
                },
                onDismiss = { viewModel.setQuickMessageDialogVisible(false) }
            )
        }

        if (uiState.showLockPermissionDialog) {
            LockPermissionDialog(
                onEnableAccessibility = { viewModel.openAccessibilitySettings() },
                onEnableDeviceAdmin = {
                    if (activity != null) {
                        viewModel.requestDeviceAdmin(activity)
                    } else {
                        viewModel.openAccessibilitySettings()
                    }
                },
                onSimulateLock = { viewModel.simulateStandbyLock() },
                onDismiss = { viewModel.setLockPermissionDialogVisible(false) }
            )
        }

        // Simulated Lock Screen Overlay
        SimulatedLockOverlay(
            isLocked = uiState.isSimulatedLocked,
            onUnlock = { viewModel.unlockSimulatedStandby() }
        )
    }
}

@Composable
private fun TopHudBar(
    batteryPercent: Int,
    isCharging: Boolean,
    isTorchOn: Boolean,
    onToggleTorch: () -> Unit,
    onResetChat: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // System telemetry badge for DEV AI
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(HudSurface)
                .border(1.dp, HudBorder, RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(NeonGreen)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "DEV AI // ONLINE",
                style = MaterialTheme.typography.labelSmall,
                color = ArcCyan
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Voice Output Language Badge (Speaks in Telugu)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(DivineSaffron.copy(alpha = 0.15f))
                    .border(1.dp, DivineSaffron.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.RecordVoiceOver,
                    contentDescription = null,
                    tint = DivineSaffron,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "SPEECH: TELUGU",
                    style = MaterialTheme.typography.labelSmall,
                    color = StarkGold
                )
            }

            // Reset conversation
            IconButton(
                onClick = onResetChat,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(HudSurface)
                    .border(1.dp, HudBorder, CircleShape)
                    .testTag("reset_chat_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset Chat",
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Torch quick toggle
            IconButton(
                onClick = onToggleTorch,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isTorchOn) StarkGold.copy(alpha = 0.25f) else HudSurface)
                    .border(1.dp, if (isTorchOn) StarkGold else HudBorder, CircleShape)
                    .testTag("torch_toggle_top")
            ) {
                Icon(
                    imageVector = if (isTorchOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                    contentDescription = "Flashlight",
                    tint = if (isTorchOn) StarkGold else TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Battery Badge
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(HudSurface)
                    .border(1.dp, HudBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
                    contentDescription = "Battery",
                    tint = if (isCharging) NeonGreen else ArcCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$batteryPercent%",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextPrimary
                )
            }
        }
    }
}

@Composable
private fun QuickActionDeck(
    onJaiShreeRam: () -> Unit,
    onLockScreen: () -> Unit,
    onWakeScreen: () -> Unit,
    onOpenMessage: () -> Unit,
    onOpenApps: () -> Unit,
    onOpenAutomation: () -> Unit,
    appsCount: Int
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Row 1: Jai Shree Ram, Lock Screen, Wake Screen
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 1. JAI SHREE RAM Golden Button
            Card(
                modifier = Modifier
                    .weight(1.3f)
                    .clickable(onClick = onJaiShreeRam)
                    .border(1.2.dp, DivineSaffron, RoundedCornerShape(12.dp))
                    .testTag("jai_shree_ram_button"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DivineSaffron.copy(alpha = 0.18f))
            ) {
                Row(
                    modifier = Modifier
                        .padding(vertical = 8.dp, horizontal = 8.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "🕉️",
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "JAI SHREE RAM",
                            style = MaterialTheme.typography.titleMedium,
                            color = StarkGold,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "జై శ్రీ రామ్",
                            style = MaterialTheme.typography.labelSmall,
                            color = DivineSaffron
                        )
                    }
                }
            }

            // 2. Screen Lock Button
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onLockScreen)
                    .border(1.dp, ArcCyanDark, RoundedCornerShape(12.dp))
                    .testTag("lock_screen_button"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = HudSurface)
            ) {
                Column(
                    modifier = Modifier
                        .padding(vertical = 8.dp, horizontal = 4.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Lock",
                        tint = ArcCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "LOCK SCREEN",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextPrimary
                    )
                }
            }

            // 3. Wake Screen Button
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onWakeScreen)
                    .border(1.dp, StarkGold.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .testTag("wake_screen_button"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = HudSurface)
            ) {
                Column(
                    modifier = Modifier
                        .padding(vertical = 8.dp, horizontal = 4.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = "Wake Screen",
                        tint = StarkGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "WAKE SCREEN",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextPrimary
                    )
                }
            }
        }

        // Row 2: Quick Message, Apps, Automation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 4. Quick Message Button
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onOpenMessage)
                    .border(1.dp, NeonGreen.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .testTag("quick_message_button"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = HudSurface)
            ) {
                Column(
                    modifier = Modifier
                        .padding(vertical = 8.dp, horizontal = 4.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = "Message",
                        tint = NeonGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "MESSAGE",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextPrimary
                    )
                }
            }

            // 5. Apps Button
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onOpenApps)
                    .border(1.dp, HudBorder, RoundedCornerShape(12.dp))
                    .testTag("open_apps_button"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = HudSurface)
            ) {
                Column(
                    modifier = Modifier
                        .padding(vertical = 8.dp, horizontal = 4.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Apps,
                        contentDescription = "Apps",
                        tint = ArcCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "APPS ($appsCount)",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextPrimary
                    )
                }
            }

            // 6. Automation Protocols Button
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onOpenAutomation)
                    .border(1.dp, HudBorder, RoundedCornerShape(12.dp))
                    .testTag("automation_button"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = HudSurface)
            ) {
                Column(
                    modifier = Modifier
                        .padding(vertical = 8.dp, horizontal = 4.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.SettingsSuggest,
                        contentDescription = "Automation",
                        tint = StarkGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "ROUTINES",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatMessageCard(
    message: ChatMessage,
    onSpeakAgain: () -> Unit
) {
    val isUser = message.sender == Sender.USER

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        val bubbleShape = if (isUser) {
            RoundedCornerShape(16.dp, 4.dp, 16.dp, 16.dp)
        } else {
            RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp)
        }

        val containerColor = when {
            message.isSacred -> DivineSaffron.copy(alpha = 0.18f)
            isUser -> ArcCyanDark.copy(alpha = 0.22f)
            else -> DarkHudBg
        }

        val borderColor = when {
            message.isSacred -> DivineSaffron.copy(alpha = 0.6f)
            isUser -> ArcCyanDark.copy(alpha = 0.5f)
            else -> HudBorder
        }

        Card(
            modifier = Modifier
                .fillMaxWidth(if (isUser) 0.8f else 0.94f)
                .border(1.dp, borderColor, bubbleShape),
            shape = bubbleShape,
            colors = CardDefaults.cardColors(containerColor = containerColor)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isUser) "YOU" else "DEV AI (SPEAKS TELUGU)",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isUser) ArcCyan else StarkGold
                    )

                    if (!isUser) {
                        IconButton(
                            onClick = onSpeakAgain,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Speak",
                                tint = ArcCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                if (message.actionBadge != null) {
                    Box(
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(ArcCyan.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "ACTION: ${message.actionBadge}",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonGreen
                        )
                    }
                }

                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary
                )
            }
        }
    }
}

@Composable
private fun BottomCommandBar(
    textInput: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    voiceState: VoiceState,
    onToggleVoice: () -> Unit,
    onQuickChipClick: (String) -> Unit
) {
    val quickChips = listOf(
        "Jai Shree Ram 🙏",
        "Show Apps 📱",
        "Open YouTube 🎬",
        "Open WhatsApp 💬",
        "Send Message ✉️",
        "Wake Screen ☀️",
        "Lock Screen 🔒",
        "Turn on Torch 🔦",
        "Run Automations ⚙️",
        "Battery Status 🔋"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(VoidBlack.copy(alpha = 0.94f))
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // Quick suggestion chips in English
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(quickChips) { chip ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(HudSurface)
                        .border(1.dp, HudBorder, RoundedCornerShape(12.dp))
                        .clickable { onQuickChipClick(chip) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = chip,
                        style = MaterialTheme.typography.bodyMedium,
                        color = StarkGold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Input Console Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = textInput,
                onValueChange = onTextChange,
                modifier = Modifier
                    .weight(1f)
                    .testTag("command_input_field"),
                placeholder = {
                    Text(
                        text = "Command Dev AI (e.g., Open YouTube)...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ArcCyan,
                    unfocusedBorderColor = HudBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = HudSurface,
                    unfocusedContainerColor = HudSurface
                ),
                trailingIcon = {
                    if (textInput.isNotBlank()) {
                        IconButton(
                            onClick = onSend,
                            modifier = Modifier.testTag("send_command_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send",
                                tint = ArcCyan
                            )
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Prominent Glowing Mic Button for SpeechRecognizer API
            val isListening = voiceState == VoiceState.LISTENING
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(if (isListening) NeonRed else ArcCyan)
                    .clickable(onClick = onToggleVoice)
                    .border(2.dp, if (isListening) NeonRed else ArcCyanDark, CircleShape)
                    .testTag("voice_mic_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = "Voice Input",
                    tint = VoidBlack,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
