package com.example.fangbianjizhang.ui.asset

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fangbianjizhang.domain.model.Account
import com.example.fangbianjizhang.domain.model.AccountType
import com.example.fangbianjizhang.util.AmountFormatter

@Composable
fun AssetScreen(viewModel: AssetViewModel = hiltViewModel()) {
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
        item { NetAssetHeader(state) }
        item { AccountGroup("资金账户", state.fundAccounts) { FundAccountItem(it) } }
        item { AccountGroup("信用账户", state.creditAccounts) { CreditAccountItem(it) } }
        item { AccountGroup("投资账户", state.investmentAccounts) { FundAccountItem(it) } }
        item { AccountGroup("贷款账户", state.loanAccounts) { LoanAccountItem(it) } }
    }
}

@Composable
private fun NetAssetHeader(state: AssetUiState) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("净资产", style = MaterialTheme.typography.bodySmall)
            Text(
                AmountFormatter.toDisplayWithSymbol(state.netAsset.net),
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("总资产", style = MaterialTheme.typography.bodySmall)
                    Text(AmountFormatter.toDisplayWithSymbol(
                        state.netAsset.fundTotal + state.netAsset.investmentTotal
                    ))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("总负债", style = MaterialTheme.typography.bodySmall)
                    Text(AmountFormatter.toDisplayWithSymbol(
                        state.netAsset.creditUsedTotal + state.netAsset.loanRemainingTotal
                    ))
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
        Column {
            Row(
                Modifier.fillMaxWidth().clickable { expanded = !expanded }.padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(if (expanded) "▼ $title" else "▶ $title")
            }
            if (expanded) {
                accounts.forEach { account -> itemContent(account) }
                if (accounts.isEmpty()) {
                    Text("暂无账户", Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun FundAccountItem(account: Account) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(account.name)
        Text(AmountFormatter.toDisplayWithSymbol(account.balance))
    }
}

@Composable
private fun CreditAccountItem(account: Account) {
    val available = (account.totalLimit ?: 0) - (account.usedAmount ?: 0)
    Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(account.name)
            Text("可用 ${AmountFormatter.toDisplayWithSymbol(available)}/${AmountFormatter.toDisplayWithSymbol(account.totalLimit ?: 0)}")
        }
        if (account.billDay != null && account.repaymentDay != null) {
            Text("账单日${account.billDay}号 还款日${account.repaymentDay}号",
                style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun LoanAccountItem(account: Account) {
    val remaining = (account.totalLoan ?: 0) - (account.alreadyPaid ?: 0)
    Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(account.name)
            Text("剩余 ${AmountFormatter.toDisplayWithSymbol(remaining)}")
        }
        if (account.monthlyPayment != null && account.repaymentDay != null) {
            Text("月供 ${AmountFormatter.toDisplayWithSymbol(account.monthlyPayment)} 每月${account.repaymentDay}号",
                style = MaterialTheme.typography.bodySmall)
        }
    }
}
