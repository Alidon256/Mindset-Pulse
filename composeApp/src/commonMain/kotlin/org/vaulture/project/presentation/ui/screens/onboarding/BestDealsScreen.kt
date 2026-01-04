package org.vaulture.project.presentation.ui.screens.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.vaulture.project.presentation.theme.AppTheme
import vaulture.composeapp.generated.resources.*

data class DealDestination(
    val id: String,
    val cityName: String,
    val imageRes: DrawableResource,
    val details: String,
    val isLarge: Boolean = false,
    val span: Int = 1
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BestDealsScreen(onGetStarted: () -> Unit) {

    val deals = listOf(
        _root_ide_package_.org.vaulture.project.presentation.ui.screens.onboarding.DealDestination(
            "1",
            "Uganda",
            Res.drawable.uganda,
            "3265 Likes",
            isLarge = true,
            span = 2
        ),
        _root_ide_package_.org.vaulture.project.presentation.ui.screens.onboarding.DealDestination(
            "2",
            "South Africa",
            Res.drawable.kenya,
            "$50",
            span = 1
        ),
        _root_ide_package_.org.vaulture.project.presentation.ui.screens.onboarding.DealDestination(
            "3",
            "Kenya",
            Res.drawable.egypt,
            "$110",
            span = 1
        ),
        _root_ide_package_.org.vaulture.project.presentation.ui.screens.onboarding.DealDestination(
            "4",
            "Egypt",
            Res.drawable.nigeria,
            "4385 Likes",
            span = 2
        )
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            // Bottom bar with indicators and button for a cohesive feel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Page indicator (now shows this is screen 3 of 3)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
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
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(32.dp)
                            .background(Color(0xFF2E7D32), RoundedCornerShape(4.dp))
                    )
                }

                Button(
                    onClick = onGetStarted,
                    modifier = Modifier.height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Text(
                        "Get Started",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
            .padding(paddingValues)
            .consumeWindowInsets(paddingValues)
        ) {
            // Integrated Title
            Text(
                "Find Your Next Adventure",
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 56.dp, bottom = 24.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(
                    deals,
                    key = { _, item -> item.id },
                    span = { _, item -> GridItemSpan(item.span) }) { _, destination ->
                    _root_ide_package_.org.vaulture.project.presentation.ui.screens.onboarding.DestinationCard(
                        destination
                    )
                }
            }
        }
    }
}

@Composable
fun DestinationCard(destination: org.vaulture.project.presentation.ui.screens.onboarding.DealDestination) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (destination.isLarge) 240.dp else 200.dp)
            .clip(RoundedCornerShape(16.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(destination.imageRes),
                contentDescription = destination.cityName,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                            startY = 300f
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = destination.cityName,
                    color = Color.White,
                    fontSize = if (destination.isLarge) 24.sp else 20.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = destination.details,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = if (destination.isLarge) 16.sp else 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Preview()
@Composable
fun BestDealsScreenPreview() {
    _root_ide_package_.org.vaulture.project.presentation.theme.AppTheme {
        _root_ide_package_.org.vaulture.project.presentation.ui.screens.onboarding.BestDealsScreen(
            onGetStarted = {})
    }
}
