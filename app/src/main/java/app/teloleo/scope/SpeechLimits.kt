package app.teloleo.scope

object SpeechLimits {
    const val MIN_RATE = 0.6f
    const val MAX_RATE = 1.8f
    const val MIN_VOLUME = 0.2f
    const val MAX_VOLUME = 1f
    const val TEMPLATE_MAX = 180
    const val QUIET_START = 22 * 60
    const val QUIET_END = 7 * 60

    fun rate(value: Float): Float = value.coerceIn(MIN_RATE, MAX_RATE)

    fun volume(value: Float): Float = value.coerceIn(MIN_VOLUME, MAX_VOLUME)

    fun template(value: String): String = value.replace('\n', ' ').trim().take(TEMPLATE_MAX)
}
