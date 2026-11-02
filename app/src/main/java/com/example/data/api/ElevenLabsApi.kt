package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

@JsonClass(generateAdapter = true)
data class VoicesResponse(
    @Json(name = "voices") val voices: List<VoiceModel> = emptyList()
)

@JsonClass(generateAdapter = true)
data class VoiceModel(
    @Json(name = "voice_id") val voiceId: String,
    @Json(name = "name") val name: String,
    @Json(name = "category") val category: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "preview_url") val previewUrl: String? = null,
    @Json(name = "labels") val labels: Map<String, String>? = null
)

@JsonClass(generateAdapter = true)
data class TextToSpeechRequest(
    @Json(name = "text") val text: String,
    @Json(name = "model_id") val modelId: String = "eleven_multilingual_v2",
    @Json(name = "voice_settings") val voiceSettings: VoiceSettingsRequest? = VoiceSettingsRequest()
)

@JsonClass(generateAdapter = true)
data class VoiceSettingsRequest(
    @Json(name = "stability") val stability: Float = 0.5f,
    @Json(name = "similarity_boost") val similarityBoost: Float = 0.75f,
    @Json(name = "style") val style: Float = 0.0f,
    @Json(name = "use_speaker_boost") val useSpeakerBoost: Boolean = true
)

@JsonClass(generateAdapter = true)
data class ElevenLabsUserResponse(
    @Json(name = "subscription") val subscription: SubscriptionInfo? = null
)

@JsonClass(generateAdapter = true)
data class SubscriptionInfo(
    @Json(name = "tier") val tier: String? = null,
    @Json(name = "character_count") val characterCount: Int = 0,
    @Json(name = "character_limit") val characterLimit: Int = 10000
)

interface ElevenLabsApi {
    @GET("v1/voices")
    suspend fun getVoices(
        @Header("xi-api-key") apiKey: String
    ): VoicesResponse

    @GET("v1/user")
    suspend fun getUserInfo(
        @Header("xi-api-key") apiKey: String
    ): ElevenLabsUserResponse

    @POST("v1/text-to-speech/{voice_id}")
    @Streaming
    suspend fun textToSpeech(
        @Path("voice_id") voiceId: String,
        @Header("xi-api-key") apiKey: String,
        @Body request: TextToSpeechRequest
    ): Response<ResponseBody>

    @Multipart
    @POST("v1/voices/add")
    suspend fun addVoice(
        @Header("xi-api-key") apiKey: String,
        @Part("name") name: RequestBody,
        @Part("description") description: RequestBody,
        @Part files: List<MultipartBody.Part>
    ): VoiceModel

    @DELETE("v1/voices/{voice_id}")
    suspend fun deleteVoice(
        @Path("voice_id") voiceId: String,
        @Header("xi-api-key") apiKey: String
    ): Response<Unit>
}
