package com.hudlauncher.app.data

import java.time.LocalDate
import java.time.format.DateTimeFormatter

/** Demobilisation countdown: [SERVICE_DAYS] days counted from a start day (epoch day). */
object DmbTimer {

    const val SERVICE_DAYS = 365

    fun today(): Long = LocalDate.now().toEpochDay()

    /** Whole days still to serve; 0 once the term is over. */
    fun daysLeft(startDay: Long, today: Long, total: Int = SERVICE_DAYS): Int =
        (startDay + total - today).coerceIn(0L, total.toLong()).toInt()

    /** Share of the term already served, 0f..1f. */
    fun progress(startDay: Long, today: Long, total: Int = SERVICE_DAYS): Float =
        ((today - startDay).toFloat() / total).coerceIn(0f, 1f)

    fun formatDay(epochDay: Long): String =
        if (epochDay <= 0L) "—" else LocalDate.ofEpochDay(epochDay).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
}
