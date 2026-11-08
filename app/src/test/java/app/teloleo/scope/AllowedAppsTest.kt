package app.teloleo.scope

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AllowedAppsTest {
    private val four = listOf("com.whatsapp", "com.telegram", "com.google.android.gm", "com.slack")

    @Test
    fun freeTierKeepsTheFirstThreeInSelectionOrder() {
        assertEquals(
            setOf("com.whatsapp", "com.telegram", "com.google.android.gm"),
            AllowedApps.speakingPackages(four, proUnlocked = false),
        )
    }

    @Test
    fun proSpeaksEverySelectedApp() {
        assertEquals(four.toSet(), AllowedApps.speakingPackages(four, proUnlocked = true))
    }

    @Test
    fun duplicatesAndBlankEntriesDoNotConsumeTheFreeSlots() {
        val selected = listOf(" com.whatsapp ", "", "com.whatsapp", "com.telegram")
        assertEquals(
            setOf("com.whatsapp", "com.telegram"),
            AllowedApps.speakingPackages(selected, proUnlocked = false),
        )
    }

    @Test
    fun fourthSelectionNeedsProAndRemovalKeepsOrder() {
        val three = listOf("com.whatsapp", "com.telegram", "com.google.android.gm")
        assertTrue(AllowedApps.changeSelection(three, "com.slack", proUnlocked = false) is SelectionChange.NeedsPro)
        assertEquals(
            listOf("com.whatsapp", "com.telegram", "com.google.android.gm", "com.slack"),
            (AllowedApps.changeSelection(three, "com.slack", proUnlocked = true) as SelectionChange.Updated).packages,
        )
        assertEquals(
            listOf("com.whatsapp", "com.google.android.gm"),
            (AllowedApps.changeSelection(three, "com.telegram", proUnlocked = false) as SelectionChange.Updated).packages,
        )
    }
}
