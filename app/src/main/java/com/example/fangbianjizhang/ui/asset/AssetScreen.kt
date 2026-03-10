package com.example.fangbianjizhang.ui.asset

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fangbianjizhang.domain.model.Account
import com.example.fangbianjizhang.domain.model.AccountType
import com.example.fangbianjizhang.ui.theme.ExpenseRed
import com.example.fangbianjizhang.util.AmountFormatter

@Composable
fun AssetScreen(viewModel: AssetViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var editingAccount by remember { mutableStateOf<Account?>(null) }

    if (state.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    editingAccount?.let { acc ->
        EditBalanceDialog(
            account = acc,
            onDismiss = { editingAccount = null },
            onSave = { newVal ->
                if (acc.type == AccountType.CREDIT) viewModel.setUsedAmount(acc, newVal)
                else viewModel.setBalance(acc, newVal)
                editingAccount = null
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { NetAssetHeader(state) }
        item { AccountGroup("资金账户", state.fundAccounts) { FundAccountItem(it) { editingAccount = it } } }
        item { AccountGroup("信用账户", state.creditAccounts) { CreditAccountItem(it) { editingAccount = it } } }
        item { AccountGroup("投资账户", state.investmentAccounts) { FundAccountItem(it) { editingAccount = it } } }
        item { AccountGroup("贷款账户", state.loanAccounts) { LoanAccountItem(it) { editingAccount = it } } }
    }
}

@Composable
private fun NetAssetHeader(state: AssetUiState) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("净资产", style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                AmountFormatter.toDisplayWithSymbol(state.netAsset.net),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("总资产", style = MaterialTheme.typography.bodySmall)
                    Text(
                        AmountFormatter.toDisplayWithSymbol(state.netAsset.fundTotal + state.netAsset.investmentTotal),
                        style = MaterialTheme.typography.titleSmall
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("总负债", style = MaterialTheme.typography.bodySmall)
                    Text(
                        AmountFormatter.toDisplayWithSymbol(state.netAsset.creditUsedTotal + state.netAsset.loanRemainingTotal),
                        style = MaterialTheme.typography.titleSmall,
                        color = ExpenseRed
                    )
                }
            }
        }
    }
}

@Composable
private fun <T : Account> AccountGroup(
    title: String,
    accounts: List<T>,
    itemContent: @Composable (T) -> Unit
) {
    var expanded by remember { mutableStateOf(true) }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.animateContentSize(spring(stiffness = Spring.StiffnessMediumLow))) {
            Row(
                Modifier.fillMaxWidth().clickable { expanded = !expanded }.padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(if (expanded) "▼" else "▶", style = MaterialTheme.typography.bodySmall)
            }
            if (expanded) {
                if (accounts.isEmpty()) {
                    Text("暂无账户", Modifier.padding(14.dp), style = MaterialTheme.typography.bodySmall)
                } else {
                    accounts.forEach { itemContent(it) }
                }
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun FundAccountItem(account: Account, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(account.icon, Modifier.padding(end = 8.dp))
            Text(account.name, style = MaterialTheme.typography.bodyLarge)
        }
        Text(AmountFormatter.toDisplayWithSymbol(account.balance), style = MaterialTheme.typography.titleSmall)
    }
}

@Composable
private fun CreditAccountItem(account: Account, onClick: () -> Unit) {
    val used = account.usedAmount ?: 0
    val installment = account.installmentAmount ?: 0
    val totalDebt = used + installment
    val available = (account.totalLimit ?: 0) - totalDebt
    Column(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(account.icon, Modifier.padding(end = 8.dp))
                Text(account.name, style = MaterialTheme.typography.bodyLarge)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("待还 ${AmountFormatter.toDisplayWithSymbol(totalDebt)}", style = MaterialTheme.typography.titleSmall, color = ExpenseRed)
                Text("可用 ${AmountFormatter.toDisplayWithSymbol(available)}", style = MaterialTheme.typography.bodySmall)
            }
        }
        Row(
            Modifier.fillMaxWidth().padding(start = 28.dp, top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "次月还款 ${AmountFormatter.toDisplayWithSymbol(used)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (installment > 0) {
                Text(
                    "分期 ${AmountFormatter.toDisplayWithSymbol(installment)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (account.billDay != null && account.repaymentDay != null) {
            Text(
                "账单日${account.billDay}号  还款日${account.repaymentDay}号",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 28.dp, top = 2.dp)
            )
        }
    }
}

@Composable
private fun LoanAccountItem(account: Account, onClick: () -> Unit) {
    val remaining = (account.totalLoan ?: 0) - (account.alreadyPaid ?: 0)
    Column(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(account.icon, Modifier.padding(end = 8.dp))
                Text(account.name, style = MaterialTheme.typography.bodyLarge)
            }
            Text("剩余 ${AmountFormatter.toDisplayWithSymbol(remaining)}", style = MaterialTheme.typography.titleSmall, color = ExpenseRed)
        }
        if (account.monthlyPayment != null && account.repaymentDay != null) {
            Text(
                "月供 ${AmountFormatter.toDisplayWithSymbol(account.monthlyPayment)}  每月${account.repaymentDay}号",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 28.dp, top = 2.dp)
            )
        }
    }
}

@Composable
private fun EditBalanceDialog(
    account: Account,
    onDismiss: () -> Unit,
    onSave: (Long) -> Unit
) {
    val isCredit = account.type == AccountType.CREDIT
    val label = if (isCredit) "已用额度" else "余额"
    val initVal = if (isCredit) account.usedAmount ?: 0 else account.balance
    var text by remember { mutableStateOf(AmountFormatter.toDisplay(initVal)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("修改${account.name}的$label") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text(label) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = {
                val v = AmountFormatter.toLong(text)
                if (v >= 0) onSave(v)
            }) { Text("保存") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}
