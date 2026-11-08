package app.teloleo.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.teloleo.R
import app.teloleo.billing.BillingOffer
import app.teloleo.billing.StoreNotice
import java.util.Locale

@Composable
fun ProScreen(
    proUnlocked: Boolean,
    offer: BillingOffer,
    onBuy: () -> Unit,
    onRestore: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val features = featureLines(stringResource(R.string.pro_features))
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TextButton(onClick = onBack) {
                Text("← ${stringResource(R.string.back)}")
            }
            Text(
                text = stringResource(R.string.pro_title),
                style = MaterialTheme.typography.headlineLarge,
            )
            Text(
                text = stringResource(R.string.pro_intro),
                style = MaterialTheme.typography.bodyLarge,
            )
            if (proUnlocked) {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                ) {
                    Text(
                        text = stringResource(R.string.pro_active),
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            } else {
                if (offer.purchasePending) {
                    Text(stringResource(R.string.pro_pending), style = MaterialTheme.typography.bodyLarge)
                }
                noticeText(offer.notice)?.let { message ->
                    Text(message, style = MaterialTheme.typography.bodyMedium)
                }
            }
            Column {
                features.forEach { line ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Text(
                            text = "✓",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Text(
                            text = line,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                }
            }
            Text(
                text = stringResource(R.string.pro_free_note),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (!proUnlocked) {
                val price = offer.formattedPrice
                Button(
                    onClick = onBuy,
                    enabled = offer.productAvailable,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(
                        text = if (price.isNullOrBlank()) {
                            stringResource(R.string.pro_buy)
                        } else {
                            stringResource(R.string.pro_buy_price, price)
                        },
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            TextButton(
                onClick = onRestore,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.pro_restore), fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

private fun featureLines(raw: String): List<String> =
    raw.split(',')
        .map { it.trim().trimEnd('.') }
        .filter { it.isNotEmpty() }
        .map { line ->
            line.replaceFirstChar { char ->
                if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else char.toString()
            }
        }

@Composable
private fun noticeText(notice: StoreNotice): String? = when (notice) {
    StoreNotice.None -> null
    StoreNotice.StoreUnavailable -> stringResource(R.string.store_unavailable)
    StoreNotice.ProductMissing -> stringResource(R.string.store_product_missing)
    StoreNotice.PurchaseFailed -> stringResource(R.string.store_purchase_failed)
}
