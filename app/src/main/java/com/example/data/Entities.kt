package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val role: String, // "user" or "assistant"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val audioUrl: String? = null,
    val actionType: String? = null,
    val actionDetail: String? = null
)

@Entity(tableName = "user_memories")
data class UserMemoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String, // "preference", "fact", "routine", "contact"
    val title: String,
    val value: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isEnabled: Boolean = true
)

@Entity(tableName = "voice_scripts")
data class VoiceScriptEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val voiceId: String,
    val voiceName: String,
    val createdAt: Long = System.currentTimeMillis()
)
