package app.teloleo.listener

import app.teloleo.scope.AllowedApps

sealed class GateResult {
    data class Speak(val signature: String, val phrase: String) : GateResult()
    data class Skip(val reason: SkipReason) : GateResult()
}

enum class SkipReason {
    OWN_APP,
    NOT_ALLOWED,
    ONGOING,
    GROUP_SUMMARY,
    MEDIA,
    PROGRESS,
    EMPTY,
    SILENCED,
    QUIET_HOURS,
    HEADPHONES,
    WORD_FILTER,
}

object NotificationGate {
    fun evaluate(
        notice: IncomingNotice,
        allowedPackages: List<String>,
        proUnlocked: Boolean,
        policy: ReadPolicy = ReadPolicy(),
    ): GateResult {
        if (notice.packageName == notice.ownPackageName) {
            return GateResult.Skip(SkipReason.OWN_APP)
        }
        if (notice.isOngoing) return GateResult.Skip(SkipReason.ONGOING)
        if (notice.isGroupSummary) return GateResult.Skip(SkipReason.GROUP_SUMMARY)
        if (notice.isMedia) return GateResult.Skip(SkipReason.MEDIA)
        if (notice.isProgress) return GateResult.Skip(SkipReason.PROGRESS)

        val speaking = AllowedApps.speakingPackages(allowedPackages, proUnlocked)
        if (notice.packageName !in speaking) return GateResult.Skip(SkipReason.NOT_ALLOWED)

        if (policy.silenced) return GateResult.Skip(SkipReason.SILENCED)
        if (proUnlocked && policy.nowMinutesOfDay >= 0 && policy.quiet.covers(policy.nowMinutesOfDay)) {
            return GateResult.Skip(SkipReason.QUIET_HOURS)
        }
        if (proUnlocked && policy.requireHeadphones && !policy.headphonesConnected) {
            return GateResult.Skip(SkipReason.HEADPHONES)
        }
        if (proUnlocked && WordFilter.blocks(notice.title, SpokenPhrase.bodyOf(notice.text, notice.bigText), policy.blockedWords)) {
            return GateResult.Skip(SkipReason.WORD_FILTER)
        }

        val phrase = phraseFor(notice, proUnlocked, policy) ?: return GateResult.Skip(SkipReason.EMPTY)

        val signature = SpokenPhrase.signature(
            packageName = notice.packageName,
            title = notice.title,
            body = SpokenPhrase.bodyOf(notice.text, notice.bigText),
        )
        return GateResult.Speak(signature, phrase)
    }

    private fun phraseFor(notice: IncomingNotice, proUnlocked: Boolean, policy: ReadPolicy): String? {
        val custom = policy.template?.takeIf { proUnlocked && it.isNotBlank() }
        if (custom != null) {
            PhraseTemplate.render(
                template = custom,
                appLabel = notice.appLabel,
                title = notice.title,
                text = notice.text,
                bigText = notice.bigText,
                redactOneTimeCodes = policy.redactOneTimeCodes,
            )?.let { return it }
        }
        return SpokenPhrase.build(
            appLabel = notice.appLabel,
            title = notice.title,
            text = notice.text,
            bigText = notice.bigText,
            redactOneTimeCodes = policy.redactOneTimeCodes,
        )
    }
}
