package app.teloleo.apps

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AppIconsTest {
    @Test
    fun scrollingPaintsCachedIconsInsteadOfLoadingThemAgain() {
        val screen = File("src/main/java/app/teloleo/ui/AppsScreen.kt").readText()
        assertTrue(screen.contains("AppIcons.peek"))
        assertFalse(screen.contains("getApplicationIcon"))
        assertFalse(screen.contains("produceState"))
        assertFalse(screen.contains("AndroidView"))

        val cache = File("src/main/java/app/teloleo/apps/AppIcons.kt").readText()
        assertTrue(cache.contains("getApplicationIcon"))
        assertTrue(cache.contains("fun peek"))
        assertTrue(cache.contains("fun warm"))

        val activity = File("src/main/java/app/teloleo/MainActivity.kt").readText()
        assertTrue(activity.contains("AppIcons.warm"))
        val warmBeforePublish = activity.indexOf("AppIcons.warm") < activity.indexOf("appsLoaded = true")
        assertTrue(warmBeforePublish)
    }
}
