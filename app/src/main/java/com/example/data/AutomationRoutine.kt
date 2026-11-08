package com.example.data

sealed interface RoutineAction {
    data class SpeakText(val textTe: String, val textEn: String) : RoutineAction
    data object LockDevice : RoutineAction
    data object WakeScreen : RoutineAction
    data class LaunchApp(val appQuery: String) : RoutineAction
    data class ToggleTorch(val enable: Boolean) : RoutineAction
    data class SetVolume(val levelPercent: Int) : RoutineAction
    data class SendMessage(val platform: String, val recipient: String, val messageText: String) : RoutineAction
    data class SetTimer(val seconds: Int, val label: String) : RoutineAction
    data class SearchWeb(val query: String) : RoutineAction
    data object EmergencySos : RoutineAction
    data class OpenSettings(val target: String) : RoutineAction
    data class Delay(val seconds: Int) : RoutineAction
    data object CheckBattery : RoutineAction
}

data class AutomationRoutine(
    val id: String,
    val titleTe: String,
    val titleEn: String,
    val descriptionTe: String,
    val descriptionEn: String,
    val triggerPhrases: List<String>,
    val icon: String,
    val actions: List<RoutineAction>,
    val isCustom: Boolean = false
)
