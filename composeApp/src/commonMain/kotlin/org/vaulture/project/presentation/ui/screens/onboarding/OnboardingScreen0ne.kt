package org.vaulture.project.presentation.ui.screens.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.vaulture.project.presentation.theme.AppTheme
import vaulture.composeapp.generated.resources.Res
import vaulture.composeapp.generated.resources.val_1

@Composable
fun OnboardingScreenOne(
    onNext: () -> Unit = {},
    onSkip: () -> Unit = {}
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween // Ensures space between top, middle, and bottom
        ) {
            // Skip button at top
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onSkip) {
                    Text(
                        "Skip",
                        color = MaterialTheme.colorScheme.onSurfaceVariant, // Use theme color
                        fontSize = 16.sp
                    )
                }
            }

            // Main content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .weight(1f) // Allows this column to take up available space
                    .padding(horizontal = 24.dp)
            ) {
                Text(
                    "Get Inspiration For Your New Day",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    lineHeight = 36.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(24.dp)) // Add clipping for a modern look
                ) {
                    Image(
                        painter = painterResource(Res.drawable.val_1),
                        contentDescription = "Better Mindset",
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF1B5E20)),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "Whether you’re seeking a quiet place to find your balance or the mental clarity to conquer the city grind, we’ve mapped out the best paths for your mindset.",
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, // Use theme color
                    textAlign = TextAlign.Center,
                    lineHeight = 26.sp // Improve line spacing for body
                )
            }

            // Navigation buttons at bottom
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp), // Consistent padding
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Improved Page indicator with fixed widths
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(32.dp) // Active indicator is wider
                            .background(Color(0xFF2E7D32), RoundedCornerShape(4.dp))
                    )
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(16.dp)
                            .background(Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                    )
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(16.dp)
                            .background(Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                    )
                }

                Button(
                    onClick = onNext,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2E7D32)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(50.dp)
                ) {
                    Text(
                        "Next",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
@Preview
fun OnboardingScreenPreview() {
    _root_ide_package_.org.vaulture.project.presentation.theme.AppTheme(useDarkTheme = false) {
        _root_ide_package_.org.vaulture.project.presentation.ui.screens.onboarding.OnboardingScreenOne()
    }
}
