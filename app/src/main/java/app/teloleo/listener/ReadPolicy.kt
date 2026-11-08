package app.teloleo.listener

import app.teloleo.scope.ProductScope

/**
 * Lo que decide si un aviso se dice, aparte de la lista de apps.
 * Horario, auriculares, filtros y plantilla solo aplican con Pro.
 * [silenced] también calla la versión gratis.
 */
data class ReadPolicy(
    val redactOneTimeCodes: Boolean = ProductScope.REDACT_ONE_TIME_CODES_BY_DEFAULT,
    val quiet: QuietHours = QuietHours.Off,
    val nowMinutesOfDay: Int = -1,
    val requireHeadphones: Boolean = false,
    val headphonesConnected: Boolean = false,
    val blockedWords: List<String> = emptyList(),
    val template: String? = null,
    val silenced: Boolean = false,
)
