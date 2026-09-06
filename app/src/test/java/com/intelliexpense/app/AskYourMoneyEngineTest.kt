package com.intelliexpense.app

import com.intelliexpense.app.ai.AskYourMoneyEngine
import com.intelliexpense.app.core.model.AccountType
import com.intelliexpense.app.core.model.SubscriptionFrequency
import com.intelliexpense.app.core.model.TransactionType
import com.intelliexpense.app.data.model.AccountEntity
import com.intelliexpense.app.data.model.BudgetEntity
import com.intelliexpense.app.data.model.CategoryEntity
import com.intelliexpense.app.data.model.SubscriptionEntity
import com.intelliexpense.app.data.model.TransactionEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class AskYourMoneyEngineTest {

    private val engine = AskYourMoneyEngine()

    private val sampleCategories = listOf(
        CategoryEntity("food_dining", "Food & Dining", budgetMonthly = 8000.0),
        CategoryEntity("shopping", "Shopping & Lifestyle", budgetMonthly = 5000.0),
        CategoryEntity("entertainment", "Entertainment & OTT", budgetMonthly = 2000.0)
    )

    private val sampleAccounts = listOf(
        AccountEntity("acc_bank", "HDFC Bank", AccountType.BANK, balance = 45000.0)
    )

    private val sampleBudgets = listOf(
        BudgetEntity("b_overall", null, "2026-09", 50000.0),
        BudgetEntity("b_food", "food_dining", "2026-09", 8000.0)
    )

    private val sampleSubscriptions = listOf(
        SubscriptionEntity(
            id = "sub_netflix",
            merchantName = "Netflix",
            amount = 649.0,
            frequency = SubscriptionFrequency.MONTHLY,
            billingDay = 15,
            nextDueDate = System.currentTimeMillis() + 10 * 86400000L
        )
    )

    private val sampleTransactions = listOf(
        TransactionEntity(
            id = UUID.randomUUID().toString(),
            accountId = "acc_bank",
            type = TransactionType.EXPENSE,
            amount = 650.0,
            merchantOriginal = "Swiggy",
            merchantNormalized = "Swiggy",
            categoryId = "food_dining",
            timestamp = System.currentTimeMillis()
        ),
        TransactionEntity(
            id = UUID.randomUUID().toString(),
            accountId = "acc_bank",
            type = TransactionType.EXPENSE,
            amount = 12500.0,
            merchantOriginal = "Croma Electronics",
            merchantNormalized = "Croma",
            categoryId = "shopping",
            timestamp = System.currentTimeMillis()
        )
    )

    @Test
    fun testCategorySpendQuery() {
        val resp = engine.answerQuery(
            query = "How much did I spend on food this month?",
            transactions = sampleTransactions,
            accounts = sampleAccounts,
            budgets = sampleBudgets,
            subscriptions = sampleSubscriptions,
            categories = sampleCategories
        )
        assertNotNull(resp)
        assertEquals("Food & Dining Spending", resp.title)
        assertTrue(resp.mainAnswer.contains("₹650"))
    }

    @Test
    fun testBiggestExpensesQuery() {
        val resp = engine.answerQuery(
            query = "Show my biggest expenses",
            transactions = sampleTransactions,
            accounts = sampleAccounts,
            budgets = sampleBudgets,
            subscriptions = sampleSubscriptions,
            categories = sampleCategories
        )
        assertNotNull(resp)
        assertEquals("Top Expenses", resp.title)
        assertTrue(resp.mainAnswer.contains("Croma"))
        assertTrue(resp.mainAnswer.contains("₹12,500"))
    }

    @Test
    fun testSubscriptionsQuery() {
        val resp = engine.answerQuery(
            query = "What subscriptions do I have?",
            transactions = sampleTransactions,
            accounts = sampleAccounts,
            budgets = sampleBudgets,
            subscriptions = sampleSubscriptions,
            categories = sampleCategories
        )
        assertNotNull(resp)
        assertEquals("Active Subscriptions", resp.title)
        assertTrue(resp.mainAnswer.contains("1 active recurring subscription"))
        assertTrue(resp.mainAnswer.contains("₹649"))
    }

    @Test
    fun testAffordabilityQuery() {
        val resp = engine.answerQuery(
            query = "Can I afford 3000?",
            transactions = sampleTransactions,
            accounts = sampleAccounts,
            budgets = sampleBudgets,
            subscriptions = sampleSubscriptions,
            categories = sampleCategories
        )
        assertNotNull(resp)
        assertEquals("Affordability Check", resp.title)
        assertTrue(resp.mainAnswer.contains("Yes") || resp.mainAnswer.contains("affordable"))
    }
}
