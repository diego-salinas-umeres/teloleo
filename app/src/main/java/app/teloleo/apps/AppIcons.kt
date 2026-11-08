package app.teloleo.apps

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas

/**
 * Iconos ya dibujados en memoria. La lista los pinta al instante;
 * al scrollear no se vuelve a pedir cada icono al sistema.
 */
object AppIcons {
    const val SIZE_DP = 40

    private val lock = Any()
    private val cache = HashMap<String, Bitmap>()
    private var sizePx: Int = 0

    fun peek(packageName: String): Bitmap? = synchronized(lock) { cache[packageName] }

    fun warm(packageManager: PackageManager, packages: Collection<String>, sizePx: Int) {
        val px = sizePx.coerceAtLeast(1)
        val wanted = LinkedHashSet<String>()
        for (pkg in packages) {
            val name = pkg.trim()
            if (name.isNotEmpty()) wanted.add(name)
        }
        val missing = synchronized(lock) {
            if (this.sizePx != px) {
                cache.clear()
                this.sizePx = px
            } else {
                cache.keys.retainAll(wanted)
            }
            wanted.filter { it !in cache }
        }
        val decoded = ArrayList<Pair<String, Bitmap>>(missing.size)
        for (pkg in missing) {
            val bitmap = rasterize(packageManager, pkg, px) ?: continue
            decoded.add(pkg to bitmap)
        }
        synchronized(lock) {
            if (this.sizePx != px) return
            for ((pkg, bitmap) in decoded) {
                if (pkg in wanted) cache[pkg] = bitmap
            }
        }
    }

    private fun rasterize(packageManager: PackageManager, packageName: String, sizePx: Int): Bitmap? {
        val drawable = try {
            packageManager.getApplicationIcon(packageName)
        } catch (_: Exception) {
            return null
        }
        return try {
            val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, sizePx, sizePx)
            drawable.draw(canvas)
            bitmap
        } catch (_: Exception) {
            null
        }
    }
}
