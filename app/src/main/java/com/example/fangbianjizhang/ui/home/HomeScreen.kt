package com.example.fangbianjizhang.ui.home

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

private val mottoList = listOf(
    "记账的人运气都不会太差，因为钱知道你在乎它。",
    "你和有钱人之间，可能就差一个记账的习惯。",
    "每一笔记录都是在给未来的自己攒底气。",
    "钱不是省出来的，但一定是记出来的。",
    "今天记下的每一分，都是明天少叹的每一口气。",
    "月光族的反义词不是有钱人，是记账的人。",
    "你不理财，财不理你。你不记账，账不饶你。",
    "坚持记账一个月，你会发现钱都花哪了。坚持三个月，钱就不敢乱跑了。",
    "别人刷短视频上瘾，你记账上瘾，段位不一样。",
    "富人的秘密：不是赚得多，而是每一笔都心里有数。",
    "打工人最大的安全感，不是存款六位数，而是知道每一位数怎么来的。",
    "记账不会让你变有钱，但不记账一定让你变穷得稀里糊涂。",
    "你以为记的是账，其实记的是对生活的掌控感。",
    "能坚持记账的人，自律程度已经打败了90%的人。",
    "今日份记账已完成，离财务自由又近了0.01%。",
    "有人靠运气暴富，有人靠记账慢富，后者更持久。",
    "钱包瘦了不可怕，可怕的是瘦了都不知道。",
    "记账就像健身，刚开始痛苦，坚持下来真香。",
    "每天记账三分钟，少走弯路一整年。",
    "你的账本，就是你最真实的人生纪录片。"
)

@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val motto = remember { mottoList.random() }

    if (state.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { MottoCard(motto) }
        item { BudgetCard(state.budgetStatus) }

        if (state.repaymentReminders.isNotEmpty()) {
            item { RepaymentSection(state.repaymentReminders) }
        }

        state.dailySummaries.forEach { summary ->
            item { DayHeader(summary) }
            items(summary.transactions) { tx -> TransactionItem(tx) }
        }

        if (state.dailySummaries.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                    Text(
                        "暂无记录，点击 + 开始记账",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun MottoCard(motto: String) {
    Text(
        text = motto,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

@Composable
private fun BudgetCard(budget: BudgetStatus) {
    if (budget.mode == BudgetMode.NONE) {
        Card(
            Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Box(Modifier.padding(16.dp)) {
                Text("点击设置预算", color = MaterialTheme.colorScheme.onPrimaryContainer)
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
        Column(Modifier.padding(16.dp).animateContentSize(spring(stiffness = Spring.StiffnessLow))) {
            Text("本月预算", style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(6.dp))
            Text(
                "已用 ${AmountFormatter.toDisplayWithSymbol(budget.spent)} / ${AmountFormatter.toDisplayWithSymbol(budget.budget)}",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { ratio.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
            Spacer(Modifier.height(6.dp))
            val remaining = budget.budget - budget.spent
            Text("剩余 ${AmountFormatter.toDisplayWithSymbol(remaining)}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun DayHeader(summary: DailySummary) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(DateUtils.formatDay(summary.dayTimestamp), style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("支 ${AmountFormatter.toDisplayWithSymbol(summary.totalExpense)}", color = ExpenseRed, style = MaterialTheme.typography.labelMedium)
            Text("收 ${AmountFormatter.toDisplayWithSymbol(summary.totalIncome)}", color = IncomeGreen, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun RepaymentSection(reminders: List<RepaymentReminder>) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("还款提醒", style = MaterialTheme.typography.titleSmall)
            reminders.forEach { r ->
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(r.accountName, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "每月${r.repaymentDay}日 · ${if (r.isCredit) "信用" else "贷款"}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Text(
                        "${r.daysLeft}天后",
                        color = if (r.daysLeft <= 3) ExpenseRed else MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun TransactionItem(tx: com.example.fangbianjizhang.domain.model.Transaction) {
    val isExpense = tx.type == TransactionType.EXPENSE
    val color = if (isExpense) ExpenseRed else IncomeGreen
    val sign = if (isExpense) "-" else "+"

    Row(
        Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(tx.type.name, style = MaterialTheme.typography.bodyLarge)
            if (!tx.note.isNullOrBlank()) {
                Text(tx.note, style = MaterialTheme.typography.bodySmall)
            }
        }
        Text(
            "$sign${AmountFormatter.toDisplayWithSymbol(tx.amount)}",
            color = color,
            style = MaterialTheme.typography.titleSmall
        )
    }
}
