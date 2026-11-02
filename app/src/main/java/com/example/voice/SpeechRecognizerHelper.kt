package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class SpeechRecognizerHelper(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _transcribedText = MutableStateFlow("")
    val transcribedText: StateFlow<String> = _transcribedText.asStateFlow()

    private val _audioRms = MutableStateFlow(0f)
    val audioRms: StateFlow<Float> = _audioRms.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    var isContinuousListening: Boolean = true
    private var lastLanguageCode: String = "auto"
    private val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())

    var onSpeechComplete: ((String) -> Unit)? = null

    init {
        initRecognizer()
    }

    private fun initRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(createListener())
            }
        }
    }

    fun startListening(languageCode: String = "auto") {
        stopListening()
        lastLanguageCode = languageCode

        if (speechRecognizer == null) {
            initRecognizer()
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)

            val locale = when (languageCode.lowercase()) {
                "bn" -> Locale("bn", "BD")
                "hi" -> Locale("hi", "IN")
                "ar" -> Locale("ar")
                "en" -> Locale.US
                else -> Locale.getDefault()
            }
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale.toLanguageTag())
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, locale.toLanguageTag())
        }

        try {
            _error.value = null
            _transcribedText.value = ""
            _isListening.value = true
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            _isListening.value = false
            _error.value = "Failed to start microphone: ${e.localizedMessage}"
        }
    }

    fun stopListening() {
        mainHandler.removeCallbacksAndMessages(null)
        _isListening.value = false
        _audioRms.value = 0f
        try {
            speechRecognizer?.stopListening()
        } catch (_: Exception) {}
    }

    fun cancel() {
        mainHandler.removeCallbacksAndMessages(null)
        _isListening.value = false
        _audioRms.value = 0f
        try {
            speechRecognizer?.cancel()
        } catch (_: Exception) {}
    }

    fun destroy() {
        mainHandler.removeCallbacksAndMessages(null)
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (_: Exception) {}
    }

    private fun createListener() = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            _isListening.value = true
            _error.value = null
        }

        override fun onBeginningOfSpeech() {
            _isListening.value = true
        }

        override fun onRmsChanged(rmsdB: Float) {
            // Normalize dB (typically -2 to 10) to 0.0..1.0 for the Orb animation
            val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
            _audioRms.value = normalized
        }

        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {
            _isListening.value = false
            _audioRms.value = 0f
        }

        override fun onError(errorCode: Int) {
            _isListening.value = false
            _audioRms.value = 0f
            val message = when (errorCode) {
                SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                SpeechRecognizer.ERROR_CLIENT -> "Client speech error"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
                SpeechRecognizer.ERROR_NETWORK -> "Network connection error"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognizer busy"
                SpeechRecognizer.ERROR_SERVER -> "Speech server error"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected"
                else -> "Speech recognition error ($errorCode)"
            }

            if (errorCode != SpeechRecognizer.ERROR_NO_MATCH && errorCode != SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                _error.value = message
            }

            // Continuous listening auto-restart on silence or timeout
            if (isContinuousListening && (errorCode == SpeechRecognizer.ERROR_NO_MATCH || errorCode == SpeechRecognizer.ERROR_SPEECH_TIMEOUT || errorCode == SpeechRecognizer.ERROR_CLIENT)) {
                mainHandler.postDelayed({
                    if (isContinuousListening && !_isListening.value) {
                        startListening(lastLanguageCode)
                    }
                }, 350L)
            }
        }

        override fun onResults(results: Bundle?) {
            _isListening.value = false
            _audioRms.value = 0f
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                val text = matches[0]
                _transcribedText.value = text
                onSpeechComplete?.invoke(text)
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                _transcribedText.value = matches[0]
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }
}
