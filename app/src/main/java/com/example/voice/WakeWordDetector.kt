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

class WakeWordDetector(
    private val context: Context,
    var targetWakePhrase: String = "hey jarvis"
) {
    private var speechRecognizer: SpeechRecognizer? = null

    private val _isMonitoring = MutableStateFlow(false)
    val isMonitoring: StateFlow<Boolean> = _isMonitoring.asStateFlow()

    private val _lastDetectedPhrase = MutableStateFlow("")
    val lastDetectedPhrase: StateFlow<String> = _lastDetectedPhrase.asStateFlow()

    var onWakeWordDetected: (() -> Unit)? = null

    fun startMonitoring() {
        if (_isMonitoring.value) return
        _isMonitoring.value = true
        startRecognizerCycle()
    }

    fun stopMonitoring() {
        _isMonitoring.value = false
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
        } catch (_: Exception) {}
        speechRecognizer = null
    }

    private fun startRecognizerCycle() {
        if (!_isMonitoring.value) return

        try {
            speechRecognizer?.destroy()
        } catch (_: Exception) {}

        if (!SpeechRecognizer.isRecognitionAvailable(context)) return

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}

                override fun onError(error: Int) {
                    if (_isMonitoring.value) {
                        // Restart cycle after brief pause
                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            startRecognizerCycle()
                        }, 500)
                    }
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    checkMatches(matches)
                    if (_isMonitoring.value) {
                        startRecognizerCycle()
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    checkMatches(matches)
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US.toLanguageTag())
        }

        try {
            speechRecognizer?.startListening(intent)
        } catch (_: Exception) {
            if (_isMonitoring.value) {
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    startRecognizerCycle()
                }, 1000)
            }
        }
    }

    private fun checkMatches(matches: List<String>?) {
        if (matches.isNullOrEmpty()) return
        val target = targetWakePhrase.lowercase().trim()
        for (match in matches) {
            val text = match.lowercase().trim()
            _lastDetectedPhrase.value = text
            if (text.contains(target) || text.contains("jarvis") || text.contains("জারভিস") || text.contains("hey jarvis")) {
                onWakeWordDetected?.invoke()
                break
            }
        }
    }
}
