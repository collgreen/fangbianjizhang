package com.example.fangbianjizhang.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.fangbianjizhang.domain.model.BudgetMode
import com.example.fangbianjizhang.domain.model.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("settings")

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val store get() = context.dataStore

    private object Keys {
        val THEME = stringPreferencesKey("theme")
        val CURRENCY = stringPreferencesKey("currency_symbol")
        val MONTH_START = intPreferencesKey("month_start_day")
        val AUTO_UPDATE = booleanPreferencesKey("auto_check_update")
        val BUDGET_MODE = stringPreferencesKey("budget_mode")
    }

    val theme: Flow<ThemeMode> = store.data.map {
        ThemeMode.valueOf(it[Keys.THEME] ?: ThemeMode.SYSTEM.name)
    }

    val currencySymbol: Flow<String> = store.data.map {
        it[Keys.CURRENCY] ?: "¥"
    }

    val monthStartDay: Flow<Int> = store.data.map {
        it[Keys.MONTH_START] ?: 1
    }

    val autoCheckUpdate: Flow<Boolean> = store.data.map {
        it[Keys.AUTO_UPDATE] ?: true
    }

    val budgetMode: Flow<BudgetMode> = store.data.map {
        BudgetMode.valueOf(it[Keys.BUDGET_MODE] ?: BudgetMode.NONE.name)
    }

    suspend fun setTheme(mode: ThemeMode) {
        store.edit { it[Keys.THEME] = mode.name }
    }

    suspend fun setCurrency(symbol: String) {
        store.edit { it[Keys.CURRENCY] = symbol }
    }

    suspend fun setMonthStartDay(day: Int) {
        store.edit { it[Keys.MONTH_START] = day }
    }

    suspend fun setAutoCheckUpdate(enabled: Boolean) {
        store.edit { it[Keys.AUTO_UPDATE] = enabled }
    }

    suspend fun setBudgetMode(mode: BudgetMode) {
        store.edit { it[Keys.BUDGET_MODE] = mode.name }
    }
}
