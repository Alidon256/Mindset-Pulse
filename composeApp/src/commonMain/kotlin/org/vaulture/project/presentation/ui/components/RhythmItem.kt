package org.vaulture.project.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import org.jetbrains.compose.ui.tooling.preview.Preview

import org.vaulture.project.domain.model.RhythmTrack
import org.vaulture.project.presentation.theme.PoppinsTypography
import org.vaulture.project.presentation.utils.formatCount


@Composable
fun RhythmItem(
    track: RhythmTrack,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false
) {

    println("RENDERING Item ID: ${track.id}, Title: '${track.title}', Artist: '${track.artist}', Duration: '${track.duration}', Listeners: ${track.listenerCount}, Thumb: '${track.thumbnailUrl}'")

    val cardColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val textColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    val subTextColor = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant

    BoxWithConstraints(modifier = modifier) {
        val screenWidthDp = maxWidth
        val isLargeScreen = screenWidthDp >= 920.dp
        val size = if (isLargeScreen) androidx.compose.ui.unit.DpSize(220.dp, 160.dp) else androidx.compose.ui.unit.DpSize(190.dp, 140.dp)
        val width = if (isLargeScreen) 230.dp else 210.dp

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .width(width)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClick() }
                    .shadow(elevation = 4.dp, shape = RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .size(size.width, size.height)
                        .background(cardColor.copy(alpha = 0.1f))
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    AsyncImage(
                        model = track.thumbnailUrl.takeIf { it.isNotEmpty() },
                        contentDescription = track.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )

                    if (track.duration.isNotEmpty()) {
                        println("Item ID ${track.id} - About to render Duration: '${track.duration}'")
                        Text(
                            text = track.duration,
                            style = PoppinsTypography().labelLarge.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = textColor
                            ),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(6.dp)
                                .background(
                                    cardColor.copy(alpha = 0.7f),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    } else {
                        println("Item ID ${track.id} - Duration is EMPTY, not rendering duration text.")
                    }
                }
            }

            Column(
                modifier = Modifier.padding(top = 8.dp)
            ) {
                println("Item ID ${track.id} - About to render Title: '${track.title}'")
                Text(
                    text = track.title.takeIf { it.isNotEmpty() } ?: "Untitled Track",
                    style = PoppinsTypography().bodyLarge.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))

                val artistText = track.artist.takeIf { it.isNotEmpty() } ?: "Unknown Artist"
                //val listenerText = "${formatCount(track.listenerCount.toLong())} listeners"
                //val fullArtistListenerString = "$artistText • $listenerText"
               // println("Item ID ${track.id} - About to render Artist/Listeners: '$fullArtistListenerString' with color: $subTextColor")
                Text(
                    text = artistText,
                    style = PoppinsTypography().bodySmall.copy(
                        fontSize = 14.sp,
                        color = subTextColor
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
@Composable
@Preview
fun RhythmItemPreview(){
    RhythmItem(
        track = RhythmTrack(
            id = "7353g",
            title = "Morning Aura",
            artist = "Mugumya Ali",
        ),
        onClick = {},
        modifier = Modifier,
        isSelected = false
    )
}
