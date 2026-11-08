package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.AutomationRoutine
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
fun AutomationDialog(
    routines: List<AutomationRoutine>,
    onExecuteRoutine: (AutomationRoutine) -> Unit,
    onAddNewTask: () -> Unit = {},
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(24.dp))
                .background(DarkHudBg)
                .border(1.5.dp, ArcCyan.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "AUTOMATION PROTOCOLS // DEV AI",
                            style = MaterialTheme.typography.titleLarge,
                            color = ArcCyan
                        )
                        Text(
                            text = "One-tap voice & device automation routines",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("close_automation_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = StarkGold
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AVAILABLE PROTOCOLS (${routines.size})",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Button(
                        onClick = onAddNewTask,
                        colors = ButtonDefaults.buttonColors(containerColor = StarkGold),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("add_new_task_button")
                    ) {
                        Text("+ ADD NEW TASK", color = VoidBlack, style = MaterialTheme.typography.labelSmall)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(routines, key = { it.id }) { routine ->
                        RoutineCard(
                            routine = routine,
                            onRun = {
                                onExecuteRoutine(routine)
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RoutineCard(
    routine: AutomationRoutine,
    onRun: () -> Unit
) {
    val iconVector: ImageVector = when (routine.icon) {
        "sun" -> Icons.Default.WbSunny
        "lock" -> Icons.Default.Lock
        "flashlight" -> Icons.Default.FlashlightOn
        "volume_off" -> Icons.Default.VolumeMute
        "moon" -> Icons.Default.NightlightRound
        else -> Icons.Default.PlayArrow
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, HudBorder, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = HudSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(ArcCyan.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = null,
                        tint = ArcCyan,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = routine.titleEn,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = routine.titleTe,
                        style = MaterialTheme.typography.bodyMedium,
                        color = StarkGold
                    )
                }

                Button(
                    onClick = onRun,
                    colors = ButtonDefaults.buttonColors(containerColor = ArcCyan),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("run_routine_${routine.id}")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = VoidBlack)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("RUN", color = VoidBlack, style = MaterialTheme.typography.labelLarge)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = routine.descriptionTe,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Trigger chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                routine.triggerPhrases.take(2).forEach { trigger ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(DarkHudBg)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "\"$trigger\"",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonGreen
                        )
                    }
                }
            }
        }
    }
}
