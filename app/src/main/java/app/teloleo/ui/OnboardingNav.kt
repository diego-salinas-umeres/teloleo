package app.teloleo.ui

/**
 * Pasos del primer arranque. El ajuste de Android no es un paso de esta lista:
 * es la pantalla del sistema, y al volver se cae en [Granted] o [Denied].
 */
internal enum class OnboardingStep {
    Welcome,
    Flow,
    Permission,
    Granted,
    Denied,
    Apps,
    Done,
}

internal data class SampleNotice(
    val title: String,
    val text: String,
)

internal object OnboardingNav {
    const val STEP_COUNT = 4

    fun progress(step: OnboardingStep): Int? = when (step) {
        OnboardingStep.Welcome -> 1
        OnboardingStep.Flow -> 2
        OnboardingStep.Permission, OnboardingStep.Granted, OnboardingStep.Denied -> 3
        OnboardingStep.Apps -> 4
        OnboardingStep.Done -> null
    }

    fun back(step: OnboardingStep, listenerEnabled: Boolean): OnboardingStep? = when (step) {
        OnboardingStep.Welcome -> null
        OnboardingStep.Flow -> OnboardingStep.Welcome
        OnboardingStep.Permission -> OnboardingStep.Flow
        OnboardingStep.Granted, OnboardingStep.Denied -> OnboardingStep.Permission
        OnboardingStep.Apps -> if (listenerEnabled) OnboardingStep.Granted else OnboardingStep.Denied
        OnboardingStep.Done -> OnboardingStep.Apps
    }

    fun afterSystemSettings(listenerEnabled: Boolean): OnboardingStep =
        if (listenerEnabled) OnboardingStep.Granted else OnboardingStep.Denied

    fun joinNames(names: List<String>): String = when (names.size) {
        0 -> ""
        1 -> names[0]
        2 -> "${names[0]} y ${names[1]}"
        else -> names.dropLast(1).joinToString(", ") + " y " + names.last()
    }

    fun sampleNotice(packageName: String): SampleNotice = when (packageName) {
        "com.whatsapp", "com.whatsapp.w4b" -> SampleNotice("Ana", "Ya salí.")
        "org.telegram.messenger" -> SampleNotice("Luis", "¿Llegas a las 7?")
        "com.google.android.gm" -> SampleNotice("Banco", "Tu código es 482913.")
        "com.google.android.apps.messaging" -> SampleNotice("Ana", "Estoy abajo.")
        "com.instagram.android" -> SampleNotice("Ana", "Te mencionó en una historia.")
        "org.thoughtcrime.securesms" -> SampleNotice("Ana", "Ya llegué.")
        "com.google.android.calendar" -> SampleNotice("Hoy", "Reunión en 10 minutos.")
        "com.Slack" -> SampleNotice("Equipo", "¿Lo ves?")
        else -> SampleNotice("", "Tienes un aviso nuevo.")
    }
}
