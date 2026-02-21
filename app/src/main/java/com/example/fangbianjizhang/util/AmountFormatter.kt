package com.example.fangbianjizhang.util

import java.math.BigDecimal

object AmountFormatter {
    fun toLong(yuan: String): Long =
        BigDecimal(yuan).multiply(BigDecimal(100)).toLong()

    fun toDisplay(fen: Long): String =
        String.format("%.2f", fen / 100.0)

    fun toDisplayWithSymbol(fen: Long, symbol: String = "¥"): String =
        "$symbol${toDisplay(fen)}"
}
