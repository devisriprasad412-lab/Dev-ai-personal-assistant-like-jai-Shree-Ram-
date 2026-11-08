package com.example.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

enum class VoiceState {
    IDLE,
    LISTENING,
    PROCESSING,
    SPEAKING,
    ERROR
}

class JarvisVoiceManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady: Boolean = false
    private var teluguSupported: Boolean = false

    private val mainHandler = Handler(Looper.getMainLooper())

    private val _voiceState = MutableStateFlow(VoiceState.IDLE)
    val voiceState: StateFlow<VoiceState> = _voiceState.asStateFlow()

    private val _soundLevel = MutableStateFlow(0f)
    val soundLevel: StateFlow<Float> = _soundLevel.asStateFlow()

    private val _lastRecognizedText = MutableStateFlow("")
    val lastRecognizedText: StateFlow<String> = _lastRecognizedText.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Continuous conversation toggle
    var continuousMode: Boolean = false
    var onSpeechResultCallback: ((String) -> Unit)? = null
    var onSpeechCompletedCallback: (() -> Unit)? = null

    init {
        initializeTts()
        initializeSpeechRecognizer()
    }

    private fun initializeTts() {
        try {
            textToSpeech = TextToSpeech(context, this)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize TextToSpeech", e)
        }
    }

    private fun initializeSpeechRecognizer() {
        mainHandler.post {
            try {
                if (SpeechRecognizer.isRecognitionAvailable(context)) {
                    speechRecognizer?.destroy()
                    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                        setRecognitionListener(createRecognitionListener())
                    }
                    Log.d(TAG, "SpeechRecognizer initialized successfully.")
                } else {
                    Log.w(TAG, "Speech recognition service not reported as available by system.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error creating SpeechRecognizer", e)
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val teluguLocale = Locale("te", "IN")
            val result = textToSpeech?.setLanguage(teluguLocale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.w(TAG, "Telugu TTS not fully bundled on device, using default locale")
                textToSpeech?.setLanguage(Locale.getDefault())
                teluguSupported = false
            } else {
                teluguSupported = true
            }

            textToSpeech?.setPitch(0.95f)
            textToSpeech?.setSpeechRate(1.0f)

            textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _voiceState.value = VoiceState.SPEAKING
                }

                override fun onDone(utteranceId: String?) {
                    _voiceState.value = VoiceState.IDLE
                    _soundLevel.value = 0f
                    onSpeechCompletedCallback?.invoke()

                    if (continuousMode) {
                        mainHandler.postDelayed({
                            startListening()
                        }, 500)
                    }
                }

                override fun onError(utteranceId: String?) {
                    _voiceState.value = VoiceState.IDLE
                    _soundLevel.value = 0f
                }
            })

            isTtsReady = true
            Log.d(TAG, "TTS initialized. Telugu supported: $teluguSupported")
        } else {
            Log.e(TAG, "TTS initialization failed with code: $status")
        }
    }

    fun startListening(
        languageCode: String = "te-IN",
        onResult: ((String) -> Unit)? = null
    ) {
        onResult?.let { onSpeechResultCallback = it }
        stopSpeaking()

        mainHandler.post {
            try {
                if (speechRecognizer == null) {
                    initializeSpeechRecognizer()
                }

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, languageCode)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra("android.speech.extra.DICTATION_MODE", true)
                }

                _voiceState.value = VoiceState.LISTENING
                _lastRecognizedText.value = ""
                _errorMessage.value = null
                speechRecognizer?.startListening(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start listening", e)
                _voiceState.value = VoiceState.ERROR
                _errorMessage.value = e.localizedMessage
            }
        }
    }

    fun stopListening() {
        mainHandler.post {
            try {
                speechRecognizer?.stopListening()
                if (_voiceState.value == VoiceState.LISTENING) {
                    _voiceState.value = VoiceState.IDLE
                }
                _soundLevel.value = 0f
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping SpeechRecognizer", e)
            }
        }
    }

    private fun createRecognitionListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _voiceState.value = VoiceState.LISTENING
                _errorMessage.value = null
            }

            override fun onBeginningOfSpeech() {
                _voiceState.value = VoiceState.LISTENING
            }

            override fun onRmsChanged(rmsdB: Float) {
                // Map RMS range to 0.1 - 1.0 for holographic visualizer
                val normalized = ((rmsdB + 2f) / 12f).coerceIn(0.1f, 1f)
                _soundLevel.value = normalized
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                _voiceState.value = VoiceState.PROCESSING
                _soundLevel.value = 0f
            }

            override fun onError(error: Int) {
                val errorDesc = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                    SpeechRecognizer.ERROR_CLIENT -> "Client error"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Audio permission required"
                    SpeechRecognizer.ERROR_NETWORK -> "Network connection required"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                    SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized, please speak clearly"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognizer busy, resetting"
                    SpeechRecognizer.ERROR_SERVER -> "Server error"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected"
                    else -> "Speech recognition error ($error)"
                }
                Log.w(TAG, "SpeechRecognizer error: $errorDesc ($error)")

                _voiceState.value = VoiceState.IDLE
                _soundLevel.value = 0f

                // If recognizer was busy, reinitialize to keep next attempt clean
                if (error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY || error == SpeechRecognizer.ERROR_CLIENT) {
                    initializeSpeechRecognizer()
                }
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val recognized = matches?.firstOrNull()?.trim() ?: ""
                _lastRecognizedText.value = recognized
                _voiceState.value = VoiceState.IDLE
                _soundLevel.value = 0f

                if (recognized.isNotBlank()) {
                    onSpeechResultCallback?.invoke(recognized)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                matches?.firstOrNull()?.let { partial ->
                    if (partial.isNotBlank()) {
                        _lastRecognizedText.value = partial
                    }
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    fun speak(text: String, isTelugu: Boolean = true) {
        if (!isTtsReady || text.isBlank()) return

        stopListening()

        val locale = if (isTelugu && teluguSupported) Locale("te", "IN") else Locale.ENGLISH
        textToSpeech?.language = locale

        _voiceState.value = VoiceState.SPEAKING
        textToSpeech?.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "UTTERANCE_${System.currentTimeMillis()}"
        )
    }

    fun speakJaiShreeRam() {
        val sacredPraise = "జై శ్రీ రామ్! జై శ్రీ రామ్! ప్రభు శ్రీరాముని అనుగ్రహం మీకు మరియు మీ కుటుంబానికి ఎల్లప్పుడూ ఉండుగాక!"
        speak(sacredPraise, isTelugu = true)
    }

    fun stopSpeaking() {
        if (textToSpeech?.isSpeaking == true) {
            textToSpeech?.stop()
        }
        _voiceState.value = VoiceState.IDLE
        _soundLevel.value = 0f
    }

    fun destroy() {
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
            textToSpeech?.stop()
            textToSpeech?.shutdown()
            textToSpeech = null
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up JarvisVoiceManager", e)
        }
    }

    companion object {
        private const val TAG = "JarvisVoiceManager"
    }
}
