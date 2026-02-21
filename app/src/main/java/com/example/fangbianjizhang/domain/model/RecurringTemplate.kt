package com.example.fangbianjizhang.domain.model

data class RecurringTemplate(
    val id: Long = 0,
    val name: String,
    val amount: Long,
    val frequency: Frequency,
    val dayOfMonth: Int? = null,
    val dayOfWeek: Int? = null,
    val intervalDays: Int? = null,
    val sourceAccountId: Long,
    val targetAccountId: Long,
    val nextDueDate: Long,
    val lastExecutedAt: Long? = null,
    val isEnabled: Boolean = true
)
