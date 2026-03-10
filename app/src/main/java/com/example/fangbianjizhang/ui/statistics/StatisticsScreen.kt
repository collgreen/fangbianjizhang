package com.example.fangbianjizhang.ui.statistics

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fangbianjizhang.data.local.db.dao.CategoryTotal
import com.example.fangbianjizhang.domain.model.BudgetMode
import com.example.fangbianjizhang.ui.theme.ExpenseRed
import com.example.fangbianjizhang.ui.theme.IncomeGreen
import com.example.fangbianjizhang.ui.theme.WarningOrange
import com.example.fangbianjizhang.util.AmountFormatter

@Composable
fun StatisticsScreen(viewModel: StatisticsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val expandedIds by viewModel.expandedCategories.collectAsStateWithLifecycle()

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
                    PieChart(
                        slices = state.categoryStats.map { it.total.toFloat() },
                        labels = state.categoryStats.map { it.category_name }
                    )
                }
            }
        }
        item { CategoryRanking(state, expandedIds, viewModel) }
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
                Text("收入", style = MaterialTheme.typography.labelMedium)
                Text(AmountFormatter.toDisplayWithSymbol(state.totalIncome), color = IncomeGreen, fontWeight = FontWeight.SemiBold)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("支出", style = MaterialTheme.typography.labelMedium)
                Text(AmountFormatter.toDisplayWithSymbol(state.totalExpense), color = ExpenseRed, fontWeight = FontWeight.SemiBold)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("结余", style = MaterialTheme.typography.labelMedium)
                Text(AmountFormatter.toDisplayWithSymbol(balance), fontWeight = FontWeight.SemiBold)
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
private fun CategoryRanking(
    state: StatisticsUiState,
    expandedIds: Set<Long>,
    viewModel: StatisticsViewModel
) {
    val total = state.categoryStats.sumOf { it.total }
    val hasBudget = state.showExpense && state.budgetMode != BudgetMode.NONE && state.totalBudget > 0
    Card(Modifier.fillMaxWidth()) {
        Column(
            Modifier.padding(14.dp).animateContentSize(spring(stiffness = Spring.StiffnessLow))
        ) {
            Text("分类排行", style = MaterialTheme.typography.titleSmall)

            if (hasBudget) {
                Spacer(Modifier.height(8.dp))
                BudgetOverview(totalBudget = state.totalBudget, totalSpent = state.totalExpense)
            }

            Spacer(Modifier.height(10.dp))
            if (state.categoryStats.isEmpty()) {
                Text("暂无数据", style = MaterialTheme.typography.bodySmall)
            }
            state.categoryStats.forEachIndexed { i, cat ->
                val pct = if (total > 0) cat.total * 100f / total else 0f
                val isExpanded = expandedIds.contains(cat.category_id)
                val catBudget = if (state.showExpense && state.budgetMode == BudgetMode.PER_CATEGORY)
                    state.categoryBudgets[cat.category_id] else null
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { viewModel.toggleCategoryExpand(cat.category_id) }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${i + 1}. ${cat.category_name}", style = MaterialTheme.typography.bodyMedium)
                        Icon(
                            if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        "${AmountFormatter.toDisplayWithSymbol(cat.total)}  ${String.format("%.0f%%", pct)}",
                        style = MaterialTheme.typography.titleSmall
                    )
                }
                LinearProgressIndicator(
                    progress = { (pct / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp))
                )
                if (catBudget != null && catBudget > 0) {
                    CategoryBudgetRow(spent = cat.total, budget = catBudget)
                }
                if (isExpanded) {
                    SubCategoryList(cat.category_id, cat.total, viewModel)
                }
            }
        }
    }
}

@Composable
private fun BudgetOverview(totalBudget: Long, totalSpent: Long) {
    val remaining = totalBudget - totalSpent
    val ratio = totalSpent.toFloat() / totalBudget
    val progressColor = when {
        ratio > 1f -> ExpenseRed
        ratio > 0.8f -> WarningOrange
        else -> MaterialTheme.colorScheme.primary
    }
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(12.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("总预算", style = MaterialTheme.typography.labelMedium)
            Text(AmountFormatter.toDisplayWithSymbol(totalBudget), style = MaterialTheme.typography.labelMedium)
        }
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { ratio.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = progressColor,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
        )
        Spacer(Modifier.height(4.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                "已用 ${AmountFormatter.toDisplayWithSymbol(totalSpent)}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                if (remaining >= 0) "剩余 ${AmountFormatter.toDisplayWithSymbol(remaining)}"
                else "超支 ${AmountFormatter.toDisplayWithSymbol(-remaining)}",
                style = MaterialTheme.typography.bodySmall,
                color = if (remaining >= 0) MaterialTheme.colorScheme.primary else ExpenseRed
            )
        }
    }
}

@Composable
private fun CategoryBudgetRow(spent: Long, budget: Long) {
    val remaining = budget - spent
    val ratio = spent.toFloat() / budget
    Row(
        Modifier.fillMaxWidth().padding(start = 4.dp, top = 2.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            "预算 ${AmountFormatter.toDisplayWithSymbol(budget)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            if (remaining >= 0) "剩余 ${AmountFormatter.toDisplayWithSymbol(remaining)}"
            else "超支 ${AmountFormatter.toDisplayWithSymbol(-remaining)}",
            style = MaterialTheme.typography.bodySmall,
            color = when {
                ratio > 1f -> ExpenseRed
                ratio > 0.8f -> WarningOrange
                else -> MaterialTheme.colorScheme.primary
            }
        )
    }
}

@Composable
private fun SubCategoryList(parentId: Long, parentTotal: Long, viewModel: StatisticsViewModel) {
    val subCategories by viewModel.getSubCategoryStats(parentId)
        .collectAsStateWithLifecycle(initialValue = emptyList())

    if (subCategories.isEmpty()) {
        Text(
            "  暂无子分类数据",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 24.dp, top = 4.dp, bottom = 4.dp)
        )
    } else {
        Column(Modifier.padding(start = 24.dp, top = 4.dp, bottom = 4.dp)) {
            subCategories.forEach { sub ->
                val subPct = if (parentTotal > 0) sub.total * 100f / parentTotal else 0f
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "${sub.category_icon} ${sub.category_name}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "${AmountFormatter.toDisplayWithSymbol(sub.total)}  ${String.format("%.0f%%", subPct)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                LinearProgressIndicator(
                    progress = { (subPct / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(1.5.dp)),
                    color = MaterialTheme.colorScheme.tertiary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            }
        }
    }
}
