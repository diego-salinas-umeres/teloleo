package app.teloleo.listener

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationGateTest {
    private val allowed = listOf("com.whatsapp", "com.telegram", "com.google.android.gm", "com.slack")

    @Test
    fun allowedAppIsSpoken() {
        val result = NotificationGate.evaluate(notice(), allowed, proUnlocked = false)
        val spoken = result as GateResult.Speak
        assertEquals("WhatsApp. Ana. Hola.", spoken.phrase)
    }

    @Test
    fun fourthAppStaysSilentUntilPro() {
        val slack = notice(packageName = "com.slack", appLabel = "Slack")
        assertEquals(SkipReason.NOT_ALLOWED, (NotificationGate.evaluate(slack, allowed, false) as GateResult.Skip).reason)
        assertTrue(NotificationGate.evaluate(slack, allowed, proUnlocked = true) is GateResult.Speak)
    }

    @Test
    fun unknownAppStaysSilent() {
        val result = NotificationGate.evaluate(
            notice(packageName = "com.other"),
            allowed,
            proUnlocked = true,
        )
        assertEquals(SkipReason.NOT_ALLOWED, (result as GateResult.Skip).reason)
    }

    @Test
    fun ownAppOngoingSummaryMediaAndProgressAreDropped() {
        assertEquals(SkipReason.OWN_APP, skip(notice(packageName = "app.teloleo", ownPackageName = "app.teloleo")))
        assertEquals(SkipReason.ONGOING, skip(notice(isOngoing = true)))
        assertEquals(SkipReason.GROUP_SUMMARY, skip(notice(isGroupSummary = true)))
        assertEquals(SkipReason.MEDIA, skip(notice(isMedia = true)))
        assertEquals(SkipReason.PROGRESS, skip(notice(isProgress = true)))
    }

    @Test
    fun proPoliciesStayOffUntilThePurchaseAndSilenceWorksForFree() {
        val quiet = ReadPolicy(
            quiet = QuietHours(enabled = true, startMinutes = 22 * 60, endMinutes = 7 * 60),
            nowMinutesOfDay = 23 * 60,
            requireHeadphones = true,
            headphonesConnected = false,
            blockedWords = listOf("hola"),
            template = "Mensaje de {app}, {remitente}. {texto}",
            silenced = false,
        )
        assertTrue(NotificationGate.evaluate(notice(), allowed, proUnlocked = false, quiet) is GateResult.Speak)
        assertEquals(
            SkipReason.QUIET_HOURS,
            (NotificationGate.evaluate(notice(), allowed, proUnlocked = true, quiet) as GateResult.Skip).reason,
        )
        val headphones = quiet.copy(quiet = QuietHours.Off, nowMinutesOfDay = 12 * 60)
        assertEquals(
            SkipReason.HEADPHONES,
            (NotificationGate.evaluate(notice(), allowed, proUnlocked = true, headphones) as GateResult.Skip).reason,
        )
        val words = ReadPolicy(blockedWords = listOf("hola"))
        assertEquals(
            SkipReason.WORD_FILTER,
            (NotificationGate.evaluate(notice(), allowed, proUnlocked = true, words) as GateResult.Skip).reason,
        )
        val template = ReadPolicy(template = "Mensaje de {app}, {remitente}. {texto}")
        val spoken = NotificationGate.evaluate(notice(), allowed, proUnlocked = true, template) as GateResult.Speak
        assertEquals("Mensaje de WhatsApp, Ana. Hola.", spoken.phrase)
        assertEquals(
            SkipReason.SILENCED,
            (NotificationGate.evaluate(notice(), allowed, proUnlocked = false, ReadPolicy(silenced = true)) as GateResult.Skip).reason,
        )
    }

    @Test
    fun emptyAllowlistSpeaksNothing() {
        assertEquals(
            SkipReason.NOT_ALLOWED,
            skip(notice(), allowed = emptyList(), proUnlocked = false),
        )
    }

    private fun skip(
        notice: IncomingNotice,
        allowed: List<String> = this.allowed,
        proUnlocked: Boolean = true,
    ): SkipReason = (NotificationGate.evaluate(notice, allowed, proUnlocked) as GateResult.Skip).reason

    private fun notice(
        packageName: String = "com.whatsapp",
        ownPackageName: String = "app.teloleo",
        appLabel: String = "WhatsApp",
        title: String? = "Ana",
        text: String? = "Hola",
        bigText: String? = null,
        isOngoing: Boolean = false,
        isGroupSummary: Boolean = false,
        isMedia: Boolean = false,
        isProgress: Boolean = false,
    ) = IncomingNotice(
        packageName = packageName,
        ownPackageName = ownPackageName,
        appLabel = appLabel,
        title = title,
        text = text,
        bigText = bigText,
        isOngoing = isOngoing,
        isGroupSummary = isGroupSummary,
        isMedia = isMedia,
        isProgress = isProgress,
    )
}
