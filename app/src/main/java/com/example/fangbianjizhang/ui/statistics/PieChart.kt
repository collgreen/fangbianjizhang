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
    Color(0xFF2DB84D), Color(0xFFE8453C), Color(0xFFF5A623),
    Color(0xFF5B8DEF), Color(0xFF9B59B6), Color(0xFF1ABC9C),
    Color(0xFFE67E22), Color(0xFFE84393), Color(0xFF00B894),
    Color(0xFF6C5CE7)
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
