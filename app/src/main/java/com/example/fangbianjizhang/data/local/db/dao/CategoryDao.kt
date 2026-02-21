package com.example.fangbianjizhang.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.fangbianjizhang.data.local.db.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE type = :type AND is_deleted = 0 ORDER BY sort_order")
    fun getByType(type: String): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE type = :type AND parent_id IS NULL AND is_deleted = 0 ORDER BY sort_order")
    fun getTopLevelByType(type: String): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE parent_id = :parentId AND is_deleted = 0 ORDER BY sort_order")
    fun getChildren(parentId: Long): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE is_deleted = 0")
    suspend fun getAllActiveList(): List<CategoryEntity>

    @Insert
    suspend fun insert(entity: CategoryEntity): Long

    @Insert
    suspend fun insertAll(entities: List<CategoryEntity>)

    @Update
    suspend fun update(entity: CategoryEntity)

    @Query("UPDATE categories SET is_deleted = 1, deleted_at = :now WHERE id = :id")
    suspend fun softDelete(id: Long, now: Long)

    @Query("UPDATE categories SET sort_order = :order WHERE id = :id")
    suspend fun updateSortOrder(id: Long, order: Int)
}
