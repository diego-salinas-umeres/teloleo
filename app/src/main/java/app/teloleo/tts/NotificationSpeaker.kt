package app.teloleo.tts

interface NotificationSpeaker {
    fun warmUp()

    /**
     * [localeTag] vacío usa el idioma del teléfono. Una etiqueta como `en-US` fija la voz.
     */
    fun speak(phrase: String, rate: Float = 1f, volume: Float = 1f, localeTag: String = "")
    fun stop()
    fun shutdown()
}
