package app.teloleo.scope

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class OnDeviceWiringTest {
    @Test
    fun pickerUsesLauncherQueriesAndBillingStaysOnTheDevice() {
        val picker = File("src/main/java/app/teloleo/apps/LauncherApps.kt").readText()
        assertTrue(picker.contains("queryIntentActivities"))
        assertTrue(picker.contains("ACTION_MAIN"))
        assertTrue(picker.contains("CATEGORY_LAUNCHER"))
        assertFalse(picker.contains("QUERY_ALL_PACKAGES"))
        assertFalse(picker.contains("getInstalledApplications"))
        assertFalse(picker.contains("getInstalledPackages"))

        val billing = File("src/main/java/app/teloleo/billing/ProBilling.kt").readText()
        assertTrue(billing.contains("queryPurchasesAsync"))
        assertTrue(billing.contains("ProductType.INAPP"))
        assertTrue(billing.contains("acknowledgePurchase"))
        assertFalse(billing.contains("consumeAsync"))
        assertFalse(billing.contains("ProductType.SUBS"))
        assertFalse(billing.contains("http://"))
        assertFalse(billing.contains("https://"))

        val privacy = File("../privacy/index.html").readText()
        assertTrue(privacy.contains("no recopila ni comparte datos"))
        assertTrue(privacy.contains("teloleo_pro"))
        assertFalse(privacy.contains("<script"))
    }
}
