package app.teloleo.data

import java.util.Locale

/**
 * Vacío sigue el idioma del teléfono. La etiqueta guardada manda.
 * Si el país no está en el motor, se prueba después solo el idioma.
 */
fun voiceLocaleCandidates(savedTag: String, system: Locale = Locale.getDefault()): List<Locale> {
    val chosen = localeForVoice(savedTag) ?: system
    return fallbackOrder(chosen)
}

/** Vacío significa el idioma del teléfono. Una etiqueta rara también. */
fun canonicalVoiceTag(raw: String): String {
    val locale = localeForVoice(raw) ?: return ""
    val language = locale.language
    val country = locale.country
    return if (country.isNullOrBlank()) language else "$language-$country"
}

fun voiceLanguageLabel(tag: String, inLocale: Locale = Locale.getDefault()): String {
    val canonical = canonicalVoiceTag(tag)
    if (canonical.isEmpty()) return ""
    return Locale.forLanguageTag(canonical).getDisplayName(inLocale).ifBlank { canonical }
}

/** Voces con red fuera. Varias voces del mismo idioma quedan en una sola etiqueta. */
fun offlineVoiceLanguageTags(voices: Iterable<Pair<String, Boolean>>): List<String> {
    return voices
        .asSequence()
        .filter { (_, needsNetwork) -> !needsNetwork }
        .map { (tag, _) -> canonicalVoiceTag(tag) }
        .filter { it.isNotEmpty() }
        .distinct()
        .toList()
}

private fun localeForVoice(raw: String): Locale? {
    val trimmed = raw.trim().replace('_', '-')
    if (trimmed.isEmpty()) return null
    val locale = Locale.forLanguageTag(trimmed)
    val language = locale.language
    if (language.isNullOrBlank() || language.equals("und", ignoreCase = true)) return null
    return locale
}

private fun fallbackOrder(locale: Locale): List<Locale> {
    val language = locale.language?.takeIf { it.isNotBlank() } ?: return listOf(locale)
    val languageOnly = Locale.forLanguageTag(language)
    val country = locale.country?.takeIf { it.isNotBlank() } ?: return listOf(languageOnly)
    return listOf(Locale.forLanguageTag("$language-$country"), languageOnly)
}
