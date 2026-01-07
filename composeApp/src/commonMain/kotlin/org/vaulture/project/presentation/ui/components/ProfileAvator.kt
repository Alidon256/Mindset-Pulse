package org.vaulture.project.presentation.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@Composable
fun ProfileAvatar(
    model: String?,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = CircleShape,
        shadowElevation = 8.dp,
        modifier = modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
    ) {
        AsyncImage(
            model = model ?: "https://images.pexels.com/photos/1065084/pexels-photo-1065084.jpeg",
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}
