package com.example.fangbianjizhang.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fangbianjizhang.domain.model.Account
import com.example.fangbianjizhang.domain.model.AccountSubType
import com.example.fangbianjizhang.domain.model.AccountType
import com.example.fangbianjizhang.domain.repository.AccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AccountManageUiState(
    val accounts: List<Account> = emptyList()
)

@HiltViewModel
class AccountManageViewModel @Inject constructor(
    private val accountRepo: AccountRepository
) : ViewModel() {

    val uiState: StateFlow<AccountManageUiState> = accountRepo.getAllActive()
        .map { AccountManageUiState(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AccountManageUiState())

    fun addAccount(
        name: String, type: AccountType, subType: AccountSubType,
        balance: Long, totalLimit: Long?, totalLoan: Long?,
        monthlyPayment: Long?, installmentAmount: Long?, billDay: Int?, repaymentDay: Int?
    ) {
        viewModelScope.launch {
            accountRepo.insert(Account(
                name = name, type = type, subType = subType,
                balance = balance, totalLimit = totalLimit,
                usedAmount = 0, installmentAmount = installmentAmount,
                totalLoan = totalLoan,
                alreadyPaid = 0, monthlyPayment = monthlyPayment,
                billDay = billDay, repaymentDay = repaymentDay,
                icon = subType.defaultIcon()
            ))
        }
    }

    fun updateAccount(account: Account) {
        viewModelScope.launch { accountRepo.update(account) }
    }

    fun deleteAccount(id: Long) {
        viewModelScope.launch { accountRepo.softDelete(id) }
    }
}

fun AccountSubType.defaultIcon() = when (this) {
    AccountSubType.BANK_CARD -> "💳"
    AccountSubType.WECHAT -> "💬"
    AccountSubType.ALIPAY -> "💙"
    AccountSubType.CREDIT_CARD -> "💳"
    AccountSubType.HUABEI -> "🌸"
    AccountSubType.BAITIAO -> "🏷️"
    AccountSubType.PENSION -> "🏦"
    AccountSubType.FIXED_DEPOSIT -> "🔒"
    AccountSubType.STOCK -> "📈"
    AccountSubType.FUTURES -> "📊"
    AccountSubType.MORTGAGE -> "🏠"
    AccountSubType.CAR_LOAN -> "🚗"
    AccountSubType.CONSUMER_LOAN -> "💰"
}
