@file:OptIn(ExperimentalMaterial3Api::class)

package app.teloleo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.teloleo.R
import app.teloleo.device.BatteryGuide
import app.teloleo.listener.PhraseTemplate
import app.teloleo.scope.SpeechLimits

@Composable
fun DisclosureSheet(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val steps = stringResource(R.string.disclosure_body)
        .split("\n\n")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
    BoardSheet(onDismiss = onDismiss, dismissOnClickOutside = false) {
        Text(
            text = stringResource(R.string.disclosure_title),
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(Modifier.height(12.dp))
        steps.forEachIndexed { index, paragraph ->
            Row(
                modifier = Modifier.padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "${index + 1}",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
                Text(
                    text = paragraph,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
        Text(
            text = stringResource(R.string.disclosure_next),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SheetButtons(
            confirm = stringResource(R.string.disclosure_confirm),
            onConfirm = onConfirm,
            dismiss = stringResource(R.string.not_now),
            onDismiss = onDismiss,
        )
    }
}

@Composable
fun LimitSheet(
    onSeePro: () -> Unit,
    onDismiss: () -> Unit,
) {
    BoardSheet(onDismiss = onDismiss) {
        Text(
            text = stringResource(R.string.free_limit_title),
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = stringResource(R.string.free_limit_body),
            modifier = Modifier.padding(top = 12.dp),
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            text = stringResource(R.string.free_limit_order),
            modifier = Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SheetButtons(
            confirm = stringResource(R.string.see_pro),
            onConfirm = onSeePro,
            dismiss = stringResource(R.string.not_now),
            onDismiss = onDismiss,
        )
    }
}

@Composable
fun TemplateSheet(
    title: String,
    initial: String,
    redactOneTimeCodes: Boolean,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    var value by remember(initial) { mutableStateOf(initial) }
    val preview = phrasePreview(value, redactOneTimeCodes)
        ?: if (value.isBlank()) {
            stringResource(R.string.template_empty_preview)
        } else {
            value.trim()
        }
    BoardSheet(onDismiss = onDismiss) {
        Text(
            text = title.ifBlank { stringResource(R.string.template_title) },
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = stringResource(R.string.template_help),
            modifier = Modifier.padding(top = 12.dp),
            style = MaterialTheme.typography.bodyLarge,
        )
        OutlinedTextField(
            value = value,
            onValueChange = { next ->
                if (next.length <= SpeechLimits.TEMPLATE_MAX) value = next
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            minLines = 3,
        )
        Text(
            text = stringResource(R.string.template_preview),
            modifier = Modifier.padding(top = 12.dp),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = preview,
            modifier = Modifier
                .padding(top = 6.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(12.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        SheetButtons(
            confirm = stringResource(R.string.save),
            onConfirm = { onSave(value) },
            dismiss = stringResource(R.string.cancel),
            onDismiss = onDismiss,
        )
    }
}

@Composable
fun BatterySheet(
    guide: BatteryGuide,
    onDismiss: () -> Unit,
) {
    BoardSheet(onDismiss = onDismiss) {
        Text(
            text = stringResource(R.string.battery_title),
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = stringResource(guide.brandRes()),
            modifier = Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(guide.guideRes()),
            modifier = Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            text = stringResource(R.string.battery_brand_note),
            modifier = Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(
            onClick = onDismiss,
            modifier = Modifier
                .padding(top = 14.dp)
                .fillMaxWidth()
                .heightIn(min = 48.dp),
            shape = RoundedCornerShape(14.dp),
        ) {
            Text(stringResource(R.string.sheet_done), fontWeight = FontWeight.SemiBold)
        }
    }
}

internal fun phrasePreview(template: String, redactOneTimeCodes: Boolean): String? {
    val trimmed = template.trim()
    if (trimmed.isEmpty()) return null
    return PhraseTemplate.render(
        template = trimmed,
        appLabel = "WhatsApp",
        title = "Ana",
        text = "Tu código es 482913",
        bigText = null,
        redactOneTimeCodes = redactOneTimeCodes,
    )
}

internal fun BatteryGuide.brandRes(): Int = when (this) {
    BatteryGuide.Xiaomi -> R.string.battery_brand_xiaomi
    BatteryGuide.Samsung -> R.string.battery_brand_samsung
    BatteryGuide.Transsion -> R.string.battery_brand_transsion
    BatteryGuide.Huawei -> R.string.battery_brand_huawei
    BatteryGuide.OppoFamily -> R.string.battery_brand_oppo
    BatteryGuide.Generic -> R.string.battery_brand_generic
}

internal fun BatteryGuide.guideRes(): Int = when (this) {
    BatteryGuide.Xiaomi -> R.string.battery_xiaomi
    BatteryGuide.Samsung -> R.string.battery_samsung
    BatteryGuide.Transsion -> R.string.battery_transsion
    BatteryGuide.Huawei -> R.string.battery_huawei
    BatteryGuide.OppoFamily -> R.string.battery_oppo
    BatteryGuide.Generic -> R.string.battery_generic
}

@Composable
private fun BoardSheet(
    onDismiss: () -> Unit,
    dismissOnClickOutside: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        properties = ModalBottomSheetProperties(
            shouldDismissOnClickOutside = dismissOnClickOutside,
        ),
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 24.dp),
                content = content,
            )
        },
    )
}

@Composable
private fun SheetButtons(
    confirm: String,
    onConfirm: () -> Unit,
    dismiss: String,
    onDismiss: () -> Unit,
) {
    Column(
        modifier = Modifier.padding(top = 14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Button(
            onClick = onConfirm,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
            shape = RoundedCornerShape(14.dp),
        ) {
            Text(confirm, fontWeight = FontWeight.SemiBold)
        }
        TextButton(
            onClick = onDismiss,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
        ) {
            Text(dismiss, fontWeight = FontWeight.SemiBold)
        }
    }
}
