package app.teloleo.data

import org.junit.Assert.assertEquals
import org.junit.Test

class TemplateEncodingTest {
    @Test
    fun roundTripKeepsThePackageAndThePhrase() {
        val stored = mapOf("com.whatsapp" to "Mensaje de {app}, {remitente}. {texto}")
        assertEquals(stored, decodeTemplates(encodeTemplates(stored)))
    }
}
