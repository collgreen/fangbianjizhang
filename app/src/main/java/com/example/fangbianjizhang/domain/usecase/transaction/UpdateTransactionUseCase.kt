package com.example.fangbianjizhang.domain.usecase.transaction

import com.example.fangbianjizhang.domain.model.AccountType
import com.example.fangbianjizhang.domain.model.Transaction
import com.example.fangbianjizhang.domain.model.TransactionType
import com.example.fangbianjizhang.domain.repository.AccountRepository
import com.example.fangbianjizhang.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class UpdateTransactionUseCase @Inject constructor(
    private val transactionRepo: TransactionRepository,
    private val accountRepo: AccountRepository
) {
    suspend operator fun invoke(newTransaction: Transaction) {
        val old = transactionRepo.getById(newTransaction.id).first() ?: return
        reverseBalanceChange(old)
        transactionRepo.update(newTransaction)
        applyBalanceChange(newTransaction)
    }

    private suspend fun reverseBalanceChange(t: Transaction) {
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
    }

    private suspend fun applyBalanceChange(t: Transaction) {
        val account = accountRepo.getById(t.accountId).first()
        when (t.type) {
            TransactionType.EXPENSE -> {
                when (account?.type) {
                    AccountType.CREDIT -> accountRepo.updateUsedAmount(t.accountId, t.amount)
                    else -> accountRepo.updateBalance(t.accountId, -t.amount)
                }
            }
            TransactionType.INCOME -> {
                when (account?.type) {
                    AccountType.CREDIT -> accountRepo.updateUsedAmount(t.accountId, -t.amount)
                    AccountType.LOAN -> accountRepo.updateAlreadyPaid(t.accountId, t.amount)
                    else -> accountRepo.updateBalance(t.accountId, t.amount)
                }
            }
            TransactionType.TRANSFER -> {
                accountRepo.updateBalance(t.accountId, -t.amount - t.fee)
                t.targetAccountId?.let { targetId ->
                    val targetAccount = accountRepo.getById(targetId).first()
                    when (targetAccount?.type) {
                        AccountType.CREDIT -> {
                            val used = targetAccount.usedAmount ?: 0
                            val installment = targetAccount.installmentAmount ?: 0
                            if (used >= t.amount) {
                                accountRepo.updateUsedAmount(targetId, -t.amount)
                            } else {
                                if (used > 0) accountRepo.updateUsedAmount(targetId, -used)
                                val remaining = t.amount - used
                                val installDeduct = remaining.coerceAtMost(installment)
                                if (installDeduct > 0) accountRepo.updateInstallmentAmount(targetId, -installDeduct)
                            }
                        }
                        AccountType.LOAN -> accountRepo.updateAlreadyPaid(targetId, t.amount)
                        else -> accountRepo.updateBalance(targetId, t.amount)
                    }
                }
            }
            TransactionType.LOAN_BORROW -> accountRepo.updateBalance(t.accountId, t.amount)
            TransactionType.LOAN_LEND -> accountRepo.updateBalance(t.accountId, -t.amount)
        }
    }
}
