package app.teloleo.tts

import org.junit.Assert.assertEquals
import org.junit.Test

class SpeechTextTest {
    @Test
    fun shoutedVaIsReadAsAWord() {
        assertEquals(
            "HOLA QUE TAL COMO te va",
            SpeechText.forEngine("HOLA QUE TAL COMO TE VA"),
        )
    }

    @Test
    fun otherPostalAbbreviationsInSpanishAreAlsoWords() {
        assertEquals(
            "la REUNION es de ANA. me dijo mi primo al llegar.",
            SpeechText.forEngine("LA REUNION ES DE ANA. ME dijo MI primo AL llegar."),
        )
    }

    @Test
    fun lowercaseAndLongerWordsStayUntouched() {
        assertEquals(
            "WhatsApp. Ana. ¿Llegas? VAMOS ya. USA.",
            SpeechText.forEngine("WhatsApp. Ana. ¿Llegas? VAMOS YA. USA."),
        )
    }

    @Test
    fun abbreviationKeepsItsPunctuation() {
        assertEquals("ok, va.", SpeechText.forEngine("OK, VA."))
    }

    @Test
    fun aWebLinkIsSpokenAsItsDomain() {
        assertEquals(
            "enlace a youtube punto com",
            SpeechText.forEngine("https://www.youtube.com/watch?v=avzvrDtkqW0"),
        )
        assertEquals(
            "Mira enlace a google punto com.",
            SpeechText.forEngine("Mira https://www.google.com/search?q=U%2B1FAEA&ie=UTF-8."),
        )
        assertEquals(
            "enlace a ejemplo punto com y enlace a otro punto test.",
            SpeechText.forEngine("https://ejemplo.com/a y http://otro.test/b?x=1."),
        )
        assertEquals(
            "Visita enlace a google punto com, por favor.",
            SpeechText.forEngine("Visita www.google.com/search?q=hola, por favor."),
        )
    }

    @Test
    fun distortedFaceIsNamed() {
        val face = String(Character.toChars(0x1FAEA))
        assertEquals("cara distorsionada", SpeechText.forEngine(face))
        assertEquals("Hola cara distorsionada.", SpeechText.forEngine("Hola $face."))
        assertEquals("Hola cara distorsionada.", SpeechText.forEngine("Hola $face\uFE0F."))
        assertEquals("Hola 😀.", SpeechText.forEngine("Hola 😀."))
    }
}
