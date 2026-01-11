package org.vaulture.project.presentation.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import org.vaulture.project.presentation.theme.PoppinsTypography

@Composable
fun StatItem(
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = PoppinsTypography().headlineMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            label,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = PoppinsTypography().labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}