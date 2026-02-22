package com.example.fangbianjizhang.ui.statistics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.*

private val pieColors = listOf(
    Color(0xFF2DB84D), Color(0xFFE8453C), Color(0xFFF5A623),
    Color(0xFF5B8DEF), Color(0xFF9B59B6), Color(0xFF1ABC9C),
    Color(0xFFE67E22), Color(0xFFE84393), Color(0xFF00B894),
    Color(0xFF6C5CE7)
)

@Composable
fun PieChart(
    slices: List<Float>,
    labels: List<String> = emptyList(),
    modifier: Modifier = Modifier
) {
    if (slices.isEmpty()) return
    val total = slices.sum()
    if (total <= 0f) return

    var rotationAngle by remember { mutableFloatStateOf(0f) }

    Canvas(
        modifier = modifier
            .size(280.dp)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val pos = change.position
                    val prev = pos - dragAmount
                    val a1 = atan2(prev.y - center.y, prev.x - center.x)
                    val a2 = atan2(pos.y - center.y, pos.x - center.x)
                    rotationAngle += Math.toDegrees((a2 - a1).toDouble()).toFloat()
                }
            }
    ) {
        val strokeWidth = 40.dp.toPx()
        val labelRadius = (size.minDimension / 2f) + 4.dp.toPx()
        val diameter = size.minDimension - strokeWidth - 60.dp.toPx()
        val center = Offset(size.width / 2f, size.height / 2f)
        val topLeft = Offset(
            (size.width - diameter) / 2f,
            (size.height - diameter) / 2f
        )

        var startAngle = -90f + rotationAngle
        val angles = slices.map { it / total * 360f }

        // Draw arcs
        angles.forEachIndexed { i, sweep ->
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

        // Draw labels
        if (labels.isNotEmpty()) {
            val paint = android.graphics.Paint().apply {
                textSize = 10.dp.toPx()
                isAntiAlias = true
                color = android.graphics.Color.DKGRAY
            }
            val arcRadius = diameter / 2f
            startAngle = -90f + rotationAngle

            angles.forEachIndexed { i, sweep ->
                if (i < labels.size && sweep > 5f) {
                    val midAngle = Math.toRadians((startAngle + sweep / 2f).toDouble())
                    val arcX = center.x + arcRadius * cos(midAngle).toFloat()
                    val arcY = center.y + arcRadius * sin(midAngle).toFloat()
                    val labelX = center.x + labelRadius * cos(midAngle).toFloat()
                    val labelY = center.y + labelRadius * sin(midAngle).toFloat()

                    // Leader line
                    drawLine(
                        color = pieColors[i % pieColors.size],
                        start = Offset(arcX, arcY),
                        end = Offset(labelX, labelY),
                        strokeWidth = 1.dp.toPx()
                    )

                    // Label text
                    val text = labels[i]
                    val textWidth = paint.measureText(text)
                    val tx = if (labelX > center.x) labelX + 2.dp.toPx()
                        else labelX - textWidth - 2.dp.toPx()
                    val ty = labelY + paint.textSize / 3f

                    drawContext.canvas.nativeCanvas.drawText(text, tx, ty, paint)
                }
                startAngle += sweep
            }
        }
    }
}
