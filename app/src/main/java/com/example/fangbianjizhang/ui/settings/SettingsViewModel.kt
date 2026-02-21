package com.example.fangbianjizhang.ui.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fangbianjizhang.data.local.datastore.PreferencesManager
import com.example.fangbianjizhang.domain.model.BudgetMode
import com.example.fangbianjizhang.domain.model.ThemeMode
import com.example.fangbianjizhang.domain.repository.BackupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val currencySymbol: String = "¥",
    val monthStartDay: Int = 1,
    val autoCheckUpdate: Boolean = true,
    val budgetMode: BudgetMode = BudgetMode.NONE
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: PreferencesManager,
    private val backupRepo: BackupRepository
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        prefs.theme,
        prefs.currencySymbol,
        prefs.monthStartDay,
        prefs.autoCheckUpdate,
        prefs.budgetMode
    ) { theme, currency, startDay, autoUpdate, budgetMode ->
        SettingsUiState(theme, currency, startDay, autoUpdate, budgetMode)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    fun setTheme(mode: ThemeMode) = viewModelScope.launch { prefs.setTheme(mode) }
    fun setCurrency(symbol: String) = viewModelScope.launch { prefs.setCurrency(symbol) }
    fun setMonthStartDay(day: Int) = viewModelScope.launch { prefs.setMonthStartDay(day) }
    fun setAutoCheckUpdate(enabled: Boolean) = viewModelScope.launch { prefs.setAutoCheckUpdate(enabled) }

    fun exportJson(uri: Uri, onDone: () -> Unit) {
        viewModelScope.launch {
            backupRepo.exportJson(uri, null)
            onDone()
        }
    }

    private val _importConflict = MutableStateFlow<Pair<Uri, List<String>>?>(null)
    val importConflict: StateFlow<Pair<Uri, List<String>>?> = _importConflict.asStateFlow()

    fun importJson(uri: Uri, onDone: (String) -> Unit) {
        viewModelScope.launch {
            backupRepo.checkImportConflicts(uri, null).fold(
                onSuccess = { conflict ->
                    if (conflict.duplicateAccountNames.isNotEmpty()) {
                        _importConflict.value = uri to conflict.duplicateAccountNames
                    } else {
                        doImport(uri, false, onDone)
                    }
                },
                onFailure = { onDone("导入失败: ${it.message}") }
            )
        }
    }

    fun confirmOverwrite(onDone: (String) -> Unit) {
        val (uri, _) = _importConflict.value ?: return
        _importConflict.value = null
        viewModelScope.launch { doImport(uri, true, onDone) }
    }

    fun cancelImport() { _importConflict.value = null }

    private suspend fun doImport(uri: Uri, overwrite: Boolean, onDone: (String) -> Unit) {
        backupRepo.importJson(uri, null, overwrite).fold(
            onSuccess = { s ->
                onDone("导入成功: ${s.accountCount}账户, ${s.categoryCount}分类, ${s.transactionCount}笔交易, ${s.budgetCount}预算, ${s.recurringCount}定期扣款")
            },
            onFailure = { onDone("导入失败: ${it.message}") }
        )
    }
}
