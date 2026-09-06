package com.intelliexpense.app.intelligence

import com.intelliexpense.app.core.model.SubscriptionFrequency
import com.intelliexpense.app.data.model.SubscriptionEntity
import com.intelliexpense.app.data.model.TransactionEntity
import java.util.Calendar
import java.util.UUID
import kotlin.math.abs

data class DetectedSubscription(
    val merchantName: String,
    val amount: Double,
    val frequency: SubscriptionFrequency,
    val billingDay: Int,
    val nextEstimatedDueDate: Long,
    val categoryId: String,
    val confidence: Float
)

object SubscriptionDetector {

    /**
     * Checks if a single transaction is a known subscription merchant from registry.
     */
    fun checkSingleTransaction(txn: TransactionEntity): DetectedSubscription? {
        val norm = IndianMerchantRegistry.normalize(txn.merchantOriginal)
        if (norm.isKnownSubscription) {
            val cal = Calendar.getInstance().apply { timeInMillis = txn.timestamp }
            val billingDay = cal.get(Calendar.DAY_OF_MONTH)
            cal.add(Calendar.MONTH, 1)
            val nextDueDate = cal.timeInMillis

            return DetectedSubscription(
                merchantName = norm.brandName,
                amount = txn.amount,
                frequency = SubscriptionFrequency.MONTHLY,
                billingDay = billingDay,
                nextEstimatedDueDate = nextDueDate,
                categoryId = norm.defaultCategoryId,
                confidence = 0.90f
            )
        }
        return null
    }

    /**
     * Analyzes historical transactions for periodic recurring cadences.
     */
    fun analyzeHistory(transactions: List<TransactionEntity>): List<DetectedSubscription> {
        val detected = mutableListOf<DetectedSubscription>()
        val grouped = transactions.groupBy { it.merchantNormalized }

        for ((merchant, txns) in grouped) {
            if (txns.size < 2) continue
            val sorted = txns.sortedBy { it.timestamp }

            // Check intervals between consecutive transactions
            val intervals = mutableListOf<Long>()
            var amountMatches = true
            val baseAmount = sorted.first().amount

            for (i in 0 until sorted.size - 1) {
                val diffDays = (sorted[i + 1].timestamp - sorted[i].timestamp) / (24 * 60 * 60 * 1000L)
                intervals.add(diffDays)

                // Check amount within 5% tolerance
                if (abs(sorted[i + 1].amount - baseAmount) > baseAmount * 0.05) {
                    amountMatches = false
                }
            }

            if (!amountMatches) continue

            val avgInterval = intervals.average()
            val frequency = when {
                avgInterval in 6.0..8.0 -> SubscriptionFrequency.WEEKLY
                avgInterval in 27.0..33.0 -> SubscriptionFrequency.MONTHLY
                avgInterval in 85.0..95.0 -> SubscriptionFrequency.QUARTERLY
                avgInterval in 350.0..375.0 -> SubscriptionFrequency.YEARLY
                else -> null
            }

            if (frequency != null) {
                val latest = sorted.last()
                val cal = Calendar.getInstance().apply { timeInMillis = latest.timestamp }
                val billingDay = cal.get(Calendar.DAY_OF_MONTH)
                when (frequency) {
                    SubscriptionFrequency.WEEKLY -> cal.add(Calendar.DAY_OF_YEAR, 7)
                    SubscriptionFrequency.MONTHLY -> cal.add(Calendar.MONTH, 1)
                    SubscriptionFrequency.QUARTERLY -> cal.add(Calendar.MONTH, 3)
                    SubscriptionFrequency.YEARLY -> cal.add(Calendar.YEAR, 1)
                }

                detected.add(
                    DetectedSubscription(
                        merchantName = merchant,
                        amount = latest.amount,
                        frequency = frequency,
                        billingDay = billingDay,
                        nextEstimatedDueDate = cal.timeInMillis,
                        categoryId = latest.categoryId,
                        confidence = 0.85f
                    )
                )
            }
        }
        return detected
    }
}
