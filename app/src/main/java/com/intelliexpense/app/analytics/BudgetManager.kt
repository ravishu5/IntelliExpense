package com.intelliexpense.app.analytics

import com.intelliexpense.app.core.model.TransactionType
import com.intelliexpense.app.core.util.DateUtils
import com.intelliexpense.app.data.model.BudgetEntity
import com.intelliexpense.app.data.model.CategoryEntity
import com.intelliexpense.app.data.model.TransactionEntity

data class BudgetStatus(
    val categoryId: String?, // null for overall
    val categoryName: String,
    val limitAmount: Double,
    val spentAmount: Double,
    val remainingAmount: Double,
    val percentageUsed: Float,
    val isExceeded: Boolean,
    val isWarning: Boolean
)

object BudgetManager {

    fun evaluateBudgets(
        budgets: List<BudgetEntity>,
        categories: List<CategoryEntity>,
        transactions: List<TransactionEntity>,
        currentMonthKey: String = DateUtils.getCurrentMonthKey()
    ): List<BudgetStatus> {
        val startOfMonth = DateUtils.getStartOfMonth()
        val endOfMonth = DateUtils.getEndOfMonth()

        val monthlyExpenses = transactions.filter {
            it.type == TransactionType.EXPENSE && it.timestamp in startOfMonth..endOfMonth
        }

        val results = mutableListOf<BudgetStatus>()

        for (b in budgets.filter { it.monthYear == currentMonthKey }) {
            val (spent, name) = if (b.categoryId == null) {
                Pair(monthlyExpenses.sumOf { it.amount }, "Overall Monthly Budget")
            } else {
                val catName = categories.firstOrNull { it.id == b.categoryId }?.name ?: b.categoryId
                val catSpent = monthlyExpenses.filter { it.categoryId == b.categoryId }.sumOf { it.amount }
                Pair(catSpent, catName)
            }

            val pct = if (b.limitAmount > 0) ((spent / b.limitAmount) * 100).toFloat() else 0f
            val isExceeded = spent >= b.limitAmount
            val isWarning = !isExceeded && pct >= b.alertThresholdPercent

            results.add(
                BudgetStatus(
                    categoryId = b.categoryId,
                    categoryName = name,
                    limitAmount = b.limitAmount,
                    spentAmount = spent,
                    remainingAmount = (b.limitAmount - spent).coerceAtLeast(0.0),
                    percentageUsed = pct,
                    isExceeded = isExceeded,
                    isWarning = isWarning
                )
            )
        }
        return results
    }
}
