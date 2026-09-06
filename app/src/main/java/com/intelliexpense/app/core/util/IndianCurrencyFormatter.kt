package com.intelliexpense.app.core.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs

object IndianCurrencyFormatter {

    /**
     * Formats an amount to Indian Rupee standard format:
     * Examples:
     * 100 -> ₹100.00
     * 1000 -> ₹1,000.00
     * 100000 -> ₹1,00,000.00
     * 12345678.50 -> ₹1,23,45,678.50
     */
    fun format(amount: Double, includeDecimals: Boolean = true): String {
        val sign = if (amount < 0) "-" else ""
        val absAmount = abs(amount)

        val longPart = absAmount.toLong()
        val decimalPart = if (includeDecimals) {
            String.format(Locale.ENGLISH, ".%02d", ((absAmount - longPart) * 100).toInt())
        } else {
            ""
        }

        val formattedLong = formatIndianNumber(longPart)
        return "${sign}₹$formattedLong$decimalPart"
    }

    /**
     * Formats large numbers compactly:
     * 1,200 -> ₹1.2K
     * 45,000 -> ₹45K
     * 1,50,000 -> ₹1.5L
     * 2,50,00,000 -> ₹2.5Cr
     */
    fun formatCompact(amount: Double): String {
        val sign = if (amount < 0) "-" else ""
        val absVal = abs(amount)

        fun formatWithOptionalDecimal(value: Double): String {
            return if (value % 1.0 == 0.0) {
                String.format(Locale.ENGLISH, "%.0f", value)
            } else {
                String.format(Locale.ENGLISH, "%.1f", value)
            }
        }

        return when {
            absVal >= 10_000_000 -> "${sign}₹${formatWithOptionalDecimal(absVal / 10_000_000)}Cr"
            absVal >= 100_000 -> "${sign}₹${formatWithOptionalDecimal(absVal / 100_000)}L"
            absVal >= 1_000 -> "${sign}₹${formatWithOptionalDecimal(absVal / 1_000)}K"
            else -> "${sign}₹${String.format(Locale.ENGLISH, "%.0f", absVal)}"
        }
    }

    private fun formatIndianNumber(value: Long): String {
        val str = value.toString()
        if (str.length <= 3) return str

        val lastThree = str.substring(str.length - 3)
        val rest = str.substring(0, str.length - 3)

        val builder = StringBuilder()
        var count = 0
        for (i in rest.length - 1 downTo 0) {
            builder.append(rest[i])
            count++
            if (count % 2 == 0 && i != 0) {
                builder.append(",")
            }
        }
        return builder.reverse().toString() + "," + lastThree
    }
}
