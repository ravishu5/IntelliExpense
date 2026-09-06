package com.intelliexpense.app

import com.intelliexpense.app.core.model.AccountType
import com.intelliexpense.app.core.model.TransactionType
import com.intelliexpense.app.data.local.TransactionDao
import com.intelliexpense.app.data.model.AccountEntity
import com.intelliexpense.app.data.model.TransactionEntity
import com.intelliexpense.app.intelligence.TransactionDeduplicator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TransactionDeduplicatorTest {

    // In-memory fake TransactionDao for fast unit testing
    private val fakeTransactions = mutableListOf<TransactionEntity>()
    private val fakeDao = object : TransactionDao {
        override fun getAllTransactionsFlow(): Flow<List<TransactionEntity>> = emptyFlow()
        override fun getTransactionsBetweenFlow(start: Long, end: Long): Flow<List<TransactionEntity>> = emptyFlow()
        override suspend fun getTransactionsBetweenSync(start: Long, end: Long): List<TransactionEntity> =
            fakeTransactions.filter { it.timestamp in start..end }
        override fun getUnreviewedTransactionsFlow(): Flow<List<TransactionEntity>> = emptyFlow()
        override fun searchTransactionsFlow(query: String): Flow<List<TransactionEntity>> = emptyFlow()
        override suspend fun getTransactionById(id: String): TransactionEntity? = fakeTransactions.firstOrNull { it.id == id }
        override suspend fun findDuplicate(amount: Double, start: Long, end: Long, upiRef: String?): TransactionEntity? =
            fakeTransactions.firstOrNull { it.amount == amount && it.timestamp in start..end && (upiRef == null || it.upiReference == upiRef) }
        override suspend fun findDuplicateFuzzy(amount: Double, start: Long, end: Long, merchantNorm: String): TransactionEntity? =
            fakeTransactions.firstOrNull { it.amount == amount && it.timestamp in start..end && it.merchantNormalized == merchantNorm }
        override suspend fun findMatchingPriorExpense(merchantNorm: String, amount: Double, beforeTimestamp: Long): TransactionEntity? =
            fakeTransactions.firstOrNull { it.type == TransactionType.EXPENSE && it.amount == amount && it.merchantNormalized == merchantNorm && it.timestamp < beforeTimestamp }
        override suspend fun insertTransaction(transaction: TransactionEntity) { fakeTransactions.add(transaction) }
        override suspend fun insertTransactions(transactions: List<TransactionEntity>) { fakeTransactions.addAll(transactions) }
        override suspend fun updateTransaction(transaction: TransactionEntity) {}
        override suspend fun deleteTransactionById(id: String) { fakeTransactions.removeIf { it.id == id } }
        override suspend fun getTotalExpenseBetween(start: Long, end: Long): Double? = null
        override suspend fun getTotalIncomeBetween(start: Long, end: Long): Double? = null
        override suspend fun getTransactionCount(): Int = fakeTransactions.size
    }

    private val deduplicator = TransactionDeduplicator(fakeDao)

    @Test
    fun testUtrExactDuplicateDetection() = runBlocking {
        val now = System.currentTimeMillis()
        fakeTransactions.add(
            TransactionEntity(
                id = "txn_1",
                accountId = "acc_bank",
                type = TransactionType.EXPENSE,
                amount = 450.0,
                merchantOriginal = "Swiggy",
                merchantNormalized = "Swiggy",
                categoryId = "food_dining",
                upiReference = "425012345678",
                timestamp = now
            )
        )

        val result = deduplicator.checkDuplicate(
            amount = 450.0,
            timestamp = now + 1000,
            upiReference = "425012345678",
            merchantNormalized = "Swiggy"
        )

        assertTrue(result.isDuplicate)
        assertEquals("txn_1", result.existingTransactionId)
    }

    @Test
    fun testFuzzyDuplicateDetectionWithinWindow() = runBlocking {
        val now = System.currentTimeMillis()
        fakeTransactions.add(
            TransactionEntity(
                id = "txn_2",
                accountId = "acc_bank",
                type = TransactionType.EXPENSE,
                amount = 1200.0,
                merchantOriginal = "Blinkit",
                merchantNormalized = "Blinkit",
                categoryId = "groceries",
                upiReference = null,
                timestamp = now
            )
        )

        // Incoming transaction 2 minutes later with same amount and merchant
        val result = deduplicator.checkDuplicate(
            amount = 1200.0,
            timestamp = now + (2 * 60 * 1000L),
            upiReference = null,
            merchantNormalized = "Blinkit"
        )

        assertTrue(result.isDuplicate)
        assertEquals("txn_2", result.existingTransactionId)
    }

    @Test
    fun testNonDuplicateOutsideWindow() = runBlocking {
        val now = System.currentTimeMillis()
        fakeTransactions.add(
            TransactionEntity(
                id = "txn_3",
                accountId = "acc_bank",
                type = TransactionType.EXPENSE,
                amount = 500.0,
                merchantOriginal = "Starbucks",
                merchantNormalized = "Starbucks",
                categoryId = "food_dining",
                timestamp = now - (60 * 60 * 1000L) // 1 hour ago
            )
        )

        val result = deduplicator.checkDuplicate(
            amount = 500.0,
            timestamp = now,
            upiReference = null,
            merchantNormalized = "Starbucks"
        )

        assertFalse(result.isDuplicate)
    }

    @Test
    fun testSelfTransferDetection() {
        val accounts = listOf(
            AccountEntity("acc_hdfc", "HDFC Salary", AccountType.BANK, accountNumberLast4 = "1234"),
            AccountEntity("acc_sbi", "SBI Savings", AccountType.BANK, accountNumberLast4 = "5678")
        )

        val result = deduplicator.detectSelfTransfer(
            description = "Transfer to own account ending 5678",
            userAccounts = accounts,
            amount = 15000.0
        )

        assertTrue(result.isSelfTransfer)
        assertEquals("acc_sbi", result.targetAccountId)
    }
}
