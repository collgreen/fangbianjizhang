package com.example.fangbianjizhang.ui.statistics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

private val pieColors = listOf(
    Color(0xFF1976D2), Color(0xFF4CAF50), Color(0xFFF44336),
    Color(0xFFFF9800), Color(0xFF9C27B0), Color(0xFF00BCD4),
    Color(0xFF795548), Color(0xFFE91E63), Color(0xFF3F51B5),
    Color(0xFF009688)
)

@Composable
fun PieChart(
    slices: List<Float>,
    modifier: Modifier = Modifier
) {
    if (slices.isEmpty()) return
    val total = slices.sum()
    if (total <= 0f) return

    Canvas(modifier = modifier.size(200.dp)) {
        val strokeWidth = 40.dp.toPx()
        val diameter = size.minDimension - strokeWidth
        val topLeft = Offset(
            (size.width - diameter) / 2f,
            (size.height - diameter) / 2f
        )
        var startAngle = -90f
        slices.forEachIndexed { i, value ->
            val sweep = value / total * 360f
            drawArc(
                color = pieColors[i % pieColors.size],
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = topLeft,
                size = Size(diameter, diameter),
                style = Stroke(width = strokeWidth)
            )
            startAngle += sweep
        }
    }
}
