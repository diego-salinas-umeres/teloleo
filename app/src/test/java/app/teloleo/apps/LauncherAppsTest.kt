package app.teloleo.apps

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class LauncherAppsTest {
    @Test
    fun pickerUsesTheLauncherQueryAndDoesNotAskForEveryPackage() {
        val source = File("src/main/java/app/teloleo/apps/LauncherApps.kt").readText()
        assertTrue(source.contains("queryIntentActivities"))
        assertTrue(source.contains("ACTION_MAIN") || source.contains("android.intent.action.MAIN"))
        assertTrue(source.contains("CATEGORY_LAUNCHER") || source.contains("android.intent.category.LAUNCHER"))
        assertFalse(source.contains("QUERY_ALL_PACKAGES"))
    }
}
