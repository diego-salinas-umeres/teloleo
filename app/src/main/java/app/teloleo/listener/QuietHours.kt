package app.teloleo.listener

/**
 * Ventana en la que no se lee. Si el inicio es posterior al fin, cruza la medianoche.
 * Inicio y fin iguales no callan el día entero: es una ventana vacía.
 */
data class QuietHours(
    val enabled: Boolean,
    val startMinutes: Int,
    val endMinutes: Int,
) {
    fun covers(nowMinutes: Int): Boolean {
        if (!enabled) return false
        val start = startMinutes.coerceIn(0, LAST_MINUTE)
        val end = endMinutes.coerceIn(0, LAST_MINUTE)
        if (start == end) return false
        val now = nowMinutes.coerceIn(0, LAST_MINUTE)
        return if (start < end) now in start until end else now >= start || now < end
    }

    companion object {
        const val LAST_MINUTE = 24 * 60 - 1
        val Off = QuietHours(enabled = false, startMinutes = 22 * 60, endMinutes = 7 * 60)
    }
}
