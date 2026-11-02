package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NovaDao {

    // Chat Messages
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentMessages(limit: Int): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long

    @Query("DELETE FROM chat_messages WHERE id = :id")
    suspend fun deleteMessage(id: Long)

    @Query("DELETE FROM chat_messages")
    suspend fun clearAllMessages()

    // Memories
    @Query("SELECT * FROM user_memories WHERE isEnabled = 1 ORDER BY timestamp DESC")
    fun getAllMemories(): Flow<List<UserMemoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: UserMemoryEntity): Long

    @Update
    suspend fun updateMemory(memory: UserMemoryEntity)

    @Query("DELETE FROM user_memories WHERE id = :id")
    suspend fun deleteMemory(id: Long)

    @Query("DELETE FROM user_memories")
    suspend fun clearAllMemories()

    // Voice Scripts
    @Query("SELECT * FROM voice_scripts ORDER BY createdAt DESC")
    fun getAllVoiceScripts(): Flow<List<VoiceScriptEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVoiceScript(script: VoiceScriptEntity): Long

    @Query("DELETE FROM voice_scripts WHERE id = :id")
    suspend fun deleteVoiceScript(id: Long)
}
