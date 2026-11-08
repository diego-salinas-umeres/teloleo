package app.teloleo.tts

import android.content.Context
import android.media.AudioAttributes
import android.os.Bundle
import android.speech.tts.TextToSpeech
import app.teloleo.data.voiceLocaleCandidates
import app.teloleo.scope.SpeechLimits
import java.util.ArrayDeque

/**
 * Voz del sistema. La frase no se escribe en el registro.
 */
class SystemSpeaker(context: Context) : NotificationSpeaker {
    private val appContext = context.applicationContext
    private val pending = ArrayDeque<Utterance>()
    private var engine: TextToSpeech? = null
    private var ready = false
    private var utterance = 0
    private var appliedLocales: List<String>? = null

    override fun warmUp() {
        synchronized(pending) {
            if (engine == null) {
                engine = TextToSpeech(appContext, ::onInit)
            }
        }
    }

    override fun speak(phrase: String, rate: Float, volume: Float, localeTag: String) {
        val cleaned = SpeechText.forEngine(phrase.trim())
        if (cleaned.isEmpty()) return
        val item = Utterance(
            cleaned,
            SpeechLimits.rate(rate),
            SpeechLimits.volume(volume),
            localeTag,
        )
        synchronized(pending) {
            val current = engine
            if (current == null || !ready) {
                enqueue(item)
                if (current == null) engine = TextToSpeech(appContext, ::onInit)
                return
            }
            speakNow(current, item)
        }
    }

    override fun stop() {
        synchronized(pending) {
            pending.clear()
            engine?.stop()
        }
    }

    override fun shutdown() {
        synchronized(pending) {
            pending.clear()
            ready = false
            appliedLocales = null
            engine?.stop()
            engine?.shutdown()
            engine = null
        }
    }

    private fun onInit(status: Int) {
        synchronized(pending) {
            val current = engine ?: return
            if (status != TextToSpeech.SUCCESS) {
                pending.clear()
                current.shutdown()
                engine = null
                ready = false
                appliedLocales = null
                return
            }
            appliedLocales = null
            current.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build(),
            )
            ready = true
            while (pending.isNotEmpty()) {
                speakNow(current, pending.removeFirst())
            }
        }
    }

    private fun applyLocale(tts: TextToSpeech, savedTag: String) {
        val candidates = voiceLocaleCandidates(savedTag)
        val key = candidates.map { it.toLanguageTag() }
        if (appliedLocales == key) return
        appliedLocales = key
        val locale = candidates.firstOrNull { candidate ->
            val available = tts.isLanguageAvailable(candidate)
            available != TextToSpeech.LANG_MISSING_DATA &&
                available != TextToSpeech.LANG_NOT_SUPPORTED
        } ?: return
        tts.setLanguage(locale)
    }

    private fun enqueue(item: Utterance) {
        while (pending.size >= MAX_PENDING) pending.removeFirst()
        pending.addLast(item)
    }

    private fun speakNow(tts: TextToSpeech, item: Utterance) {
        applyLocale(tts, item.localeTag)
        tts.setSpeechRate(item.rate)
        val params = Bundle()
        params.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, item.volume)
        val id = "teloleo-${++utterance}"
        tts.speak(item.phrase, TextToSpeech.QUEUE_ADD, params, id)
    }

    private data class Utterance(
        val phrase: String,
        val rate: Float,
        val volume: Float,
        val localeTag: String,
    )

    private companion object {
        const val MAX_PENDING = 3
    }
}
