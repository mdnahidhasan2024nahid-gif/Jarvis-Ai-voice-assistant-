package com.example

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log

class AssistantActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "AssistantActivity triggered by system assist intent: ${intent?.action}")

        try {
            val mainIntent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("EXTRA_START_VOICE_ASSIST", true)
            }
            startActivity(mainIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch MainActivity from AssistantActivity", e)
        }

        finish()
    }

    companion object {
        private const val TAG = "JarvisAssistActivity"
    }
}
