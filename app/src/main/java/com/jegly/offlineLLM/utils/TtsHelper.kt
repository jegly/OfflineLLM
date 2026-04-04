package com.jegly.offlineLLM.utils

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.widget.Toast
import java.util.Locale
import java.util.UUID

class TtsHelper(private val context: Context) {

    private var tts: TextToSpeech? = null
    private var isReady = false
    private var onDoneCallback: (() -> Unit)? = null
    private val handler = Handler(Looper.getMainLooper())

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.getDefault())
                isReady = result != TextToSpeech.LANG_MISSING_DATA
                        && result != TextToSpeech.LANG_NOT_SUPPORTED
                if (!isReady) {
                    val enResult = tts?.setLanguage(Locale.ENGLISH)
                    isReady = enResult != TextToSpeech.LANG_MISSING_DATA
                            && enResult != TextToSpeech.LANG_NOT_SUPPORTED
                }
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    override fun onDone(utteranceId: String?) {
                        handler.post { onDoneCallback?.invoke() }
                    }
                    @Deprecated("Deprecated")
                    override fun onError(utteranceId: String?) {
                        handler.post { onDoneCallback?.invoke() }
                    }
                })
            }
        }
    }

    fun speak(text: String, onDone: () -> Unit = {}) {
        onDoneCallback = onDone

        if (!isReady) {
            handler.post {
                Toast.makeText(
                    context,
                    "TTS unavailable. Go to Settings > Accessibility > Text-to-speech to configure.",
                    Toast.LENGTH_LONG
                ).show()
                onDone()
            }
            return
        }

        val utteranceId = UUID.randomUUID().toString()
        val result = tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)

        if (result != TextToSpeech.SUCCESS) {
            handler.post {
                Toast.makeText(context, "TTS failed to start.", Toast.LENGTH_SHORT).show()
                onDone()
            }
            return
        }

        // Safety timeout: reset button after 60 seconds max
        handler.postDelayed({
            if (tts?.isSpeaking != true) {
                onDoneCallback?.invoke()
            }
        }, 60000)
    }

    fun stop() {
        tts?.stop()
        handler.post { onDoneCallback?.invoke() }
        onDoneCallback = null
    }

    fun isSpeaking(): Boolean = tts?.isSpeaking == true

    fun shutdown() {
        handler.removeCallbacksAndMessages(null)
        tts?.stop()
        tts?.shutdown()
        tts = null
        isReady = false
    }
}
