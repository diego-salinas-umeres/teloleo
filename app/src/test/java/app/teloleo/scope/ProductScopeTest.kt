package app.teloleo.scope

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ProductScopeTest {
    @Test
    fun v1IsAndroidOnlyWithAThreeAppFreeTier() {
        assertEquals("app.teloleo", ProductScope.APPLICATION_ID)
        assertEquals(26, ProductScope.MIN_SDK)
        assertEquals(3, ProductScope.FREE_APP_LIMIT)
    }

    @Test
    fun proIsAOneTimeUnlockWithoutAdsOrABackend() {
        assertEquals("teloleo_pro", ProductScope.PRO_PRODUCT_ID)
        assertFalse(ProductScope.PRO_IS_SUBSCRIPTION)
        assertFalse(ProductScope.ADS_ENABLED)
        assertFalse(ProductScope.BACKEND_ENABLED)
        assertTrue(ProductScope.REDACT_ONE_TIME_CODES_BY_DEFAULT)
        assertFalse(ProductScope.COLLECTS_PERSONAL_DATA)
        assertFalse(ProductScope.SHARES_DATA)
        assertFalse(ProductScope.STORES_NOTIFICATION_TEXT)
        assertTrue(ProductScope.redactOrDefault(null))
        assertFalse(ProductScope.redactOrDefault(false))
    }

    @Test
    fun moduleStaysOnComposeAndDoesNotPullAdsOrABackend() {
        val gradle = File("build.gradle.kts").readText()
        assertTrue(gradle.contains("compose = true"))
        assertTrue(gradle.contains("material3"))
        assertTrue(gradle.contains("""applicationId = "app.teloleo""""))
        assertTrue(gradle.contains("minSdk = 26"))
        assertTrue(gradle.contains("billing-ktx"))
        assertFalse(gradle.contains("play-services-ads"))
        assertFalse(gradle.contains("retrofit"))
        assertFalse(gradle.contains("okhttp"))
        assertFalse(gradle.contains("firebase"))
        assertFalse(gradle.contains("revenuecat", ignoreCase = true))
    }

    @Test
    fun manifestListensToNotificationsAndDoesNotOpenTheNetwork() {
        val manifest = File("src/main/AndroidManifest.xml").readText()
        assertTrue(manifest.contains("NotificationListenerService"))
        assertTrue(manifest.contains("android.intent.action.MAIN"))
        assertFalse(manifest.contains("INTERNET"))
        assertFalse(manifest.contains("QUERY_ALL_PACKAGES"))
        assertFalse(manifest.contains("BIND_ACCESSIBILITY_SERVICE"))
        assertFalse(manifest.contains("AD_ID"))
    }
}
