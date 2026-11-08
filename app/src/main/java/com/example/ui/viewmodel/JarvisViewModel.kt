package com.example.ui.viewmodel

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.ActionType
import com.example.ai.JarvisAiRepository
import com.example.data.AppInfo
import com.example.data.AutomationManager
import com.example.data.AutomationRoutine
import com.example.data.RoutineAction
import com.example.speech.JarvisVoiceManager
import com.example.speech.VoiceState
import com.example.util.BatteryInfo
import com.example.util.DeviceController
import com.example.util.LockResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

data class ChatMessage(
    val id: String,
    val sender: Sender,
    val text: String,
    val actionBadge: String? = null,
    val isSacred: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

enum class Sender {
    USER,
    JARVIS
}

data class JarvisUiState(
    val preferTelugu: Boolean = true,
    val isContinuousMode: Boolean = false,
    val voiceState: VoiceState = VoiceState.IDLE,
    val soundLevel: Float = 0f,
    val lastRecognizedText: String = "",
    val errorMessage: String? = null,
    val messages: List<ChatMessage> = emptyList(),
    val installedApps: List<AppInfo> = emptyList(),
    val routines: List<AutomationRoutine> = emptyList(),
    val batteryInfo: BatteryInfo = BatteryInfo(95, false, "బ్యాటరీ 95%"),
    val isTorchOn: Boolean = false,
    val isSimulatedLocked: Boolean = false,
    val showAppsDialog: Boolean = false,
    val showAutomationDialog: Boolean = false,
    val showLockPermissionDialog: Boolean = false,
    val showQuickMessageDialog: Boolean = false,
    val initialMessageText: String = "",
    val showCustomAutomationDialog: Boolean = false,
    val activeRoutineName: String? = null
)

class JarvisViewModel(application: Application) : AndroidViewModel(application) {

    private val deviceController = DeviceController(application)
    private val voiceManager = JarvisVoiceManager(application)
    private val automationManager = AutomationManager()
    private val aiRepository = JarvisAiRepository()

    private var currentActivity: WeakReference<Activity>? = null

    private val _uiState = MutableStateFlow(
        JarvisUiState(
            preferTelugu = true,
            messages = listOf(
                ChatMessage(
                    id = "msg_init",
                    sender = Sender.JARVIS,
                    text = "జై శ్రీ రామ్! నేను మీ దేవ్ AI (DEV AI) అసిస్టెంట్. నేను మీకు తెలుగులో సమాధానాలు చెబుతూ, మీ వాయిస్ కమాండ్స్ ద్వారా యాప్స్ ఓపెన్ చేయడం, స్క్రీన్ లాక్ మరియు ఆన్ చేయడం, వాట్సాప్ మెసేజింగ్, టార్చ్, మరియు ఆటోమేషన్ పనులు చేయగలను.",
                    isSacred = true
                )
            )
        )
    )
    val uiState: StateFlow<JarvisUiState> = _uiState.asStateFlow()

    init {
        // Observe voice state and sound level for reactor animations
        viewModelScope.launch {
            voiceManager.voiceState.collect { state ->
                _uiState.value = _uiState.value.copy(voiceState = state)
            }
        }

        viewModelScope.launch {
            voiceManager.soundLevel.collect { level ->
                _uiState.value = _uiState.value.copy(soundLevel = level)
            }
        }

        viewModelScope.launch {
            voiceManager.lastRecognizedText.collect { text ->
                _uiState.value = _uiState.value.copy(lastRecognizedText = text)
            }
        }

        viewModelScope.launch {
            voiceManager.errorMessage.collect { error ->
                _uiState.value = _uiState.value.copy(errorMessage = error)
            }
        }

        viewModelScope.launch {
            automationManager.routines.collect { routines ->
                _uiState.value = _uiState.value.copy(routines = routines)
            }
        }

        // Setup speech completion callback
        voiceManager.onSpeechResultCallback = { recognizedText ->
            processCommand(recognizedText)
        }

        refreshDeviceStatus()
    }

    fun attachActivity(activity: Activity) {
        currentActivity = WeakReference(activity)
    }

    fun refreshDeviceStatus() {
        viewModelScope.launch {
            val battery = deviceController.getBatteryInfo()
            val apps = deviceController.getInstalledApps()
            val torch = deviceController.isFlashlightOn()
            _uiState.value = _uiState.value.copy(
                batteryInfo = battery,
                installedApps = apps,
                isTorchOn = torch
            )
        }
    }

    fun toggleLanguage() {
        val newPref = !_uiState.value.preferTelugu
        _uiState.value = _uiState.value.copy(preferTelugu = newPref)
        val text = if (newPref) "తెలుగు భాష ఎంపిక చేయబడింది." else "Language set to English."
        voiceManager.speak(text, isTelugu = newPref)
    }

    fun toggleContinuousConversationMode() {
        val newMode = !_uiState.value.isContinuousMode
        _uiState.value = _uiState.value.copy(isContinuousMode = newMode)
        voiceManager.continuousMode = newMode
        val msg = if (newMode) "నిరంతర వాయిస్ సంభాషణ ఆన్ చేయబడింది." else "సాధారణ వాయిస్ మోడ్."
        voiceManager.speak(msg, isTelugu = true)
    }

    fun speakJaiShreeRam() {
        val userMsg = ChatMessage(
            id = "msg_${System.currentTimeMillis()}_u",
            sender = Sender.USER,
            text = "Jai Shree Ram 🙏",
            isSacred = true
        )
        val jarvisMsg = ChatMessage(
            id = "msg_${System.currentTimeMillis()}_j",
            sender = Sender.JARVIS,
            text = "జై శ్రీ రామ్! జై శ్రీ రామ్! సీతా లక్ష్మణ సమేత శ్రీరామచంద్ర ప్రభువు యొక్క కృపాకటాక్షములు మీకు మరియు మీ కుటుంబానికి ఎల్లవేళలా ఉండాలని ప్రార్థిస్తున్నాను.",
            actionBadge = "JAI SHREE RAM",
            isSacred = true
        )
        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + userMsg + jarvisMsg
        )
        voiceManager.speakJaiShreeRam()
    }

    fun startListening() {
        val langCode = if (_uiState.value.preferTelugu) "te-IN" else "en-IN"
        voiceManager.startListening(langCode) { recognizedText ->
            processCommand(recognizedText)
        }
    }

    fun stopListening() {
        voiceManager.stopListening()
    }

    fun processCommand(command: String) {
        if (command.isBlank()) return

        val userMsg = ChatMessage(
            id = "msg_${System.currentTimeMillis()}_u",
            sender = Sender.USER,
            text = command
        )
        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + userMsg,
            lastRecognizedText = ""
        )

        viewModelScope.launch {
            try {
                val parsed = aiRepository.processUserCommand(command, _uiState.value.preferTelugu)

                val badge = if (parsed.actionType != ActionType.NONE) parsed.actionType.name else null
                val jarvisMsg = ChatMessage(
                    id = "msg_${System.currentTimeMillis()}_j",
                    sender = Sender.JARVIS,
                    text = parsed.replyText,
                    actionBadge = badge,
                    isSacred = parsed.actionType == ActionType.JAI_SHREE_RAM
                )
                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + jarvisMsg
                )

                // Always speak response in Telugu
                voiceManager.speak(parsed.replyText, isTelugu = true)

                // Execute parsed hardware / OS action
                executeAction(parsed.actionType, parsed.actionTarget, parsed.extraData)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val errorMsg = "Error: ${e.localizedMessage}"
                val jarvisMsg = ChatMessage(
                    id = "msg_${System.currentTimeMillis()}_err",
                    sender = Sender.JARVIS,
                    text = errorMsg
                )
                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + jarvisMsg
                )
            }
        }
    }

    private fun executeAction(actionType: ActionType, target: String, extra: String = "") {
        when (actionType) {
            ActionType.LOCK_SCREEN -> {
                lockScreen()
            }
            ActionType.WAKE_SCREEN -> {
                wakeScreen()
            }
            ActionType.OPEN_APP -> {
                openAppByQuery(target)
            }
            ActionType.SHOW_APPS -> {
                setAppsDialogVisible(true)
            }
            ActionType.TOGGLE_TORCH -> {
                val turnOff = target.contains("off")
                deviceController.toggleFlashlight(if (turnOff) false else true)
                _uiState.value = _uiState.value.copy(isTorchOn = deviceController.isFlashlightOn())
            }
            ActionType.SET_VOLUME -> {
                val percent = target.toIntOrNull() ?: 50
                deviceController.setVolumePercent(percent)
            }
            ActionType.SEND_MESSAGE -> {
                val messageText = if (extra.isNotBlank()) extra else target
                deviceController.sendWhatsAppMessage(null, messageText)
            }
            ActionType.EMERGENCY_SOS -> {
                deviceController.toggleFlashlight(true)
                deviceController.openEmergencyDialer()
            }
            ActionType.SEARCH_WEB -> {
                deviceController.searchWeb(target)
            }
            ActionType.RUN_ROUTINE -> {
                val routine = automationManager.findRoutineByTrigger(target)
                    ?: _uiState.value.routines.firstOrNull()
                if (routine != null) {
                    executeRoutine(routine)
                }
            }
            ActionType.BATTERY_CHECK -> {
                val battery = deviceController.getBatteryInfo()
                _uiState.value = _uiState.value.copy(batteryInfo = battery)
                voiceManager.speak(battery.statusText, isTelugu = true)
            }
            ActionType.JAI_SHREE_RAM -> {
                // Handled in speech
            }
            ActionType.NONE -> {
                // No hardware action needed
            }
        }
    }

    fun wakeScreen() {
        _uiState.value = _uiState.value.copy(isSimulatedLocked = false)
        deviceController.wakeScreen(currentActivity?.get())
    }

    fun lockScreen() {
        val result = deviceController.lockScreen()
        when (result) {
            is LockResult.Success -> {
                // Screen locked natively
            }
            is LockResult.NeedsAccessibilityPermission, is LockResult.NeedsDeviceAdminPermission -> {
                _uiState.value = _uiState.value.copy(showLockPermissionDialog = true)
            }
            is LockResult.Error -> {
                _uiState.value = _uiState.value.copy(showLockPermissionDialog = true)
            }
        }
    }

    fun openAppByQuery(query: String) {
        val (success, matchedLabel) = deviceController.findAndLaunchApp(query)
        if (success) {
            val confirmed = "${matchedLabel ?: query} అప్లికేషన్ ఓపెన్ చేయబడింది సర్."
            voiceManager.speak(confirmed, isTelugu = true)
        } else {
            _uiState.value = _uiState.value.copy(showAppsDialog = true)
            val notFound = "'$query' యాప్ నేరుగా కనుగొనబడలేదు. మీ ఇన్‌స్టాల్ చేసిన యాప్‌ల లిస్ట్ స్క్రీన్‌పై చూపించబడింది, దయచేసి ఎంచుకోండి."
            voiceManager.speak(notFound, isTelugu = true)
        }
    }

    fun launchAppByPackage(packageName: String) {
        deviceController.launchAppByPackage(packageName)
    }

    fun toggleTorch() {
        deviceController.toggleFlashlight()
        _uiState.value = _uiState.value.copy(isTorchOn = deviceController.isFlashlightOn())
    }

    fun sendWhatsAppMessage(phone: String?, message: String) {
        deviceController.sendWhatsAppMessage(phone, message)
    }

    fun sendSmsMessage(phone: String?, message: String) {
        deviceController.sendSmsMessage(phone, message)
    }

    fun addCustomRoutine(routine: AutomationRoutine) {
        automationManager.addCustomRoutine(routine)
    }

    fun executeRoutine(routine: AutomationRoutine) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(activeRoutineName = routine.titleEn)
            for (action in routine.actions) {
                when (action) {
                    is RoutineAction.SpeakText -> {
                        voiceManager.speak(action.textTe, isTelugu = true)
                        delay(2000)
                    }
                    is RoutineAction.WakeScreen -> {
                        wakeScreen()
                        delay(500)
                    }
                    is RoutineAction.LockDevice -> {
                        lockScreen()
                    }
                    is RoutineAction.LaunchApp -> {
                        deviceController.findAndLaunchApp(action.appQuery)
                    }
                    is RoutineAction.ToggleTorch -> {
                        deviceController.toggleFlashlight(action.enable)
                        _uiState.value = _uiState.value.copy(isTorchOn = deviceController.isFlashlightOn())
                    }
                    is RoutineAction.SetVolume -> {
                        deviceController.setVolumePercent(action.levelPercent)
                    }
                    is RoutineAction.SendMessage -> {
                        if (action.platform == "sms") {
                            deviceController.sendSmsMessage(action.recipient, action.messageText)
                        } else {
                            deviceController.sendWhatsAppMessage(action.recipient, action.messageText)
                        }
                    }
                    is RoutineAction.EmergencySos -> {
                        deviceController.toggleFlashlight(true)
                        deviceController.openEmergencyDialer()
                    }
                    is RoutineAction.SearchWeb -> {
                        deviceController.searchWeb(action.query)
                    }
                    is RoutineAction.OpenSettings -> {
                        deviceController.openSettings(action.target)
                    }
                    is RoutineAction.Delay -> {
                        delay((action.seconds * 1000).toLong())
                    }
                    is RoutineAction.CheckBattery -> {
                        val battery = deviceController.getBatteryInfo()
                        _uiState.value = _uiState.value.copy(batteryInfo = battery)
                        voiceManager.speak(battery.statusText, isTelugu = true)
                        delay(1500)
                    }
                    is RoutineAction.SetTimer -> {
                        deviceController.setTimer(action.seconds, action.label)
                    }
                }
            }
            _uiState.value = _uiState.value.copy(activeRoutineName = null)
        }
    }

    fun openAccessibilitySettings() {
        deviceController.openAccessibilitySettings()
    }

    fun requestDeviceAdmin(activity: Activity) {
        deviceController.requestDeviceAdmin(activity)
    }

    fun simulateStandbyLock() {
        _uiState.value = _uiState.value.copy(isSimulatedLocked = true)
    }

    fun unlockSimulatedStandby() {
        wakeScreen()
        val welcome = "జై శ్రీ రామ్! దేవ్ AI తిరిగి సిద్ధంగా ఉంది."
        voiceManager.speak(welcome, isTelugu = true)
    }

    fun setAppsDialogVisible(visible: Boolean) {
        _uiState.value = _uiState.value.copy(showAppsDialog = visible)
    }

    fun setAutomationDialogVisible(visible: Boolean) {
        _uiState.value = _uiState.value.copy(showAutomationDialog = visible)
    }

    fun setLockPermissionDialogVisible(visible: Boolean) {
        _uiState.value = _uiState.value.copy(showLockPermissionDialog = visible)
    }

    fun setQuickMessageDialogVisible(visible: Boolean, text: String = "") {
        _uiState.value = _uiState.value.copy(
            showQuickMessageDialog = visible,
            initialMessageText = text
        )
    }

    fun setCustomAutomationDialogVisible(visible: Boolean) {
        _uiState.value = _uiState.value.copy(showCustomAutomationDialog = visible)
    }

    fun speakMessage(text: String) {
        voiceManager.speak(text, isTelugu = true)
    }

    fun resetConversation() {
        aiRepository.resetConversation()
        _uiState.value = _uiState.value.copy(
            messages = listOf(
                ChatMessage(
                    id = "msg_${System.currentTimeMillis()}",
                    sender = Sender.JARVIS,
                    text = "జై శ్రీ రామ్! సంభాషణ రీసెట్ చేయబడింది. కొత్త కమాండ్ ఇవ్వండి.",
                    isSacred = true
                )
            )
        )
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.destroy()
    }
}
