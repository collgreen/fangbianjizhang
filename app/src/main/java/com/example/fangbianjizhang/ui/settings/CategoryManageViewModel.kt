package com.example.fangbianjizhang.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fangbianjizhang.domain.model.Category
import com.example.fangbianjizhang.domain.model.CategoryType
import com.example.fangbianjizhang.domain.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoryManageUiState(
    val type: CategoryType = CategoryType.EXPENSE,
    val categories: List<Category> = emptyList(),
    val childrenMap: Map<Long, List<Category>> = emptyMap()
)

@HiltViewModel
class CategoryManageViewModel @Inject constructor(
    private val categoryRepo: CategoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CategoryManageUiState())
    val uiState: StateFlow<CategoryManageUiState> = _state.asStateFlow()
    private var categoriesJob: Job? = null

    init { loadCategories() }

    fun setType(type: CategoryType) {
        _state.value = _state.value.copy(type = type, childrenMap = emptyMap())
        loadCategories()
    }

    fun loadChildren(parentId: Long) {
        viewModelScope.launch {
            categoryRepo.getChildren(parentId).first().let { children ->
                _state.value = _state.value.copy(
                    childrenMap = _state.value.childrenMap + (parentId to children)
                )
            }
        }
    }

    fun addCategory(name: String) {
        viewModelScope.launch {
            categoryRepo.insert(Category(
                name = name,
                type = _state.value.type,
                icon = if (_state.value.type == CategoryType.EXPENSE) "📦" else "💰"
            ))
        }
    }

    private fun loadCategories() {
        categoriesJob?.cancel()
        categoriesJob = viewModelScope.launch {
            categoryRepo.getTopLevelByType(_state.value.type).collect { cats ->
                _state.value = _state.value.copy(categories = cats)
            }
        }
    }
}
