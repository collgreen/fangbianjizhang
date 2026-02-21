package com.example.fangbianjizhang.domain.usecase.transaction

import com.example.fangbianjizhang.domain.model.Account
import com.example.fangbianjizhang.domain.model.AccountType
import com.example.fangbianjizhang.domain.model.Transaction
import com.example.fangbianjizhang.domain.model.TransactionType
import com.example.fangbianjizhang.domain.repository.AccountRepository
import com.example.fangbianjizhang.domain.repository.TransactionRepository
import javax.inject.Inject

class InsertTransactionUseCase @Inject constructor(
    private val transactionRepo: TransactionRepository,
    private val accountRepo: AccountRepository
) {
    suspend operator fun invoke(transaction: Transaction) {
        transactionRepo.insert(transaction)
        applyBalanceChange(transaction)
    }

    private suspend fun applyBalanceChange(t: Transaction) {
        when (t.type) {
            TransactionType.EXPENSE -> {
                accountRepo.updateBalance(t.accountId, -t.amount)
            }
            TransactionType.INCOME -> {
                accountRepo.updateBalance(t.accountId, t.amount)
            }
            TransactionType.TRANSFER -> {
                accountRepo.updateBalance(t.accountId, -t.amount - t.fee)
                t.targetAccountId?.let { targetId ->
                    accountRepo.updateBalance(targetId, t.amount)
                }
            }
            TransactionType.LOAN_BORROW -> {
                accountRepo.updateBalance(t.accountId, t.amount)
            }
            TransactionType.LOAN_LEND -> {
                accountRepo.updateBalance(t.accountId, -t.amount)
            }
        }
    }
}
