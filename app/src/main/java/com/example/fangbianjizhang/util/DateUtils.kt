package com.example.fangbianjizhang.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

object DateUtils {
    private val zone = ZoneId.systemDefault()

    fun monthRange(year: Int, month: Int): Pair<Long, Long> {
        val ym = YearMonth.of(year, month)
        val start = ym.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val end = ym.atEndOfMonth().atTime(LocalTime.MAX).atZone(zone).toInstant().toEpochMilli()
        return start to end
    }

    fun dayRange(timestamp: Long): Pair<Long, Long> {
        val date = Instant.ofEpochMilli(timestamp).atZone(zone).toLocalDate()
        val start = date.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = date.atTime(LocalTime.MAX).atZone(zone).toInstant().toEpochMilli()
        return start to end
    }

    fun toLocalDate(timestamp: Long): LocalDate =
        Instant.ofEpochMilli(timestamp).atZone(zone).toLocalDate()

    fun formatDay(timestamp: Long): String {
        val date = toLocalDate(timestamp)
        val dow = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.CHINESE)
        return "${date.monthValue}月${date.dayOfMonth}日 $dow"
    }

    fun currentYearMonth(): String {
        val now = LocalDate.now()
        return String.format("%d-%02d", now.year, now.monthValue)
    }
}
