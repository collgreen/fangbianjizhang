package com.example.fangbianjizhang.domain.usecase.transaction

import com.example.fangbianjizhang.domain.model.Account
import com.example.fangbianjizhang.domain.model.AccountType
import com.example.fangbianjizhang.domain.model.Transaction
import com.example.fangbianjizhang.domain.model.TransactionType
import com.example.fangbianjizhang.domain.repository.AccountRepository
import com.example.fangbianjizhang.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.first
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
                        AccountType.CREDIT -> applyCreditRepayment(targetAccount, t.amount)
                        AccountType.LOAN -> accountRepo.updateAlreadyPaid(targetId, t.amount)
                        else -> accountRepo.updateBalance(targetId, t.amount)
                    }
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

    /** 信用卡还款：优先还次月账单，剩余还分期 */
    private suspend fun applyCreditRepayment(account: Account, amount: Long) {
        val usedAmount = account.usedAmount ?: 0
        val installmentAmount = account.installmentAmount ?: 0
        if (usedAmount >= amount) {
            accountRepo.updateUsedAmount(account.id, -amount)
        } else {
            if (usedAmount > 0) accountRepo.updateUsedAmount(account.id, -usedAmount)
            val remaining = amount - usedAmount
            val installDeduct = remaining.coerceAtMost(installmentAmount)
            if (installDeduct > 0) accountRepo.updateInstallmentAmount(account.id, -installDeduct)
        }
    }
}
