package com.example.fangbianjizhang.data.mapper

import com.example.fangbianjizhang.data.local.db.entity.CategoryEntity
import com.example.fangbianjizhang.domain.model.Category
import com.example.fangbianjizhang.domain.model.CategoryType

fun CategoryEntity.toDomain() = Category(
    id = id, name = name,
    type = CategoryType.valueOf(type),
    parentId = parentId, icon = icon,
    sortOrder = sortOrder, isDefault = isDefault
)

fun Category.toEntity(now: Long = System.currentTimeMillis()) = CategoryEntity(
    id = id, name = name,
    type = type.name, parentId = parentId,
    icon = icon, sortOrder = sortOrder,
    isDefault = isDefault, createdAt = now
)
