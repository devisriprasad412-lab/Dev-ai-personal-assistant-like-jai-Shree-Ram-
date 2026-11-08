package com.example.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AutomationManager {

    private val defaultRoutines = listOf(
        AutomationRoutine(
            id = "routine_morning",
            titleTe = "జై శ్రీ రామ్ ప్రాతః కాలం",
            titleEn = "Morning Devotional & Status",
            descriptionTe = "జై శ్రీ రామ్ వందనం, సమయం, బ్యాటరీ స్థితి మరియు సిస్టమ్ డయాగ్నోస్టిక్స్.",
            descriptionEn = "Devotional greeting, time, battery status, and Dev AI system readiness.",
            triggerPhrases = listOf("jai shree ram", "జై శ్రీ రామ్", "good morning", "శుభోదయం", "morning routine"),
            icon = "sun",
            actions = listOf(
                RoutineAction.SpeakText(
                    textTe = "జై శ్రీ రామ్! శుభోదయం సర్. దేవ్ AI సిస్టమ్స్ అన్నీ నూరు శాతం ఆపరేషనల్ గా సిద్ధంగా ఉన్నాయి.",
                    textEn = "Jai Shree Ram! Good morning sir. Dev AI systems are fully operational."
                ),
                RoutineAction.CheckBattery,
                RoutineAction.SetVolume(80)
            )
        ),
        AutomationRoutine(
            id = "routine_wake_screen",
            titleTe = "వేక్ & ఓపెన్ స్క్రీన్",
            titleEn = "Wake & Open Screen Protocol",
            descriptionTe = "స్క్రీన్‌ను తక్షణమే ఆన్ చేసి వేక్ అప్ చేస్తుంది.",
            descriptionEn = "Wakes up and turns on the device screen instantly.",
            triggerPhrases = listOf("open screen", "wake screen", "turn on screen", "screen open", "స్క్రీన్ ఆన్ చెయ్యి", "వేక్ అప్"),
            icon = "visibility",
            actions = listOf(
                RoutineAction.WakeScreen,
                RoutineAction.SpeakText(
                    textTe = "స్క్రీన్ ఆన్ చేయబడింది సర్. దేవ్ AI సిద్ధంగా ఉంది.",
                    textEn = "Screen awakened and active, sir."
                )
            )
        ),
        AutomationRoutine(
            id = "routine_message",
            titleTe = "వాట్సాప్ మెసేజింగ్",
            titleEn = "WhatsApp Quick Message",
            descriptionTe = "వాట్సాప్ చాట్‌ను తెరిచి తక్షణమే మెసేజ్ పంపుతుంది.",
            descriptionEn = "Opens WhatsApp composer with pre-filled message text.",
            triggerPhrases = listOf("send message", "whatsapp message", "మెసేజ్ పంపు", "వాట్సాప్ మెసేజ్"),
            icon = "chat",
            actions = listOf(
                RoutineAction.SpeakText(
                    textTe = "వాట్సాప్ మెసేజింగ్ కన్సోల్ సిద్ధం చేస్తున్నాను సర్.",
                    textEn = "Preparing WhatsApp messaging console, sir."
                ),
                RoutineAction.SendMessage("whatsapp", "", "Hello, messaging via Dev AI!")
            )
        ),
        AutomationRoutine(
            id = "routine_lock_screen",
            titleTe = "స్క్రీన్ లాక్ & సెక్యూరిటీ",
            titleEn = "Screen Lock & Security",
            descriptionTe = "డివైస్ స్క్రీన్‌ను వెంటనే సురక్షితంగా లాక్ చేస్తుంది.",
            descriptionEn = "Instantly locks the device screen securely via Dev AI.",
            triggerPhrases = listOf("lock screen", "స్క్రీన్ లాక్", "lock phone", "లాక్ చెయ్యి", "secure device"),
            icon = "lock",
            actions = listOf(
                RoutineAction.SpeakText(
                    textTe = "స్క్రీన్ లాక్ చేయబడుతోంది. దేవ్ AI సెక్యూరిటీ యాక్టివేట్ అయ్యింది.",
                    textEn = "Locking device screen. Dev AI security active."
                ),
                RoutineAction.Delay(1),
                RoutineAction.LockDevice
            )
        ),
        AutomationRoutine(
            id = "routine_torch",
            titleTe = "ఫ్లాష్‌లైట్ టార్చ్ ఆన్/ఆఫ్",
            titleEn = "Flashlight Torch Toggle",
            descriptionTe = "కెమెరా ఫ్లాష్‌లైట్ టార్చ్‌ను ఆన్ లేదా ఆఫ్ చేస్తుంది.",
            descriptionEn = "Toggles mobile hardware flashlight torch on or off.",
            triggerPhrases = listOf("flashlight", "torch", "టార్చ్", "లైట్ వెయ్", "torch on"),
            icon = "flashlight",
            actions = listOf(
                RoutineAction.ToggleTorch(true),
                RoutineAction.SpeakText(
                    textTe = "ఫ్లాష్‌లైట్ టార్చ్ ఆన్ చేయబడింది సర్.",
                    textEn = "Flashlight torch activated, sir."
                )
            )
        ),
        AutomationRoutine(
            id = "routine_focus",
            titleTe = "ఫోకస్ / సైలెంట్ మోడ్",
            titleEn = "Focus & Silent Mode",
            descriptionTe = "వాల్యూమ్ మ్యూట్ చేస్తుంది మరియు అడ్డంకులను నివారిస్తుంది.",
            descriptionEn = "Mutes device volume to eliminate distractions.",
            triggerPhrases = listOf("focus mode", "silent mode", "ఫోకస్ మోడ్", "సైలెంట్ చెయ్యి"),
            icon = "volume_off",
            actions = listOf(
                RoutineAction.SetVolume(0),
                RoutineAction.SpeakText(
                    textTe = "డివైస్ సైలెంట్ చేయబడింది. ఫోకస్ మోడ్ ఆన్ లో ఉంది.",
                    textEn = "Device muted. Focus mode engaged."
                )
            )
        ),
        AutomationRoutine(
            id = "routine_sos",
            titleTe = "ఎమర్జెన్సీ SOS అలర్ట్",
            titleEn = "Emergency SOS Alert",
            descriptionTe = "ఫ్లాష్‌లైట్ హెచ్చరికను వెలిగించి ఎమర్జెన్సీ డయలర్‌ను తెరుస్తుంది.",
            descriptionEn = "Flashes emergency strobe alert and launches safety dialer.",
            triggerPhrases = listOf("emergency", "sos", "help me", "ఎమర్జెన్సీ", "కాపాడండి"),
            icon = "warning",
            actions = listOf(
                RoutineAction.SpeakText(
                    textTe = "ఎమర్జెన్సీ ప్రోటోకాల్ ప్రారంభించబడింది. సహాయక డయలర్ ఓపెన్ అవుతోంది.",
                    textEn = "Emergency SOS protocol activated. Opening assistance dialer."
                ),
                RoutineAction.ToggleTorch(true),
                RoutineAction.EmergencySos
            )
        ),
        AutomationRoutine(
            id = "routine_night",
            titleTe = "శుభరాత్రి & స్లీప్ మోడ్",
            titleEn = "Night Sleep Routine",
            descriptionTe = "శుభరాత్రి వందనం, వాల్యూమ్ తగ్గించడం మరియు స్క్రీన్ లాక్ చేయడం.",
            descriptionEn = "Good night blessing, volume dim, and auto screen lock.",
            triggerPhrases = listOf("good night", "sleep mode", "శుభరాత్రి", "పడుకుంటున్నాను"),
            icon = "moon",
            actions = listOf(
                RoutineAction.SpeakText(
                    textTe = "జై శ్రీ రామ్ సర్. మీకు శుభరాత్రి. దేవ్ AI స్లీప్ మోడ్‌లోకి వెళ్తోంది.",
                    textEn = "Jai Shree Ram sir. Good night. Dev AI entering sleep mode."
                ),
                RoutineAction.SetVolume(20),
                RoutineAction.ToggleTorch(false),
                RoutineAction.Delay(2),
                RoutineAction.LockDevice
            )
        )
    )

    private val _routines = MutableStateFlow(defaultRoutines)
    val routines: StateFlow<List<AutomationRoutine>> = _routines.asStateFlow()

    fun addCustomRoutine(routine: AutomationRoutine) {
        _routines.value = _routines.value + routine
    }

    fun findRoutineByTrigger(query: String): AutomationRoutine? {
        val lower = query.lowercase().trim()
        return _routines.value.firstOrNull { routine ->
            routine.triggerPhrases.any { trigger -> lower.contains(trigger.lowercase()) }
        }
    }
}
