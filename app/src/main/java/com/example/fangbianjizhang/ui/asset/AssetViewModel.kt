package com.example.fangbianjizhang.ui.asset

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fangbianjizhang.domain.model.Account
import com.example.fangbianjizhang.domain.model.AccountType
import com.example.fangbianjizhang.domain.repository.AccountRepository
import com.example.fangbianjizhang.domain.repository.NetAsset
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class AssetUiState(
    val netAsset: NetAsset = NetAsset(0, 0, 0, 0),
    val fundAccounts: List<Account> = emptyList(),
    val creditAccounts: List<Account> = emptyList(),
    val investmentAccounts: List<Account> = emptyList(),
    val loanAccounts: List<Account> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class AssetViewModel @Inject constructor(
    private val accountRepo: AccountRepository
) : ViewModel() {

    val uiState: StateFlow<AssetUiState> = combine(
        accountRepo.getNetAsset(),
        accountRepo.getByType(AccountType.FUND),
        accountRepo.getByType(AccountType.CREDIT),
        accountRepo.getByType(AccountType.INVESTMENT),
        accountRepo.getByType(AccountType.LOAN)
    ) { net, fund, credit, invest, loan ->
        AssetUiState(net, fund, credit, invest, loan, isLoading = false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AssetUiState())
}
