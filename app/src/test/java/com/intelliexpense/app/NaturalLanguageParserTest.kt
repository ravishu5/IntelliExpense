package com.intelliexpense.app

import com.intelliexpense.app.capture.NaturalLanguageExpenseParser
import com.intelliexpense.app.core.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NaturalLanguageParserTest {

    @Test
    fun testDirectAmountAndMerchant() {
        val result = NaturalLanguageExpenseParser.parse("Swiggy 450")
        assertNotNull(result)
        assertEquals(450.0, result!!.amount, 0.01)
        assertEquals("Swiggy", result.merchantNormalized)
        assertEquals("food_dining", result.categoryId)
        assertEquals(TransactionType.EXPENSE, result.type)
    }

    @Test
    fun testDetailedSentenceWithYesterday() {
        val result = NaturalLanguageExpenseParser.parse("Spent 1200 for groceries at Blinkit yesterday via HDFC")
        assertNotNull(result)
        assertEquals(1200.0, result!!.amount, 0.01)
        assertEquals("Blinkit", result.merchantNormalized)
        assertEquals("groceries", result.categoryId)
        assertTrue(result.timestamp < System.currentTimeMillis() - 12 * 60 * 60 * 1000L)
    }

    @Test
    fun testCashExpense() {
        val result = NaturalLanguageExpenseParser.parse("Auto 60 cash")
        assertNotNull(result)
        assertEquals(60.0, result!!.amount, 0.01)
        assertEquals("Cash", result.paymentMethod)
        assertEquals("travel_mobility", result.categoryId)
    }

    @Test
    fun testSalaryIncome() {
        val result = NaturalLanguageExpenseParser.parse("Received 85000 salary from Infosys")
        assertNotNull(result)
        assertEquals(85000.0, result!!.amount, 0.01)
        assertEquals(TransactionType.INCOME, result.type)
        assertEquals("salary_income", result.categoryId)
    }
}
