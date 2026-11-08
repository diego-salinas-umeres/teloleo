package app.teloleo.listener

import android.content.ComponentName
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.os.SystemClock
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import app.teloleo.data.ReaderSettings
import app.teloleo.data.SettingsRepository
import app.teloleo.tts.SystemSpeaker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class NotificationSpeakerService : NotificationListenerService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val settings = MutableStateFlow(ReaderSettings())
    private val debouncer = SpeakDebouncer()
    private val shakeDetector = ShakeDetector()
    private lateinit var speaker: SystemSpeaker
    private var sensorManager: SensorManager? = null
    private var shakeListening = false

    private val shakeListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (!settings.value.shakeEnabled()) return
            val x = event.values.getOrNull(0) ?: return
            val y = event.values.getOrNull(1) ?: return
            val z = event.values.getOrNull(2) ?: return
            if (!shakeDetector.sample(x, y, z, SystemClock.elapsedRealtime())) return
            SpeakerCommands.requestSilence(
                SpeakerCommands.SHAKE_SILENCE_MILLIS,
                SystemClock.elapsedRealtime(),
            )
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
    }

    override fun onCreate() {
        super.onCreate()
        speaker = SystemSpeaker(this)
        speaker.warmUp()
        val repository = SettingsRepository(this)
        scope.launch {
            repository.settings.collect { next ->
                settings.value = next
                syncShake(next.shakeEnabled())
                if (next.readingPaused && ::speaker.isInitialized) speaker.stop()
            }
        }
        scope.launch {
            SpeakerCommands.silencedUntil.collect { until ->
                if (SystemClock.elapsedRealtime() < until && ::speaker.isInitialized) {
                    speaker.stop()
                }
            }
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        speaker.warmUp()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return
        try {
            val current = settings.value
            val notice = sbn.toIncomingNotice(
                ownPackageName = packageName,
                appLabel = appLabelOf(sbn.packageName),
            )
            val policy = ReadPolicy(
                redactOneTimeCodes = current.redactOneTimeCodes,
                quiet = QuietHours(
                    enabled = current.quietHoursEnabled,
                    startMinutes = current.quietStartMinutes,
                    endMinutes = current.quietEndMinutes,
                ),
                nowMinutesOfDay = minutesNow(),
                requireHeadphones = current.headphonesOnly,
                headphonesConnected = headphonesConnected(),
                blockedWords = current.wordFilters,
                template = if (current.proUnlocked) current.templateFor(notice.packageName) else null,
                silenced = current.readingPaused ||
                    SystemClock.elapsedRealtime() < SpeakerCommands.silencedUntil.value,
            )
            when (val result = NotificationGate.evaluate(
                notice = notice,
                allowedPackages = current.allowedPackages,
                proUnlocked = current.proUnlocked,
                policy = policy,
            )) {
                is GateResult.Skip -> Unit
                is GateResult.Speak -> {
                    val now = SystemClock.elapsedRealtime()
                    if (debouncer.shouldSpeak(result.signature, now)) {
                        speaker.speak(
                            result.phrase,
                            current.spokenRate(),
                            current.spokenVolume(),
                            current.voiceLocale,
                        )
                    }
                }
            }
        } catch (_: RuntimeException) {
            // Un aviso mal formado no debe tumbar el servicio que el sistema mantiene enlazado.
        }
    }

    override fun onListenerDisconnected() {
        requestRebind(ComponentName(this, NotificationSpeakerService::class.java))
    }

    override fun onDestroy() {
        syncShake(false)
        scope.cancel()
        if (::speaker.isInitialized) speaker.shutdown()
        super.onDestroy()
    }

    private fun syncShake(enabled: Boolean) {
        val manager = sensorManager ?: getSystemService(SensorManager::class.java)?.also { sensorManager = it }
        if (manager == null) return
        if (!enabled) {
            if (shakeListening) {
                manager.unregisterListener(shakeListener)
                shakeListening = false
            }
            return
        }
        if (shakeListening) return
        val sensor = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) ?: return
        manager.registerListener(shakeListener, sensor, SensorManager.SENSOR_DELAY_UI)
        shakeListening = true
    }

    private fun headphonesConnected(): Boolean {
        return try {
            val manager = getSystemService(AudioManager::class.java) ?: return false
            val types = manager.getDevices(AudioManager.GET_DEVICES_OUTPUTS).map { it.type }
            AudioRoute.connected(types)
        } catch (_: RuntimeException) {
            false
        }
    }

    private fun minutesNow(): Int {
        val calendar = Calendar.getInstance()
        return calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
    }

    private fun appLabelOf(packageName: String): String {
        return try {
            val info = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(info).toString()
        } catch (_: PackageManager.NameNotFoundException) {
            packageName.substringAfterLast('.')
        }
    }
}
