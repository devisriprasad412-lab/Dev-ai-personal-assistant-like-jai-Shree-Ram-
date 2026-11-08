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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import com.example.data.AutomationRoutine
import com.example.data.RoutineAction
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.DarkHudBg
import com.example.ui.theme.HudBorder
import com.example.ui.theme.HudSurface
import com.example.ui.theme.StarkGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoidBlack

@Composable
fun CustomAutomationDialog(
    onSaveRoutine: (AutomationRoutine) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var triggerPhrase by remember { mutableStateOf("") }
    var actionWakeScreen by remember { mutableStateOf(false) }
    var actionTorch by remember { mutableStateOf(false) }
    var actionSpeakGreeting by remember { mutableStateOf(true) }
    var actionLockScreen by remember { mutableStateOf(false) }

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
                                .background(StarkGold.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Construction,
                                contentDescription = null,
                                tint = StarkGold,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "NEW AUTOMATION TASK",
                            style = MaterialTheme.typography.titleMedium,
                            color = ArcCyan
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = StarkGold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_routine_title"),
                    label = { Text("Task Name", color = TextSecondary) },
                    placeholder = { Text("e.g. Work Readiness Routine", color = TextSecondary) },
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

                OutlinedTextField(
                    value = triggerPhrase,
                    onValueChange = { triggerPhrase = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_routine_trigger"),
                    label = { Text("Voice Trigger Phrase", color = TextSecondary) },
                    placeholder = { Text("e.g. activate work mode", color = TextSecondary) },
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

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "SELECT ACTIONS TO EXECUTE:",
                    style = MaterialTheme.typography.labelSmall,
                    color = ArcCyan
                )

                Spacer(modifier = Modifier.height(6.dp))

                ActionCheckboxRow(
                    label = "Wake & Turn On Screen (వేక్ స్క్రీన్)",
                    checked = actionWakeScreen,
                    onCheckedChange = { actionWakeScreen = it }
                )

                ActionCheckboxRow(
                    label = "Speak Salutation in Telugu (జై శ్రీ రామ్)",
                    checked = actionSpeakGreeting,
                    onCheckedChange = { actionSpeakGreeting = it }
                )

                ActionCheckboxRow(
                    label = "Toggle Flashlight Torch (టార్చ్)",
                    checked = actionTorch,
                    onCheckedChange = { actionTorch = it }
                )

                ActionCheckboxRow(
                    label = "Lock Screen at End (స్క్రీన్ లాక్)",
                    checked = actionLockScreen,
                    onCheckedChange = { actionLockScreen = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val actions = mutableListOf<RoutineAction>()
                        if (actionWakeScreen) actions.add(RoutineAction.WakeScreen)
                        if (actionSpeakGreeting) {
                            actions.add(
                                RoutineAction.SpeakText(
                                    textTe = "జై శ్రీ రామ్! మీ కస్టమ్ ఆటోమేషన్ టాస్క్ విజయవంతంగా ఎగ్జిక్యూట్ అయ్యింది.",
                                    textEn = "Custom automation routine executed successfully."
                                )
                            )
                        }
                        if (actionTorch) actions.add(RoutineAction.ToggleTorch(true))
                        if (actionLockScreen) actions.add(RoutineAction.LockDevice)

                        val newRoutine = AutomationRoutine(
                            id = "custom_${System.currentTimeMillis()}",
                            titleTe = title.ifBlank { "కస్టమ్ ఆటోమేషన్" },
                            titleEn = title.ifBlank { "Custom Automation Task" },
                            descriptionTe = "యూజర్ క్రియేట్ చేసిన అడ్వాన్స్డ్ టాస్క్.",
                            descriptionEn = "User created custom automation task.",
                            triggerPhrases = listOf(triggerPhrase.ifBlank { title.lowercase() }),
                            icon = "construction",
                            actions = actions,
                            isCustom = true
                        )
                        onSaveRoutine(newRoutine)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_custom_routine_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ArcCyan)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = VoidBlack)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("SAVE AUTOMATION TASK", color = VoidBlack, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun ActionCheckboxRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = ArcCyan,
                uncheckedColor = TextSecondary,
                checkmarkColor = VoidBlack
            )
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary
        )
    }
}
