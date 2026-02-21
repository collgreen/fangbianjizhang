package com.example.fangbianjizhang.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fangbianjizhang.domain.model.Account
import com.example.fangbianjizhang.domain.model.Frequency
import com.example.fangbianjizhang.domain.model.RecurringTemplate
import com.example.fangbianjizhang.domain.repository.AccountRepository
import com.example.fangbianjizhang.domain.repository.RecurringRepository
import com.example.fangbianjizhang.util.AmountFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecurringManageUiState(
    val templates: List<RecurringTemplate> = emptyList(),
    val accounts: List<Account> = emptyList()
)

@HiltViewModel
class RecurringManageViewModel @Inject constructor(
    private val recurringRepo: RecurringRepository,
    private val accountRepo: AccountRepository
) : ViewModel() {

    val uiState: StateFlow<RecurringManageUiState> = combine(
        recurringRepo.getAll(),
        accountRepo.getAllActive()
    ) { templates, accounts ->
        RecurringManageUiState(templates, accounts)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RecurringManageUiState())

    fun toggleEnabled(id: Long, enabled: Boolean) {
        viewModelScope.launch { recurringRepo.setEnabled(id, enabled) }
    }

    fun delete(id: Long) {
        viewModelScope.launch { recurringRepo.softDelete(id) }
    }

    fun add(
        name: String,
        amountYuan: String,
        frequency: Frequency,
        sourceAccountId: Long,
        targetAccountId: Long,
        dayOfMonth: Int?
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            recurringRepo.insert(
                RecurringTemplate(
                    name = name,
                    amount = AmountFormatter.toLong(amountYuan),
                    frequency = frequency,
                    dayOfMonth = if (frequency == Frequency.MONTHLY) dayOfMonth else null,
                    dayOfWeek = if (frequency == Frequency.WEEKLY) dayOfMonth else null,
                    intervalDays = if (frequency == Frequency.CUSTOM_DAYS) dayOfMonth else null,
                    sourceAccountId = sourceAccountId,
                    targetAccountId = targetAccountId,
                    nextDueDate = now
                )
            )
        }
    }
}
