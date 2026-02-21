package com.example.fangbianjizhang.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fangbianjizhang.domain.model.BudgetMode
import com.example.fangbianjizhang.domain.model.TransactionType
import com.example.fangbianjizhang.domain.repository.DailySummary
import com.example.fangbianjizhang.ui.theme.ExpenseRed
import com.example.fangbianjizhang.ui.theme.IncomeGreen
import com.example.fangbianjizhang.ui.theme.WarningOrange
import com.example.fangbianjizhang.util.AmountFormatter
import com.example.fangbianjizhang.util.DateUtils

@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { BudgetCard(state.budgetStatus) }

        state.dailySummaries.forEach { summary ->
            item { DayHeader(summary) }
            items(summary.transactions) { tx ->
                TransactionItem(tx)
            }
        }

        if (state.dailySummaries.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                    Text("暂无记录，点击 + 开始记账", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
private fun BudgetCard(budget: BudgetStatus) {
    if (budget.mode == BudgetMode.NONE) {
        Card(Modifier.fillMaxWidth()) {
            Box(Modifier.padding(16.dp)) {
                Text("点击设置预算", style = MaterialTheme.typography.bodyLarge)
            }
        }
        return
    }

    val ratio = if (budget.budget > 0) budget.spent.toFloat() / budget.budget else 0f
    val progressColor = when {
        ratio > 1f -> ExpenseRed
        ratio > 0.8f -> WarningOrange
        else -> MaterialTheme.colorScheme.primary
    }

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("本月预算", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(4.dp))
            Text(
                "已用 ${AmountFormatter.toDisplayWithSymbol(budget.spent)} / ${AmountFormatter.toDisplayWithSymbol(budget.budget)}",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { ratio.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = progressColor
            )
            Spacer(Modifier.height(4.dp))
            val remaining = budget.budget - budget.spent
            Text(
                "剩余 ${AmountFormatter.toDisplayWithSymbol(remaining)}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun DayHeader(summary: DailySummary) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(DateUtils.formatDay(summary.dayTimestamp), style = MaterialTheme.typography.bodyLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("支出 ${AmountFormatter.toDisplayWithSymbol(summary.totalExpense)}", color = ExpenseRed)
            Text("收入 ${AmountFormatter.toDisplayWithSymbol(summary.totalIncome)}", color = IncomeGreen)
        }
    }
}

@Composable
private fun TransactionItem(tx: com.example.fangbianjizhang.domain.model.Transaction) {
    val isExpense = tx.type == TransactionType.EXPENSE
    val color = if (isExpense) ExpenseRed else IncomeGreen
    val sign = if (isExpense) "-" else "+"

    Row(
        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(tx.type.name, style = MaterialTheme.typography.bodyLarge)
            if (!tx.note.isNullOrBlank()) {
                Text(tx.note, style = MaterialTheme.typography.bodySmall)
            }
        }
        Text("$sign${AmountFormatter.toDisplayWithSymbol(tx.amount)}", color = color)
    }
}
