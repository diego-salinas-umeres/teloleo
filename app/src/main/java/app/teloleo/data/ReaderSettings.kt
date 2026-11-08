package app.teloleo.data

import app.teloleo.scope.SpeechLimits

data class ReaderSettings(
    val allowedPackages: List<String> = emptyList(),
    val proUnlocked: Boolean = false,
    val redactOneTimeCodes: Boolean = true,
    val quietHoursEnabled: Boolean = false,
    val quietStartMinutes: Int = SpeechLimits.QUIET_START,
    val quietEndMinutes: Int = SpeechLimits.QUIET_END,
    val headphonesOnly: Boolean = false,
    val wordFilters: List<String> = emptyList(),
    val phraseTemplate: String = "",
    val appTemplates: Map<String, String> = emptyMap(),
    val speechRate: Float = 1f,
    val speechVolume: Float = 1f,
    val shakeToSilence: Boolean = false,
    /** Callada desde Inicio, hasta que se vuelva a activar. El permiso no cambia. */
    val readingPaused: Boolean = false,
    val voiceLocale: String = "",
    val onboardingComplete: Boolean = false,
) {
    fun spokenRate(): Float = if (proUnlocked) speechRate else 1f

    fun spokenVolume(): Float = if (proUnlocked) speechVolume else 1f

    fun shakeEnabled(): Boolean = proUnlocked && shakeToSilence

    fun templateFor(packageName: String): String? {
        val specific = appTemplates[packageName]
        if (!specific.isNullOrBlank()) return specific
        return phraseTemplate.takeIf { it.isNotBlank() }
    }
}
