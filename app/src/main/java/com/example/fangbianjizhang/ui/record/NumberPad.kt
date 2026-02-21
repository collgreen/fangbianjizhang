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

@Composable
fun NumberPad(vm: RecordViewModel) {
    val keys = listOf(
        listOf("7", "8", "9", "删"),
        listOf("4", "5", "6", ""),
        listOf("1", "2", "3", ""),
        listOf(".", "0", "", "完")
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
                    val isAction = key == "删" || key == "完"
                    val bg = when {
                        key == "完" -> MaterialTheme.colorScheme.primary
                        isAction -> MaterialTheme.colorScheme.surfaceContainer
                        key.isNotEmpty() -> MaterialTheme.colorScheme.surface
                        else -> MaterialTheme.colorScheme.surfaceContainerLow
                    }
                    val textColor = when {
                        key == "完" -> MaterialTheme.colorScheme.onPrimary
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                    Box(
                        Modifier
                            .weight(1f)
                            .height(52.dp)
                            .padding(vertical = 2.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(bg)
                            .clickable(enabled = key.isNotEmpty()) {
                                when (key) {
                                    "删" -> vm.deleteDigit()
                                    "完" -> vm.save()
                                    else -> vm.appendDigit(key)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = key,
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
