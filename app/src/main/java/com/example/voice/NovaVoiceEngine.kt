package com.example.voice

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.example.data.api.ApiClient
import com.example.data.api.TextToSpeechRequest
import com.example.data.api.VoiceSettingsRequest
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

class NovaVoiceEngine(private val context: Context) : TextToSpeech.OnInitListener {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var mediaPlayer: MediaPlayer? = null
    private var androidTts: TextToSpeech? = null
    private var isTtsInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _currentVoiceSource = MutableStateFlow("Android TTS")
    val currentVoiceSource: StateFlow<String> = _currentVoiceSource.asStateFlow()

    private val _speakingWaveform = MutableStateFlow(0f)
    val speakingWaveform: StateFlow<Float> = _speakingWaveform.asStateFlow()

    val lastVoiceError = MutableStateFlow<String?>(null)

    private var amplitudeJob: Job? = null

    init {
        try {
            androidTts = TextToSpeech(context.applicationContext, this)
        } catch (_: Exception) {}
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsInitialized = true
            androidTts?.language = Locale("bn", "BD")
            androidTts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                    startSimulatedAmplitude()
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                    stopSimulatedAmplitude()
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                    stopSimulatedAmplitude()
                }
            })
        }
    }

    fun speak(
        text: String,
        apiKey: String?,
        voiceId: String?,
        speed: Float = 1.0f,
        language: String = "bn",
        stability: Float = 0.5f,
        similarityBoost: Float = 0.75f,
        style: Float = 0.0f,
        useSpeakerBoost: Boolean = true,
        modelId: String = "eleven_multilingual_v2",
        onCompletion: (() -> Unit)? = null
    ) {
        stopSpeaking()

        val cleanText = text.replace(Regex("\\[ACTION:[^\\]]+\\]"), "").trim()
        if (cleanText.isEmpty()) {
            onCompletion?.invoke()
            return
        }

        if (!apiKey.isNullOrBlank() && !voiceId.isNullOrBlank()) {
            scope.launch {
                try {
                    _currentVoiceSource.value = "ElevenLabs"
                    val success = speakWithElevenLabs(
                        text = cleanText,
                        apiKey = apiKey,
                        voiceId = voiceId,
                        speed = speed,
                        stability = stability,
                        similarityBoost = similarityBoost,
                        style = style,
                        useSpeakerBoost = useSpeakerBoost,
                        modelId = modelId,
                        onCompletion = onCompletion
                    )
                    if (!success) {
                        speakWithAndroidTts(cleanText, speed, language, onCompletion)
                    }
                } catch (e: Exception) {
                    lastVoiceError.value = "Voice playback error: ${e.localizedMessage}"
                    speakWithAndroidTts(cleanText, speed, language, onCompletion)
                }
            }
        } else {
            speakWithAndroidTts(cleanText, speed, language, onCompletion)
        }
    }

    private suspend fun speakWithElevenLabs(
        text: String,
        apiKey: String,
        voiceId: String,
        speed: Float,
        stability: Float,
        similarityBoost: Float,
        style: Float,
        useSpeakerBoost: Boolean,
        modelId: String,
        onCompletion: (() -> Unit)?
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.elevenLabsApi.textToSpeech(
                voiceId = voiceId,
                apiKey = apiKey,
                request = TextToSpeechRequest(
                    text = text,
                    modelId = modelId.ifBlank { "eleven_multilingual_v2" },
                    voiceSettings = VoiceSettingsRequest(
                        stability = stability.coerceIn(0.0f, 1.0f),
                        similarityBoost = similarityBoost.coerceIn(0.0f, 1.0f),
                        style = style.coerceIn(0.0f, 1.0f),
                        useSpeakerBoost = useSpeakerBoost
                    )
                )
            )

            if (!response.isSuccessful || response.body() == null) {
                val err = response.errorBody()?.string() ?: "HTTP ${response.code()}"
                lastVoiceError.value = "ElevenLabs error: $err"
                return@withContext false
            }

            lastVoiceError.value = null
            val tempFile = File(context.cacheDir, "nova_elevenlabs_${System.currentTimeMillis()}.mp3")
            response.body()!!.byteStream().use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }

            withContext(Dispatchers.Main) {
                playAudioFile(tempFile, speed, onCompletion)
            }
            true
        } catch (e: Exception) {
            lastVoiceError.value = "ElevenLabs error: ${e.localizedMessage}"
            false
        }
    }

    private fun playAudioFile(file: File, speed: Float, onCompletion: (() -> Unit)?) {
        try {
            stopSpeaking()
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
                        .build()
                )
                setDataSource(file.absolutePath)
                prepare()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    playbackParams = PlaybackParams().apply { this.speed = speed.coerceIn(0.5f, 2.0f) }
                }

                setOnCompletionListener {
                    _isSpeaking.value = false
                    stopSimulatedAmplitude()
                    onCompletion?.invoke()
                    try { file.delete() } catch (_: Exception) {}
                }

                setOnErrorListener { _, _, _ ->
                    _isSpeaking.value = false
                    stopSimulatedAmplitude()
                    try { file.delete() } catch (_: Exception) {}
                    false
                }

                start()
                _isSpeaking.value = true
                startSimulatedAmplitude()
            }
        } catch (e: Exception) {
            _isSpeaking.value = false
            stopSimulatedAmplitude()
        }
    }

    fun playPreviewUrl(url: String, onCompletion: (() -> Unit)? = null) {
        stopSpeaking()
        scope.launch(Dispatchers.Main) {
            try {
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    setDataSource(url)
                    prepareAsync()
                    setOnPreparedListener {
                        start()
                        _isSpeaking.value = true
                        startSimulatedAmplitude()
                    }
                    setOnCompletionListener {
                        _isSpeaking.value = false
                        stopSimulatedAmplitude()
                        onCompletion?.invoke()
                    }
                }
            } catch (_: Exception) {
                _isSpeaking.value = false
            }
        }
    }

    private fun speakWithAndroidTts(
        text: String,
        speed: Float,
        language: String,
        onCompletion: (() -> Unit)?
    ) {
        _currentVoiceSource.value = "Android System TTS"
        if (!isTtsInitialized || androidTts == null) {
            onCompletion?.invoke()
            return
        }

        try {
            val locale = when (language.lowercase()) {
                "bn" -> Locale("bn", "BD")
                "hi" -> Locale("hi", "IN")
                "ar" -> Locale("ar")
                "en" -> Locale.US
                else -> Locale.getDefault()
            }
            androidTts?.language = locale
            androidTts?.setSpeechRate(speed.coerceIn(0.5f, 2.0f))

            val utteranceId = "nova_${System.currentTimeMillis()}"
            androidTts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        } catch (e: Exception) {
            _isSpeaking.value = false
            stopSimulatedAmplitude()
            onCompletion?.invoke()
        }
    }

    private fun startSimulatedAmplitude() {
        amplitudeJob?.cancel()
        amplitudeJob = scope.launch {
            var phase = 0f
            while (isActive && _isSpeaking.value) {
                phase += 0.25f
                val dynamicAmp = (0.35f + 0.55f * kotlin.math.abs(kotlin.math.sin(phase))).toFloat()
                _speakingWaveform.value = dynamicAmp
                delay(60)
            }
            _speakingWaveform.value = 0f
        }
    }

    private fun stopSimulatedAmplitude() {
        amplitudeJob?.cancel()
        amplitudeJob = null
        _speakingWaveform.value = 0f
    }

    fun stopSpeaking() {
        _isSpeaking.value = false
        stopSimulatedAmplitude()
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.stop()
            }
            mediaPlayer?.reset()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (_: Exception) {}

        try {
            androidTts?.stop()
        } catch (_: Exception) {}
    }

    fun destroy() {
        stopSpeaking()
        scope.cancel()
        try {
            androidTts?.shutdown()
            androidTts = null
        } catch (_: Exception) {}
    }
}
