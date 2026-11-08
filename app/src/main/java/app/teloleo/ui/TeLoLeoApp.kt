package app.teloleo.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.teloleo.R
import app.teloleo.apps.LaunchableApp
import app.teloleo.billing.BillingOffer
import app.teloleo.data.ReaderSettings
import app.teloleo.device.BatteryGuides
import app.teloleo.scope.AllowedApps
import app.teloleo.scope.SelectionChange

private enum class AppPage { Home, Apps, Lectura, Privacy, Pro, Voice }

private class DirectListenerOpener {
    var open: () -> Unit = {}
}

@Composable
fun TeLoLeoApp(
    listenerEnabled: Boolean,
    settings: ReaderSettings,
    apps: List<LaunchableApp>,
    appsLoaded: Boolean,
    offer: BillingOffer,
    manufacturer: String,
    onPackages: (List<String>) -> Unit,
    onOpenListenerSettings: () -> Unit,
    silencedUntil: Long,
    onDeactivateReading: () -> Unit,
    onActivateReading: () -> Unit,
    onResumeReading: () -> Unit,
    onRedact: (Boolean) -> Unit,
    onQuietHours: (Boolean, Int, Int) -> Unit,
    onHeadphonesOnly: (Boolean) -> Unit,
    onShake: (Boolean) -> Unit,
    onSpeechRate: (Float) -> Unit,
    onSpeechVolume: (Float) -> Unit,
    onWordFilters: (List<String>) -> Unit,
    onTemplate: (packageName: String?, template: String) -> Unit,
    onVoiceLocale: (String) -> Unit,
    onBuyPro: () -> Unit,
    onRefreshPro: () -> Unit,
    isListenerEnabled: () -> Boolean,
    onCompleteOnboarding: () -> Unit,
) {
    var pageName by rememberSaveable { mutableStateOf(AppPage.Home.name) }
    var originName by rememberSaveable { mutableStateOf(AppPage.Home.name) }
    val page = runCatching { AppPage.valueOf(pageName) }.getOrDefault(AppPage.Home)
    var showDisclosure by rememberSaveable { mutableStateOf(false) }
    var showLimit by rememberSaveable { mutableStateOf(false) }
    var showBattery by rememberSaveable { mutableStateOf(false) }
    var templateFor by remember { mutableStateOf<TemplateEdit?>(null) }
    var onboardingPro by rememberSaveable { mutableStateOf(false) }
    val alreadyEnabled = remember { DirectListenerOpener() }
    val sheetOpen = showDisclosure || showLimit || showBattery || templateFor != null

    fun closeOverlay() {
        val origin = runCatching { AppPage.valueOf(originName) }.getOrDefault(AppPage.Home)
        pageName = when (origin) {
            AppPage.Home, AppPage.Apps, AppPage.Lectura -> origin.name
            AppPage.Privacy, AppPage.Pro, AppPage.Voice -> AppPage.Home.name
        }
    }

    fun openOverlay(target: AppPage) {
        if (page == AppPage.Home || page == AppPage.Apps || page == AppPage.Lectura) {
            originName = page.name
        }
        pageName = target.name
    }

    BackHandler(enabled = !sheetOpen && page != AppPage.Home) {
        when (page) {
            AppPage.Privacy, AppPage.Pro, AppPage.Voice -> closeOverlay()
            else -> pageName = AppPage.Home.name
        }
    }
    LaunchedEffect(page) {
        if (page == AppPage.Pro) onRefreshPro()
    }
    LaunchedEffect(onboardingPro) {
        if (onboardingPro) onRefreshPro()
    }

    if (!settings.onboardingComplete) {
        Box(Modifier.fillMaxSize()) {
            OnboardingScreen(
                listenerEnabled = listenerEnabled,
                isListenerEnabled = isListenerEnabled,
                settings = settings,
                apps = apps,
                appsLoaded = appsLoaded,
                onPackages = onPackages,
                onOpenListenerSettings = onOpenListenerSettings,
                onSeePro = { onboardingPro = true },
                onFinished = onCompleteOnboarding,
            )
            if (onboardingPro) {
                BackHandler { onboardingPro = false }
                ProScreen(
                    proUnlocked = settings.proUnlocked,
                    offer = offer,
                    onBuy = onBuyPro,
                    onRestore = onRefreshPro,
                    onBack = { onboardingPro = false },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        return
    }

    when (page) {
        AppPage.Privacy -> PrivacyScreen(onBack = { closeOverlay() })
        AppPage.Pro -> ProScreen(
            proUnlocked = settings.proUnlocked,
            offer = offer,
            onBuy = onBuyPro,
            onRestore = onRefreshPro,
            onBack = { closeOverlay() },
        )
        AppPage.Voice -> VoiceLanguageScreen(
            savedTag = settings.voiceLocale,
            onSelect = onVoiceLocale,
            onBack = { closeOverlay() },
        )
        AppPage.Home, AppPage.Apps, AppPage.Lectura -> Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
            bottomBar = {
                BoardTabs(
                    page = page,
                    onSelect = { selected -> pageName = selected.name },
                )
            },
        ) { padding ->
            val body = Modifier.fillMaxSize().padding(padding)
            when (page) {
                AppPage.Home -> HomeScreen(
                    listenerEnabled = listenerEnabled,
                    settings = settings,
                    manufacturer = manufacturer,
                    onAskListenerAccess = { showDisclosure = true },
                    onManageListener = { alreadyEnabled.open() },
                    silencedUntil = silencedUntil,
                    onDeactivateReading = onDeactivateReading,
                    onActivateReading = onActivateReading,
                    onResumeReading = onResumeReading,
                    onOpenPrivacy = { openOverlay(AppPage.Privacy) },
                    onOpenPro = { openOverlay(AppPage.Pro) },
                    onOpenApps = { pageName = AppPage.Apps.name },
                    onOpenBattery = { showBattery = true },
                    modifier = body,
                )
                AppPage.Apps -> AppsScreen(
                    settings = settings,
                    apps = apps,
                    appsLoaded = appsLoaded,
                    onToggleApp = { pkg ->
                        when (val change = AllowedApps.changeSelection(settings.allowedPackages, pkg, settings.proUnlocked)) {
                            is SelectionChange.Updated -> onPackages(change.packages)
                            SelectionChange.NeedsPro -> showLimit = true
                        }
                    },
                    onTemplate = { app ->
                        templateFor = TemplateEdit(
                            packageName = app.packageName,
                            title = app.label,
                            initial = settings.appTemplates[app.packageName].orEmpty(),
                        )
                    },
                    modifier = body,
                )
                AppPage.Lectura -> ReadingOptions(
                    settings = settings,
                    onQuietHours = onQuietHours,
                    onHeadphonesOnly = onHeadphonesOnly,
                    onShake = onShake,
                    onSpeechRate = onSpeechRate,
                    onSpeechVolume = onSpeechVolume,
                    onWordFilters = onWordFilters,
                    onEditTemplate = {
                        templateFor = TemplateEdit(
                            packageName = null,
                            title = "",
                            initial = settings.phraseTemplate,
                        )
                    },
                    onOpenVoice = { openOverlay(AppPage.Voice) },
                    onRedact = onRedact,
                    onOpenPro = { openOverlay(AppPage.Pro) },
                    modifier = body,
                )
                AppPage.Privacy, AppPage.Pro, AppPage.Voice -> Unit
            }
        }
    }

    if (showDisclosure) {
        DisclosureSheet(
            onConfirm = {
                showDisclosure = false
                onOpenListenerSettings()
            },
            onDismiss = { showDisclosure = false },
        )
    }
    if (showLimit) {
        LimitSheet(
            onSeePro = {
                showLimit = false
                openOverlay(AppPage.Pro)
            },
            onDismiss = { showLimit = false },
        )
    }
    if (showBattery) {
        BatterySheet(
            guide = BatteryGuides.forManufacturer(manufacturer),
            onDismiss = { showBattery = false },
        )
    }
    templateFor?.let { edit ->
        TemplateSheet(
            title = edit.title,
            initial = edit.initial,
            redactOneTimeCodes = settings.redactOneTimeCodes,
            onDismiss = { templateFor = null },
            onSave = { value ->
                onTemplate(edit.packageName, value)
                templateFor = null
            },
        )
    }

    SideEffect {
        alreadyEnabled.open = { onOpenListenerSettings() }
    }
}

@Composable
private fun BoardTabs(
    page: AppPage,
    onSelect: (AppPage) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding(),
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            TabButton(AppPage.Home, R.string.tab_home, page, onSelect)
            TabButton(AppPage.Apps, R.string.tab_apps, page, onSelect)
            TabButton(AppPage.Lectura, R.string.tab_reading, page, onSelect)
        }
    }
}

@Composable
private fun RowScope.TabButton(
    target: AppPage,
    label: Int,
    page: AppPage,
    onSelect: (AppPage) -> Unit,
) {
    val isSelected = page == target
    val background = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    val color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    Box(
        modifier = Modifier
            .weight(1f)
            .height(48.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(background)
            .semantics { selected = isSelected }
            .clickable(role = Role.Tab) { onSelect(target) },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(label),
            color = color,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

private data class TemplateEdit(
    val packageName: String?,
    val title: String,
    val initial: String,
)
