package app.teloleo.listener

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Test

class SpokenPhraseTest {
    @Test
    fun hiddenPreviewSpeaksOnlyTheApp() {
        assertEquals("WhatsApp.", SpokenPhrase.build("WhatsApp", "WhatsApp", null, null))
    }

    @Test
    fun titleAndBigTextAreSpokenOnce() {
        assertEquals(
            "WhatsApp. Ana. ¿Llegas?",
            SpokenPhrase.build("WhatsApp", "Ana", "¿Llegas?", "¿Llegas?"),
        )
    }

    @Test
    fun prefersTheLongerBigText() {
        assertEquals(
            "Gmail. Pedro. El informe completo va adjunto.",
            SpokenPhrase.build("Gmail", "Pedro", "El informe", "El informe completo va adjunto."),
        )
    }

    @Test
    fun oneTimeCodesAreNotSpoken() {
        val phrase = SpokenPhrase.build("Banco", "Banco", "Tu código es 482913", null)
        assertEquals("Banco. Tu código es oculto.", phrase)
        assertFalse(phrase!!.contains("482913"))
        assertEquals("oculto", SpokenPhrase.redact("482 913", true))
        assertEquals("oculto", SpokenPhrase.redact("482-913", true))
    }

    @Test
    fun phoneNumbersFromUnknownChatsStaySpoken() {
        assertEquals(
            "WhatsApp. 612 345 654. mensaje.",
            SpokenPhrase.build("WhatsApp", "612 345 654", "mensaje", null),
        )
        assertEquals(
            "WhatsApp. +34 612 345 654. mensaje.",
            SpokenPhrase.build("WhatsApp", "+34 612 345 654", "mensaje", null),
        )
        assertEquals(
            "Tu código es oculto. Llama al 612 345 654.",
            SpokenPhrase.redact("Tu código es 482913. Llama al 612 345 654.", true),
        )
        assertEquals("555-123-4567", SpokenPhrase.redact("555-123-4567", true))
    }

    @Test
    fun differentCodesKeepDifferentSignatures() {
        val first = SpokenPhrase.signature("com.bank", "Banco", "Tu código es 111111")
        val second = SpokenPhrase.signature("com.bank", "Banco", "Tu código es 222222")
        assertNotEquals(first, second)
    }
}
