package com.example.fangbianjizhang.domain.model

data class Account(
    val id: Long = 0,
    val name: String,
    val type: AccountType,
    val subType: AccountSubType,
    val balance: Long = 0,
    val totalLimit: Long? = null,
    val usedAmount: Long? = null,
    val installmentAmount: Long? = null,
    val totalLoan: Long? = null,
    val alreadyPaid: Long? = null,
    val monthlyPayment: Long? = null,
    val billDay: Int? = null,
    val repaymentDay: Int? = null,
    val icon: String,
    val sortOrder: Int = 0,
    val includeInTotal: Boolean = true
)
