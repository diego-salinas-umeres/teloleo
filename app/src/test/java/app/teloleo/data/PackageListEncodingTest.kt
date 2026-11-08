package app.teloleo.data

import org.junit.Assert.assertEquals
import org.junit.Test

class PackageListEncodingTest {
    @Test
    fun roundTripPreservesSelectionOrder() {
        val packages = listOf("com.b", "com.a", "com.c")
        assertEquals(packages, decodePackages(encodePackages(packages)))
    }

    @Test
    fun blankLinesAndDuplicatesAreDropped() {
        assertEquals(listOf("com.a", "com.b"), decodePackages("com.a\n\ncom.a\n com.b \n"))
    }

    @Test
    fun templatesRoundTripWithoutBreakingTheSeparator() {
        val encoded = encodeTemplates(mapOf("com.whatsapp" to "Mensaje de {app}, {remitente}"))
        assertEquals(
            mapOf("com.whatsapp" to "Mensaje de {app}, {remitente}"),
            decodeTemplates(encoded),
        )
        assertEquals(emptyMap<String, String>(), decodeTemplates(null))
    }
}
