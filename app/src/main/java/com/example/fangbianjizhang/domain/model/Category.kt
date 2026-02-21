package com.example.fangbianjizhang.domain.model

data class Category(
    val id: Long = 0,
    val name: String,
    val type: CategoryType,
    val parentId: Long? = null,
    val icon: String,
    val sortOrder: Int = 0,
    val isDefault: Boolean = false
)
