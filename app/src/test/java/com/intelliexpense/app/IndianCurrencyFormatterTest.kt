package com.intelliexpense.app

import com.intelliexpense.app.core.util.IndianCurrencyFormatter
import org.junit.Assert.assertEquals
import org.junit.Test

class IndianCurrencyFormatterTest {

    @Test
    fun testIndianNumberFormatting() {
        assertEquals("₹100.00", IndianCurrencyFormatter.format(100.0))
        assertEquals("₹1,000.00", IndianCurrencyFormatter.format(1000.0))
        assertEquals("₹10,000.00", IndianCurrencyFormatter.format(10000.0))
        assertEquals("₹1,50,000.00", IndianCurrencyFormatter.format(150000.0))
        assertEquals("₹1,23,45,678.00", IndianCurrencyFormatter.format(12345678.0))
    }

    @Test
    fun testCompactFormatting() {
        assertEquals("₹45K", IndianCurrencyFormatter.formatCompact(45000.0))
        assertEquals("₹1.5L", IndianCurrencyFormatter.formatCompact(150000.0))
        assertEquals("₹2.5Cr", IndianCurrencyFormatter.formatCompact(25000000.0))
    }
}
