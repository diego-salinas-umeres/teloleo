package app.teloleo.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.teloleo.R
import app.teloleo.apps.AppIcons
import app.teloleo.apps.LaunchableApp
import app.teloleo.data.ReaderSettings
import app.teloleo.scope.AllowedApps

@Composable
fun AppsScreen(
    settings: ReaderSettings,
    apps: List<LaunchableApp>,
    appsLoaded: Boolean,
    onToggleApp: (String) -> Unit,
    onTemplate: (LaunchableApp) -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by remember { mutableStateOf("") }
    val speaking = AllowedApps.speakingPackages(settings.allowedPackages, settings.proUnlocked)
    val known = apps.associateBy { it.packageName }
    val selected = settings.allowedPackages.map { pkg -> known[pkg] ?: LaunchableApp(pkg, pkg) }
    val others = apps.filter { it.packageName !in settings.allowedPackages }
    val needle = query.trim()
    val selectedVisible = selected.filter { it.matches(needle) }
    val othersVisible = others.filter { it.matches(needle) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "title") {
            Text(
                text = stringResource(R.string.tab_apps),
                style = MaterialTheme.typography.headlineSmall,
            )
        }
        item(key = "search") {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                placeholder = { Text(stringResource(R.string.search_apps)) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        TextButton(onClick = { query = "" }) {
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
        }
        item(key = "count") {
            Text(
                text = if (settings.proUnlocked) {
                    stringResource(R.string.pro_count, settings.allowedPackages.size)
                } else {
                    stringResource(R.string.free_count, settings.allowedPackages.size)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (!appsLoaded) {
            item(key = "loading") { Text(stringResource(R.string.loading_apps)) }
            items(count = 3, key = { "skeleton-$it" }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                )
            }
        } else {
            if (needle.isEmpty() && settings.allowedPackages.isEmpty()) {
                item(key = "empty") {
                    Text(stringResource(R.string.silence_by_default), style = MaterialTheme.typography.bodyLarge)
                }
            }
            if (selectedVisible.isNotEmpty()) {
                item(key = "speaking-header") {
                    Text(stringResource(R.string.section_speaking), style = MaterialTheme.typography.titleLarge)
                }
                items(selectedVisible, key = { "on-${it.packageName}" }, contentType = { "app" }) { app ->
                    AppRow(
                        app = app,
                        selected = true,
                        speaks = app.packageName in speaking,
                        showTemplate = settings.proUnlocked,
                        onToggle = { onToggleApp(app.packageName) },
                        onTemplate = { onTemplate(app) },
                    )
                }
            }
            if (needle.isEmpty() || othersVisible.isNotEmpty()) {
                item(key = "others-header") {
                    Text(stringResource(R.string.section_others), style = MaterialTheme.typography.titleLarge)
                }
                items(othersVisible, key = { "off-${it.packageName}" }, contentType = { "app" }) { app ->
                    AppRow(
                        app = app,
                        selected = false,
                        speaks = false,
                        showTemplate = false,
                        onToggle = { onToggleApp(app.packageName) },
                        onTemplate = {},
                    )
                }
            }
            if (needle.isNotEmpty() && selectedVisible.isEmpty() && othersVisible.isEmpty()) {
                item(key = "none") { Text(stringResource(R.string.no_search_hits)) }
            }
        }
    }
}

@Composable
private fun AppRow(
    app: LaunchableApp,
    selected: Boolean,
    speaks: Boolean,
    showTemplate: Boolean,
    onToggle: () -> Unit,
    onTemplate: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onToggle)
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AppIcon(app)
            Column(Modifier.weight(1f)) {
                Text(
                    text = app.label,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                if (selected && !speaks) {
                    Text(
                        text = stringResource(R.string.saved_until_pro),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        if (showTemplate) {
            Surface(
                onClick = onTemplate,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Text(
                    text = stringResource(R.string.template_action),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
        Switch(checked = selected, onCheckedChange = { onToggle() })
    }
}

@Composable
private fun AppIcon(app: LaunchableApp) {
    val bitmap = remember(app.packageName) { AppIcons.peek(app.packageName) }
    val image = remember(bitmap) { bitmap?.asImageBitmap() }
    if (image == null) {
        Box(
            modifier = Modifier
                .size(AppIcons.SIZE_DP.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = app.label.take(1).uppercase(),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold,
            )
        }
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

private fun LaunchableApp.matches(needle: String): Boolean {
    if (needle.isEmpty()) return true
    return label.contains(needle, ignoreCase = true) || packageName.contains(needle, ignoreCase = true)
}
