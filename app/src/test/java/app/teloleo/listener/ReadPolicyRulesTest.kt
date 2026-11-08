package app.teloleo.listener

import android.media.AudioDeviceInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReadPolicyRulesTest {
    @Test
    fun quietHoursCrossMidnightAndIgnoreAnEmptyWindow() {
        val overnight = QuietHours(enabled = true, startMinutes = 22 * 60, endMinutes = 7 * 60)
        assertTrue(overnight.covers(23 * 60))
        assertTrue(overnight.covers(6 * 60))
        assertFalse(overnight.covers(12 * 60))
        assertFalse(overnight.copy(enabled = false).covers(23 * 60))
        assertFalse(QuietHours(enabled = true, startMinutes = 8 * 60, endMinutes = 8 * 60).covers(8 * 60))
    }

    @Test
    fun wordFilterMatchesInsideTheTextIgnoringCase() {
        assertTrue(WordFilter.blocks("Ana", "Promoción de verano", listOf("promo")))
        assertFalse(WordFilter.blocks("Ana", "Hola", listOf("promo")))
        assertFalse(WordFilter.blocks("Ana", "Hola", emptyList()))
    }

    @Test
    fun templateDropsAnEmptySenderAndHidesCodes() {
        assertEquals(
            "Mensaje de WhatsApp. Hola.",
            PhraseTemplate.render(
                "Mensaje de {app}, {remitente}. {texto}",
                "WhatsApp",
                "WhatsApp",
                "Hola",
                null,
                redactOneTimeCodes = true,
            ),
        )
        val hidden = PhraseTemplate.render(
            "{app}. {texto}",
            "Banco",
            null,
            "Tu código es 482913",
            null,
            redactOneTimeCodes = true,
        )
        assertEquals("Banco. Tu código es oculto.", hidden)
        assertFalse(hidden!!.contains("482913"))
        assertEquals(null, PhraseTemplate.render("solo texto fijo", "Banco", null, "Hola", null, true))
    }

    @Test
    fun shakeNeedsASecondSampleAndThenWaits() {
        val detector = ShakeDetector(threshold = 12f, cooldownMillis = 800)
        assertFalse(detector.sample(0f, 0f, 9.8f, nowMillis = 0))
        assertTrue(detector.sample(0f, 0f, 30f, nowMillis = 100))
        assertFalse(detector.sample(0f, 0f, 0f, nowMillis = 200))
        assertTrue(detector.sample(0f, 0f, 30f, nowMillis = 1_200))
    }

    @Test
    fun headphonesAreNotThePhoneSpeaker() {
        assertTrue(AudioRoute.isHeadphone(AudioDeviceInfo.TYPE_WIRED_HEADSET))
        assertTrue(AudioRoute.isHeadphone(AudioDeviceInfo.TYPE_BLUETOOTH_A2DP))
        assertFalse(AudioRoute.isHeadphone(AudioDeviceInfo.TYPE_BUILTIN_SPEAKER))
        assertTrue(AudioRoute.connected(listOf(AudioDeviceInfo.TYPE_BUILTIN_SPEAKER, AudioDeviceInfo.TYPE_USB_HEADSET)))
        assertFalse(AudioRoute.connected(listOf(AudioDeviceInfo.TYPE_BUILTIN_SPEAKER)))
    }
}
