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
import coil3.compose.AsyncImage
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.vaulture.project.presentation.theme.AppTheme
import vaulture.composeapp.generated.resources.Res
import vaulture.composeapp.generated.resources.bg_two

@Composable

fun ConnectScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToSignUp: () -> Unit
) {
    AppTheme {
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


                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = 50.dp,
                            bottom = 20.dp
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Build Your Mindset",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Connect with other like minded people",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(500.dp)
                ) {
                    AsyncImage(
                        model = "https://images.pexels.com/photos/3772612/pexels-photo-3772612.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
                        contentDescription = "Nature",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(AsymmetricBottomCurveShapeImproved())
                            .background(MaterialTheme.colorScheme.primary),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(60.dp))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 40.dp)
                ) {
                    Button(
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
                        onClick = onNavigateToSignUp,
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
        ConnectScreen(
            onNavigateToLogin = {},
            onNavigateToSignUp = {}
        )
    }
}
