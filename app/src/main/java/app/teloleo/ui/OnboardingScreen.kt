package app.teloleo.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import app.teloleo.R
import app.teloleo.apps.AppIcons
import app.teloleo.apps.LaunchableApp
import app.teloleo.data.ReaderSettings
import app.teloleo.listener.SpokenPhrase
import app.teloleo.scope.AllowedApps
import app.teloleo.scope.SelectionChange
import app.teloleo.tts.SystemSpeaker

@Composable
fun OnboardingScreen(
    listenerEnabled: Boolean,
    isListenerEnabled: () -> Boolean,
    settings: ReaderSettings,
    apps: List<LaunchableApp>,
    appsLoaded: Boolean,
    onPackages: (List<String>) -> Unit,
    onOpenListenerSettings: () -> Unit,
    onSeePro: () -> Unit,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var stepName by rememberSaveable { mutableStateOf(OnboardingStep.Welcome.name) }
    var awaitingSettings by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    var showLimit by rememberSaveable { mutableStateOf(false) }
    val step = runCatching { OnboardingStep.valueOf(stepName) }.getOrDefault(OnboardingStep.Welcome)
    fun go(next: OnboardingStep) {
        stepName = next.name
    }

    ReturnFromSettingsEffect(
        awaiting = awaitingSettings,
        isListenerEnabled = isListenerEnabled,
        onReturned = { enabled ->
            awaitingSettings = false
            go(OnboardingNav.afterSystemSettings(enabled))
        },
    )
    BackHandler(enabled = !showLimit && OnboardingNav.back(step, listenerEnabled) != null) {
        OnboardingNav.back(step, listenerEnabled)?.let(::go)
    }

    val exampleSpoken = SpokenPhrase.build(
        stringResource(R.string.onboarding_example_app),
        stringResource(R.string.onboarding_example_title),
        stringResource(R.string.onboarding_example_text),
        null,
        redactOneTimeCodes = true,
    ).orEmpty()
    val context = LocalContext.current
    val speaker = remember { SystemSpeaker(context) }
    DisposableEffect(speaker) {
        speaker.warmUp()
        onDispose { speaker.shutdown() }
    }
    DisposableEffect(step) {
        onDispose { speaker.stop() }
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, speaker) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) speaker.stop()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    val hear = { phrase: String ->
        speaker.stop()
        speaker.speak(
            phrase,
            settings.spokenRate(),
            settings.spokenVolume(),
            settings.voiceLocale,
        )
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        bottomBar = {
            OnboardingBar(
                step = step,
                selectedCount = settings.allowedPackages.size,
                proUnlocked = settings.proUnlocked,
                appsLoaded = appsLoaded,
                appsEmpty = apps.isEmpty(),
                onPrimary = {
                    when (step) {
                        OnboardingStep.Welcome -> go(OnboardingStep.Flow)
                        OnboardingStep.Flow -> go(OnboardingStep.Permission)
                        OnboardingStep.Permission -> {
                            awaitingSettings = true
                            onOpenListenerSettings()
                        }
                        OnboardingStep.Granted -> go(OnboardingStep.Apps)
                        OnboardingStep.Denied -> go(OnboardingStep.Permission)
                        OnboardingStep.Apps -> go(OnboardingStep.Done)
                        OnboardingStep.Done -> onFinished()
                    }
                },
                onSecondary = {
                    when (step) {
                        OnboardingStep.Permission -> go(OnboardingStep.Apps)
                        OnboardingStep.Denied -> go(OnboardingStep.Apps)
                        else -> Unit
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OnboardingNav.back(step, listenerEnabled)?.let {
                TextButton(
                    onClick = { go(it) },
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Text(stringResource(R.string.back), fontWeight = FontWeight.Bold)
                }
            }
            OnboardingNav.progress(step)?.let { number ->
                Text(
                    text = stringResource(R.string.onboarding_progress, number),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                )
                ProgressTrack(number)
            }
            when (step) {
                OnboardingStep.Welcome -> WelcomeStep(exampleSpoken) { hear(exampleSpoken) }
                OnboardingStep.Flow -> FlowStep()
                OnboardingStep.Permission -> PermissionStep()
                OnboardingStep.Granted -> GrantedStep()
                OnboardingStep.Denied -> DeniedStep()
                OnboardingStep.Apps -> AppsStep(
                    listenerEnabled = listenerEnabled,
                    settings = settings,
                    apps = apps,
                    appsLoaded = appsLoaded,
                    query = query,
                    onQuery = { query = it },
                    onToggle = { pkg ->
                        when (val change = AllowedApps.changeSelection(settings.allowedPackages, pkg, settings.proUnlocked)) {
                            is SelectionChange.Updated -> onPackages(change.packages)
                            SelectionChange.NeedsPro -> showLimit = true
                        }
                    },
                    onAskPermission = { go(OnboardingStep.Permission) },
                )
                OnboardingStep.Done -> DoneStep(settings, apps, listenerEnabled, onHear = hear)
            }
        }
    }
    if (showLimit) {
        LimitSheet(
            onSeePro = {
                showLimit = false
                onSeePro()
            },
            onDismiss = { showLimit = false },
        )
    }
}

@Composable
private fun ReturnFromSettingsEffect(
    awaiting: Boolean,
    isListenerEnabled: () -> Boolean,
    onReturned: (Boolean) -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, awaiting) {
        if (!awaiting) return@DisposableEffect onDispose {}
        val lifecycle = lifecycleOwner.lifecycle
        var leftApp = !lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> leftApp = true
                Lifecycle.Event.ON_RESUME -> if (leftApp) {
                    leftApp = false
                    onReturned(isListenerEnabled())
                }
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
}

@Composable
private fun ProgressTrack(step: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(4.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.outline),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(step / OnboardingNav.STEP_COUNT.toFloat())
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.primary),
        )
    }
}

@Composable
private fun WelcomeStep(spoken: String, onHear: () -> Unit) {
    Text(stringResource(R.string.onboarding_welcome_title), style = MaterialTheme.typography.headlineLarge)
    Text(
        text = stringResource(R.string.onboarding_welcome_body),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LetterAvatar(stringResource(R.string.onboarding_example_app).take(1))
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.onboarding_example_title), fontWeight = FontWeight.SemiBold)
                Text(
                    text = stringResource(R.string.onboarding_example_text),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
    PhraseCard(spoken, onHear)
    Text(
        text = stringResource(R.string.onboarding_on_device),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

private enum class FlowFace { Home, Apps, Reading }

@Composable
private fun FlowStep() {
    var faceName by rememberSaveable { mutableStateOf(FlowFace.Home.name) }
    val face = runCatching { FlowFace.valueOf(faceName) }.getOrDefault(FlowFace.Home)
    val caption = when (face) {
        FlowFace.Home -> R.string.onboarding_preview_home
        FlowFace.Apps -> R.string.onboarding_preview_apps
        FlowFace.Reading -> R.string.onboarding_preview_reading
    }
    Text(stringResource(R.string.onboarding_flow_title), style = MaterialTheme.typography.headlineLarge)
    NumberedCard(
        listOf(
            R.string.onboarding_flow_1_title to R.string.onboarding_flow_1_body,
            R.string.onboarding_flow_2_title to R.string.onboarding_flow_2_body,
            R.string.onboarding_flow_3_title to R.string.onboarding_flow_3_body,
        ),
    )
    FlowPeek(face)
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        MapChip(R.string.tab_home, selected = face == FlowFace.Home, modifier = Modifier.weight(1f)) {
            faceName = FlowFace.Home.name
        }
        MapChip(R.string.tab_apps, selected = face == FlowFace.Apps, modifier = Modifier.weight(1f)) {
            faceName = FlowFace.Apps.name
        }
        MapChip(R.string.tab_reading, selected = face == FlowFace.Reading, modifier = Modifier.weight(1f)) {
            faceName = FlowFace.Reading.name
        }
    }
    Text(
        text = stringResource(caption),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun FlowPeek(face: FlowFace) {
    val showApps = face == FlowFace.Apps
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Box(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 4.dp)) {
            FlowAppsFace(
                Modifier
                    .alpha(if (showApps) 1f else 0f)
                    .then(if (showApps) Modifier else Modifier.clearAndSetSemantics {}),
            )
            if (!showApps) {
                Box(Modifier.matchParentSize(), contentAlignment = Alignment.TopStart) {
                    when (face) {
                        FlowFace.Home -> FlowHomeFace()
                        FlowFace.Reading -> FlowReadingFace()
                        FlowFace.Apps -> Unit
                    }
                }
            }
        }
    }
}

@Composable
private fun FlowHomeFace() {
    Column(Modifier.padding(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = stringResource(R.string.status_reading),
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = stringResource(R.string.home_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(R.string.listener_on),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun FlowAppsFace(modifier: Modifier = Modifier) {
    Column(modifier) {
        FlowAppRow(stringResource(R.string.onboarding_example_app), selected = true)
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        FlowAppRow(stringResource(R.string.onboarding_preview_telegram), selected = false)
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        FlowAppRow(stringResource(R.string.onboarding_preview_gmail), selected = false)
    }
}

@Composable
private fun FlowAppRow(name: String, selected: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 44.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        LetterAvatar(name.take(1))
        Text(
            text = name,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Switch(checked = selected, onCheckedChange = null)
    }
}

@Composable
private fun FlowReadingFace() {
    Column {
        FlowProRow(R.string.quiet_hours)
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        FlowProRow(R.string.onboarding_preview_headphones)
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        FlowProRow(R.string.word_filters)
    }
}

@Composable
private fun FlowProRow(title: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 44.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(title),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(R.string.onboarding_preview_pro),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun PermissionStep() {
    Text(stringResource(R.string.onboarding_permission_title), style = MaterialTheme.typography.headlineLarge)
    Text(
        text = stringResource(R.string.disclosure_title),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    NumberedCard(
        listOf(
            R.string.onboarding_fact_read_title to R.string.onboarding_fact_read_body,
            R.string.onboarding_fact_where_title to R.string.onboarding_fact_where_body,
            R.string.onboarding_fact_off_title to R.string.onboarding_fact_off_body,
            R.string.onboarding_fact_codes_title to R.string.onboarding_fact_codes_body,
        ),
    )
}

@Composable
private fun GrantedStep() {
    Text(stringResource(R.string.onboarding_granted_title), style = MaterialTheme.typography.headlineLarge)
    Text(
        text = stringResource(R.string.onboarding_granted_body),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun DeniedStep() {
    Text(stringResource(R.string.onboarding_denied_title), style = MaterialTheme.typography.headlineLarge)
    Text(
        text = stringResource(R.string.onboarding_denied_body),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun AppsStep(
    listenerEnabled: Boolean,
    settings: ReaderSettings,
    apps: List<LaunchableApp>,
    appsLoaded: Boolean,
    query: String,
    onQuery: (String) -> Unit,
    onToggle: (String) -> Unit,
    onAskPermission: () -> Unit,
) {
    if (!listenerEnabled) {
        Surface(
            onClick = onAskPermission,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
        ) {
            Text(
                text = stringResource(R.string.onboarding_need_permission),
                modifier = Modifier.padding(14.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold,
            )
        }
    }
    Text(stringResource(R.string.onboarding_apps_title), style = MaterialTheme.typography.headlineLarge)
    Text(
        text = stringResource(R.string.onboarding_apps_body),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    OutlinedTextField(
        value = query,
        onValueChange = onQuery,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        placeholder = { Text(stringResource(R.string.search_apps)) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                TextButton(onClick = { onQuery("") }) {
                    Text(stringResource(R.string.search_clear))
                }
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            focusedBorderColor = MaterialTheme.colorScheme.outline,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        ),
    )
    if (!appsLoaded) {
        Text(stringResource(R.string.loading_apps))
        return
    }
    val needle = query.trim()
    val known = apps.associateBy { it.packageName }
    val selected = settings.allowedPackages.map { pkg -> known[pkg] ?: LaunchableApp(pkg, pkg) }
        .filter { it.matches(needle) }
    val others = apps.filter { it.packageName !in settings.allowedPackages && it.matches(needle) }
    if (needle.isNotEmpty() && selected.isEmpty() && others.isEmpty()) {
        Text(stringResource(R.string.no_search_hits), color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }
    if (selected.isNotEmpty()) {
        SectionLabel(R.string.section_speaking)
        selected.forEach { app ->
            OnboardingAppRow(app, selected = true, onToggle = { onToggle(app.packageName) })
        }
    }
    if (others.isNotEmpty()) {
        if (selected.isNotEmpty()) SectionLabel(R.string.section_others)
        others.forEach { app ->
            OnboardingAppRow(app, selected = false, onToggle = { onToggle(app.packageName) })
        }
    }
}

@Composable
private fun DoneStep(
    settings: ReaderSettings,
    apps: List<LaunchableApp>,
    listenerEnabled: Boolean,
    onHear: (String) -> Unit,
) {
    val known = apps.associateBy { it.packageName }
    val names = OnboardingNav.joinNames(
        settings.allowedPackages.map { pkg -> known[pkg]?.label ?: pkg },
    )
    val first = settings.allowedPackages.firstOrNull()
    val sample = first?.let { OnboardingNav.sampleNotice(it) }
    val label = first?.let { known[it]?.label ?: it }.orEmpty()
    val spoken = sample?.let {
        SpokenPhrase.build(label, it.title, it.text, null, settings.redactOneTimeCodes)
    }
    val plain = sample?.let {
        SpokenPhrase.build(label, it.title, it.text, null, redactOneTimeCodes = false)
    }
    Text(stringResource(R.string.onboarding_done_title), style = MaterialTheme.typography.headlineLarge)
    Text(
        text = if (names.isEmpty()) {
            stringResource(R.string.onboarding_done_none)
        } else {
            stringResource(R.string.onboarding_done_apps, names)
        },
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    if (!spoken.isNullOrBlank()) PhraseCard(spoken) { onHear(spoken) }
    Text(
        text = stringResource(
            if (spoken != null && spoken != plain) R.string.onboarding_done_code_hidden else R.string.onboarding_done_codes,
        ),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    if (!listenerEnabled) {
        Text(
            text = stringResource(R.string.onboarding_done_missing_permission),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    Text(
        text = stringResource(R.string.onboarding_done_battery),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun OnboardingBar(
    step: OnboardingStep,
    selectedCount: Int,
    proUnlocked: Boolean,
    appsLoaded: Boolean,
    appsEmpty: Boolean,
    onPrimary: () -> Unit,
    onSecondary: () -> Unit,
) {
    val canLeaveApps = selectedCount > 0 || (appsLoaded && appsEmpty)
    val primaryLabel = when (step) {
        OnboardingStep.Welcome -> R.string.onboarding_start
        OnboardingStep.Flow -> R.string.onboarding_next
        OnboardingStep.Permission -> R.string.disclosure_confirm
        OnboardingStep.Granted -> R.string.choose_apps
        OnboardingStep.Denied -> R.string.enable_listener
        OnboardingStep.Apps -> if (canLeaveApps) R.string.sheet_done else R.string.onboarding_need_one
        OnboardingStep.Done -> R.string.onboarding_enter
    }
    val secondary = when (step) {
        OnboardingStep.Permission -> R.string.not_now
        OnboardingStep.Denied -> R.string.onboarding_mark_anyway
        else -> null
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(top = 8.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (step == OnboardingStep.Permission) {
            Text(
                text = stringResource(R.string.disclosure_next),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (step == OnboardingStep.Apps) {
            Text(
                text = stringResource(
                    if (proUnlocked) R.string.pro_count else R.string.free_count,
                    selectedCount,
                ),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Button(
            onClick = onPrimary,
            enabled = step != OnboardingStep.Apps || canLeaveApps,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text(
                text = stringResource(primaryLabel),
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
        }
        secondary?.let { label ->
            TextButton(
                onClick = onSecondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
            ) {
                Text(stringResource(label), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun PhraseCard(spoken: String, onHear: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = stringResource(R.string.onboarding_example_kicker),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
            )
            Text(text = spoken, style = MaterialTheme.typography.headlineSmall)
            FilledTonalButton(
                onClick = onHear,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text(
                    text = stringResource(R.string.onboarding_hear),
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun NumberedCard(rows: List<Pair<Int, Int>>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(Modifier.padding(8.dp)) {
            rows.forEachIndexed { index, (title, body) ->
                Row(
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "${index + 1}",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(title), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = stringResource(body),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MapChip(
    label: Int,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val background = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerLow
    val color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier = modifier
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(background)
            .border(
                width = 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(14.dp),
            )
            .semantics { this.selected = selected }
            .clickable(role = Role.Tab, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(stringResource(label), color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SectionLabel(label: Int) {
    Text(
        text = stringResource(label),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun OnboardingAppRow(
    app: LaunchableApp,
    selected: Boolean,
    onToggle: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(role = Role.Switch, onClick = onToggle),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AppMark(app)
        Text(
            text = app.label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Switch(checked = selected, onCheckedChange = null)
    }
}

@Composable
private fun AppMark(app: LaunchableApp) {
    val bitmap = remember(app.packageName) { AppIcons.peek(app.packageName) }
    val image = remember(bitmap) { bitmap?.asImageBitmap() }
    if (image == null) {
        LetterAvatar(app.label.take(1).uppercase())
    } else {
        Image(
            bitmap = image,
            contentDescription = null,
            modifier = Modifier
                .size(AppIcons.SIZE_DP.dp)
                .clip(RoundedCornerShape(14.dp)),
        )
    }
}

@Composable
private fun LetterAvatar(letter: String) {
    Box(
        modifier = Modifier
            .size(AppIcons.SIZE_DP.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = letter,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.Bold,
        )
    }
}

private fun LaunchableApp.matches(needle: String): Boolean {
    if (needle.isEmpty()) return true
    return label.contains(needle, ignoreCase = true) || packageName.contains(needle, ignoreCase = true)
}
