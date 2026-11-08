package app.teloleo.listener

import kotlin.math.abs

/**
 * Un golpe breve del acelerómetro. No guarda muestras: solo compara con la anterior.
 */
class ShakeDetector(
    private val threshold: Float = 30f,
    private val cooldownMillis: Long = 1_200L,
) {
    private var primed = false
    private var shook = false
    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f
    private var lastShakeAt = 0L

    fun sample(x: Float, y: Float, z: Float, nowMillis: Long): Boolean {
        if (!primed) {
            lastX = x
            lastY = y
            lastZ = z
            primed = true
            return false
        }
        val delta = abs(x - lastX) + abs(y - lastY) + abs(z - lastZ)
        lastX = x
        lastY = y
        lastZ = z
        if (delta < threshold) return false
        if (shook && nowMillis - lastShakeAt < cooldownMillis) return false
        lastShakeAt = nowMillis
        shook = true
        return true
    }
}
