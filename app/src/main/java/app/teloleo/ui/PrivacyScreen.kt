package app.teloleo.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.teloleo.R

@Composable
fun PrivacyScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
                text = stringResource(R.string.privacy_title),
                style = MaterialTheme.typography.headlineLarge,
            )
            Text(
                text = stringResource(R.string.privacy_intro),
                style = MaterialTheme.typography.bodyLarge,
            )
            PolicyBlock(R.string.privacy_read_title, R.string.privacy_read_body)
            PolicyBlock(R.string.privacy_where_title, R.string.privacy_where_body)
            PolicyBlock(R.string.privacy_stored_title, R.string.privacy_stored_body)
            PolicyBlock(R.string.privacy_codes_title, R.string.privacy_codes_body)
            PolicyBlock(R.string.privacy_off_title, R.string.privacy_off_body)
            PolicyBlock(R.string.privacy_safety_title, R.string.privacy_safety_body)
            PolicyBlock(R.string.privacy_pro_title, R.string.privacy_pro_body)
            PolicyBlock(R.string.privacy_children_title, R.string.privacy_children_body)
            PolicyBlock(R.string.privacy_contact_title, R.string.privacy_contact_body)
            Text(
                text = stringResource(R.string.privacy_url_note),
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PolicyBlock(title: Int, body: Int) {
    Text(
        text = stringResource(title),
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier.padding(top = 10.dp),
    )
    Text(
        text = stringResource(body),
        style = MaterialTheme.typography.bodyLarge,
    )
}
