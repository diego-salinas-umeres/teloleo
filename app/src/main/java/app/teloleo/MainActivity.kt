package app.teloleo

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import app.teloleo.BuildConfig
import app.teloleo.apps.AppIcons
import app.teloleo.apps.LaunchableApp
import app.teloleo.apps.LauncherApps
import app.teloleo.billing.BillingOffer
import app.teloleo.billing.ProBilling
import app.teloleo.data.ReaderSettings
import app.teloleo.data.SettingsRepository
import app.teloleo.listener.SpeakerCommands
import app.teloleo.ui.TeLoLeoApp
import app.teloleo.ui.TeLoLeoTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private val repository by lazy { SettingsRepository(this) }
    private val offerState = mutableStateOf(BillingOffer())
    private lateinit var billing: ProBilling

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (BuildConfig.PRO_PREVIEW) {
            lifecycleScope.launch { repository.setProUnlocked(true) }
        }
        billing = ProBilling(
            activity = this,
            onEntitlement = { unlocked ->
                lifecycleScope.launch {
                    repository.setProUnlocked(unlocked || BuildConfig.PRO_PREVIEW)
                }
            },
            onOffer = { offer -> offerState.value = offer },
        )
        billing.start()
        setContent {
            val settings by repository.settings.collectAsStateWithLifecycle(ReaderSettings())
            val silencedUntil by SpeakerCommands.silencedUntil.collectAsStateWithLifecycle()
            val lifecycleOwner = LocalLifecycleOwner.current
            var listenerEnabled by remember { mutableStateOf(isListenerEnabled()) }
            var resumeTick by remember { mutableIntStateOf(0) }
            var apps by remember { mutableStateOf<List<LaunchableApp>>(emptyList()) }
            var appsLoaded by remember { mutableStateOf(false) }
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        listenerEnabled = isListenerEnabled()
                        resumeTick += 1
                        billing.refresh()
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
            }
            LaunchedEffect(resumeTick) {
                val iconPx = (AppIcons.SIZE_DP * resources.displayMetrics.density).toInt()
                val loaded = withContext(Dispatchers.IO) {
                    val list = LauncherApps.load(packageManager, packageName)
                    AppIcons.warm(packageManager, list.map { it.packageName }, iconPx)
                    list
                }
                apps = loaded
                appsLoaded = true
            }
            TeLoLeoTheme {
                TeLoLeoApp(
                    listenerEnabled = listenerEnabled,
                    settings = settings,
                    apps = apps,
                    appsLoaded = appsLoaded,
                    offer = offerState.value,
                    manufacturer = Build.MANUFACTURER.orEmpty(),
                    onPackages = { packages ->
                        lifecycleScope.launch { repository.setAllowedPackages(packages) }
                    },
                    onOpenListenerSettings = {
                        startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                    },
                    silencedUntil = silencedUntil,
                    onDeactivateReading = {
                        lifecycleScope.launch { repository.setReadingPaused(true) }
                    },
                    onActivateReading = {
                        SpeakerCommands.resume()
                        lifecycleScope.launch { repository.setReadingPaused(false) }
                    },
                    onResumeReading = { SpeakerCommands.resume() },
                    onRedact = { redact ->
                        lifecycleScope.launch { repository.setRedactOneTimeCodes(redact) }
                    },
                    onQuietHours = { enabled, start, end ->
                        lifecycleScope.launch { repository.setQuietHours(enabled, start, end) }
                    },
                    onHeadphonesOnly = { enabled ->
                        lifecycleScope.launch { repository.setHeadphonesOnly(enabled) }
                    },
                    onShake = { enabled ->
                        lifecycleScope.launch { repository.setShakeToSilence(enabled) }
                    },
                    onSpeechRate = { rate ->
                        lifecycleScope.launch { repository.setSpeechRate(rate) }
                    },
                    onSpeechVolume = { volume ->
                        lifecycleScope.launch { repository.setSpeechVolume(volume) }
                    },
                    onWordFilters = { words ->
                        lifecycleScope.launch { repository.setWordFilters(words) }
                    },
                    onTemplate = { packageName, template ->
                        lifecycleScope.launch {
                            if (packageName == null) repository.setPhraseTemplate(template)
                            else repository.setAppTemplate(packageName, template)
                        }
                    },
                    onVoiceLocale = { tag ->
                        lifecycleScope.launch { repository.setVoiceLocale(tag) }
                    },
                    onBuyPro = { billing.purchase() },
                    onRefreshPro = { billing.refresh() },
                    isListenerEnabled = ::isListenerEnabled,
                    onCompleteOnboarding = {
                        lifecycleScope.launch { repository.setOnboardingComplete(true) }
                    },
                )
            }
        }
    }

    override fun onDestroy() {
        if (::billing.isInitialized) billing.destroy()
        super.onDestroy()
    }

    private fun isListenerEnabled(): Boolean =
        NotificationManagerCompat.getEnabledListenerPackages(this).contains(packageName)
}
