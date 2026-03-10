package com.example.fangbianjizhang.data.repository

import com.example.fangbianjizhang.data.local.db.dao.AccountDao
import com.example.fangbianjizhang.data.mapper.toDomain
import com.example.fangbianjizhang.data.mapper.toEntity
import com.example.fangbianjizhang.domain.model.Account
import com.example.fangbianjizhang.domain.model.AccountType
import com.example.fangbianjizhang.domain.repository.AccountRepository
import com.example.fangbianjizhang.domain.repository.NetAsset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AccountRepositoryImpl @Inject constructor(
    private val dao: AccountDao
) : AccountRepository {

    override fun getAllActive(): Flow<List<Account>> =
        dao.getAllActive().map { list -> list.map { it.toDomain() } }

    override fun getByType(type: AccountType): Flow<List<Account>> =
        dao.getByType(type.name).map { list -> list.map { it.toDomain() } }

    override fun getById(id: Long): Flow<Account?> =
        dao.getById(id).map { it?.toDomain() }

    override suspend fun insert(account: Account): Long =
        dao.insert(account.toEntity())

    override suspend fun update(account: Account) =
        dao.update(account.toEntity())

    override suspend fun softDelete(id: Long) =
        dao.softDelete(id, System.currentTimeMillis())

    override suspend fun updateBalance(id: Long, delta: Long) =
        dao.updateBalance(id, delta, System.currentTimeMillis())

    override suspend fun updateUsedAmount(id: Long, delta: Long) =
        dao.updateUsedAmount(id, delta, System.currentTimeMillis())

    override suspend fun updateInstallmentAmount(id: Long, delta: Long) =
        dao.updateInstallmentAmount(id, delta, System.currentTimeMillis())

    override suspend fun updateAlreadyPaid(id: Long, delta: Long) =
        dao.updateAlreadyPaid(id, delta, System.currentTimeMillis())

    override fun getNetAsset(): Flow<NetAsset> =
        combine(
            dao.getFundTotal(),
            dao.getInvestmentTotal(),
            dao.getCreditUsedTotal(),
            dao.getLoanRemainingTotal()
        ) { fund, invest, credit, loan ->
            NetAsset(fund, invest, credit, loan)
        }
}
