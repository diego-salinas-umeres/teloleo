package app.teloleo.scope

/**
 * La lista guardada conserva el orden en que el usuario marcó las apps.
 * Sin Pro solo hablan las primeras [ProductScope.FREE_APP_LIMIT].
 */
object AllowedApps {
    fun speakingPackages(selected: List<String>, proUnlocked: Boolean): Set<String> {
        val speaking = if (proUnlocked) distinct(selected) else distinct(selected).take(ProductScope.FREE_APP_LIMIT)
        return speaking.toSet()
    }

    /**
     * Marcar añade al final, para que el cupo gratis respete el orden de elección.
     * Quitar no reordena el resto. La cuarta app, sin Pro, no entra en la lista.
     */
    fun changeSelection(
        selected: List<String>,
        packageName: String,
        proUnlocked: Boolean,
    ): SelectionChange {
        val current = distinct(selected)
        val pkg = packageName.trim()
        if (pkg.isEmpty()) return SelectionChange.Updated(current)
        if (pkg in current) return SelectionChange.Updated(current.filterNot { it == pkg })
        if (!proUnlocked && current.size >= ProductScope.FREE_APP_LIMIT) return SelectionChange.NeedsPro
        return SelectionChange.Updated(current + pkg)
    }

    private fun distinct(selected: List<String>): List<String> =
        selected.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
}

sealed class SelectionChange {
    data class Updated(val packages: List<String>) : SelectionChange()
    data object NeedsPro : SelectionChange()
}
