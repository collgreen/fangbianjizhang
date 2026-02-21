package com.example.fangbianjizhang.domain.model

data class Budget(
    val id: Long = 0,
    val categoryId: Long? = null,
    val amount: Long,
    val yearMonth: String
)
