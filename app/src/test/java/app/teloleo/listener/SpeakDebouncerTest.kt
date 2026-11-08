package app.teloleo.listener

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SpeakDebouncerTest {
    private val debouncer = SpeakDebouncer(windowMillis = 8_000)

    @Test
    fun identicalPhraseIsSilentInsideTheWindow() {
        val signature = signatureOf("Hola")
        assertTrue(debouncer.shouldSpeak(signature, nowMillis = 1_000))
        assertFalse(debouncer.shouldSpeak(signature, nowMillis = 5_000))
    }

    @Test
    fun samePhraseSpeaksAgainAfterTheWindow() {
        val signature = signatureOf("Hola")
        assertTrue(debouncer.shouldSpeak(signature, nowMillis = 1_000))
        assertTrue(debouncer.shouldSpeak(signature, nowMillis = 9_000))
    }

    @Test
    fun aNewMessageIsNotSwallowed() {
        assertTrue(debouncer.shouldSpeak(signatureOf("Hola"), nowMillis = 1_000))
        assertTrue(debouncer.shouldSpeak(signatureOf("Ya voy"), nowMillis = 1_100))
    }

    private fun signatureOf(body: String): String {
        val result = NotificationGate.evaluate(
            IncomingNotice(
                packageName = "com.whatsapp",
                ownPackageName = "app.teloleo",
                appLabel = "WhatsApp",
                title = "Ana",
                text = body,
                bigText = null,
                isOngoing = false,
                isGroupSummary = false,
                isMedia = false,
                isProgress = false,
            ),
            allowedPackages = listOf("com.whatsapp"),
            proUnlocked = false,
        ) as GateResult.Speak
        return result.signature
    }
}
