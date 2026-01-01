// Create a new file: ErrorScreen.kt
package org.vaulture.project.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh // Or a custom error/warning icon
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.vaulture.project.theme.AppTheme


@Composable
fun ErrorScreen(
    onTryAgain: () -> Unit,
    onBack: (() -> Unit)? = null // Make back navigation optional
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface // Or Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                // painter = painterResource(Res.drawable.ic_error_refresh), // Use your custom refresh/error icon
                imageVector = Icons.Filled.Refresh, // Placeholder Material Icon
                contentDescription = "Error Refresh",
                tint = MaterialTheme.colorScheme.primary, // Green color from mockup
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Ooops....",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "Something went wrong. We’re doing everything to fix it and it could be up and running soon.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = onTryAgain,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)) // Green
            ) {
                Text("Try again", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(12.dp))

            onBack?.let { // Show "Back" button only if onBack is provided
                TextButton(onClick = it) {
                    Text(
                        "Back",
                        color = MaterialTheme.colorScheme.primary, // Green
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
@Composable
@Preview
fun ErrorScreenPreview() {
    AppTheme {
        ErrorScreen({}, {})
    }
}