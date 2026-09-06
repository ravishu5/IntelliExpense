package com.intelliexpense.app.intelligence

import com.intelliexpense.app.core.model.TransactionType
import com.intelliexpense.app.data.local.TransactionDao
import com.intelliexpense.app.data.model.AccountEntity
import com.intelliexpense.app.data.model.TransactionEntity
import kotlin.math.abs

data class DeduplicationResult(
    val isDuplicate: Boolean,
    val existingTransactionId: String? = null,
    val reason: String? = null
)

data class SelfTransferResult(
    val isSelfTransfer: Boolean,
    val sourceAccountId: String? = null,
    val targetAccountId: String? = null
)

class TransactionDeduplicator(private val transactionDao: TransactionDao) {

    /**
     * Checks if an incoming captured transaction (e.g. from SMS or Share Sheet) is already recorded.
     * Time window: +/- 10 minutes.
     */
    suspend fun checkDuplicate(
        amount: Double,
        timestamp: Long,
        upiReference: String?,
        merchantNormalized: String,
        windowMillis: Long = 10 * 60 * 1000L
    ): DeduplicationResult {
        val start = timestamp - windowMillis
        val end = timestamp + windowMillis

        // 1. Strict UTR check if available
        if (!upiReference.isNullOrBlank()) {
            val match = transactionDao.findDuplicate(amount, start, end, upiReference)
            if (match != null) {
                return DeduplicationResult(
                    isDuplicate = true,
                    existingTransactionId = match.id,
                    reason = "Exact UTR match ($upiReference) with transaction ${match.id}"
                )
            }
        }

        // 2. Exact amount & normalized merchant check within time window
        val fuzzyMatch = transactionDao.findDuplicateFuzzy(amount, start, end, merchantNormalized)
        if (fuzzyMatch != null) {
            return DeduplicationResult(
                isDuplicate = true,
                existingTransactionId = fuzzyMatch.id,
                reason = "Matching amount (₹$amount) and merchant ($merchantNormalized) within 10 minutes"
            )
        }

        return DeduplicationResult(isDuplicate = false)
    }

    /**
     * Detects if an incoming transaction is a self-transfer between the user's own bank/UPI accounts.
     */
    fun detectSelfTransfer(
        description: String?,
        userAccounts: List<AccountEntity>,
        amount: Double
    ): SelfTransferResult {
        val desc = description?.lowercase().orEmpty()

        val selfKeywords = listOf("self transfer", "transfer to own", "to self", "from self", "own account")
        val hasSelfKeyword = selfKeywords.any { desc.contains(it) }

        // Check if description mentions another known account last4 or name
        for (acc in userAccounts) {
            val last4 = acc.accountNumberLast4
            if (last4 != null && last4.isNotBlank() && desc.contains(last4)) {
                return SelfTransferResult(
                    isSelfTransfer = true,
                    targetAccountId = acc.id
                )
            }
            if (acc.name.isNotBlank() && desc.contains(acc.name.lowercase())) {
                return SelfTransferResult(
                    isSelfTransfer = true,
                    targetAccountId = acc.id
                )
            }
        }

        if (hasSelfKeyword) {
            return SelfTransferResult(isSelfTransfer = true)
        }

        return SelfTransferResult(isSelfTransfer = false)
    }

    /**
     * Detects if an incoming credit transaction is a refund for a previous expense.
     */
    suspend fun detectRefund(
        merchantNormalized: String,
        amount: Double,
        creditTimestamp: Long,
        lookbackDays: Int = 30
    ): Boolean {
        val lookbackStart = creditTimestamp - (lookbackDays * 24 * 60 * 60 * 1000L)
        val priorExpense = transactionDao.findMatchingPriorExpense(
            merchantNorm = merchantNormalized,
            amount = amount,
            beforeTimestamp = creditTimestamp
        )
        return priorExpense != null && priorExpense.timestamp >= lookbackStart
    }
}
