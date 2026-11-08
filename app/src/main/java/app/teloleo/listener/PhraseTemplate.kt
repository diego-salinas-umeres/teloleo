package app.teloleo.listener

import java.util.Locale

/**
 * Plantilla de Pro. Huecos: {app}, {remitente}, {titulo}, {texto}.
 * {remitente} y {titulo} son el título del aviso, que suele ser quien escribe.
 */
object PhraseTemplate {
    private val placeholder = Regex("""\{(app|remitente|titulo|texto)\}""", RegexOption.IGNORE_CASE)

    fun render(
        template: String,
        appLabel: String,
        title: String?,
        text: String?,
        bigText: String?,
        redactOneTimeCodes: Boolean,
    ): String? {
        if (!placeholder.containsMatchIn(template)) return null
        val label = appLabel.trim()
        val sender = title?.trim()?.takeUnless { it.isBlank() || it.equals(label, ignoreCase = true) }.orEmpty()
        val body = SpokenPhrase.bodyOf(text, bigText).orEmpty()
        val rendered = placeholder.replace(template) { match ->
            when (match.groupValues[1].lowercase(Locale.ROOT)) {
                "app" -> SpokenPhrase.redact(label, redactOneTimeCodes)
                "remitente", "titulo" -> SpokenPhrase.redact(sender, redactOneTimeCodes)
                "texto" -> SpokenPhrase.redact(body, redactOneTimeCodes)
                else -> ""
            }
        }
        val phrase = tidy(rendered)
        return phrase.takeIf { it.isNotEmpty() }
    }

    internal fun tidy(value: String): String {
        var text = value.replace(Regex("""[ \t]+"""), " ")
        text = text.replace(Regex(""",\s*\."""), ".")
        text = text.replace(Regex("""\s+([,.;:!?])"""), "$1")
        text = text.replace(Regex("""([,;:])(?:\s*[,;:])+"""), "$1")
        text = text.replace(Regex("""\.\s*\."""), ".")
        text = text.replace(Regex("""\s+"""), " ").trim()
        text = text.trim(' ', ',', ';', ':')
        if (text.isBlank()) return ""
        return if (text.last() in TERMINAL) text else "$text."
    }

    private val TERMINAL = setOf('.', '?', '!', '…')
}
