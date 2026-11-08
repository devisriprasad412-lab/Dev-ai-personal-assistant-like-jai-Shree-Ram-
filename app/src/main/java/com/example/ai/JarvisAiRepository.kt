package com.example.ai

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.Chat
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import java.util.Locale

data class JarvisParsedResponse(
    val replyText: String,
    val actionType: ActionType,
    val actionTarget: String = "",
    val extraData: String = "",
    val isTelugu: Boolean = true
)

enum class ActionType {
    NONE,
    JAI_SHREE_RAM,
    LOCK_SCREEN,
    WAKE_SCREEN,
    OPEN_APP,
    SHOW_APPS,
    TOGGLE_TORCH,
    SET_VOLUME,
    SEND_MESSAGE,
    RUN_ROUTINE,
    BATTERY_CHECK,
    EMERGENCY_SOS,
    SEARCH_WEB
}

class JarvisAiRepository {

    // Lazy initialization of Firebase AI Logic model per guidelines
    private val model by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                temperature = 0.45f
            }
        )
    }

    // Interactive multi-turn chat session
    private var activeChatSession: Chat? = null

    private fun getOrCreateChatSession(): Chat {
        return activeChatSession ?: run {
            val initialHistory = listOf(
                content("user") {
                    text("System Setup: You are DEV AI (దేవ్ AI), an advanced personal AI assistant. Your primary spoken language is ALWAYS fluent, respectful, natural Telugu (తెలుగు). The user interface is in English, but you speak to the user in Telugu.")
                },
                content("model") {
                    text("నమస్కారం! నేను దేవ్ AI (DEV AI). జై శ్రీ రామ్! నేను మీకు తెలుగులో సమాధానాలు ఇస్తూ, మీ అన్ని ఆదేశాలను నెరవేర్చడానికి సిద్ధంగా ఉన్నాను.")
                }
            )
            val newChat = model.startChat(history = initialHistory)
            activeChatSession = newChat
            newChat
        }
    }

    suspend fun processUserCommand(input: String, preferTelugu: Boolean = true): JarvisParsedResponse {
        val trimmed = input.trim()
        val lower = trimmed.lowercase(Locale.ROOT)

        // 1. Instant Local Intent Matching for mission-critical hardware commands
        val localIntent = evaluateLocalIntent(lower)
        if (localIntent != null) {
            return localIntent
        }

        // 2. Multi-turn Conversational AI with integrated Gemini AI (Firebase AI Logic)
        return try {
            val systemContext = """
                You are DEV AI (దేవ్ AI), the user's personal assistant.
                User input: "$trimmed"
                
                CRITICAL INSTRUCTION:
                Speak your answer in clear, respectful, natural Telugu (తెలుగు) so Text-to-Speech can read it aloud smoothly to the user.
                
                Action Rules:
                1. If user says or mentions "Jai Shree Ram" ("జై శ్రీ రామ్"), Rama, or greets, reply with immense devotion saying "జై శ్రీ రామ్!" and provide auspicious blessings. Include tag [ACTION:JAI_SHREE_RAM:].
                2. If user requests screen lock ("lock screen", "స్క్రీన్ లాక్", "lock phone", "లాక్ చెయ్యి"), confirm in Telugu that you are locking the screen and include [ACTION:LOCK_SCREEN:].
                3. If user requests open screen or turn on display ("open screen", "wake screen", "turn on screen", "స్క్రీన్ ఆన్ చెయ్యి", "వేక్ అప్"), confirm in Telugu that screen is opened and include [ACTION:WAKE_SCREEN:].
                4. If user requests sending a message or chatting ("message", "whatsapp message", "send message", "మెసేజ్ పంపు"), confirm in Telugu and include [ACTION:SEND_MESSAGE:whatsapp:message_content].
                5. If user requests opening an app ("open youtube", "open whatsapp", "open camera", "యూట్యూబ్ ఓపెన్ చెయ్యి"), confirm in Telugu and include [ACTION:OPEN_APP:app_name].
                6. If user requests viewing or listing installed apps ("show apps", "list apps", "installed apps", "యాప్స్ చూపించు", "యాప్స్ లిస్ట్"), confirm in Telugu and include [ACTION:SHOW_APPS:].
                7. If user requests flashlight/torch ("flashlight", "torch", "లైట్ వెయ్"), confirm in Telugu and include [ACTION:TOGGLE_TORCH:on/off].
                8. If user requests emergency or help ("emergency", "sos", "ఎమర్జెన్సీ"), confirm and include [ACTION:EMERGENCY_SOS:].
                9. If user requests automation routine ("automation", "routine", "ఆటోమేషన్"), confirm in Telugu and include [ACTION:RUN_ROUTINE:routine_morning].
                10. For all other questions, knowledge, or conversation, provide a concise, smart answer in natural spoken Telugu (2-3 sentences max).
                
                Always start the output with: [ACTION:TYPE:TARGET] followed by the spoken Telugu response.
            """.trimIndent()

            val chat = getOrCreateChatSession()
            val response = withTimeout(40_000L) {
                chat.sendMessage(systemContext)
            }

            val text = response.text.orEmpty()
            parseModelResponse(text)
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Gemini AI call timed out, falling back to local reasoning", e)
            fallbackResponse(trimmed)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Gemini AI call failed, resetting chat session", e)
            activeChatSession = null
            fallbackResponse(trimmed)
        }
    }

    private fun evaluateLocalIntent(lower: String): JarvisParsedResponse? {
        // A. Jai Shree Ram
        if (lower.contains("jai shree ram") || lower.contains("jai sri ram") || lower.contains("జై శ్రీ రామ్") || lower.contains("శ్రీ రామ్") || lower.contains("ram")) {
            return JarvisParsedResponse(
                replyText = "జై శ్రీ రామ్! సీతా లక్ష్మణ సమేత శ్రీరామచంద్రుని దివ్య ఆశీస్సులు మీకు ఎల్లవేళలా ఉండుగాక!",
                actionType = ActionType.JAI_SHREE_RAM,
                actionTarget = "jai_shree_ram",
                isTelugu = true
            )
        }

        // B. Wake / Open Screen
        if (lower.contains("open screen") || lower.contains("wake screen") || lower.contains("turn on screen") || lower.contains("screen open") || lower.contains("స్క్రీన్ ఆన్ చెయ్యి") || lower.contains("వేక్ అప్") || lower.contains("స్క్రీన్ తెరువు")) {
            return JarvisParsedResponse(
                replyText = "స్క్రీన్ ఆన్ చేయబడింది సర్. దేవ్ AI సిద్ధంగా ఉంది.",
                actionType = ActionType.WAKE_SCREEN,
                isTelugu = true
            )
        }

        // C. Lock Screen
        if (lower.contains("lock screen") || lower.contains("screen lock") || lower.contains("స్క్రీన్ లాక్") || lower.contains("లాక్ చెయ్యి") || lower.contains("లాక్ చేయి") || lower.contains("lock phone") || lower.contains("lock")) {
            return JarvisParsedResponse(
                replyText = "దేవ్ AI ఆదేశానుసారం డివైస్ స్క్రీన్ లాక్ చేయబడుతోంది.",
                actionType = ActionType.LOCK_SCREEN,
                isTelugu = true
            )
        }

        // D. Messaging & Chatting (WhatsApp / SMS)
        if (lower.contains("send message") || lower.contains("whatsapp message") || lower.contains("మెసేజ్ పంపు") || lower.contains("వాట్సాప్ మెసేజ్") || lower.startsWith("message ") || lower.contains("చాట్ చెయ్యి")) {
            val messageText = extractMessageText(lower)
            return JarvisParsedResponse(
                replyText = "వాట్సాప్ మెసేజింగ్ విండో ఓపెన్ చేస్తున్నాను సర్.",
                actionType = ActionType.SEND_MESSAGE,
                actionTarget = "whatsapp",
                extraData = messageText,
                isTelugu = true
            )
        }

        // E. Emergency SOS
        if (lower.contains("emergency") || lower.contains("sos") || lower.contains("ఎమర్జెన్సీ") || lower.contains("హెల్ప్") || lower.contains("కాపాడండి")) {
            return JarvisParsedResponse(
                replyText = "ఎమర్జెన్సీ ప్రోటోకాల్ ప్రారంభించబడింది. సహాయక డయలర్ ఓపెన్ చేస్తున్నాను.",
                actionType = ActionType.EMERGENCY_SOS,
                isTelugu = true
            )
        }

        // F. Flashlight
        if (lower.contains("flashlight") || lower.contains("torch") || lower.contains("ఫ్లాష్‌లైట్") || lower.contains("టార్చ్") || lower.contains("లైట్ వెయ్") || lower.contains("లైట్ ఆపు")) {
            val turnOff = lower.contains("off") || lower.contains("ఆపు")
            return JarvisParsedResponse(
                replyText = if (turnOff) "ఫ్లాష్‌లైట్ ఆఫ్ చేయబడింది సర్." else "ఫ్లాష్‌లైట్ టార్చ్ ఆన్ చేయబడింది సర్.",
                actionType = ActionType.TOGGLE_TORCH,
                actionTarget = if (turnOff) "off" else "on",
                isTelugu = true
            )
        }

        // G1. Show / List Installed Apps
        if (lower.contains("show apps") || lower.contains("list apps") || lower.contains("all apps") ||
            lower.contains("view apps") || lower.contains("installed apps") || lower.contains("show all apps") ||
            lower.contains("యాప్స్ చూపించు") || lower.contains("యాప్స్ లిస్ట్") || lower.contains("నా యాప్స్") ||
            lower.contains("యాప్స్ లిస్ట్ చూపించు") || lower.contains("యాప్‌లు చూపించు") || lower.contains("యాప్ ల లిస్ట్")) {
            return JarvisParsedResponse(
                replyText = "మీ ఫోన్‌లో ఇన్‌స్టాల్ చేయబడిన యాప్‌ల జాబితా స్క్రీన్‌పై చూపించబడింది సర్. మీరు ఏ యాప్‌నైనా నేరుగా వాయిస్ కమాండ్ ద్వారా లేదా ట్యాప్ చేసి ఓపెన్ చేయవచ్చు.",
                actionType = ActionType.SHOW_APPS,
                actionTarget = "installed_apps",
                isTelugu = true
            )
        }

        // G2. Open App via Voice Command
        if (lower.startsWith("open ") || lower.startsWith("launch ") || lower.startsWith("start ") ||
            lower.contains("ఓపెన్ చెయ్యి") || lower.contains("ఓపెన్ చేయి") || lower.contains("ఓపెన్ చేయండి") || lower.contains("తెరువు")) {
            val targetApp = extractAppTarget(lower)
            return JarvisParsedResponse(
                replyText = "$targetApp అప్లికేషన్ ఓపెన్ చేస్తున్నాను సర్.",
                actionType = ActionType.OPEN_APP,
                actionTarget = targetApp,
                isTelugu = true
            )
        }

        // H. Battery Telemetry
        if (lower.contains("battery") || lower.contains("బ్యాటరీ") || lower.contains("ఛార్జింగ్")) {
            return JarvisParsedResponse(
                replyText = "సిస్టమ్ బ్యాటరీ స్థితిని లెక్కిస్తున్నాను.",
                actionType = ActionType.BATTERY_CHECK,
                isTelugu = true
            )
        }

        // I. Automation
        if (lower.contains("automation") || lower.contains("routine") || lower.contains("ఆటోమేషన్") || lower.contains("రొటీన్")) {
            return JarvisParsedResponse(
                replyText = "దేవ్ AI ఆటోమేషన్ రొటీన్స్ ఎగ్జిక్యూట్ చేస్తున్నాను.",
                actionType = ActionType.RUN_ROUTINE,
                actionTarget = "routine_morning",
                isTelugu = true
            )
        }

        // J. Web Search
        if (lower.startsWith("search ") || lower.contains("గూగుల్ లో వెతుకు")) {
            val query = lower.removePrefix("search for ").removePrefix("search ").trim()
            return JarvisParsedResponse(
                replyText = "గూగుల్ సెర్చ్‌లో వెతుకుతున్నాను సర్.",
                actionType = ActionType.SEARCH_WEB,
                actionTarget = query,
                isTelugu = true
            )
        }

        return null
    }

    private fun extractMessageText(text: String): String {
        return text
            .removePrefix("send message ")
            .removePrefix("whatsapp message ")
            .removePrefix("message ")
            .removePrefix("మెసేజ్ పంపు ")
            .removeSuffix(" అని పంపు")
            .trim()
            .ifBlank { "Hello from Dev AI!" }
    }

    private fun extractAppTarget(text: String): String {
        return text
            .removePrefix("open the ")
            .removePrefix("launch the ")
            .removePrefix("start the ")
            .removePrefix("open ")
            .removePrefix("launch ")
            .removePrefix("start ")
            .removePrefix("run ")
            .removePrefix("play ")
            .removeSuffix(" application")
            .removeSuffix(" app")
            .removeSuffix(" అప్లికేషన్")
            .removeSuffix(" యాప్")
            .removeSuffix(" ఓపెన్ చెయ్యి")
            .removeSuffix(" ఓపెన్ చేయి")
            .removeSuffix(" ఓపెన్ చేయండి")
            .removeSuffix(" తెరువు")
            .removeSuffix(" స్టార్ట్ చెయ్యి")
            .trim()
    }

    private fun parseModelResponse(rawText: String): JarvisParsedResponse {
        var actionType = ActionType.NONE
        var actionTarget = ""
        var speech = rawText

        val actionRegex = Regex("""\[ACTION:([A-Z_]+)(?::([^\]:]*))?(?::([^\]]*))?\]""")
        val match = actionRegex.find(rawText)
        if (match != null) {
            val typeStr = match.groupValues[1]
            actionTarget = match.groupValues.getOrNull(2) ?: ""
            actionType = when (typeStr) {
                "LOCK_SCREEN" -> ActionType.LOCK_SCREEN
                "WAKE_SCREEN" -> ActionType.WAKE_SCREEN
                "OPEN_APP" -> ActionType.OPEN_APP
                "TOGGLE_TORCH" -> ActionType.TOGGLE_TORCH
                "SET_VOLUME" -> ActionType.SET_VOLUME
                "SEND_MESSAGE" -> ActionType.SEND_MESSAGE
                "RUN_ROUTINE" -> ActionType.RUN_ROUTINE
                "JAI_SHREE_RAM" -> ActionType.JAI_SHREE_RAM
                "BATTERY_CHECK" -> ActionType.BATTERY_CHECK
                "EMERGENCY_SOS" -> ActionType.EMERGENCY_SOS
                "SEARCH_WEB" -> ActionType.SEARCH_WEB
                else -> ActionType.NONE
            }
            speech = rawText.replace(match.value, "").trim()
        }

        if (speech.isBlank()) {
            speech = "మీ ఆదేశం స్వీకరించబడింది సర్."
        }

        return JarvisParsedResponse(
            replyText = speech,
            actionType = actionType,
            actionTarget = actionTarget,
            isTelugu = true
        )
    }

    private fun fallbackResponse(input: String): JarvisParsedResponse {
        val lower = input.lowercase(Locale.ROOT)
        return evaluateLocalIntent(lower) ?: JarvisParsedResponse(
            replyText = "జై శ్రీ రామ్! మీ ఆదేశం: '$input'. నేను మీ దేవ్ AI అసిస్టెంట్‌ని, యాప్స్ ఓపెన్ చేయడం, స్క్రీన్ లాక్ మరియు ఆన్ చేయడం, వాట్సాప్ మెసేజింగ్, టార్చ్ మరియు ఆటోమేషన్ పనులు చేయగలను.",
            actionType = ActionType.NONE,
            isTelugu = true
        )
    }

    fun resetConversation() {
        activeChatSession = null
    }

    companion object {
        private const val TAG = "DevAiRepo"
    }
}
