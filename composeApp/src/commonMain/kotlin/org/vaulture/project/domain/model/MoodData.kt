package org.vaulture.project.domain.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class MoodData(
    val name: String,
    val icon: ImageVector,
    val selectedColor: Color,
    val contentColor: Color
)