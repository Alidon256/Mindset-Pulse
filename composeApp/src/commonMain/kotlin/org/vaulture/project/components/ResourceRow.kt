package org.vaulture.project.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Groups
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.vaulture.project.data.models.ResourceItemData
import org.vaulture.project.screens.home.ResourceItem

@Composable
fun ResourceRow(isVertical: Boolean = false, onItemClick: () -> Unit = {}) {
    val resources = listOf(
        ResourceItemData(
            "Box Breathing",
            "Anxiety Relief",
            Icons.Default.Air,
            Color(0xFFE3F2FD),
            Color(0xFF1565C0)
        ),
        ResourceItemData("Sleep Hygiene", "Rest Better", Icons.Default.Bedtime, Color(0xFFF3E5F5), Color(0xFF7B1FA2)),
        ResourceItemData("Talk to Someone", "Find Support", Icons.Default.Groups, Color(0xFFE8F5E9), Color(0xFF2E7D32))
    )

    if (isVertical) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            resources.forEach { res -> ResourceItem(res, onClick = onItemClick) }
        }
    } else {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(resources) { res -> ResourceItem(res, Modifier.width(280.dp), onClick = onItemClick) }
        }
    }
}
