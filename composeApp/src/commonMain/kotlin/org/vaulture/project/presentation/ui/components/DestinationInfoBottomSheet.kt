// Create a new file: DestinationInfoBottomSheet.kt
package org.vaulture.project.presentation.ui.components // Or screens if it's more of a screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.vaulture.project.presentation.theme.AppTheme
import vaulture.composeapp.generated.resources.* // Your resources

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DestinationInfoBottomSheet(
    sheetState: SheetState, // Pass from caller: val sheetState = rememberModalBottomSheetState()
    onDismissRequest: () -> Unit,
    onBookNow: () -> Unit,
    onMoreDetails: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        containerColor = Color.Transparent, // Make the sheet itself transparent to see the image
        dragHandle = null // Remove default drag handle if image fills top
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                // Height can be intrinsic or fixed. For this design, it seems fixed or based on content.
                // .height(450.dp) // Example height, adjust as needed or use wrapContentHeight
                .background(Color.Transparent) // Ensure Box is transparent
        ) {
            // Background Image
            Image(
                painter = painterResource(Res.drawable.ic_hotel2), // Replace with actual image
                contentDescription = "Rattlesnake Canyon",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)) // Clip image to sheet shape
            )

            // Gradient overlay for text readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                            startY = 200f // Adjust gradient start to ensure top of image is clearer
                        )
                    )
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            )

            // Content on top of the image
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter) // Align content to the bottom
                    .padding(24.dp)
            ) {
                Text(
                    "Rattlesnake Canyon",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    ),
                    fontSize = 26.sp
                )
                Text(
                    "Page, USA", // Example location
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.White.copy(alpha = 0.9f))
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    "$244",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    ),
                    fontSize = 30.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onBookNow,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)) // Green
                ) {
                    Text("Book now", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(
                    onClick = onMoreDetails,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "More details",
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun DestinationIfoPreview() {
    _root_ide_package_.org.vaulture.project.presentation.theme.AppTheme {
        org.vaulture.project.presentation.ui.components.DestinationInfoBottomSheet(
            sheetState = rememberModalBottomSheetState(),
            onDismissRequest = {},
            onBookNow = {},
            onMoreDetails = {}
        )
    }
}
