package com.intelliexpense.app.core.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    private val displayDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
    private val monthYearFormat = SimpleDateFormat("MMM yyyy", Locale.ENGLISH)
    private val monthYearKeyFormat = SimpleDateFormat("yyyy-MM", Locale.ENGLISH)
    private val fullDateTimeFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH)

    fun formatDate(timestamp: Long): String = displayDateFormat.format(Date(timestamp))
    fun formatTime(timestamp: Long): String = timeFormat.format(Date(timestamp))
    fun formatMonthYear(timestamp: Long): String = monthYearFormat.format(Date(timestamp))
    fun formatFullDateTime(timestamp: Long): String = fullDateTimeFormat.format(Date(timestamp))
    fun getMonthKey(timestamp: Long): String = monthYearKeyFormat.format(Date(timestamp))

    fun getCurrentMonthKey(): String = monthYearKeyFormat.format(Date())

    fun getStartOfMonth(cal: Calendar = Calendar.getInstance()): Long {
        val c = cal.clone() as Calendar
        c.set(Calendar.DAY_OF_MONTH, 1)
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }

    fun getEndOfMonth(cal: Calendar = Calendar.getInstance()): Long {
        val c = cal.clone() as Calendar
        c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH))
        c.set(Calendar.HOUR_OF_DAY, 23)
        c.set(Calendar.MINUTE, 59)
        c.set(Calendar.SECOND, 59)
        c.set(Calendar.MILLISECOND, 999)
        return c.timeInMillis
    }

    fun getStartOfDay(timestamp: Long = System.currentTimeMillis()): Long {
        val c = Calendar.getInstance()
        c.timeInMillis = timestamp
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }

    fun getDaysAgo(days: Int): Long {
        val c = Calendar.getInstance()
        c.add(Calendar.DAY_OF_YEAR, -days)
        return c.timeInMillis
    }

    fun getRelativeDateHeader(timestamp: Long): String {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply { timeInMillis = timestamp }

        val sameYear = now.get(Calendar.YEAR) == target.get(Calendar.YEAR)
        val nowDay = now.get(Calendar.DAY_OF_YEAR)
        val targetDay = target.get(Calendar.DAY_OF_YEAR)

        return if (sameYear) {
            when (nowDay - targetDay) {
                0 -> "Today"
                1 -> "Yesterday"
                in 2..6 -> SimpleDateFormat("EEEE", Locale.ENGLISH).format(Date(timestamp))
                else -> displayDateFormat.format(Date(timestamp))
            }
        } else {
            displayDateFormat.format(Date(timestamp))
        }
    }
}
