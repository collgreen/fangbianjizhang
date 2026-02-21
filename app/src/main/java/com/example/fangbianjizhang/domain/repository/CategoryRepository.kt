package com.example.fangbianjizhang.domain.repository

import com.example.fangbianjizhang.domain.model.Category
import com.example.fangbianjizhang.domain.model.CategoryType
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getByType(type: CategoryType): Flow<List<Category>>
    fun getTopLevelByType(type: CategoryType): Flow<List<Category>>
    fun getChildren(parentId: Long): Flow<List<Category>>
    suspend fun insert(category: Category): Long
    suspend fun update(category: Category)
    suspend fun softDelete(id: Long)
    suspend fun updateSortOrder(ids: List<Long>)
}
