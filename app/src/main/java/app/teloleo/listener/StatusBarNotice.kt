package app.teloleo.listener

import android.app.Notification
import android.service.notification.StatusBarNotification

internal fun StatusBarNotification.toIncomingNotice(
    ownPackageName: String,
    appLabel: String,
): IncomingNotice {
    val extras = notification.extras
    val template = extras.getString(Notification.EXTRA_TEMPLATE).orEmpty()
    val category = notification.category
    val progressMax = extras.getInt(Notification.EXTRA_PROGRESS_MAX, 0)
    val indeterminate = extras.getBoolean(Notification.EXTRA_PROGRESS_INDETERMINATE, false)
    val flags = notification.flags
    return IncomingNotice(
        packageName = packageName,
        ownPackageName = ownPackageName,
        appLabel = appLabel,
        title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString(),
        text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString(),
        bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString(),
        isOngoing = flags and Notification.FLAG_ONGOING_EVENT != 0 ||
            category == Notification.CATEGORY_SERVICE,
        isGroupSummary = flags and Notification.FLAG_GROUP_SUMMARY != 0,
        isMedia = template.endsWith("MediaStyle") ||
            extras.containsKey(Notification.EXTRA_MEDIA_SESSION) ||
            category == Notification.CATEGORY_TRANSPORT,
        isProgress = indeterminate || progressMax > 0 || category == Notification.CATEGORY_PROGRESS,
    )
}
