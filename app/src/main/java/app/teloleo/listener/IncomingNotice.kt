package app.teloleo.listener

data class IncomingNotice(
    val packageName: String,
    val ownPackageName: String,
    val appLabel: String,
    val title: String?,
    val text: String?,
    val bigText: String?,
    val isOngoing: Boolean,
    val isGroupSummary: Boolean,
    val isMedia: Boolean,
    val isProgress: Boolean,
)
