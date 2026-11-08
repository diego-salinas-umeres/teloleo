package app.teloleo.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.teloleo.R
import app.teloleo.data.ReaderSettings
import app.teloleo.data.voiceLanguageLabel
import java.util.Locale

@Composable
fun ReadingOptions(
    settings: ReaderSettings,
    onQuietHours: (Boolean, Int, Int) -> Unit,
    onHeadphonesOnly: (Boolean) -> Unit,
    onShake: (Boolean) -> Unit,
    onSpeechRate: (Float) -> Unit,
    onSpeechVolume: (Float) -> Unit,
    onWordFilters: (List<String>) -> Unit,
    onEditTemplate: () -> Unit,
    onOpenVoice: () -> Unit,
    onRedact: (Boolean) -> Unit,
    onOpenPro: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pro = settings.proUnlocked
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.reading_title),
            style = MaterialTheme.typography.headlineSmall,
        )
        val voiceLabel = voiceLanguageLabel(settings.voiceLocale)
        VoiceLanguageRow(
            detail = voiceLabel.ifBlank { stringResource(R.string.voice_phone) },
            onClick = onOpenVoice,
        )
        OptionSurface {
            ToggleRow(
                title = stringResource(R.string.redact_codes),
                detail = stringResource(R.string.redact_codes_detail),
                checked = settings.redactOneTimeCodes,
                onChecked = onRedact,
            )
        }
        if (!pro) {
            TextButton(onClick = onOpenPro) {
                Text(stringResource(R.string.with_pro), fontWeight = FontWeight.Bold)
            }
        }
        if (pro) {
            OptionSurface {
                ToggleRow(
                    title = stringResource(R.string.quiet_hours),
                    detail = null,
                    checked = settings.quietHoursEnabled,
                    onChecked = { enabled ->
                        onQuietHours(enabled, settings.quietStartMinutes, settings.quietEndMinutes)
                    },
                )
                if (settings.quietHoursEnabled) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        HourSelector(
                            label = stringResource(R.string.quiet_from),
                            minutes = settings.quietStartMinutes,
                            onSelect = { minute ->
                                onQuietHours(true, minute, settings.quietEndMinutes)
                            },
                        )
                        HourSelector(
                            label = stringResource(R.string.quiet_until),
                            minutes = settings.quietEndMinutes,
                            onSelect = { minute ->
                                onQuietHours(true, settings.quietStartMinutes, minute)
                            },
                        )
                    }
                }
            }
            OptionSurface {
                ToggleRow(
                    title = stringResource(R.string.headphones_only),
                    detail = null,
                    checked = settings.headphonesOnly,
                    onChecked = onHeadphonesOnly,
                )
            }
            OptionSurface {
                ToggleRow(
                    title = stringResource(R.string.shake_to_silence),
                    detail = stringResource(R.string.shake_detail),
                    checked = settings.shakeToSilence,
                    onChecked = onShake,
                )
            }
            OptionSurface {
                RateSlider(
                    label = stringResource(R.string.speech_rate),
                    value = settings.speechRate,
                    valueRange = 0.7f..1.5f,
                    onFinished = onSpeechRate,
                )
            }
            OptionSurface {
                RateSlider(
                    label = stringResource(R.string.speech_volume),
                    value = settings.speechVolume,
                    valueRange = 0.3f..1f,
                    onFinished = onSpeechVolume,
                )
            }
            OptionSurface {
                Text(
                    text = stringResource(R.string.phrase_template),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                val spoken = phrasePreview(settings.phraseTemplate, settings.redactOneTimeCodes)
                val raw = settings.phraseTemplate.trim()
                val emptyLabel = stringResource(R.string.template_empty_preview)
                Text(
                    text = spoken ?: raw.ifEmpty { emptyLabel },
                    style = MaterialTheme.typography.bodyLarge,
                )
                TextButton(onClick = onEditTemplate) {
                    Text(stringResource(R.string.template_action), fontWeight = FontWeight.Bold)
                }
            }
            WordFilters(
                saved = settings.wordFilters,
                onSave = onWordFilters,
            )
        } else {
            LockedRow(stringResource(R.string.quiet_hours), null, onOpenPro)
            LockedRow(stringResource(R.string.headphones_only), null, onOpenPro)
            LockedRow(stringResource(R.string.shake_to_silence), stringResource(R.string.shake_detail), onOpenPro)
            LockedRow(stringResource(R.string.speech_rate), null, onOpenPro)
            LockedRow(stringResource(R.string.speech_volume), null, onOpenPro)
            LockedRow(stringResource(R.string.phrase_template), null, onOpenPro)
            LockedRow(stringResource(R.string.word_filters), stringResource(R.string.word_filters_detail), onOpenPro)
        }
    }
}

@Composable
private fun VoiceLanguageRow(
    detail: String,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(R.string.voice_language),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun OptionSurface(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content,
        )
    }
}

@Composable
private fun LockedRow(
    title: String,
    detail: String?,
    onOpenPro: () -> Unit,
) {
    Surface(
        onClick = onOpenPro,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(R.string.open_pro),
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                )
            }
            if (!detail.isNullOrBlank()) {
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ToggleRow(
    title: String,
    detail: String?,
    checked: Boolean,
    onChecked: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            if (!detail.isNullOrBlank()) {
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Switch(checked = checked, onCheckedChange = onChecked)
    }
}

@Composable
private fun RowScope.HourSelector(
    label: String,
    minutes: Int,
    onSelect: (Int) -> Unit,
) {
    var open by remember { mutableStateOf(false) }
    Column(modifier = Modifier.weight(1f)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedButton(
            onClick = { open = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(formatMinutes(minutes))
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            (0..23).forEach { hour ->
                DropdownMenuItem(
                    text = { Text(formatMinutes(hour * 60)) },
                    onClick = {
                        open = false
                        onSelect(hour * 60)
                    },
                )
            }
        }
    }
}

@Composable
private fun RateSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onFinished: (Float) -> Unit,
) {
    var current by remember(value) { mutableFloatStateOf(value.coerceIn(valueRange)) }
    Text(
        text = "$label ${String.format(Locale.getDefault(), "%.1f", current)}",
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.SemiBold,
    )
    Slider(
        value = current,
        onValueChange = { current = it },
        onValueChangeFinished = { onFinished(current) },
        valueRange = valueRange,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WordFilters(
    saved: List<String>,
    onSave: (List<String>) -> Unit,
) {
    var draft by remember { mutableStateOf("") }
    OptionSurface {
        Text(
            text = stringResource(R.string.word_filters),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(R.string.word_filters_detail),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (saved.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                saved.forEachIndexed { index, word ->
                    Surface(
                        onClick = { onSave(saved.filterIndexed { wordIndex, _ -> wordIndex != index }) },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                    ) {
                        Text(
                            text = "$word ×",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                modifier = Modifier.weight(1f),
                singleLine = true,
                placeholder = { Text(stringResource(R.string.word_filters_hint)) },
            )
            TextButton(
                onClick = {
                    val words = draft.split(',').map { it.trim() }.filter { it.length >= 2 }
                    val merged = saved.toMutableList()
                    words.forEach { word ->
                        if (merged.none { it.equals(word, ignoreCase = true) }) merged.add(word)
                    }
                    if (merged != saved) onSave(merged)
                    if (words.isNotEmpty()) draft = ""
                },
                modifier = Modifier.heightIn(min = 48.dp),
            ) {
                Text(stringResource(R.string.filter_add), fontWeight = FontWeight.Bold)
            }
        }
    }
}

internal fun formatMinutes(minutes: Int): String {
    val hour = (minutes / 60).coerceIn(0, 23)
    val minute = (minutes % 60).coerceIn(0, 59)
    return "%02d:%02d".format(hour, minute)
}
