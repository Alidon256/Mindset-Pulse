package org.vaulture.project.presentation.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun StressTrendChart(height: Dp = 220.dp) {
    val dataPoints = listOf(35f, 42f, 38f, 25f, 60f, 72f, 45f)
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    val graphColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth().height(height).padding(horizontal = 16.dp)
    ) {
        Column(Modifier.padding(24.dp)) {
            Box(Modifier.weight(1f).fillMaxWidth()) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val h = size.height
                    val spacing = width / (dataPoints.size - 1)
                    val path = Path()

                    dataPoints.forEachIndexed { index, value ->
                        val x = index * spacing
                        val y = h - (value / 100f * h)
                        if (index == 0) path.moveTo(x, y)
                        else {
                            val prevX = (index - 1) * spacing
                            val prevY = h - (dataPoints[index - 1] / 100f * h)
                            val controlX1 = prevX + spacing / 2
                            val controlX2 = x - spacing / 2
                            path.cubicTo(controlX1, prevY, controlX2, y, x, y)
                        }
                    }

                    val fillPath = Path().apply {
                        addPath(path)
                        lineTo(width, h)
                        lineTo(0f, h)
                        close()
                    }

                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(graphColor.copy(alpha = 0.3f), Color.Transparent)
                        )
                    )

                    // Stroke
                    drawPath(
                        path = path,
                        color = graphColor,
                        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Dots
                    dataPoints.forEachIndexed { index, value ->
                        val x = index * spacing
                        val y = h - (value / 100f * h)
                        drawCircle(surfaceColor, radius = 6.dp.toPx(), center = Offset(x, y))
                        drawCircle(graphColor, radius = 4.dp.toPx(), center = Offset(x, y))
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                days.forEach { day ->
                    Text(
                        day.first().toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
