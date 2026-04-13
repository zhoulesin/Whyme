package com.zhoulesin.whyme.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

class TextToSpeechHelper(private val context: Context) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    fun initialize(callback: (Boolean) -> Unit) {
        tts = TextToSpeech(context) { status ->
            isInitialized = (status == TextToSpeech.SUCCESS)
            if (isInitialized) {
                // 设置语言为英语
                val result = tts?.setLanguage(Locale.ENGLISH)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    isInitialized = false
                    Log.e("TextToSpeechHelper", "Language not supported")
                }
            } else {
                Log.e("TextToSpeechHelper", "Initialization failed")
            }
            callback(isInitialized)
        }

        // 设置进度监听器
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                Log.d("TextToSpeechHelper", "Speech started")
            }

            override fun onDone(utteranceId: String?) {
                Log.d("TextToSpeechHelper", "Speech completed")
            }

            override fun onError(utteranceId: String?) {
                Log.e("TextToSpeechHelper", "Speech error")
            }
        })
    }

    fun speak(text: String) {
        if (isInitialized) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "utteranceId")
        } else {
            Log.e("TextToSpeechHelper", "TTS not initialized")
        }
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.shutdown()
    }

    fun setLanguage(language: String) {
        if (isInitialized) {
            val locale = when (language) {
                "en" -> Locale.ENGLISH
                "zh" -> Locale.CHINESE
                "ja" -> Locale.JAPANESE
                else -> Locale.ENGLISH
            }
            val result = tts?.setLanguage(locale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TextToSpeechHelper", "Language not supported: $language")
            }
        }
    }

    fun setSpeechRate(rate: Float) {
        tts?.setSpeechRate(rate) // 1.0是正常速度
    }

//    /**
//     * 设置音量
//     * @param volume 音量值，范围 0.0f 到 1.0f
//     */
//    fun setVolume(volume: Float) {
//        // TextToSpeech 的 setVolume 方法只需要一个参数
//        tts?.setVolume(volume)
//    }
}