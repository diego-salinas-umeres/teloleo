package app.teloleo.listener

/**
 * Los avisos se actualizan y se agrupan. La misma frase no se vuelve a decir
 * dentro de la ventana.
 */
class SpeakDebouncer(
    private val windowMillis: Long = DEFAULT_WINDOW_MILLIS,
) {
    private val lastSpokenAt = LinkedHashMap<String, Long>()

    @Synchronized
    fun shouldSpeak(signature: String, nowMillis: Long): Boolean {
        val previous = lastSpokenAt[signature]
        if (previous != null && nowMillis - previous < windowMillis) {
            return false
        }
        lastSpokenAt[signature] = nowMillis
        while (lastSpokenAt.size > MAX_ENTRIES) {
            val oldest = lastSpokenAt.keys.firstOrNull() ?: break
            lastSpokenAt.remove(oldest)
        }
        return true
    }

    companion object {
        const val DEFAULT_WINDOW_MILLIS = 8_000L
        private const val MAX_ENTRIES = 48
    }
}
