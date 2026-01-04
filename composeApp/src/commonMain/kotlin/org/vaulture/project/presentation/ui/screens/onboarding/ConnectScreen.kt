package org.vaulture.project.presentation.ui.screens.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.vaulture.project.presentation.theme.AppTheme
import vaulture.composeapp.generated.resources.Res
import vaulture.composeapp.generated.resources.bg_two

@Composable
// Updated: Rename parameters for clarity
fun ConnectScreen(
    onNavigateToLogin: () -> Unit
) {
    _root_ide_package_.org.vaulture.project.presentation.theme.AppTheme {
        Scaffold(
            containerColor = Color.White
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // Top Section: Text
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = 50.dp,
                            bottom = 20.dp
                        ), // Adjusted top and added bottom padding
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Travel Deeper",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Connect with other travellers",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // Curved Image Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(500.dp) // Adjusted height, fine-tune this with the curve
                ) {
                    Image(
                        painter = painterResource(Res.drawable.bg_two),
                        contentDescription = "Hikers enjoying a scenic view",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(_root_ide_package_.org.vaulture.project.presentation.ui.screens.onboarding.AsymmetricBottomCurveShapeImproved()) // Using improved shape
                            .background(Color(0xFF1B5E20)), // Dark green from mockup
                        contentScale = ContentScale.Crop
                    )
                }

                // Spacer to create significant distance before buttons
                Spacer(modifier = Modifier.height(60.dp)) // Increased spacer

                // Bottom Section with Buttons
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 40.dp)
                ) {
                    Button(
                        // Updated: Use the navigation callback
                        onClick = onNavigateToLogin,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2E7D32)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Log In",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        // Updated: Use the navigation callback
                        onClick = onNavigateToLogin,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF2E7D32)
                        ),
                        border = BorderStroke(1.dp, Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Create account",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(0.5f))
                Spacer(modifier = Modifier.navigationBarsPadding())
            }
        }
    }
}

// Custom Shape for the Asymmetric Bottom Curve - Improved Version
fun AsymmetricBottomCurveShapeImproved(): Shape {
    return GenericShape { size, _ ->
        val width = size.width
        val height = size.height
        val topCurveStart = height * 0.10f
        val bottomCurveEnd = height * 0.85f
        moveTo(0f, height * 0.60f)
        cubicTo(width * 0.20f, height * 0.95f, width * 0.70f, height * 0.90f, width, bottomCurveEnd)
        lineTo(width, topCurveStart)
        cubicTo(width * 0.70f, height * 0.20f, width * 0.20f, height * 0.05f, 0f, topCurveStart)
        lineTo(0f, height * 0.60f)
        close()
    }
}
@Preview()
@Composable
fun ConnectScreenPreview() {
    MaterialTheme {
        // Updated: Provide an empty lambda for the preview
        _root_ide_package_.org.vaulture.project.presentation.ui.screens.onboarding.ConnectScreen(
            onNavigateToLogin = {})
    }
}
