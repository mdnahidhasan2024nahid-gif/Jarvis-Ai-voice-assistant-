package com.example.services

import android.app.assist.AssistContent
import android.app.assist.AssistStructure
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.service.voice.VoiceInteractionSession
import android.util.Log
import com.example.MainActivity

class JarvisVoiceInteractionSession(context: Context) : VoiceInteractionSession(context) {

    override fun onShow(args: Bundle?, showFlags: Int) {
        super.onShow(args, showFlags)
        Log.d(TAG, "JarvisVoiceInteractionSession onShow triggered with flags: $showFlags")

        // Launch MainActivity directly into voice conversation mode
        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("EXTRA_START_VOICE_ASSIST", true)
            }
            context.startActivity(intent)
            hide()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start MainActivity from voice interaction session", e)
        }
    }

    override fun onHandleAssist(
        data: Bundle?,
        structure: AssistStructure?,
        content: AssistContent?
    ) {
        super.onHandleAssist(data, structure, content)
        Log.d(TAG, "JarvisVoiceInteractionSession assist data received")
    }

    companion object {
        private const val TAG = "JarvisVoiceSession"
    }
}
