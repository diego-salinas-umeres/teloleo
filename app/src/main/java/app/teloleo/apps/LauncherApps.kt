package app.teloleo.apps

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import java.util.Locale

data class LaunchableApp(
    val packageName: String,
    val label: String,
)

/**
 * Apps con icono en el lanzador. La visibilidad sale del <queries> de MAIN y LAUNCHER.
 */
object LauncherApps {
    const val ACTION = Intent.ACTION_MAIN
    const val CATEGORY = Intent.CATEGORY_LAUNCHER

    fun load(packageManager: PackageManager, ownPackage: String): List<LaunchableApp> {
        val intent = Intent(ACTION).addCategory(CATEGORY)
        return query(packageManager, intent)
            .mapNotNull { info ->
                val pkg = info.activityInfo?.packageName ?: return@mapNotNull null
                if (pkg == ownPackage) return@mapNotNull null
                val label = info.loadLabel(packageManager)?.toString()?.trim().orEmpty().ifEmpty { pkg }
                LaunchableApp(pkg, label)
            }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase(Locale.ROOT) }
    }

    private fun query(packageManager: PackageManager, intent: Intent): List<ResolveInfo> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            queryModern(packageManager, intent)
        } else {
            queryLegacy(packageManager, intent)
        }
    }

    private fun queryModern(packageManager: PackageManager, intent: Intent): List<ResolveInfo> =
        packageManager.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0))

    @Suppress("DEPRECATION")
    private fun queryLegacy(packageManager: PackageManager, intent: Intent): List<ResolveInfo> =
        packageManager.queryIntentActivities(intent, 0)
}
