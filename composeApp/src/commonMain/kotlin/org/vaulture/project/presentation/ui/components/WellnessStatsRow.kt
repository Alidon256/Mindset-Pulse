package org.vaulture.project.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.vaulture.project.domain.model.WellnessStats
import kotlin.math.roundToInt

@Composable
fun WellnessStatsRow(
    stats: WellnessStats
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            StatItem(
                "Check-ins",
                "${stats.totalCheckIns}"
            )
            StatItem(
                "Mindful Mins",
                "${stats.totalMinutes}"
            )
            StatItem(
                "Consistency",
                "${(stats.consistency * 100).roundToInt()}%"
            )
        }
    }
}