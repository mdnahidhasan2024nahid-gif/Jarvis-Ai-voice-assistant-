package com.example.services

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.service.voice.VoiceInteractionService
import android.util.Log

class JarvisVoiceInteractionService : VoiceInteractionService() {

    override fun onReady() {
        super.onReady()
        isServiceActive = true
        Log.d(TAG, "JarvisVoiceInteractionService is ready and active as system assistant")
    }

    override fun onShutdown() {
        super.onShutdown()
        isServiceActive = false
        Log.d(TAG, "JarvisVoiceInteractionService has shut down")
    }

    companion object {
        private const val TAG = "JarvisVoiceInteraction"
        var isServiceActive: Boolean = false
            private set

        fun getComponentName(context: Context): ComponentName {
            return ComponentName(context, JarvisVoiceInteractionService::class.java)
        }
    }
}
