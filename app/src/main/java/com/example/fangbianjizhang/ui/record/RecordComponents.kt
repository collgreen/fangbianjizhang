package com.example.fangbianjizhang.ui.record

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fangbianjizhang.domain.model.TransactionType

@Composable
fun TypeTabs(current: TransactionType, onSelect: (TransactionType) -> Unit) {
    val types = listOf(
        TransactionType.EXPENSE to "支出",
        TransactionType.INCOME to "收入",
        TransactionType.TRANSFER to "转账",
        TransactionType.LOAN_BORROW to "借贷"
    )
    TabRow(selectedTabIndex = types.indexOfFirst { it.first == current }.coerceAtLeast(0)) {
        types.forEach { (type, label) ->
            Tab(
                selected = current == type,
                onClick = { onSelect(type) },
                text = { Text(label) }
            )
        }
    }
}

@Composable
fun AmountDisplay(amount: String) {
    val displayText = "¥ ${amount.ifEmpty { "0.00" }}"
    val fontSize = when {
        displayText.length > 18 -> 20.sp
        displayText.length > 14 -> 24.sp
        else -> 30.sp // headlineLarge default ~30sp
    }
    Box(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        Text(
            text = displayText,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

@Composable
fun CategoryGrid(state: RecordUiState, vm: RecordViewModel) {
    Column {
        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            modifier = Modifier.heightIn(max = 160.dp),
            contentPadding = PaddingValues(8.dp)
        ) {
            items(state.categories) { cat ->
                val selected = cat.id == state.categoryId
                Box(
                    Modifier
                        .padding(4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (selected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceContainerLow
                        )
                        .clickable { vm.selectCategory(cat.id) }
                        .padding(vertical = 10.dp, horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        cat.name,
                        style = MaterialTheme.typography.labelMedium,
                        textAlign = TextAlign.Center,
                        color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                               else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
        // Sub-categories row
        if (state.subCategories.isNotEmpty()) {
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.subCategories.forEach { sub ->
                    FilterChip(
                        selected = sub.id == state.subCategoryId,
                        onClick = { vm.selectSubCategory(sub.id) },
                        label = { Text(sub.name) }
                    )
                }
            }
        }
    }
}

@Composable
fun AccountSelector(state: RecordUiState, vm: RecordViewModel) {
    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text("账户", style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(4.dp))
        Row(
            Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            state.accounts.forEach { acc ->
                FilterChip(
                    selected = acc.id == state.accountId,
                    onClick = { vm.selectAccount(acc.id) },
                    label = { Text(acc.name) }
                )
            }
        }
        if (state.type == TransactionType.TRANSFER) {
            Spacer(Modifier.height(8.dp))
            Text("目标账户", style = MaterialTheme.typography.labelMedium)
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.accounts.filter { it.id != state.accountId }.forEach { acc ->
                    FilterChip(
                        selected = acc.id == state.targetAccountId,
                        onClick = { vm.selectTargetAccount(acc.id) },
                        label = { Text(acc.name) }
                    )
                }
            }
        }
    }
}

@Composable
fun NoteField(note: String, onNoteChange: (String) -> Unit) {
    OutlinedTextField(
        value = note,
        onValueChange = onNoteChange,
        label = { Text("备注") },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        singleLine = true,
        shape = RoundedCornerShape(10.dp)
    )
}
