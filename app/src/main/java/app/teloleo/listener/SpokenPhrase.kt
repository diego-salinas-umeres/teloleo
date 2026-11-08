package app.teloleo.listener

import app.teloleo.scope.ProductScope
import java.security.MessageDigest
import java.util.Locale

object SpokenPhrase {
    private val oneTimeCode = Regex("""(?<!\d)(?:\d{3}[\s-]\d{3}|\d{4,8})(?!\d)""")
    /** Un teléfono son 9 a 15 dígitos, con + y separadores. No es un código. */
    private val phoneNumber = Regex(
        """(?<!\d)\+?(?:\d[ \t\u00A0\u202F./()-]{0,3}){8,14}\d(?!\d)""",
    )

    fun build(
        appLabel: String,
        title: String?,
        text: String?,
        bigText: String?,
        redactOneTimeCodes: Boolean = ProductScope.REDACT_ONE_TIME_CODES_BY_DEFAULT,
    ): String? {
        val label = appLabel.trim()
        val body = listOf(bigText, text).firstOrNull { !it.isNullOrBlank() }?.trim()
        val cleanTitle = title?.trim()?.takeUnless { it.isBlank() || it.equals(label, ignoreCase = true) }
        val spokenTitle = cleanTitle?.let { redact(it, redactOneTimeCodes) }
        val spokenBody = body?.let { redact(it, redactOneTimeCodes) }
        val parts = listOfNotNull(
            label.takeIf { it.isNotEmpty() },
            spokenTitle?.takeIf { it.isNotBlank() },
            spokenBody?.takeIf { it.isNotBlank() },
        )
        if (parts.isEmpty()) return null
        val phrase = parts.joinToString(". ").replace(Regex("\\s+"), " ").trim()
        if (phrase.isBlank()) return null
        return if (phrase.last() in TERMINAL_PUNCTUATION) phrase else "$phrase."
    }

    /**
     * Firma del contenido original, solo en memoria, para no repetir el mismo aviso.
     * No se guarda en disco.
     */
    fun signature(packageName: String, title: String?, body: String?): String {
        val raw = listOf(packageName, normalize(title), normalize(body)).joinToString("|")
        val digest = MessageDigest.getInstance("SHA-256").digest(raw.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { byte -> "%02x".format(byte) }
    }

    fun bodyOf(text: String?, bigText: String?): String? =
        listOf(bigText, text).firstOrNull { !it.isNullOrBlank() }?.trim()

    fun redact(value: String, redact: Boolean): String {
        if (!redact) return value
        val phones = ArrayList<String>()
        val shielded = phoneNumber.replace(value) { match ->
            val token = "\uE000${phones.size}\uE000"
            phones += match.value
            token
        }
        var spoken = oneTimeCode.replace(shielded, "oculto")
        for (index in phones.lastIndex downTo 0) {
            spoken = spoken.replace("\uE000$index\uE000", phones[index])
        }
        return spoken
    }

    private fun normalize(value: String?): String =
        value?.trim()?.replace(Regex("\\s+"), " ")?.lowercase(Locale.ROOT).orEmpty()

    private val TERMINAL_PUNCTUATION = setOf('.', '?', '!', '…')
}
