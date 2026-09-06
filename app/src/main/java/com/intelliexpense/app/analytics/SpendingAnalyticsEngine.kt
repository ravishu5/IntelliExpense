package com.intelliexpense.app.analytics

import com.intelliexpense.app.core.model.TransactionType
import com.intelliexpense.app.core.util.DateUtils
import com.intelliexpense.app.data.model.TransactionEntity
import java.util.Calendar
import kotlin.math.abs

data class CategorySpend(
    val categoryId: String,
    val categoryName: String,
    val totalAmount: Double,
    val percentage: Float,
    val transactionCount: Int
)

data class MerchantSpend(
    val merchantName: String,
    val totalAmount: Double,
    val transactionCount: Int
)

data class MonthComparison(
    val currentMonthSpend: Double,
    val previousMonthSpend: Double,
    val percentageChange: Float, // +20.5% or -10.2%
    val isHigher: Boolean
)

data class SpendingAnalyticsSummary(
    val totalExpense: Double,
    val totalIncome: Double,
    val netSavings: Double,
    val savingsRate: Float, // e.g. 35.0%
    val dailyAverage: Double,
    val projectedMonthEndSpend: Double,
    val monthComparison: MonthComparison,
    val categoryBreakdown: List<CategorySpend>,
    val topMerchants: List<MerchantSpend>
)

object SpendingAnalyticsEngine {

    fun generateMonthlyAnalytics(
        transactions: List<TransactionEntity>,
        categoryNameMap: Map<String, String>,
        targetCal: Calendar = Calendar.getInstance()
    ): SpendingAnalyticsSummary {
        val currentStart = DateUtils.getStartOfMonth(targetCal)
        val currentEnd = DateUtils.getEndOfMonth(targetCal)

        val prevCal = targetCal.clone() as Calendar
        prevCal.add(Calendar.MONTH, -1)
        val prevStart = DateUtils.getStartOfMonth(prevCal)
        val prevEnd = DateUtils.getEndOfMonth(prevCal)

        val currentMonthTxns = transactions.filter { it.timestamp in currentStart..currentEnd }
        val prevMonthTxns = transactions.filter { it.timestamp in prevStart..prevEnd }

        val currentExpense = currentMonthTxns
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }

        val currentIncome = currentMonthTxns
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }

        val prevExpense = prevMonthTxns
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }

        val netSavings = currentIncome - currentExpense
        val savingsRate = if (currentIncome > 0) ((netSavings / currentIncome) * 100).toFloat().coerceIn(-100f, 100f) else 0f

        val pctChange = if (prevExpense > 0) {
            (((currentExpense - prevExpense) / prevExpense) * 100).toFloat()
        } else {
            0f
        }

        // Daily average and projected spend
        val now = Calendar.getInstance()
        val currentDay = if (targetCal.get(Calendar.MONTH) == now.get(Calendar.MONTH)) {
            now.get(Calendar.DAY_OF_MONTH).coerceAtLeast(1)
        } else {
            targetCal.getActualMaximum(Calendar.DAY_OF_MONTH)
        }
        val daysInMonth = targetCal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val dailyAverage = if (currentDay > 0) currentExpense / currentDay else 0.0
        val projectedMonthEnd = dailyAverage * daysInMonth

        // Category breakdown
        val expenseTxns = currentMonthTxns.filter { it.type == TransactionType.EXPENSE }
        val categoryBreakdown = expenseTxns.groupBy { it.categoryId }
            .map { (catId, txns) ->
                val sum = txns.sumOf { it.amount }
                val pct = if (currentExpense > 0) ((sum / currentExpense) * 100).toFloat() else 0f
                val name = categoryNameMap[catId] ?: catId.replace('_', ' ').replaceFirstChar { it.uppercase() }
                CategorySpend(
                    categoryId = catId,
                    categoryName = name,
                    totalAmount = sum,
                    percentage = pct,
                    transactionCount = txns.size
                )
            }
            .sortedByDescending { it.totalAmount }

        // Top merchants
        val topMerchants = expenseTxns.groupBy { it.merchantNormalized }
            .map { (merchant, txns) ->
                MerchantSpend(
                    merchantName = merchant,
                    totalAmount = txns.sumOf { it.amount },
                    transactionCount = txns.size
                )
            }
            .sortedByDescending { it.totalAmount }
            .take(5)

        return SpendingAnalyticsSummary(
            totalExpense = currentExpense,
            totalIncome = currentIncome,
            netSavings = netSavings,
            savingsRate = savingsRate,
            dailyAverage = dailyAverage,
            projectedMonthEndSpend = projectedMonthEnd,
            monthComparison = MonthComparison(
                currentMonthSpend = currentExpense,
                previousMonthSpend = prevExpense,
                percentageChange = abs(pctChange),
                isHigher = pctChange > 0
            ),
            categoryBreakdown = categoryBreakdown,
            topMerchants = topMerchants
        )
    }
}
