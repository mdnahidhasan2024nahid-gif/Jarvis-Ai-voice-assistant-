package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "nova_preferences")

class NovaPreferences(private val context: Context) {

    companion object {
        val KEY_GEMINI_API_KEY = stringPreferencesKey("gemini_api_key")
        val KEY_ELEVENLABS_API_KEY = stringPreferencesKey("elevenlabs_api_key")
        val KEY_SELECTED_VOICE_ID = stringPreferencesKey("selected_voice_id")
        val KEY_SELECTED_VOICE_NAME = stringPreferencesKey("selected_voice_name")
        val KEY_LANGUAGE = stringPreferencesKey("app_language")
        val KEY_PERSONALITY = stringPreferencesKey("assistant_personality")
        val KEY_SPEECH_SPEED = floatPreferencesKey("speech_speed")
        val KEY_PROMPTS_LEFT = intPreferencesKey("prompts_left")
        val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val KEY_BACKGROUND_SERVICE = booleanPreferencesKey("background_service_enabled")
        val KEY_MEMORY_ENABLED = booleanPreferencesKey("memory_enabled")
        val KEY_CONTINUOUS_LISTENING = booleanPreferencesKey("continuous_listening_enabled")
        val KEY_FLOATING_ORB_ENABLED = booleanPreferencesKey("floating_orb_enabled")
        val KEY_VOICE_STABILITY = floatPreferencesKey("voice_stability")
        val KEY_VOICE_SIMILARITY = floatPreferencesKey("voice_similarity")
        val KEY_VOICE_STYLE = floatPreferencesKey("voice_style")
        val KEY_VOICE_SPEAKER_BOOST = booleanPreferencesKey("voice_speaker_boost")
        val KEY_ELEVENLABS_MODEL_ID = stringPreferencesKey("elevenlabs_model_id")
    }

    val geminiApiKey: Flow<String> = context.dataStore.data.map {
        it[KEY_GEMINI_API_KEY] ?: ""
    }

    val elevenLabsApiKey: Flow<String> = context.dataStore.data.map {
        it[KEY_ELEVENLABS_API_KEY] ?: ""
    }

    val selectedVoiceId: Flow<String> = context.dataStore.data.map {
        it[KEY_SELECTED_VOICE_ID] ?: "21m00Tcm4TlvDq8ikWAM" // Default: Rachel
    }

    val selectedVoiceName: Flow<String> = context.dataStore.data.map {
        it[KEY_SELECTED_VOICE_NAME] ?: "Rachel (Jarvis Female)"
    }

    val language: Flow<String> = context.dataStore.data.map {
        it[KEY_LANGUAGE] ?: "bn" // Default Bengali/Auto
    }

    val personality: Flow<String> = context.dataStore.data.map {
        it[KEY_PERSONALITY] ?: "friendly"
    }

    val speechSpeed: Flow<Float> = context.dataStore.data.map {
        it[KEY_SPEECH_SPEED] ?: 1.0f
    }

    val promptsLeft: Flow<Int> = context.dataStore.data.map {
        it[KEY_PROMPTS_LEFT] ?: 18
    }

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data.map {
        it[KEY_ONBOARDING_COMPLETED] ?: false
    }

    val isBackgroundServiceEnabled: Flow<Boolean> = context.dataStore.data.map {
        it[KEY_BACKGROUND_SERVICE] ?: false
    }

    val isMemoryEnabled: Flow<Boolean> = context.dataStore.data.map {
        it[KEY_MEMORY_ENABLED] ?: true
    }

    val isContinuousListening: Flow<Boolean> = context.dataStore.data.map {
        it[KEY_CONTINUOUS_LISTENING] ?: true
    }

    val isFloatingOrbEnabled: Flow<Boolean> = context.dataStore.data.map {
        it[KEY_FLOATING_ORB_ENABLED] ?: true
    }

    val voiceStability: Flow<Float> = context.dataStore.data.map {
        it[KEY_VOICE_STABILITY] ?: 0.5f
    }

    val voiceSimilarity: Flow<Float> = context.dataStore.data.map {
        it[KEY_VOICE_SIMILARITY] ?: 0.75f
    }

    val voiceStyle: Flow<Float> = context.dataStore.data.map {
        it[KEY_VOICE_STYLE] ?: 0.0f
    }

    val voiceSpeakerBoost: Flow<Boolean> = context.dataStore.data.map {
        it[KEY_VOICE_SPEAKER_BOOST] ?: true
    }

    val elevenLabsModelId: Flow<String> = context.dataStore.data.map {
        it[KEY_ELEVENLABS_MODEL_ID] ?: "eleven_multilingual_v2"
    }

    suspend fun saveGeminiApiKey(key: String) {
        context.dataStore.edit { it[KEY_GEMINI_API_KEY] = key.trim() }
    }

    suspend fun saveElevenLabsApiKey(key: String) {
        context.dataStore.edit { it[KEY_ELEVENLABS_API_KEY] = key.trim() }
    }

    suspend fun saveSelectedVoice(voiceId: String, voiceName: String) {
        context.dataStore.edit {
            it[KEY_SELECTED_VOICE_ID] = voiceId
            it[KEY_SELECTED_VOICE_NAME] = voiceName
        }
    }

    suspend fun saveVoiceSettings(stability: Float, similarity: Float, style: Float, speakerBoost: Boolean) {
        context.dataStore.edit {
            it[KEY_VOICE_STABILITY] = stability
            it[KEY_VOICE_SIMILARITY] = similarity
            it[KEY_VOICE_STYLE] = style
            it[KEY_VOICE_SPEAKER_BOOST] = speakerBoost
        }
    }

    suspend fun saveLanguage(lang: String) {
        context.dataStore.edit { it[KEY_LANGUAGE] = lang }
    }

    suspend fun savePersonality(personality: String) {
        context.dataStore.edit { it[KEY_PERSONALITY] = personality }
    }

    suspend fun saveSpeechSpeed(speed: Float) {
        context.dataStore.edit { it[KEY_SPEECH_SPEED] = speed }
    }

    suspend fun setContinuousListening(enabled: Boolean) {
        context.dataStore.edit { it[KEY_CONTINUOUS_LISTENING] = enabled }
    }

    suspend fun setFloatingOrbEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_FLOATING_ORB_ENABLED] = enabled }
    }

    suspend fun decrementPromptsLeft() {
        context.dataStore.edit {
            val current = it[KEY_PROMPTS_LEFT] ?: 18
            if (current > 0) it[KEY_PROMPTS_LEFT] = current - 1
        }
    }

    suspend fun resetPromptsLeft(count: Int = 20) {
        context.dataStore.edit { it[KEY_PROMPTS_LEFT] = count }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[KEY_ONBOARDING_COMPLETED] = completed }
    }

    suspend fun setBackgroundServiceEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_BACKGROUND_SERVICE] = enabled }
    }

    suspend fun setMemoryEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_MEMORY_ENABLED] = enabled }
    }

    suspend fun clearAllData() {
        context.dataStore.edit { it.clear() }
    }
}
