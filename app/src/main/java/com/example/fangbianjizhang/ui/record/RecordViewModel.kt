package com.example.fangbianjizhang.ui.record

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fangbianjizhang.domain.model.Account
import com.example.fangbianjizhang.domain.model.Category
import com.example.fangbianjizhang.domain.model.CategoryType
import com.example.fangbianjizhang.domain.model.Transaction
import com.example.fangbianjizhang.domain.model.TransactionType
import com.example.fangbianjizhang.domain.repository.AccountRepository
import com.example.fangbianjizhang.domain.repository.CategoryRepository
import com.example.fangbianjizhang.domain.repository.TransactionRepository
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
    val saved: Boolean = false,
    val isEditMode: Boolean = false
)

@HiltViewModel
class RecordViewModel @Inject constructor(
    private val insertTransaction: InsertTransactionUseCase,
    private val categoryRepo: CategoryRepository,
    private val accountRepo: AccountRepository,
    private val transactionRepo: TransactionRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(RecordUiState())
    val uiState: StateFlow<RecordUiState> = _state.asStateFlow()
    private var childrenJob: Job? = null
    private var categoriesJob: Job? = null

    private val editTransactionId: Long? = savedStateHandle.get<Long>("transactionId")

    init {
        loadAccounts()
        if (editTransactionId != null && editTransactionId > 0) {
            loadTransaction(editTransactionId)
        } else {
            loadCategories()
        }
    }

    private fun loadTransaction(id: Long) {
        viewModelScope.launch {
            transactionRepo.getById(id).first()?.let { tx ->
                _state.value = _state.value.copy(
                    isEditMode = true,
                    type = tx.type,
                    amount = AmountFormatter.toDisplay(tx.amount),
                    categoryId = tx.categoryId,
                    accountId = tx.accountId,
                    targetAccountId = tx.targetAccountId,
                    note = tx.note ?: "",
                    date = tx.transactionDate,
                    counterparty = tx.counterparty ?: ""
                )
                loadCategories()
                // Load sub-categories if category has a parent
                tx.categoryId?.let { catId ->
                    categoryRepo.getChildren(catId).first().let { children ->
                        if (children.isNotEmpty()) {
                            // catId is a parent, load its children
                            _state.value = _state.value.copy(subCategories = children)
                        } else {
                            // catId might be a child, find its parent
                            val allCats = _state.value.categories
                            val parentCat = allCats.find { it.id == catId }
                            if (parentCat == null) {
                                // catId is a sub-category, find parent
                                val catType = when (tx.type) {
                                    TransactionType.EXPENSE -> CategoryType.EXPENSE
                                    TransactionType.INCOME -> CategoryType.INCOME
                                    else -> null
                                }
                                if (catType != null) {
                                    categoryRepo.getByType(catType).first().let { allByType ->
                                        val subCat = allByType.find { it.id == catId }
                                        if (subCat?.parentId != null) {
                                            _state.value = _state.value.copy(
                                                categoryId = subCat.parentId,
                                                subCategoryId = catId
                                            )
                                            categoryRepo.getChildren(subCat.parentId).first().let { subs ->
                                                _state.value = _state.value.copy(subCategories = subs)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
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
            if (s.isEditMode && editTransactionId != null) {
                transactionRepo.update(
                    Transaction(
                        id = editTransactionId,
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
            } else {
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
            }
            _state.value = _state.value.copy(isSaving = false, saved = true)
        }
    }
}
