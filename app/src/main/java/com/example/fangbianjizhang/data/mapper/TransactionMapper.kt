package com.example.fangbianjizhang.data.mapper

import com.example.fangbianjizhang.data.local.db.entity.TransactionEntity
import com.example.fangbianjizhang.domain.model.Transaction
import com.example.fangbianjizhang.domain.model.TransactionType

fun TransactionEntity.toDomain() = Transaction(
    id = id, type = TransactionType.valueOf(type),
    amount = amount, categoryId = categoryId,
    accountId = accountId, targetAccountId = targetAccountId,
    fee = fee, counterparty = counterparty,
    dueDate = dueDate, note = note,
    imageUri = imageUri, transactionDate = transactionDate,
    recurringId = recurringId
)

fun Transaction.toEntity(now: Long = System.currentTimeMillis()) = TransactionEntity(
    id = id, type = type.name,
    amount = amount, categoryId = categoryId,
    accountId = accountId, targetAccountId = targetAccountId,
    fee = fee, counterparty = counterparty,
    dueDate = dueDate, note = note,
    imageUri = imageUri, transactionDate = transactionDate,
    recurringId = recurringId,
    createdAt = now, updatedAt = now
)
