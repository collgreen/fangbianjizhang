package com.example.fangbianjizhang.ui.record

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class PadKey(val label: String, val weight: Float = 1f)

@Composable
fun NumberPad(vm: RecordViewModel) {
    // 5列布局：数字3列 + 运算符列 + 操作列
    val keys = listOf(
        listOf(PadKey("7"), PadKey("8"), PadKey("9"), PadKey("÷"), PadKey("删")),
        listOf(PadKey("4"), PadKey("5"), PadKey("6"), PadKey("×"), PadKey("=")),
        listOf(PadKey("1"), PadKey("2"), PadKey("3"), PadKey("-"), PadKey(".")),
        listOf(PadKey("0", weight = 3f), PadKey("+"), PadKey("完"))
    )

    Column(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        keys.forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                row.forEach { key ->
                    val isAction = key.label in listOf("删", "完", "+", "-", "×", "÷", "=")
                    val bg = when {
                        key.label == "完" -> MaterialTheme.colorScheme.primary
                        isAction -> MaterialTheme.colorScheme.surfaceContainer
                        else -> MaterialTheme.colorScheme.surface
                    }
                    val textColor = when {
                        key.label == "完" -> MaterialTheme.colorScheme.onPrimary
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                    Box(
                        Modifier
                            .weight(key.weight)
                            .height(52.dp)
                            .padding(vertical = 2.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(bg)
                            .clickable {
                                when (key.label) {
                                    "删" -> vm.deleteDigit()
                                    "完" -> vm.save()
                                    "=" -> vm.calculate()
                                    else -> vm.appendDigit(key.label)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = key.label,
                            fontSize = if (isAction) 16.sp else 20.sp,
                            fontWeight = if (isAction) FontWeight.Medium else FontWeight.Normal,
                            color = textColor,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
