package com.example.fangbianjizhang.domain.usecase.recurring

import com.example.fangbianjizhang.domain.model.Frequency
import com.example.fangbianjizhang.domain.model.RecurringTemplate
import com.example.fangbianjizhang.domain.model.Transaction
import com.example.fangbianjizhang.domain.model.TransactionType
import com.example.fangbianjizhang.domain.repository.AccountRepository
import com.example.fangbianjizhang.domain.repository.RecurringRepository
import com.example.fangbianjizhang.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

data class ExecutionResult(
    val templateId: Long,
    val templateName: String,
    val success: Boolean,
    val errorMessage: String? = null
)

class ExecuteRecurringUseCase @Inject constructor(
    private val recurringRepo: RecurringRepository,
    private val transactionRepo: TransactionRepository,
    private val accountRepo: AccountRepository
) {
    suspend operator fun invoke(): List<ExecutionResult> {
        val now = System.currentTimeMillis()
        val dueTemplates = recurringRepo.getDueTemplates(now).first()
        return dueTemplates.map { template ->
            try {
                executeTemplate(template, now)
                ExecutionResult(template.id, template.name, success = true)
            } catch (e: Exception) {
                ExecutionResult(template.id, template.name, false, e.message)
            }
        }
    }

    private suspend fun executeTemplate(t: RecurringTemplate, now: Long) {
        val transaction = Transaction(
            type = TransactionType.TRANSFER,
            amount = t.amount,
            accountId = t.sourceAccountId,
            targetAccountId = t.targetAccountId,
            note = "定期: ${t.name}",
            transactionDate = now,
            recurringId = t.id
        )
        transactionRepo.insert(transaction)
        accountRepo.updateBalance(t.sourceAccountId, -t.amount)
        accountRepo.updateBalance(t.targetAccountId, t.amount)
        recurringRepo.updateNextDueDate(t.id, calcNextDueDate(t))
    }

    private fun calcNextDueDate(t: RecurringTemplate): Long {
        val zone = ZoneId.systemDefault()
        val current = Instant.ofEpochMilli(t.nextDueDate).atZone(zone).toLocalDate()
        val next = when (t.frequency) {
            Frequency.MONTHLY -> current.plusMonths(1)
            Frequency.WEEKLY -> current.plusWeeks(1)
            Frequency.CUSTOM_DAYS -> current.plusDays(t.intervalDays?.toLong() ?: 30)
        }
        return next.atStartOfDay(zone).toInstant().toEpochMilli()
    }
}
