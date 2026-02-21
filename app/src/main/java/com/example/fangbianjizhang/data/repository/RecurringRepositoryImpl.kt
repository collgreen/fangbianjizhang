package com.example.fangbianjizhang.data.repository

import com.example.fangbianjizhang.data.local.db.dao.RecurringDao
import com.example.fangbianjizhang.data.local.db.entity.RecurringEntity
import com.example.fangbianjizhang.domain.model.Frequency
import com.example.fangbianjizhang.domain.model.RecurringTemplate
import com.example.fangbianjizhang.domain.repository.RecurringRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RecurringRepositoryImpl @Inject constructor(
    private val dao: RecurringDao
) : RecurringRepository {

    override fun getAll(): Flow<List<RecurringTemplate>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    override fun getDueTemplates(todayTimestamp: Long): Flow<List<RecurringTemplate>> =
        dao.getDueTemplates(todayTimestamp).map { list -> list.map { it.toDomain() } }

    override suspend fun insert(template: RecurringTemplate): Long {
        val now = System.currentTimeMillis()
        return dao.insert(template.toEntity(now))
    }

    override suspend fun update(template: RecurringTemplate) {
        dao.update(template.toEntity(System.currentTimeMillis()))
    }

    override suspend fun setEnabled(id: Long, enabled: Boolean) =
        dao.setEnabled(id, enabled, System.currentTimeMillis())

    override suspend fun updateNextDueDate(id: Long, nextDate: Long) =
        dao.updateNextDueDate(id, nextDate, System.currentTimeMillis())

    override suspend fun softDelete(id: Long) =
        dao.softDelete(id, System.currentTimeMillis())

    private fun RecurringEntity.toDomain() = RecurringTemplate(
        id = id, name = name, amount = amount,
        frequency = Frequency.valueOf(frequency),
        dayOfMonth = dayOfMonth, dayOfWeek = dayOfWeek,
        intervalDays = intervalDays,
        sourceAccountId = sourceAccountId,
        targetAccountId = targetAccountId,
        nextDueDate = nextDueDate,
        lastExecutedAt = lastExecutedAt,
        isEnabled = isEnabled
    )

    private fun RecurringTemplate.toEntity(now: Long) = RecurringEntity(
        id = id, name = name, amount = amount,
        frequency = frequency.name,
        dayOfMonth = dayOfMonth, dayOfWeek = dayOfWeek,
        intervalDays = intervalDays,
        sourceAccountId = sourceAccountId,
        targetAccountId = targetAccountId,
        nextDueDate = nextDueDate,
        lastExecutedAt = lastExecutedAt,
        isEnabled = isEnabled,
        createdAt = now, updatedAt = now
    )
}
