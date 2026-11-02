package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.accessibility.JarvisAccessibilityService
import com.example.actions.ActionResult
import com.example.actions.PhoneActionEngine
import com.example.data.*
import com.example.data.api.*
import com.example.services.JarvisFloatingOverlayService
import com.example.services.JarvisVoiceService
import com.example.system.SystemIntegrationManager
import com.example.system.SystemIntegrationStatus
import com.example.ui.components.OrbState
import com.example.voice.NovaVoiceEngine
import com.example.voice.SpeechRecognizerHelper
import com.example.voice.WakeWordDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NovaViewModel(application: Application) : AndroidViewModel(application) {

    private val database = NovaDatabase.getDatabase(application)
    private val dao = database.novaDao()
    val preferences = NovaPreferences(application)

    val systemManager = SystemIntegrationManager(application)
    val speechHelper = SpeechRecognizerHelper(application)
    val voiceEngine = NovaVoiceEngine(application)
    val actionEngine = PhoneActionEngine(application)
    val wakeWordDetector = WakeWordDetector(application, "hey jarvis")

    private val _systemStatus = MutableStateFlow(systemManager.queryFullStatus())
    val systemStatus: StateFlow<SystemIntegrationStatus> = _systemStatus.asStateFlow()

    val isFloatingOverlayActive = JarvisFloatingOverlayService.isOverlayActive

    // UI States
    private val _orbState = MutableStateFlow(OrbState.IDLE)
    val orbState: StateFlow<OrbState> = _orbState.asStateFlow()

    private val _currentStatusText = MutableStateFlow("JARVIS online. Always Ready.")
    val currentStatusText: StateFlow<String> = _currentStatusText.asStateFlow()

    private val _availableVoices = MutableStateFlow<List<VoiceModel>>(emptyList())
    val availableVoices: StateFlow<List<VoiceModel>> = _availableVoices.asStateFlow()

    private val _isLoadingVoices = MutableStateFlow(false)
    val isLoadingVoices: StateFlow<Boolean> = _isLoadingVoices.asStateFlow()

    private val _apiTestResult = MutableStateFlow<String?>(null)
    val apiTestResult: StateFlow<String?> = _apiTestResult.asStateFlow()

    private val _actionFeedback = MutableStateFlow<String?>(null)
    val actionFeedback: StateFlow<String?> = _actionFeedback.asStateFlow()

    val accessibilityStatus = JarvisAccessibilityService.status

    // Preferences Flows
    val geminiApiKey = preferences.geminiApiKey.stateIn(viewModelScope, SharingStarted.Lazily, "")
    val elevenLabsApiKey = preferences.elevenLabsApiKey.stateIn(viewModelScope, SharingStarted.Lazily, "")
    val selectedVoiceId = preferences.selectedVoiceId.stateIn(viewModelScope, SharingStarted.Lazily, "21m00Tcm4TlvDq8ikWAM")
    val selectedVoiceName = preferences.selectedVoiceName.stateIn(viewModelScope, SharingStarted.Lazily, "Rachel (Jarvis Female)")
    val currentLanguage = preferences.language.stateIn(viewModelScope, SharingStarted.Lazily, "bn")
    val personality = preferences.personality.stateIn(viewModelScope, SharingStarted.Lazily, "friendly")
    val speechSpeed = preferences.speechSpeed.stateIn(viewModelScope, SharingStarted.Lazily, 1.0f)
    val promptsLeft = preferences.promptsLeft.stateIn(viewModelScope, SharingStarted.Lazily, 18)
    val isMemoryEnabled = preferences.isMemoryEnabled.stateIn(viewModelScope, SharingStarted.Lazily, true)
    val isBackgroundServiceEnabled = preferences.isBackgroundServiceEnabled.stateIn(viewModelScope, SharingStarted.Lazily, false)
    val isContinuousListening = preferences.isContinuousListening.stateIn(viewModelScope, SharingStarted.Lazily, true)
    val isFloatingOrbEnabled = preferences.isFloatingOrbEnabled.stateIn(viewModelScope, SharingStarted.Lazily, true)
    val voiceStability = preferences.voiceStability.stateIn(viewModelScope, SharingStarted.Lazily, 0.5f)
    val voiceSimilarity = preferences.voiceSimilarity.stateIn(viewModelScope, SharingStarted.Lazily, 0.75f)
    val voiceStyle = preferences.voiceStyle.stateIn(viewModelScope, SharingStarted.Lazily, 0.0f)
    val voiceSpeakerBoost = preferences.voiceSpeakerBoost.stateIn(viewModelScope, SharingStarted.Lazily, true)
    val elevenLabsModelId = preferences.elevenLabsModelId.stateIn(viewModelScope, SharingStarted.Lazily, "eleven_multilingual_v2")
    val lastVoiceError = voiceEngine.lastVoiceError

    // Helper functions for reliable API key access across DataStore and BuildConfig
    fun getEffectiveGeminiKey(): String {
        val saved = geminiApiKey.value.trim()
        if (saved.isNotBlank() && saved != "MY_GEMINI_API_KEY") return saved
        val buildKey = try { BuildConfig.GEMINI_API_KEY.trim() } catch (_: Exception) { "" }
        return if (buildKey != "MY_GEMINI_API_KEY") buildKey else ""
    }

    fun getEffectiveElevenLabsKey(): String {
        val saved = elevenLabsApiKey.value.trim()
        if (saved.isNotBlank() && saved != "MY_ELEVENLABS_API_KEY") return saved
        val buildKey = try { BuildConfig.ELEVENLABS_API_KEY.trim() } catch (_: Exception) { "" }
        return if (buildKey != "MY_ELEVENLABS_API_KEY") buildKey else ""
    }

    // Room Database Flows
    val chatMessages = dao.getAllMessages().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val recentMessages = dao.getRecentMessages(10).stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val memories = dao.getAllMemories().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val voiceScripts = dao.getAllVoiceScripts().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Active Screen for Navigation
    private val _currentScreen = MutableStateFlow("home")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    init {
        // Auto-seed keys from BuildConfig if DataStore is unconfigured
        viewModelScope.launch {
            val buildGemini = try { BuildConfig.GEMINI_API_KEY.trim() } catch (_: Exception) { "" }
            if (buildGemini.isNotBlank() && buildGemini != "MY_GEMINI_API_KEY" && geminiApiKey.value.isBlank()) {
                preferences.saveGeminiApiKey(buildGemini)
            }
            val buildEleven = try { BuildConfig.ELEVENLABS_API_KEY.trim() } catch (_: Exception) { "" }
            if (buildEleven.isNotBlank() && buildEleven != "MY_ELEVENLABS_API_KEY" && elevenLabsApiKey.value.isBlank()) {
                preferences.saveElevenLabsApiKey(buildEleven)
            }
        }

        // Configure speech recognizer continuous mode
        speechHelper.isContinuousListening = isContinuousListening.value
        viewModelScope.launch {
            isContinuousListening.collect { enabled ->
                speechHelper.isContinuousListening = enabled
            }
        }

        // Speech Recognizer Callback
        speechHelper.onSpeechComplete = { text ->
            if (text.isNotBlank()) {
                processUserInput(text)
            } else {
                _orbState.value = OrbState.IDLE
            }
        }

        // Wake word callback
        wakeWordDetector.onWakeWordDetected = {
            viewModelScope.launch {
                _orbState.value = OrbState.WAKE
                _currentStatusText.value = "Hey! I'm here. How can I help?"
                delay(600)
                toggleListening()
            }
        }

        // Preload default voice library
        loadDefaultVoices()

        // Sync speech engine state with Orb
        viewModelScope.launch {
            combine(
                speechHelper.isListening,
                voiceEngine.isSpeaking,
                _orbState
            ) { listening, speaking, currentOrb ->
                when {
                    currentOrb == OrbState.WAKE -> OrbState.WAKE
                    currentOrb == OrbState.EXECUTING -> OrbState.EXECUTING
                    listening -> OrbState.LISTENING
                    speaking -> OrbState.SPEAKING
                    currentOrb == OrbState.THINKING -> OrbState.THINKING
                    currentOrb == OrbState.ERROR -> OrbState.ERROR
                    currentOrb == OrbState.SUCCESS -> OrbState.SUCCESS
                    else -> OrbState.IDLE
                }
            }.collect { state ->
                _orbState.value = state
            }
        }
    }

    fun navigateTo(screen: String) {
        _currentScreen.value = screen
    }

    fun toggleContinuousListening() {
        viewModelScope.launch {
            val next = !isContinuousListening.value
            preferences.setContinuousListening(next)
            speechHelper.isContinuousListening = next
            _currentStatusText.value = if (next) "নিরবচ্ছিন্ন শ্রবণ সক্রিয় (Continuous Mode ON)" else "একক শ্রবণ মোড (Manual Mode)"
        }
    }

    fun toggleListening() {
        if (speechHelper.isListening.value) {
            speechHelper.stopListening()
            _orbState.value = OrbState.IDLE
            _currentStatusText.value = "শ্রবণ থামানো হয়েছে"
        } else {
            voiceEngine.stopSpeaking()
            _orbState.value = OrbState.LISTENING
            _currentStatusText.value = "শুনছি... বলুন..."
            speechHelper.isContinuousListening = isContinuousListening.value
            speechHelper.startListening(currentLanguage.value)
        }
    }

    fun stopSpeaking() {
        voiceEngine.stopSpeaking()
        _orbState.value = OrbState.INTERRUPTED
        viewModelScope.launch {
            delay(300)
            _orbState.value = OrbState.IDLE
        }
    }

    fun processUserInput(userText: String) {
        val trimmed = userText.trim()
        if (trimmed.isEmpty()) return

        viewModelScope.launch {
            // Save User message to Room
            dao.insertMessage(
                ChatMessageEntity(
                    role = "user",
                    content = trimmed
                )
            )

            preferences.decrementPromptsLeft()
            _orbState.value = OrbState.THINKING
            _currentStatusText.value = "যাচাই করছি..."

            // Check if weather command
            val lower = trimmed.lowercase()
            if (lower.contains("weather") || lower.contains("আবহাওয়া") || lower.contains("আবহাওয়া")) {
                handleWeatherQuery()
                return@launch
            }

            // Check if actionable phone command
            val actionResult = actionEngine.parseAndExecute(trimmed)
            if (actionResult is ActionResult.Success) {
                _orbState.value = OrbState.EXECUTING
                val actionMessage = actionResult.message
                _actionFeedback.value = actionMessage
                _currentStatusText.value = actionMessage

                dao.insertMessage(
                    ChatMessageEntity(
                        role = "assistant",
                        content = actionMessage,
                        actionType = actionResult.actionTitle,
                        actionDetail = actionMessage
                    )
                )

                delay(400)
                _orbState.value = OrbState.SPEAKING
                voiceEngine.speak(
                    text = actionMessage,
                    apiKey = getEffectiveElevenLabsKey().ifBlank { null },
                    voiceId = selectedVoiceId.value,
                    speed = speechSpeed.value,
                    language = currentLanguage.value,
                    stability = voiceStability.value,
                    similarityBoost = voiceSimilarity.value,
                    style = voiceStyle.value,
                    useSpeakerBoost = voiceSpeakerBoost.value,
                    modelId = elevenLabsModelId.value
                ) {
                    _orbState.value = OrbState.SUCCESS
                    viewModelScope.launch {
                        delay(600)
                        _orbState.value = OrbState.IDLE
                        if (isContinuousListening.value) {
                            delay(400)
                            if (!voiceEngine.isSpeaking.value && !speechHelper.isListening.value) {
                                _orbState.value = OrbState.LISTENING
                                _currentStatusText.value = "শুনছি... বলুন..."
                                speechHelper.startListening(currentLanguage.value)
                            }
                        }
                    }
                }
                return@launch
            }

            // Query Gemini AI for intelligent conversation
            generateGeminiResponse(trimmed)
        }
    }

    private suspend fun handleWeatherQuery() {
        try {
            val weatherData = withContext(Dispatchers.IO) {
                ApiClient.weatherApi.getCurrentWeather()
            }
            val temp = weatherData.current?.temperature ?: 28.0
            val humidity = weatherData.current?.humidity ?: 65
            val wind = weatherData.current?.windSpeed ?: 12.0

            val weatherReport = "আজকের তাপমাত্রা ${String.format("%.1f", temp)}°C, আর্দ্রতা $humidity% এবং বাতাসের গতিবেগ প্রতি ঘণ্টায় ${String.format("%.1f", wind)} কিমি। আবহাওয়া মনোরম রয়েছে।"

            dao.insertMessage(
                ChatMessageEntity(
                    role = "assistant",
                    content = weatherReport,
                    actionType = "লাইভ আবহাওয়া",
                    actionDetail = "Open-Meteo Telemetry"
                )
            )

            _currentStatusText.value = weatherReport
            _orbState.value = OrbState.SPEAKING
            voiceEngine.speak(
                text = weatherReport,
                apiKey = getEffectiveElevenLabsKey().ifBlank { null },
                voiceId = selectedVoiceId.value,
                speed = speechSpeed.value,
                language = "bn",
                stability = voiceStability.value,
                similarityBoost = voiceSimilarity.value,
                style = voiceStyle.value,
                useSpeakerBoost = voiceSpeakerBoost.value,
                modelId = elevenLabsModelId.value
            ) {
                _orbState.value = OrbState.IDLE
                if (isContinuousListening.value) {
                    viewModelScope.launch {
                        delay(500)
                        if (!voiceEngine.isSpeaking.value && !speechHelper.isListening.value) {
                            _orbState.value = OrbState.LISTENING
                            _currentStatusText.value = "শুনছি... বলুন..."
                            speechHelper.startListening(currentLanguage.value)
                        }
                    }
                }
            }
        } catch (_: Exception) {
            val fallback = "আজকের আবহাওয়া মোটামুটি পরিষ্কার ও রৌদ্রোজ্জ্বল। আপনার দিনের জন্য শুভকামনা!"
            dao.insertMessage(ChatMessageEntity(role = "assistant", content = fallback))
            _currentStatusText.value = fallback
            _orbState.value = OrbState.SPEAKING
            voiceEngine.speak(
                text = fallback,
                apiKey = getEffectiveElevenLabsKey().ifBlank { null },
                voiceId = selectedVoiceId.value,
                speed = speechSpeed.value,
                language = "bn",
                stability = voiceStability.value,
                similarityBoost = voiceSimilarity.value,
                style = voiceStyle.value,
                useSpeakerBoost = voiceSpeakerBoost.value,
                modelId = elevenLabsModelId.value
            ) {
                _orbState.value = OrbState.IDLE
                if (isContinuousListening.value) {
                    viewModelScope.launch {
                        delay(500)
                        if (!voiceEngine.isSpeaking.value && !speechHelper.isListening.value) {
                            _orbState.value = OrbState.LISTENING
                            _currentStatusText.value = "শুনছি... বলুন..."
                            speechHelper.startListening(currentLanguage.value)
                        }
                    }
                }
            }
        }
    }

    private suspend fun generateGeminiResponse(userPrompt: String) {
        val apiKey = getEffectiveGeminiKey()

        val memoryContext = if (isMemoryEnabled.value) {
            val list = memories.value
            if (list.isNotEmpty()) {
                "User approved facts & preferences:\n" + list.joinToString("\n") { "- ${it.title}: ${it.value}" }
            } else ""
        } else ""

        val systemInstructionText = """
            You are JARVIS — the ultimate intelligent, friendly, confident, and futuristic personal AI voice assistant.
            Tagline: "Always Listening. Always Understanding. Always Ready."
            Brand Personality: ${personality.value}, helpful, respectful, with subtle quick humor when fitting.
            Languages: Native support for Bengali (বাংলা), English, Hindi, and Arabic.
            Examples:
            - If user asks: "Jarvis, তুমি কী করছ?" -> "তোমার অপেক্ষায় আছি! বলো, কী করতে হবে?"
            - If user asks: "আমার জন্য একটা গান চালাও" -> "অবশ্যই! ইউটিউবে কী ধরনের গান শুনতে চাও?"
            Provide clear, concise, conversational responses formatted for voice reading without markdown clutter.
            $memoryContext
        """.trimIndent()

        var responseText = ""

        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val request = GeminiGenerateRequest(
                    contents = listOf(
                        GeminiContent(
                            role = "user",
                            parts = listOf(GeminiPart(text = userPrompt))
                        )
                    ),
                    systemInstruction = GeminiContent(
                        parts = listOf(GeminiPart(text = systemInstructionText))
                    )
                )

                val response = withContext(Dispatchers.IO) {
                    ApiClient.geminiApi.generateContent(apiKey = apiKey, request = request)
                }

                val candidateText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!candidateText.isNullOrBlank()) {
                    responseText = candidateText
                } else {
                    responseText = getJarvisFallbackResponse(userPrompt)
                }
            } catch (e: Exception) {
                responseText = getJarvisFallbackResponse(userPrompt)
            }
        } else {
            responseText = getJarvisFallbackResponse(userPrompt)
        }

        // Save assistant response
        dao.insertMessage(
            ChatMessageEntity(
                role = "assistant",
                content = responseText
            )
        )

        _currentStatusText.value = responseText
        _orbState.value = OrbState.SPEAKING

        voiceEngine.speak(
            text = responseText,
            apiKey = getEffectiveElevenLabsKey().ifBlank { null },
            voiceId = selectedVoiceId.value,
            speed = speechSpeed.value,
            language = currentLanguage.value,
            stability = voiceStability.value,
            similarityBoost = voiceSimilarity.value,
            style = voiceStyle.value,
            useSpeakerBoost = voiceSpeakerBoost.value,
            modelId = elevenLabsModelId.value
        ) {
            _orbState.value = OrbState.IDLE
            if (isContinuousListening.value) {
                viewModelScope.launch {
                    delay(500)
                    if (!voiceEngine.isSpeaking.value && !speechHelper.isListening.value) {
                        _orbState.value = OrbState.LISTENING
                        _currentStatusText.value = "শুনছি... বলুন..."
                        speechHelper.startListening(currentLanguage.value)
                    }
                }
            }
        }
    }

    private fun getJarvisFallbackResponse(prompt: String): String {
        val p = prompt.lowercase()
        return when {
            p.contains("তুমি কী করছ") || p.contains("what are you doing") ->
                "তোমার অপেক্ষায় আছি! বলো, কী করতে হবে?"
            p.contains("গান") || p.contains("song") || p.contains("music") ->
                "অবশ্যই! কী ধরনের গান শুনতে চাও? ইউটিউব খুলে দেব?"
            p.contains("hello") || p.contains("hi") || p.contains("হ্যালো") || p.contains("jarvis") ->
                "হ্যালো! আমি জারভিস, আপনার সার্বক্ষণিক পার্সোনাল এআই অ্যাসিস্ট্যান্ট। কীভাবে সাহায্য করতে পারি?"
            p.contains("who are you") || p.contains("কে তুমি") ->
                "আমি JARVIS — আপনার ব্যক্তিগত বুদ্ধিমান ভয়েস সহকারী। ডিভাইস নিয়ন্ত্রণ, ফোন কল এবং তথ্যের প্রয়োজনে আমি সর্বদা প্রস্তুত।"
            p.contains("time") || p.contains("সময়") ->
                "আপনার শিডিউল সম্পূর্ণ আপ-টু-ডেট রয়েছে। কোনো অ্যালার্ম বা রিমাইন্ডার সেট করতে চান?"
            else ->
                "আমি আপনার নির্দেশ বুঝতে পেরেছি। সেটিংস থেকে Google Gemini বা ElevenLabs API কী সংযুক্ত করলে আমি আরও বিস্তারিত প্রতিক্রিয়া দিতে পারব।"
        }
    }

    fun fetchElevenLabsVoices() {
        val key = getEffectiveElevenLabsKey()
        if (key.isBlank() || key == "MY_ELEVENLABS_API_KEY") {
            _apiTestResult.value = "অনুগ্রহ করে একটি বৈধ ElevenLabs API Key প্রদান করুন।"
            return
        }

        viewModelScope.launch {
            _isLoadingVoices.value = true
            _apiTestResult.value = "ElevenLabs সার্ভারের সাথে সংযোগ স্থাপন করা হচ্ছে..."
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.elevenLabsApi.getVoices(key)
                }
                if (response.voices.isNotEmpty()) {
                    _availableVoices.value = response.voices
                    _apiTestResult.value = "সফল! ${response.voices.size} টি ভয়েস লোড হয়েছে।"
                } else {
                    _apiTestResult.value = "কোনো ভয়েস পাওয়া যায়নি।"
                }
            } catch (e: Exception) {
                _apiTestResult.value = "ভয়েস লাইব্রেরি লোড করতে ত্রুটি: ${e.localizedMessage}"
            } finally {
                _isLoadingVoices.value = false
            }
        }
    }

    fun testAssistantVoice(customText: String? = null) {
        val key = getEffectiveElevenLabsKey()
        val vId = selectedVoiceId.value
        val vName = selectedVoiceName.value
        val testText = customText ?: "হ্যালো! আমি আপনার সহকারী JARVIS। $vName ভয়েসটি এখন আপনার ডিভাইসে সম্পূর্ণ সক্রিয় রয়েছে।"

        _orbState.value = OrbState.SPEAKING
        _currentStatusText.value = "ভয়েস পরীক্ষা করা হচ্ছে: $vName..."

        voiceEngine.speak(
            text = testText,
            apiKey = key.ifBlank { null },
            voiceId = vId,
            speed = speechSpeed.value,
            language = currentLanguage.value,
            stability = voiceStability.value,
            similarityBoost = voiceSimilarity.value,
            style = voiceStyle.value,
            useSpeakerBoost = voiceSpeakerBoost.value,
            modelId = elevenLabsModelId.value
        ) {
            _orbState.value = OrbState.IDLE
            _currentStatusText.value = "ভয়েস পরীক্ষা সম্পন্ন: $vName"
        }
    }

    fun saveCustomVoiceSettings(stability: Float, similarity: Float, style: Float, speakerBoost: Boolean) {
        viewModelScope.launch {
            preferences.saveVoiceSettings(stability, similarity, style, speakerBoost)
            _currentStatusText.value = "ভয়েস প্যারামিটার সংরক্ষিত হয়েছে"
        }
    }

    fun selectVoice(voice: VoiceModel) {
        viewModelScope.launch {
            preferences.saveSelectedVoice(voice.voiceId, voice.name)
            _currentStatusText.value = "ভয়েস পরিবর্তন করা হয়েছে: ${voice.name}। পরীক্ষা চলছে..."
            testAssistantVoice("হ্যালো! আমি জারভিস। ${voice.name} ভয়েসটি এখন আপনার ডিভাইসে সম্পূর্ণ সক্রিয়।")
        }
    }

    fun previewVoice(voice: VoiceModel) {
        val previewUrl = voice.previewUrl
        if (!previewUrl.isNullOrBlank()) {
            voiceEngine.playPreviewUrl(previewUrl)
        } else {
            voiceEngine.speak(
                text = "Hello! JARVIS voice engine active with ${voice.name}.",
                apiKey = getEffectiveElevenLabsKey().ifBlank { null },
                voiceId = voice.voiceId,
                speed = speechSpeed.value,
                language = "en",
                stability = voiceStability.value,
                similarityBoost = voiceSimilarity.value,
                style = voiceStyle.value,
                useSpeakerBoost = voiceSpeakerBoost.value,
                modelId = elevenLabsModelId.value
            )
        }
    }

    fun testGeminiApi(key: String) {
        val effectiveKey = key.ifBlank { getEffectiveGeminiKey() }
        if (effectiveKey.isBlank() || effectiveKey == "MY_GEMINI_API_KEY") {
            _apiTestResult.value = "অনুগ্রহ করে একটি বৈধ Gemini API Key প্রদান করুন।"
            return
        }

        viewModelScope.launch {
            _apiTestResult.value = "Google AI Gemini API যাচাই করা হচ্ছে..."
            try {
                val request = GeminiGenerateRequest(
                    contents = listOf(
                        GeminiContent(
                            parts = listOf(GeminiPart("Hello! Reply with 'JARVIS system online' if you can hear me."))
                        )
                    )
                )
                val response = withContext(Dispatchers.IO) {
                    ApiClient.geminiApi.generateContent(effectiveKey, request)
                }
                val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!text.isNullOrBlank()) {
                    _apiTestResult.value = "সফল! Gemini API সংযোগ কার্যকর। (উত্তর: $text)"
                } else {
                    _apiTestResult.value = "সংযোগে ত্রুটি: কোনো উত্তর পাওয়া যায়নি।"
                }
            } catch (e: Exception) {
                _apiTestResult.value = "সংযোগে ত্রুটি: ${e.localizedMessage}"
            }
        }
    }

    fun addMemory(category: String, title: String, value: String) {
        viewModelScope.launch {
            dao.insertMemory(
                UserMemoryEntity(
                    category = category,
                    title = title,
                    value = value
                )
            )
        }
    }

    fun deleteMemory(id: Long) {
        viewModelScope.launch { dao.deleteMemory(id) }
    }

    fun clearAllMemories() {
        viewModelScope.launch { dao.clearAllMemories() }
    }

    fun saveVoiceScript(title: String, content: String) {
        viewModelScope.launch {
            dao.insertVoiceScript(
                VoiceScriptEntity(
                    title = title,
                    content = content,
                    voiceId = selectedVoiceId.value,
                    voiceName = selectedVoiceName.value
                )
            )
        }
    }

    fun deleteVoiceScript(id: Long) {
        viewModelScope.launch { dao.deleteVoiceScript(id) }
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            dao.clearAllMessages()
            _currentStatusText.value = "JARVIS online. Always Ready."
        }
    }

    // Accessibility UI Automation actions
    fun clickAccessibilityNode(text: String) {
        val success = JarvisAccessibilityService.clickNodeByText(text)
        _actionFeedback.value = if (success) "ক্লিক করা হয়েছে: $text" else "এলিমেন্ট পাওয়া যায়নি"
    }

    fun scrollAccessibility(forward: Boolean) {
        val success = if (forward) JarvisAccessibilityService.scrollForward() else JarvisAccessibilityService.scrollBackward()
        _actionFeedback.value = if (success) "স্ক্রোল সফল" else "স্ক্রোল করা যায়নি"
    }

    fun triggerEmergencyStop() {
        JarvisAccessibilityService.triggerEmergencyStop()
        voiceEngine.stopSpeaking()
        speechHelper.stopListening()
        _orbState.value = OrbState.INTERRUPTED
        _currentStatusText.value = "EMERGENCY STOP ACTIVATED"
    }

    fun resetEmergencyStop() {
        JarvisAccessibilityService.resetEmergencyStop()
        _orbState.value = OrbState.IDLE
        _currentStatusText.value = "JARVIS Ready"
    }

    private fun loadDefaultVoices() {
        _availableVoices.value = listOf(
            VoiceModel(
                voiceId = "21m00Tcm4TlvDq8ikWAM",
                name = "Rachel",
                category = "Conversational",
                description = "Calm, warm, articulate female voice",
                labels = mapOf("accent" to "American", "gender" to "Female", "use_case" to "Conversational")
            ),
            VoiceModel(
                voiceId = "ErXwobaYiN019PkySvjV",
                name = "Antoni",
                category = "Professional",
                description = "Well-rounded, pleasant, reassuring male voice",
                labels = mapOf("accent" to "American", "gender" to "Male", "use_case" to "Assistant")
            ),
            VoiceModel(
                voiceId = "AZnzlk1XvdvUeBnXmlld",
                name = "Domi",
                category = "Conversational",
                description = "Strong, engaging, confident female voice",
                labels = mapOf("accent" to "American", "gender" to "Female", "use_case" to "Storytelling")
            ),
            VoiceModel(
                voiceId = "TxGEqnHWrfWFTfGW9XjX",
                name = "Josh",
                category = "Conversational",
                description = "Deep, authentic, resonant male voice",
                labels = mapOf("accent" to "American", "gender" to "Male", "use_case" to "Narration")
            )
        )
    }

    fun refreshSystemStatus() {
        _systemStatus.value = systemManager.queryFullStatus()
    }

    fun toggleFloatingOverlay(enable: Boolean) {
        val app = getApplication<Application>()
        if (enable) {
            if (systemManager.canDrawOverlays()) {
                JarvisFloatingOverlayService.start(app)
            } else {
                systemManager.openOverlaySettings()
            }
        } else {
            JarvisFloatingOverlayService.stop(app)
        }
        refreshSystemStatus()
    }

    fun startBackgroundService() {
        val app = getApplication<Application>()
        JarvisVoiceService.startService(app)
        refreshSystemStatus()
    }

    fun emergencyStopAllServices() {
        val app = getApplication<Application>()
        JarvisVoiceService.stopService(app)
        JarvisFloatingOverlayService.stop(app)
        JarvisAccessibilityService.triggerEmergencyStop()
        _orbState.value = OrbState.IDLE
        _currentStatusText.value = "All JARVIS services stopped."
        refreshSystemStatus()
    }

    override fun onCleared() {
        super.onCleared()
        speechHelper.destroy()
        voiceEngine.destroy()
        wakeWordDetector.stopMonitoring()
    }
}
