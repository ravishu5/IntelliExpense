package com.intelliexpense.app

import com.intelliexpense.app.analytics.SpendingAnalyticsEngine
import com.intelliexpense.app.core.model.TransactionType
import com.intelliexpense.app.data.model.TransactionEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class SpendingAnalyticsEngineTest {

    @Test
    fun testSpendingAnalyticsCalculations() {
        val now = Calendar.getInstance()
        val currentTimestamp = now.timeInMillis

        val txns = listOf(
            TransactionEntity(
                id = "t1",
                accountId = "acc_1",
                type = TransactionType.INCOME,
                amount = 100000.0,
                merchantOriginal = "Salary",
                merchantNormalized = "Salary",
                categoryId = "salary_income",
                timestamp = currentTimestamp
            ),
            TransactionEntity(
                id = "t2",
                accountId = "acc_1",
                type = TransactionType.EXPENSE,
                amount = 12000.0,
                merchantOriginal = "Swiggy",
                merchantNormalized = "Swiggy",
                categoryId = "food_dining",
                timestamp = currentTimestamp
            ),
            TransactionEntity(
                id = "t3",
                accountId = "acc_1",
                type = TransactionType.EXPENSE,
                amount = 8000.0,
                merchantOriginal = "Blinkit",
                merchantNormalized = "Blinkit",
                categoryId = "groceries",
                timestamp = currentTimestamp
            )
        )

        val categoryMap = mapOf(
            "food_dining" to "Food & Dining",
            "groceries" to "Groceries"
        )

        val summary = SpendingAnalyticsEngine.generateMonthlyAnalytics(txns, categoryMap, now)

        assertEquals(20000.0, summary.totalExpense, 0.01)
        assertEquals(100000.0, summary.totalIncome, 0.01)
        assertEquals(80000.0, summary.netSavings, 0.01)
        assertEquals(80.0f, summary.savingsRate, 0.1f)
        assertEquals(2, summary.categoryBreakdown.size)
        assertEquals("food_dining", summary.categoryBreakdown.first().categoryId)
        assertEquals(60.0f, summary.categoryBreakdown.first().percentage, 0.5f) // 12000 / 20000 = 60%
    }
}
