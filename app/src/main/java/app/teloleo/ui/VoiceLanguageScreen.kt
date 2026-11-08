package app.teloleo.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import app.teloleo.R
import app.teloleo.data.canonicalVoiceTag
import app.teloleo.data.offlineVoiceLanguageTags
import app.teloleo.data.voiceLanguageLabel
import app.teloleo.data.voiceLocaleCandidates
import java.util.Locale

@Composable
fun VoiceLanguageScreen(
    savedTag: String,
    onSelect: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val probe = remember { VoiceProbe() }
    var reload by remember { mutableIntStateOf(0) }
    val chosen = canonicalVoiceTag(savedTag)
    val displayLocale = Locale.getDefault()
    val languages = probe.tags
        .map { tag -> tag to voiceLanguageLabel(tag, displayLocale) }
        .sortedBy { (_, label) -> label.lowercase(displayLocale) }

    DisposableEffect(lifecycleOwner) {
        // El alta del observer entrega el ON_RESUME actual. Ese no es un regreso.
        var caughtUp = false
        val observer = LifecycleEventObserver { _, event ->
            if (event != Lifecycle.Event.ON_RESUME) return@LifecycleEventObserver
            if (!caughtUp) {
                caughtUp = true
                return@LifecycleEventObserver
            }
            reload += 1
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    DisposableEffect(reload) {
        probe.start(context.applicationContext, savedTag)
        onDispose { probe.stop() }
    }
    LaunchedEffect(savedTag, probe.ready) {
        probe.onTag(savedTag)
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TextButton(onClick = onBack) {
                Text("← ${stringResource(R.string.back)}")
            }
            Text(
                text = stringResource(R.string.voice_language),
                style = MaterialTheme.typography.headlineLarge,
            )
            LanguageRow(
                label = stringResource(R.string.voice_phone),
                picked = chosen.isEmpty(),
                onClick = { onSelect("") },
            )
            if (probe.ready && probe.missingData) {
                Text(
                    text = stringResource(R.string.voice_missing),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Button(
                    onClick = { openVoiceDataInstaller(context) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(
                        text = stringResource(R.string.voice_install),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            when {
                !probe.ready -> Text(
                    text = stringResource(R.string.voice_loading),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                languages.isEmpty() && !probe.missingData -> Text(
                    text = stringResource(R.string.voice_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                else -> {
                    languages.forEach { (tag, label) ->
                        LanguageRow(
                            label = label,
                            picked = tag == chosen,
                            onClick = { onSelect(tag) },
                        )
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun LanguageRow(
    label: String,
    picked: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .semantics { selected = picked },
        shape = RoundedCornerShape(22.dp),
        color = if (picked) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        border = BorderStroke(
            width = 1.dp,
            color = if (picked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
        ),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(14.dp),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (picked) FontWeight.Bold else FontWeight.Medium,
        )
    }
}

private class VoiceProbe {
    private var engine: TextToSpeech? = null
    private var generation = 0
    private var requestedTag = ""
    var tags by mutableStateOf<List<String>>(emptyList())
        private set
    var missingData by mutableStateOf(false)
        private set
    var ready by mutableStateOf(false)
        private set

    fun start(context: Context, tag: String) {
        val previous = engine
        engine = null
        previous?.stop()
        previous?.shutdown()
        val ticket = ++generation
        requestedTag = tag
        ready = false
        tags = emptyList()
        missingData = false
        var created: TextToSpeech? = null
        created = TextToSpeech(context) { status ->
            val current = created ?: return@TextToSpeech
            if (ticket != generation) return@TextToSpeech
            if (status != TextToSpeech.SUCCESS) {
                tags = emptyList()
                missingData = true
                ready = true
                return@TextToSpeech
            }
            tags = readOfflineTags(current)
            missingData = current.missingVoiceData(requestedTag)
            ready = true
        }
        engine = created
    }

    fun onTag(tag: String) {
        requestedTag = tag
        val current = engine ?: return
        if (!ready) return
        missingData = current.missingVoiceData(tag)
    }

    fun stop() {
        generation += 1
        val current = engine ?: return
        engine = null
        current.stop()
        current.shutdown()
    }
}

private fun readOfflineTags(tts: TextToSpeech): List<String> {
    val voices = try {
        tts.voices
    } catch (_: RuntimeException) {
        null
    } ?: return emptyList()
    return offlineVoiceLanguageTags(
        voices.map { voice -> voice.locale.toLanguageTag() to voice.isNetworkConnectionRequired },
    )
}

private fun TextToSpeech.missingVoiceData(savedTag: String): Boolean {
    return voiceLocaleCandidates(savedTag).all { locale ->
        val available = isLanguageAvailable(locale)
        available == TextToSpeech.LANG_MISSING_DATA ||
            available == TextToSpeech.LANG_NOT_SUPPORTED
    }
}

private fun openVoiceDataInstaller(context: Context) {
    try {
        context.startActivity(Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA))
    } catch (_: ActivityNotFoundException) {
        // Este teléfono no ofrece la descarga de voces.
    }
}
