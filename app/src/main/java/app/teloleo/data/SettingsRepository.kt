package app.teloleo.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import app.teloleo.scope.SpeechLimits
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.settingsDataStore by preferencesDataStore(name = "teloleo_settings")

/**
 * Ajustes en el dispositivo. Un string set de DataStore no conserva el orden,
 * y el cupo gratis depende de ese orden, así que los paquetes van en una sola cadena.
 *
 * Pro no se concede desde estos métodos, salvo [setProUnlocked], que solo
 * llama el cliente de Play Billing cuando la compra está en el teléfono.
 */
class SettingsRepository(context: Context) {
    private val dataStore = context.applicationContext.settingsDataStore

    val settings: Flow<ReaderSettings> = dataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { prefs -> prefs.readSettings() }

    suspend fun setAllowedPackages(packages: List<String>) = edit { it[ALLOWED_PACKAGES] = encodePackages(packages) }

    suspend fun setProUnlocked(unlocked: Boolean) = edit { it[PRO_UNLOCKED] = unlocked }

    suspend fun setRedactOneTimeCodes(redact: Boolean) = edit { it[REDACT_CODES] = redact }

    suspend fun setQuietHours(enabled: Boolean, startMinutes: Int, endMinutes: Int) = edit {
        it[QUIET_ENABLED] = enabled
        it[QUIET_START] = clockMinute(startMinutes)
        it[QUIET_END] = clockMinute(endMinutes)
    }

    suspend fun setHeadphonesOnly(enabled: Boolean) = edit { it[HEADPHONES_ONLY] = enabled }

    suspend fun setShakeToSilence(enabled: Boolean) = edit { it[SHAKE] = enabled }

    /** Pausa la lectura sin tocar el permiso. Sigue así hasta que se vuelva a activar. */
    suspend fun setReadingPaused(paused: Boolean) = edit { it[READING_PAUSED] = paused }

    /** Vacío guarda el idioma del teléfono. Una etiqueta, por ejemplo `en-US`, lo fija. */
    suspend fun setVoiceLocale(localeTag: String) = edit { it[VOICE_LOCALE] = canonicalVoiceTag(localeTag) }

    /** El recorrido de bienvenida no vuelve a salir. */
    suspend fun setOnboardingComplete(done: Boolean) = edit { it[ONBOARDING_DONE] = done }

    suspend fun setSpeechRate(rate: Float) = edit { it[SPEECH_RATE] = SpeechLimits.rate(rate) }

    suspend fun setSpeechVolume(volume: Float) = edit { it[SPEECH_VOLUME] = SpeechLimits.volume(volume) }

    suspend fun setWordFilters(words: List<String>) = edit {
        it[WORD_FILTERS] = encodePackages(words.filter { word -> word.trim().length >= 2 })
    }

    suspend fun setPhraseTemplate(template: String) = edit {
        it[PHRASE_TEMPLATE] = SpeechLimits.template(template)
    }

    suspend fun setAppTemplate(packageName: String, template: String) = edit { prefs ->
        val current = decodeTemplates(prefs[APP_TEMPLATES]).toMutableMap()
        val name = packageName.trim()
        val text = SpeechLimits.template(template)
        if (name.isEmpty() || text.isEmpty()) current.remove(name) else current[name] = text
        prefs[APP_TEMPLATES] = encodeTemplates(current)
    }

    private suspend fun edit(block: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        dataStore.edit(block)
    }

    private companion object {
        val ALLOWED_PACKAGES = stringPreferencesKey("allowed_packages")
        val PRO_UNLOCKED = booleanPreferencesKey("pro_unlocked")
        val REDACT_CODES = booleanPreferencesKey("redact_one_time_codes")
        val QUIET_ENABLED = booleanPreferencesKey("quiet_hours_enabled")
        val QUIET_START = intPreferencesKey("quiet_start_minutes")
        val QUIET_END = intPreferencesKey("quiet_end_minutes")
        val HEADPHONES_ONLY = booleanPreferencesKey("headphones_only")
        val WORD_FILTERS = stringPreferencesKey("word_filters")
        val PHRASE_TEMPLATE = stringPreferencesKey("phrase_template")
        val APP_TEMPLATES = stringPreferencesKey("app_templates")
        val SPEECH_RATE = floatPreferencesKey("speech_rate")
        val SPEECH_VOLUME = floatPreferencesKey("speech_volume")
        val SHAKE = booleanPreferencesKey("shake_to_silence")
        val READING_PAUSED = booleanPreferencesKey("reading_paused")
        val VOICE_LOCALE = stringPreferencesKey("voice_locale")
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_complete")

        fun androidx.datastore.preferences.core.Preferences.readSettings(): ReaderSettings {
            return ReaderSettings(
                allowedPackages = decodePackages(this[ALLOWED_PACKAGES]),
                proUnlocked = this[PRO_UNLOCKED] ?: false,
                redactOneTimeCodes = this[REDACT_CODES] ?: true,
                quietHoursEnabled = this[QUIET_ENABLED] ?: false,
                quietStartMinutes = clockMinute(this[QUIET_START] ?: SpeechLimits.QUIET_START),
                quietEndMinutes = clockMinute(this[QUIET_END] ?: SpeechLimits.QUIET_END),
                headphonesOnly = this[HEADPHONES_ONLY] ?: false,
                wordFilters = decodePackages(this[WORD_FILTERS]).filter { it.length >= 2 },
                phraseTemplate = SpeechLimits.template(this[PHRASE_TEMPLATE].orEmpty()),
                appTemplates = decodeTemplates(this[APP_TEMPLATES]),
                speechRate = SpeechLimits.rate(this[SPEECH_RATE] ?: 1f),
                speechVolume = SpeechLimits.volume(this[SPEECH_VOLUME] ?: 1f),
                shakeToSilence = this[SHAKE] ?: false,
                readingPaused = this[READING_PAUSED] ?: false,
                voiceLocale = canonicalVoiceTag(this[VOICE_LOCALE].orEmpty()),
                onboardingComplete = this[ONBOARDING_DONE] ?: false,
            )
        }
    }
}

internal fun encodePackages(packages: List<String>): String =
    packages.map { it.trim() }.filter { it.isNotEmpty() }.distinct().joinToString("\n")

internal fun decodePackages(raw: String?): List<String> =
    raw?.lineSequence()
        ?.map { it.trim() }
        ?.filter { it.isNotEmpty() }
        ?.distinct()
        ?.toList()
        ?: emptyList()

internal fun encodeTemplates(templates: Map<String, String>): String =
    templates.mapNotNull { (pkg, template) ->
        val name = pkg.trim()
        val text = template.replace('\n', ' ').replace('\t', ' ').trim()
        if (name.isEmpty() || text.isEmpty()) null else "$name\t$text"
    }.joinToString("\n")

internal fun decodeTemplates(raw: String?): Map<String, String> {
    if (raw.isNullOrBlank()) return emptyMap()
    val decoded = linkedMapOf<String, String>()
    raw.lineSequence().forEach { line ->
        val tab = line.indexOf('\t')
        if (tab <= 0) return@forEach
        val pkg = line.substring(0, tab).trim()
        val text = line.substring(tab + 1).trim()
        if (pkg.isNotEmpty() && text.isNotEmpty()) decoded[pkg] = text
    }
    return decoded
}

private fun clockMinute(minutes: Int): Int = minutes.coerceIn(0, 24 * 60 - 1)
