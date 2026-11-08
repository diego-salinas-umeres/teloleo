package app.teloleo.listener

import java.util.Locale

object WordFilter {
    fun blocks(title: String?, body: String?, words: List<String>): Boolean {
        val needles = words.map { it.trim().lowercase(Locale.ROOT) }.filter { it.isNotEmpty() }
        if (needles.isEmpty()) return false
        val haystack = listOfNotNull(title, body)
            .joinToString(" ")
            .lowercase(Locale.ROOT)
        if (haystack.isBlank()) return false
        return needles.any { haystack.contains(it) }
    }
}
