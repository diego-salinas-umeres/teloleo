package app.teloleo.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class PlayComplianceCopyTest {
    @Test
    fun disclosureComesBeforeTheSystemSettingAndPrivacyStaysOnDevice() {
        val app = File("src/main/java/app/teloleo/ui/TeLoLeoApp.kt").readText()
        val ask = app.indexOf("onAskListenerAccess = { showDisclosure = true }")
        val open = app.indexOf("onOpenListenerSettings()")
        assertTrue(ask >= 0)
        assertTrue(open > ask)

        val strings = File("src/main/res/values/strings.xml").readText()
        assertTrue(strings.contains("El texto no sale del teléfono"))
        assertTrue(strings.contains("no se vende"))
        assertTrue(strings.contains("códigos de un solo uso"))
        assertTrue(strings.contains("no recopila ni comparte datos"))
        assertTrue(strings.contains("Entendido, abrir ajustes"))

        val page = File("../privacy/index.html").readText()
        assertTrue(page.contains("El texto no sale del teléfono"))
        assertTrue(page.contains("no recopila ni comparte datos"))
        assertFalse(page.contains("analytics"))
        assertFalse(page.contains("http://"))
        assertFalse(page.contains("https://"))
    }
}
