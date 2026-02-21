package com.example.fangbianjizhang.domain.usecase.transaction

import com.example.fangbianjizhang.domain.model.TransactionType
import com.example.fangbianjizhang.domain.repository.AccountRepository
import com.example.fangbianjizhang.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class DeleteTransactionUseCase @Inject constructor(
    private val transactionRepo: TransactionRepository,
    private val accountRepo: AccountRepository
) {
    suspend operator fun invoke(id: Long) {
        val t = transactionRepo.getById(id).first() ?: return
        when (t.type) {
            TransactionType.EXPENSE -> accountRepo.updateBalance(t.accountId, t.amount)
            TransactionType.INCOME -> accountRepo.updateBalance(t.accountId, -t.amount)
            TransactionType.TRANSFER -> {
                accountRepo.updateBalance(t.accountId, t.amount + t.fee)
                t.targetAccountId?.let { accountRepo.updateBalance(it, -t.amount) }
            }
            TransactionType.LOAN_BORROW -> accountRepo.updateBalance(t.accountId, -t.amount)
            TransactionType.LOAN_LEND -> accountRepo.updateBalance(t.accountId, t.amount)
        }
        transactionRepo.softDelete(id)
    }
}
