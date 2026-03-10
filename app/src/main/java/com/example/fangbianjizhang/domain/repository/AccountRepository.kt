package com.example.fangbianjizhang.domain.repository

import com.example.fangbianjizhang.domain.model.Account
import com.example.fangbianjizhang.domain.model.AccountType
import kotlinx.coroutines.flow.Flow

data class NetAsset(
    val fundTotal: Long,
    val investmentTotal: Long,
    val creditUsedTotal: Long,
    val loanRemainingTotal: Long
) {
    val net: Long get() = fundTotal + investmentTotal - creditUsedTotal - loanRemainingTotal
}

interface AccountRepository {
    fun getAllActive(): Flow<List<Account>>
    fun getByType(type: AccountType): Flow<List<Account>>
    fun getById(id: Long): Flow<Account?>
    suspend fun insert(account: Account): Long
    suspend fun update(account: Account)
    suspend fun softDelete(id: Long)
    suspend fun updateBalance(id: Long, delta: Long)
    suspend fun updateUsedAmount(id: Long, delta: Long)
    suspend fun updateInstallmentAmount(id: Long, delta: Long)
    suspend fun updateAlreadyPaid(id: Long, delta: Long)
    fun getNetAsset(): Flow<NetAsset>
}
