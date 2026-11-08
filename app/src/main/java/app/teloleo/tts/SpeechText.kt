package app.teloleo.tts

import java.net.IDN
import java.net.URI
import java.util.Locale

/**
 * El motor de voz expande siglas postales escritas en mayúsculas: VA se oye como Virginia,
 * LA como Los Ángeles y DE como Delaware. Una palabra de dos letras en mayúsculas se pasa
 * a minúsculas para que se lea como palabra.
 */
object SpeechText {
    private const val DISTORTED_FACE = 0x1FAEA
    private const val VARIATION_SELECTOR = 0xFE0F
    private val twoLetterShout = Regex("""(?<![\p{L}\p{N}])[A-Z]{2}(?![\p{L}\p{N}])""")
    private val webLink = Regex(
        """(?i)(?<![\p{L}\p{N}])(?:https?://|www\.)[^\s<>"'{}|\\^`\[\]]+""",
    )
    private val extraSpace = Regex("""[ \t\n]{2,}""")
    private val spaceBeforePunctuation = Regex("""\s+([,.;:!?…])""")
    private val trailingPunctuation = charArrayOf('.', ',', ';', ':', '!', '?', '…', ')', ']', '»')

    fun forEngine(phrase: String): String {
        val spoken = shortenLinks(nameUnknownEmoji(phrase))
            .replace(extraSpace, " ")
            .replace(spaceBeforePunctuation, "$1")
            .trim()
        return twoLetterShout.replace(spoken) { match -> match.value.lowercase(Locale.ROOT) }
    }

    /** La ruta y la consulta no se dictan. Se dice el dominio: «enlace a youtube punto com». */
    private fun shortenLinks(phrase: String): String =
        webLink.replace(phrase) { match ->
            val value = match.value
            var end = value.length
            while (end > 0 && value[end - 1] in trailingPunctuation) end--
            spokenLink(value.substring(0, end)) + value.substring(end)
        }

    private fun spokenLink(link: String): String {
        val domain = domainOf(link) ?: return "enlace"
        val spoken = domain.split('.').filter { it.isNotEmpty() }.joinToString(" punto ")
        return if (spoken.isEmpty()) "enlace" else "enlace a $spoken"
    }

    private fun domainOf(link: String): String? {
        val candidate = if ("://" in link) link else "https://$link"
        val host = try {
            URI(candidate).host
        } catch (_: Exception) {
            null
        } ?: return null
        val unicode = try {
            IDN.toUnicode(host)
        } catch (_: IllegalArgumentException) {
            host
        }
        val withoutWww = if (unicode.startsWith("www.", ignoreCase = true)) unicode.substring(4) else unicode
        return withoutWww.trim('.').lowercase(Locale.ROOT).takeIf { it.isNotEmpty() }
    }

    /** Unicode 17. La voz del sistema aún no dice este emoji, así que se nombra. */
    private fun nameUnknownEmoji(phrase: String): String = buildString(phrase.length) {
        var index = 0
        while (index < phrase.length) {
            val codePoint = phrase.codePointAt(index)
            if (codePoint == DISTORTED_FACE) {
                append(" cara distorsionada ")
                index += Character.charCount(codePoint)
                if (index < phrase.length && phrase.codePointAt(index) == VARIATION_SELECTOR) {
                    index += 1
                }
            } else {
                appendCodePoint(codePoint)
                index += Character.charCount(codePoint)
            }
        }
    }
}
