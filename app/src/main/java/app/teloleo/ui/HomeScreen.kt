package app.teloleo.ui

import android.os.SystemClock
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.teloleo.R
import app.teloleo.data.ReaderSettings
import app.teloleo.device.BatteryGuides
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    listenerEnabled: Boolean,
    settings: ReaderSettings,
    manufacturer: String,
    onAskListenerAccess: () -> Unit,
    onManageListener: () -> Unit,
    silencedUntil: Long,
    onDeactivateReading: () -> Unit,
    onActivateReading: () -> Unit,
    onResumeReading: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onOpenPro: () -> Unit,
    onOpenApps: () -> Unit,
    onOpenBattery: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val guide = BatteryGuides.forManufacturer(manufacturer)
    var nowMillis by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }
    LaunchedEffect(silencedUntil) {
        while (true) {
            val now = SystemClock.elapsedRealtime()
            nowMillis = now
            val remaining = silencedUntil - now
            if (remaining <= 0L) break
            delay(minOf(1_000L, remaining))
        }
    }
    val paused = listenerEnabled && settings.readingPaused
    val countingDown = listenerEnabled && !paused && silencedUntil > nowMillis
    val headline = when {
        paused -> R.string.status_paused
        listenerEnabled -> R.string.status_reading
        else -> R.string.status_silent
    }
    val status = when {
        paused -> R.string.listener_paused
        listenerEnabled -> R.string.listener_on
        else -> R.string.listener_off
    }
    val detail = when {
        paused -> stringResource(R.string.paused_detail)
        countingDown -> stringResource(R.string.silence_remaining, remainingClock(silencedUntil - nowMillis))
        listenerEnabled -> stringResource(R.string.turn_off_hint)
        else -> stringResource(R.string.listener_off_detail)
    }
    val readingAction = when {
        paused -> R.string.activate_reading to onActivateReading
        countingDown -> R.string.resume to onResumeReading
        else -> R.string.deactivate_reading to onDeactivateReading
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineSmall,
            )
            Surface(
                onClick = onOpenPro,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.inverseSurface,
            ) {
                Text(
                    text = stringResource(if (settings.proUnlocked) R.string.plan_pro else R.string.plan_free),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    color = MaterialTheme.colorScheme.inverseOnSurface,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(headline),
                style = MaterialTheme.typography.headlineLarge,
            )
            Text(
                text = stringResource(R.string.home_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(status),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = detail,
                    modifier = Modifier.padding(top = 4.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (listenerEnabled) {
                    Row(
                        modifier = Modifier.padding(top = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Button(
                            onClick = onManageListener,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp),
                            shape = RoundedCornerShape(14.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.manage_listener),
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        Button(
                            onClick = readingAction.second,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = if (paused || countingDown) {
                                ButtonDefaults.buttonColors()
                            } else {
                                ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                )
                            },
                        ) {
                            Text(
                                text = stringResource(readingAction.first),
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = onAskListenerAccess,
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .fillMaxWidth()
                            .heightIn(min = 48.dp),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.enable_listener),
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
        Button(
            onClick = onOpenApps,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
            shape = RoundedCornerShape(14.dp),
        ) {
            Text(stringResource(R.string.choose_apps), fontWeight = FontWeight.SemiBold)
        }
        Surface(
            onClick = onOpenBattery,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.battery_title),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = stringResource(guide.brandRes()),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = onOpenPrivacy) {
                Text(stringResource(R.string.open_privacy), fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = onOpenPro) {
                Text(stringResource(R.string.open_pro), fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun remainingClock(millis: Long): String {
    val totalSeconds = ((millis + 999L) / 1_000L).coerceAtLeast(0L)
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}
