package com.example.fangbianjizhang.data.repository

import com.example.fangbianjizhang.data.local.db.dao.CategoryDao
import com.example.fangbianjizhang.data.mapper.toDomain
import com.example.fangbianjizhang.data.mapper.toEntity
import com.example.fangbianjizhang.domain.model.Category
import com.example.fangbianjizhang.domain.model.CategoryType
import com.example.fangbianjizhang.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val dao: CategoryDao
) : CategoryRepository {

    override fun getByType(type: CategoryType): Flow<List<Category>> =
        dao.getByType(type.name).map { list -> list.map { it.toDomain() } }

    override fun getTopLevelByType(type: CategoryType): Flow<List<Category>> =
        dao.getTopLevelByType(type.name).map { list -> list.map { it.toDomain() } }

    override fun getChildren(parentId: Long): Flow<List<Category>> =
        dao.getChildren(parentId).map { list -> list.map { it.toDomain() } }

    override suspend fun insert(category: Category): Long =
        dao.insert(category.toEntity())

    override suspend fun update(category: Category) =
        dao.update(category.toEntity())

    override suspend fun softDelete(id: Long) =
        dao.softDelete(id, System.currentTimeMillis())

    override suspend fun updateSortOrder(ids: List<Long>) {
        ids.forEachIndexed { index, id ->
            dao.updateSortOrder(id, index)
        }
    }
}
