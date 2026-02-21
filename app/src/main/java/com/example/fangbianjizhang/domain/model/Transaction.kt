package com.example.fangbianjizhang.domain.model

data class Transaction(
    val id: Long = 0,
    val type: TransactionType,
    val amount: Long,
    val categoryId: Long? = null,
    val categoryName: String? = null,
    val accountId: Long,
    val targetAccountId: Long? = null,
    val fee: Long = 0,
    val counterparty: String? = null,
    val dueDate: Long? = null,
    val note: String? = null,
    val imageUri: String? = null,
    val transactionDate: Long,
    val recurringId: Long? = null
)
