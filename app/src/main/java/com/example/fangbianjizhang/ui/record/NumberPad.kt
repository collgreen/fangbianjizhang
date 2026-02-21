package com.example.fangbianjizhang.ui.record

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    Column(Modifier.fillMaxWidth()) {
        keys.forEach { row ->
            Row(Modifier.fillMaxWidth()) {
                row.forEach { key ->
                    Box(
                        Modifier
                            .weight(1f)
                            .height(56.dp)
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
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
