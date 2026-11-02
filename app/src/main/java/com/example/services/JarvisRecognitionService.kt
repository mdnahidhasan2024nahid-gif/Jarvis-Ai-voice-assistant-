package com.example.services

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognitionService
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log

class JarvisRecognitionService : RecognitionService() {

    private var systemRecognizer: SpeechRecognizer? = null

    override fun onCreate() {
        super.onCreate()
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            systemRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        }
    }

    override fun onStartListening(recognizerIntent: Intent?, listener: Callback?) {
        Log.d(TAG, "JarvisRecognitionService onStartListening")
        if (systemRecognizer == null) {
            listener?.error(SpeechRecognizer.ERROR_CLIENT)
            return
        }

        systemRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                listener?.readyForSpeech(params)
            }
            override fun onBeginningOfSpeech() {
                listener?.beginningOfSpeech()
            }
            override fun onRmsChanged(rmsdB: Float) {
                listener?.rmsChanged(rmsdB)
            }
            override fun onBufferReceived(buffer: ByteArray?) {
                listener?.bufferReceived(buffer)
            }
            override fun onEndOfSpeech() {
                listener?.endOfSpeech()
            }
            override fun onError(error: Int) {
                listener?.error(error)
            }
            override fun onResults(results: Bundle?) {
                listener?.results(results)
            }
            override fun onPartialResults(partialResults: Bundle?) {
                listener?.partialResults(partialResults)
            }
            override fun onEvent(eventType: Int, params: Bundle?) {
                listener?.segmentResults(params ?: Bundle())
            }
        })

        systemRecognizer?.startListening(recognizerIntent ?: Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH))
    }

    override fun onCancel(listener: Callback?) {
        Log.d(TAG, "JarvisRecognitionService onCancel")
        systemRecognizer?.cancel()
    }

    override fun onStopListening(listener: Callback?) {
        Log.d(TAG, "JarvisRecognitionService onStopListening")
        systemRecognizer?.stopListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        systemRecognizer?.destroy()
        systemRecognizer = null
    }

    companion object {
        private const val TAG = "JarvisRecognitionSvc"
    }
}
