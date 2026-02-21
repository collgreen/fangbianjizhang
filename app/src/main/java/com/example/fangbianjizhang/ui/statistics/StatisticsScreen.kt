package com.example.fangbianjizhang.ui.statistics

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
import com.example.fangbianjizhang.ui.theme.ExpenseRed
import com.example.fangbianjizhang.ui.theme.IncomeGreen
import com.example.fangbianjizhang.util.AmountFormatter

@Composable
fun StatisticsScreen(viewModel: StatisticsViewModel = hiltViewModel()) {
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
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { MonthSelector(state, viewModel) }
        item { SummaryRow(state) }
        item { TypeToggle(state.showExpense, viewModel::toggleType) }
        if (state.categoryStats.isNotEmpty()) {
            item {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    PieChart(slices = state.categoryStats.map { it.total.toFloat() })
                }
            }
        }
        item { CategoryRanking(state) }
    }
}

@Composable
private fun MonthSelector(state: StatisticsUiState, vm: StatisticsViewModel) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = { vm.prevMonth() }) { Text("◀") }
        Text("${state.year}年${state.month}月", style = MaterialTheme.typography.headlineMedium)
        TextButton(onClick = { vm.nextMonth() }) { Text("▶") }
    }
}

@Composable
private fun SummaryRow(state: StatisticsUiState) {
    val balance = state.totalIncome - state.totalExpense
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("收入", style = MaterialTheme.typography.bodySmall)
                Text(AmountFormatter.toDisplayWithSymbol(state.totalIncome), color = IncomeGreen)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("支出", style = MaterialTheme.typography.bodySmall)
                Text(AmountFormatter.toDisplayWithSymbol(state.totalExpense), color = ExpenseRed)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("结余", style = MaterialTheme.typography.bodySmall)
                Text(AmountFormatter.toDisplayWithSymbol(balance))
            }
        }
    }
}

@Composable
private fun TypeToggle(showExpense: Boolean, onToggle: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        FilterChip(selected = showExpense, onClick = onToggle, label = { Text("支出") })
        Spacer(Modifier.width(8.dp))
        FilterChip(selected = !showExpense, onClick = onToggle, label = { Text("收入") })
    }
}

@Composable
private fun CategoryRanking(state: StatisticsUiState) {
    val total = state.categoryStats.sumOf { it.total }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text("分类排行", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(8.dp))
            if (state.categoryStats.isEmpty()) {
                Text("暂无数据", style = MaterialTheme.typography.bodySmall)
            }
            state.categoryStats.forEachIndexed { i, cat ->
                val pct = if (total > 0) cat.total * 100f / total else 0f
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${i + 1}. ${cat.category_name}")
                    Text("${AmountFormatter.toDisplayWithSymbol(cat.total)}  ${String.format("%.0f%%", pct)}")
                }
                LinearProgressIndicator(
                    progress = { (pct / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(4.dp)
                )
            }
        }
    }
}
