package com.example.fangbianjizhang.domain.repository

import com.example.fangbianjizhang.domain.model.RecurringTemplate
import kotlinx.coroutines.flow.Flow

interface RecurringRepository {
    fun getAll(): Flow<List<RecurringTemplate>>
    fun getDueTemplates(todayTimestamp: Long): Flow<List<RecurringTemplate>>
    suspend fun insert(template: RecurringTemplate): Long
    suspend fun update(template: RecurringTemplate)
    suspend fun setEnabled(id: Long, enabled: Boolean)
    suspend fun updateNextDueDate(id: Long, nextDate: Long)
    suspend fun softDelete(id: Long)
}
