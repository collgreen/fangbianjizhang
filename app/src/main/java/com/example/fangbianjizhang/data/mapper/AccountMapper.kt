package com.example.fangbianjizhang.data.mapper

import com.example.fangbianjizhang.data.local.db.entity.AccountEntity
import com.example.fangbianjizhang.domain.model.Account
import com.example.fangbianjizhang.domain.model.AccountSubType
import com.example.fangbianjizhang.domain.model.AccountType

fun AccountEntity.toDomain() = Account(
    id = id, name = name,
    type = AccountType.valueOf(type),
    subType = AccountSubType.valueOf(subType),
    balance = balance, totalLimit = totalLimit,
    usedAmount = usedAmount, totalLoan = totalLoan,
    alreadyPaid = alreadyPaid, monthlyPayment = monthlyPayment,
    billDay = billDay, repaymentDay = repaymentDay,
    icon = icon, sortOrder = sortOrder,
    includeInTotal = includeInTotal
)

fun Account.toEntity(now: Long = System.currentTimeMillis()) = AccountEntity(
    id = id, name = name,
    type = type.name, subType = subType.name,
    balance = balance, totalLimit = totalLimit,
    usedAmount = usedAmount, totalLoan = totalLoan,
    alreadyPaid = alreadyPaid, monthlyPayment = monthlyPayment,
    billDay = billDay, repaymentDay = repaymentDay,
    icon = icon, sortOrder = sortOrder,
    includeInTotal = includeInTotal,
    createdAt = now, updatedAt = now
)
