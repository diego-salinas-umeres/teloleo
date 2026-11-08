package app.teloleo.data

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class VoiceLocaleTest {
    @Test
    fun emptyTagFollowsThePhoneLanguage() {
        val phone = Locale.forLanguageTag("es-MX")
        assertEquals(listOf("es-MX", "es"), tagsOf("", phone))
        assertEquals(listOf("es-MX", "es"), tagsOf("   ", phone))
        assertEquals("", canonicalVoiceTag(" "))
    }

    @Test
    fun savedTagOverridesThePhoneLanguage() {
        val phone = Locale.forLanguageTag("es-MX")
        assertEquals(listOf("en-US", "en"), tagsOf("en-US", phone))
        assertEquals(listOf("en-US", "en"), tagsOf("en_US", phone))
        assertEquals("en-US", canonicalVoiceTag("en_US"))
    }

    @Test
    fun countryFallsBackToTheLanguage() {
        val order = tagsOf("fr-CA", Locale.forLanguageTag("en-US"))
        assertEquals(listOf("fr-CA", "fr"), order)
        assertEquals(listOf("ja"), tagsOf("ja", Locale.forLanguageTag("en-US")))
    }

    @Test
    fun offlineVoicesGroupByLanguageAndSkipTheNetwork() {
        val tags = offlineVoiceLanguageTags(
            listOf(
                "en-US" to false,
                "en_US" to false,
                "en-GB" to false,
                "fr-FR" to true,
                "es-ES" to false,
            ),
        )
        assertEquals(listOf("en-US", "en-GB", "es-ES"), tags)
    }

    private fun tagsOf(saved: String, phone: Locale): List<String> =
        voiceLocaleCandidates(saved, phone).map { it.toLanguageTag() }
}
