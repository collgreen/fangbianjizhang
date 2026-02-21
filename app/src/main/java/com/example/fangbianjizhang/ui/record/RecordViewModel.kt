package com.example.fangbianjizhang.ui.record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fangbianjizhang.domain.model.Account
import com.example.fangbianjizhang.domain.model.Category
import com.example.fangbianjizhang.domain.model.CategoryType
import com.example.fangbianjizhang.domain.model.Transaction
import com.example.fangbianjizhang.domain.model.TransactionType
import com.example.fangbianjizhang.domain.repository.AccountRepository
import com.example.fangbianjizhang.domain.repository.CategoryRepository
import com.example.fangbianjizhang.domain.usecase.transaction.InsertTransactionUseCase
import com.example.fangbianjizhang.util.AmountFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecordUiState(
    val type: TransactionType = TransactionType.EXPENSE,
    val amount: String = "",
    val categoryId: Long? = null,
    val subCategoryId: Long? = null,
    val accountId: Long? = null,
    val targetAccountId: Long? = null,
    val note: String = "",
    val date: Long = System.currentTimeMillis(),
    val counterparty: String = "",
    val categories: List<Category> = emptyList(),
    val subCategories: List<Category> = emptyList(),
    val accounts: List<Account> = emptyList(),
    val isSaving: Boolean = false,
    val saved: Boolean = false
)

@HiltViewModel
class RecordViewModel @Inject constructor(
    private val insertTransaction: InsertTransactionUseCase,
    private val categoryRepo: CategoryRepository,
    private val accountRepo: AccountRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RecordUiState())
    val uiState: StateFlow<RecordUiState> = _state.asStateFlow()
    private var childrenJob: Job? = null
    private var categoriesJob: Job? = null

    init {
        loadAccounts()
        loadCategories()
    }

    private fun loadAccounts() {
        viewModelScope.launch {
            accountRepo.getAllActive().collect { accounts ->
                _state.value = _state.value.copy(
                    accounts = accounts,
                    accountId = _state.value.accountId ?: accounts.firstOrNull()?.id
                )
            }
        }
    }

    private fun loadCategories() {
        categoriesJob?.cancel()
        categoriesJob = viewModelScope.launch {
            val catType = when (_state.value.type) {
                TransactionType.EXPENSE -> CategoryType.EXPENSE
                TransactionType.INCOME -> CategoryType.INCOME
                else -> return@launch
            }
            categoryRepo.getTopLevelByType(catType).collect { cats ->
                _state.value = _state.value.copy(categories = cats)
            }
        }
    }

    fun setType(type: TransactionType) {
        _state.value = _state.value.copy(
            type = type, categoryId = null,
            subCategoryId = null, subCategories = emptyList()
        )
        loadCategories()
    }

    fun appendDigit(digit: String) {
        val current = _state.value.amount
        if (digit == "." && current.contains(".")) return
        if (current.contains(".") && current.substringAfter(".").length >= 2) return
        _state.value = _state.value.copy(amount = current + digit)
    }

    fun deleteDigit() {
        val current = _state.value.amount
        if (current.isNotEmpty()) {
            _state.value = _state.value.copy(amount = current.dropLast(1))
        }
    }

    fun selectCategory(id: Long) {
        _state.value = _state.value.copy(categoryId = id, subCategoryId = null)
        childrenJob?.cancel()
        childrenJob = viewModelScope.launch {
            categoryRepo.getChildren(id).collect { children ->
                _state.value = _state.value.copy(subCategories = children)
            }
        }
    }

    fun selectSubCategory(id: Long) {
        _state.value = _state.value.copy(subCategoryId = id)
    }

    fun selectAccount(id: Long) {
        _state.value = _state.value.copy(accountId = id)
    }

    fun selectTargetAccount(id: Long) {
        _state.value = _state.value.copy(targetAccountId = id)
    }

    fun setNote(note: String) {
        _state.value = _state.value.copy(note = note)
    }

    fun setCounterparty(name: String) {
        _state.value = _state.value.copy(counterparty = name)
    }

    fun save() {
        val s = _state.value
        if (s.amount.isBlank() || s.accountId == null) return

        _state.value = s.copy(isSaving = true)
        viewModelScope.launch {
            val amountFen = AmountFormatter.toLong(s.amount)
            val catId = s.subCategoryId ?: s.categoryId
            insertTransaction(
                Transaction(
                    type = s.type,
                    amount = amountFen,
                    categoryId = catId,
                    accountId = s.accountId,
                    targetAccountId = s.targetAccountId,
                    note = s.note.ifBlank { null },
                    counterparty = s.counterparty.ifBlank { null },
                    transactionDate = s.date
                )
            )
            _state.value = _state.value.copy(isSaving = false, saved = true)
        }
    }
}
