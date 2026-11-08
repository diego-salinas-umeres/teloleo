package app.teloleo.scope

/**
 * Alcance cerrado de la v1.
 *
 * Un solo módulo Android, en Kotlin, Jetpack Compose y Material 3.
 * El texto de los avisos se procesa en el teléfono: no hay servidor, no hay
 * anuncios y el Pro es un producto no consumible de pago único.
 */
object ProductScope {
    const val APPLICATION_ID = "app.teloleo"
    const val MIN_SDK = 26

    /** Apps que hablan sin haber comprado Pro. No hay cupo de lecturas. */
    const val FREE_APP_LIMIT = 3

    /**
     * Identificador del producto no consumible. Play Billing se conecta aparte;
     * el desbloqueo vive en el teléfono.
     */
    const val PRO_PRODUCT_ID = "teloleo_pro"
    const val PRO_IS_SUBSCRIPTION = false

    const val ADS_ENABLED = false
    const val BACKEND_ENABLED = false

    /** Un código de un solo uso no se dice en voz alta salvo que se cambie el ajuste. */
    const val REDACT_ONE_TIME_CODES_BY_DEFAULT = true

    /**
     * Ficha de Seguridad de los datos: nada se recopila ni se comparte.
     * El texto del aviso solo vive en memoria el tiempo de decirlo.
     */
    const val COLLECTS_PERSONAL_DATA = false
    const val SHARES_DATA = false
    const val STORES_NOTIFICATION_TEXT = false

    fun redactOrDefault(stored: Boolean?): Boolean = stored ?: REDACT_ONE_TIME_CODES_BY_DEFAULT
}
