package org.vaulture.project.domain.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class Level(
    val name: String,
    val icon: ImageVector,
    val color1: Color,
    val color2: Color,
    val threshold: Int
)