package app.teloleo.ui

import app.teloleo.listener.SpokenPhrase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OnboardingNavTest {
    @Test
    fun progressCoversTheFourCountedSteps() {
        assertEquals(1, OnboardingNav.progress(OnboardingStep.Welcome))
        assertEquals(2, OnboardingNav.progress(OnboardingStep.Flow))
        assertEquals(3, OnboardingNav.progress(OnboardingStep.Permission))
        assertEquals(3, OnboardingNav.progress(OnboardingStep.Granted))
        assertEquals(3, OnboardingNav.progress(OnboardingStep.Denied))
        assertEquals(4, OnboardingNav.progress(OnboardingStep.Apps))
        assertNull(OnboardingNav.progress(OnboardingStep.Done))
    }

    @Test
    fun backReturnsToThePreviousIdea() {
        assertNull(OnboardingNav.back(OnboardingStep.Welcome, listenerEnabled = false))
        assertEquals(OnboardingStep.Welcome, OnboardingNav.back(OnboardingStep.Flow, false))
        assertEquals(OnboardingStep.Flow, OnboardingNav.back(OnboardingStep.Permission, false))
        assertEquals(OnboardingStep.Permission, OnboardingNav.back(OnboardingStep.Granted, true))
        assertEquals(OnboardingStep.Permission, OnboardingNav.back(OnboardingStep.Denied, false))
        assertEquals(OnboardingStep.Granted, OnboardingNav.back(OnboardingStep.Apps, listenerEnabled = true))
        assertEquals(OnboardingStep.Denied, OnboardingNav.back(OnboardingStep.Apps, listenerEnabled = false))
        assertEquals(OnboardingStep.Apps, OnboardingNav.back(OnboardingStep.Done, true))
    }

    @Test
    fun returningFromAndroidFollowsThePermission() {
        assertEquals(OnboardingStep.Granted, OnboardingNav.afterSystemSettings(listenerEnabled = true))
        assertEquals(OnboardingStep.Denied, OnboardingNav.afterSystemSettings(listenerEnabled = false))
    }

    @Test
    fun namesJoinInSpanish() {
        assertEquals("", OnboardingNav.joinNames(emptyList()))
        assertEquals("WhatsApp", OnboardingNav.joinNames(listOf("WhatsApp")))
        assertEquals("WhatsApp y Telegram", OnboardingNav.joinNames(listOf("WhatsApp", "Telegram")))
        assertEquals(
            "WhatsApp, Telegram y Gmail",
            OnboardingNav.joinNames(listOf("WhatsApp", "Telegram", "Gmail")),
        )
    }

    @Test
    fun sampleCodeIsSpokenAsHidden() {
        val sample = OnboardingNav.sampleNotice("com.google.android.gm")
        val spoken = SpokenPhrase.build("Gmail", sample.title, sample.text, null, redactOneTimeCodes = true)
        val plain = SpokenPhrase.build("Gmail", sample.title, sample.text, null, redactOneTimeCodes = false)
        assertEquals("Gmail. Banco. Tu código es oculto.", spoken)
        assertEquals("Gmail. Banco. Tu código es 482913.", plain)
    }
}
