package app.teloleo.listener

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Plazo compartido del silencio. La actividad y el servicio lo ven. 0 significa que no está callada. */
object SpeakerCommands {
    private val deadline = MutableStateFlow(0L)

    /** Instante `elapsedRealtime` en que termina el silencio. */
    val silencedUntil: StateFlow<Long> = deadline.asStateFlow()

    fun requestSilence(durationMillis: Long, now: Long) {
        deadline.value = now + durationMillis.coerceAtLeast(0L)
    }

    fun resume() {
        deadline.value = 0L
    }

    const val SHAKE_SILENCE_MILLIS = 120_000L
}
