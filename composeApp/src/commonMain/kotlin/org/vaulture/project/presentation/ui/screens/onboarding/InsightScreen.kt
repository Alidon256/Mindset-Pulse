package org.vaulture.project.presentation.ui.screens.onboarding

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
import coil3.compose.AsyncImage

data class PulseStory(
    val id: String,
    val title: String,
    val imageUrl: String,
    val category: String,
    val isLarge: Boolean = false,
    val span: Int = 1
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightScreen(onGetStarted: () -> Unit) {

    val stories = listOf(
        PulseStory(
            "1",
            "Managing Burnout",
            "https://images.pexels.com/photos/3772612/pexels-photo-3772612.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
            "Wellness Guide",
            isLarge = true,
            span = 2
        ),
        PulseStory(
            "2",
            "Daily Meditation",
            "https://images.pexels.com/photos/1051838/pexels-photo-1051838.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
            "Practice",
            span = 1
        ),
        PulseStory(
            "3",
            "Deep Sleep",
            "https://images.pexels.com/photos/355863/pexels-photo-355863.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
            "Rest",
            span = 1
        ),
        PulseStory(
            "4",
            "Nature Connection",
            "https://images.pexels.com/photos/15286/pexels-photo.jpg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
            "Grounding",
            isLarge = true,
            span = 2
        )
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize(),
        bottomBar = {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier.height(8.dp).width(16.dp)
                            .background(Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                    )
                    Box(
                        modifier = Modifier.height(8.dp).width(16.dp)
                            .background(Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                    )
                    Box(
                        modifier = Modifier.height(8.dp).width(32.dp)
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
                    )
                }

                Button(
                    onClick = onGetStarted,
                    modifier = Modifier.height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        "Start Pulse",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    ) { paddingValues ->
        BoxWithConstraints(modifier = Modifier.padding(paddingValues)) {
            val isWideScreen = maxWidth > 600.dp
            val gridColumns = if (isWideScreen) 4 else 2

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .consumeWindowInsets(paddingValues)
            ) {
                Text(
                    "Discover Your Balance",
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 40.dp, bottom = 24.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(gridColumns),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(
                        stories,
                        key = { _, item -> item.id },
                        span = { _, item ->
                            val spanCount = if (isWideScreen) {
                                if (item.isLarge) 2 else 1
                            } else {
                                GridItemSpan(item.span)
                            }
                            spanCount as GridItemSpan
                        }
                    ) { _, story ->
                        PulseStoryCard(story)
                    }
                }
            }
        }
    }
}

@Composable
fun PulseStoryCard(story: PulseStory) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (story.isLarge) 240.dp else 200.dp)
            .clip(RoundedCornerShape(16.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = story.imageUrl,
                contentDescription = story.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                            startY = 200f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = story.category.uppercase(),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = story.title,
                    color = Color.White,
                    fontSize = if (story.isLarge) 22.sp else 18.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}
