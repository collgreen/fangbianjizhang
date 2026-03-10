package com.example.fangbianjizhang.domain.usecase.transaction

import com.example.fangbianjizhang.domain.model.AccountType
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
        val account = accountRepo.getById(t.accountId).first()
        when (t.type) {
            TransactionType.EXPENSE -> {
                when (account?.type) {
                    AccountType.CREDIT -> accountRepo.updateUsedAmount(t.accountId, -t.amount)
                    else -> accountRepo.updateBalance(t.accountId, t.amount)
                }
            }
            TransactionType.INCOME -> {
                when (account?.type) {
                    AccountType.CREDIT -> accountRepo.updateUsedAmount(t.accountId, t.amount)
                    AccountType.LOAN -> accountRepo.updateAlreadyPaid(t.accountId, -t.amount)
                    else -> accountRepo.updateBalance(t.accountId, -t.amount)
                }
            }
            TransactionType.TRANSFER -> {
                accountRepo.updateBalance(t.accountId, t.amount + t.fee)
                t.targetAccountId?.let { targetId ->
                    val targetAccount = accountRepo.getById(targetId).first()
                    when (targetAccount?.type) {
                        AccountType.CREDIT -> accountRepo.updateUsedAmount(targetId, t.amount)
                        AccountType.LOAN -> accountRepo.updateAlreadyPaid(targetId, -t.amount)
                        else -> accountRepo.updateBalance(targetId, -t.amount)
                    }
                }
            }
            TransactionType.LOAN_BORROW -> accountRepo.updateBalance(t.accountId, -t.amount)
            TransactionType.LOAN_LEND -> accountRepo.updateBalance(t.accountId, t.amount)
        }
        transactionRepo.softDelete(id)
    }
}
