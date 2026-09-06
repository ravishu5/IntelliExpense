package com.intelliexpense.app.data.repository

import com.intelliexpense.app.core.model.AccountType
import com.intelliexpense.app.core.model.TransactionType
import com.intelliexpense.app.data.local.AccountDao
import com.intelliexpense.app.data.local.BudgetDao
import com.intelliexpense.app.data.local.CategoryDao
import com.intelliexpense.app.data.local.SavingsGoalDao
import com.intelliexpense.app.data.local.SplitDao
import com.intelliexpense.app.data.local.SubscriptionDao
import com.intelliexpense.app.data.local.TransactionDao
import com.intelliexpense.app.data.model.AccountEntity
import com.intelliexpense.app.data.model.BudgetEntity
import com.intelliexpense.app.data.model.CategoryEntity
import com.intelliexpense.app.data.model.SavingsGoalEntity
import com.intelliexpense.app.data.model.SplitExpenseEntity
import com.intelliexpense.app.data.model.SplitMemberEntity
import com.intelliexpense.app.data.model.SubscriptionEntity
import com.intelliexpense.app.data.model.TransactionEntity
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject

interface FinancialRepository {
    // Accounts
    fun getAllAccountsFlow(): Flow<List<AccountEntity>>
    suspend fun getAllAccounts(): List<AccountEntity>
    suspend fun getAccountById(id: String): AccountEntity?
    suspend fun findAccountByLast4(last4: String): AccountEntity?
    suspend fun saveAccount(account: AccountEntity)
    suspend fun deleteAccount(account: AccountEntity)

    // Transactions
    fun getAllTransactionsFlow(): Flow<List<TransactionEntity>>
    fun getTransactionsBetweenFlow(start: Long, end: Long): Flow<List<TransactionEntity>>
    suspend fun getTransactionsBetween(start: Long, end: Long): List<TransactionEntity>
    fun getUnreviewedTransactionsFlow(): Flow<List<TransactionEntity>>
    fun searchTransactionsFlow(query: String): Flow<List<TransactionEntity>>
    suspend fun getTransactionById(id: String): TransactionEntity?
    suspend fun findDuplicate(amount: Double, start: Long, end: Long, upiRef: String?): TransactionEntity?
    suspend fun recordTransaction(transaction: TransactionEntity): Boolean
    suspend fun updateTransaction(transaction: TransactionEntity)
    suspend fun deleteTransaction(id: String)
    suspend fun getTotalExpenseBetween(start: Long, end: Long): Double
    suspend fun getTotalIncomeBetween(start: Long, end: Long): Double
    suspend fun getTransactionCount(): Int

    // Categories
    fun getAllCategoriesFlow(): Flow<List<CategoryEntity>>
    suspend fun getAllCategories(): List<CategoryEntity>
    suspend fun saveCategory(category: CategoryEntity)

    // Budgets
    fun getBudgetsForMonthFlow(monthYear: String): Flow<List<BudgetEntity>>
    suspend fun getBudgetsForMonth(monthYear: String): List<BudgetEntity>
    suspend fun saveBudget(budget: BudgetEntity)
    suspend fun deleteBudget(id: String)

    // Subscriptions
    fun getActiveSubscriptionsFlow(): Flow<List<SubscriptionEntity>>
    suspend fun getActiveSubscriptions(): List<SubscriptionEntity>
    suspend fun saveSubscription(subscription: SubscriptionEntity)
    suspend fun deleteSubscription(id: String)

    // Savings Goals
    fun getAllGoalsFlow(): Flow<List<SavingsGoalEntity>>
    suspend fun getAllGoals(): List<SavingsGoalEntity>
    suspend fun saveGoal(goal: SavingsGoalEntity)
    suspend fun updateGoalAmount(id: String, currentAmount: Double)
    suspend fun deleteGoal(id: String)

    // Splits / Who Owes Me
    fun getAllSplitsFlow(): Flow<List<SplitExpenseEntity>>
    fun getSplitMembersFlow(splitId: String): Flow<List<SplitMemberEntity>>
    suspend fun getUnsettledMembers(): List<SplitMemberEntity>
    suspend fun saveSplit(split: SplitExpenseEntity, members: List<SplitMemberEntity>)
    suspend fun setMemberPaid(memberId: String, isPaid: Boolean)
    suspend fun setSplitSettled(splitId: String, settled: Boolean)
    suspend fun deleteSplit(splitId: String)

    // Export & Restore
    suspend fun exportDataJson(): String
    suspend fun restoreDataJson(jsonString: String): Boolean
}

class FinancialRepositoryImpl(
    private val accountDao: AccountDao,
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val budgetDao: BudgetDao,
    private val subscriptionDao: SubscriptionDao,
    private val savingsGoalDao: SavingsGoalDao,
    private val splitDao: SplitDao
) : FinancialRepository {

    override fun getAllAccountsFlow(): Flow<List<AccountEntity>> = accountDao.getAllAccountsFlow()
    override suspend fun getAllAccounts(): List<AccountEntity> = accountDao.getAllAccountsSync()
    override suspend fun getAccountById(id: String): AccountEntity? = accountDao.getAccountById(id)
    override suspend fun findAccountByLast4(last4: String): AccountEntity? = accountDao.findAccountByLast4(last4)
    override suspend fun saveAccount(account: AccountEntity) = accountDao.insertAccount(account)
    override suspend fun deleteAccount(account: AccountEntity) = accountDao.deleteAccount(account)

    override fun getAllTransactionsFlow(): Flow<List<TransactionEntity>> = transactionDao.getAllTransactionsFlow()
    override fun getTransactionsBetweenFlow(start: Long, end: Long): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsBetweenFlow(start, end)
    override suspend fun getTransactionsBetween(start: Long, end: Long): List<TransactionEntity> =
        transactionDao.getTransactionsBetweenSync(start, end)
    override fun getUnreviewedTransactionsFlow(): Flow<List<TransactionEntity>> =
        transactionDao.getUnreviewedTransactionsFlow()
    override fun searchTransactionsFlow(query: String): Flow<List<TransactionEntity>> =
        transactionDao.searchTransactionsFlow(query)
    override suspend fun getTransactionById(id: String): TransactionEntity? = transactionDao.getTransactionById(id)
    override suspend fun findDuplicate(amount: Double, start: Long, end: Long, upiRef: String?): TransactionEntity? =
        transactionDao.findDuplicate(amount, start, end, upiRef)

    override suspend fun recordTransaction(transaction: TransactionEntity): Boolean {
        // Insert transaction
        transactionDao.insertTransaction(transaction)

        // Adjust account balances
        when (transaction.type) {
            TransactionType.EXPENSE -> {
                accountDao.adjustBalance(transaction.accountId, -transaction.amount)
            }
            TransactionType.INCOME -> {
                accountDao.adjustBalance(transaction.accountId, transaction.amount)
            }
            TransactionType.REFUND -> {
                accountDao.adjustBalance(transaction.accountId, transaction.amount)
            }
            TransactionType.TRANSFER -> {
                accountDao.adjustBalance(transaction.accountId, -transaction.amount)
                transaction.targetAccountId?.let { targetId ->
                    accountDao.adjustBalance(targetId, transaction.amount)
                }
            }
        }
        return true
    }

    override suspend fun updateTransaction(transaction: TransactionEntity) {
        val old = transactionDao.getTransactionById(transaction.id)
        if (old != null) {
            // Revert old impact
            when (old.type) {
                TransactionType.EXPENSE -> accountDao.adjustBalance(old.accountId, old.amount)
                TransactionType.INCOME -> accountDao.adjustBalance(old.accountId, -old.amount)
                TransactionType.REFUND -> accountDao.adjustBalance(old.accountId, -old.amount)
                TransactionType.TRANSFER -> {
                    accountDao.adjustBalance(old.accountId, old.amount)
                    old.targetAccountId?.let { accountDao.adjustBalance(it, -old.amount) }
                }
            }
        }

        transactionDao.updateTransaction(transaction)

        // Apply new impact
        when (transaction.type) {
            TransactionType.EXPENSE -> accountDao.adjustBalance(transaction.accountId, -transaction.amount)
            TransactionType.INCOME -> accountDao.adjustBalance(transaction.accountId, transaction.amount)
            TransactionType.REFUND -> accountDao.adjustBalance(transaction.accountId, transaction.amount)
            TransactionType.TRANSFER -> {
                accountDao.adjustBalance(transaction.accountId, -transaction.amount)
                transaction.targetAccountId?.let { accountDao.adjustBalance(it, transaction.amount) }
            }
        }
    }

    override suspend fun deleteTransaction(id: String) {
        val old = transactionDao.getTransactionById(id) ?: return
        when (old.type) {
            TransactionType.EXPENSE -> accountDao.adjustBalance(old.accountId, old.amount)
            TransactionType.INCOME -> accountDao.adjustBalance(old.accountId, -old.amount)
            TransactionType.REFUND -> accountDao.adjustBalance(old.accountId, -old.amount)
            TransactionType.TRANSFER -> {
                accountDao.adjustBalance(old.accountId, old.amount)
                old.targetAccountId?.let { accountDao.adjustBalance(it, -old.amount) }
            }
        }
        transactionDao.deleteTransactionById(id)
    }

    override suspend fun getTotalExpenseBetween(start: Long, end: Long): Double =
        transactionDao.getTotalExpenseBetween(start, end) ?: 0.0

    override suspend fun getTotalIncomeBetween(start: Long, end: Long): Double =
        transactionDao.getTotalIncomeBetween(start, end) ?: 0.0

    override suspend fun getTransactionCount(): Int = transactionDao.getTransactionCount()

    override fun getAllCategoriesFlow(): Flow<List<CategoryEntity>> = categoryDao.getAllCategoriesFlow()
    override suspend fun getAllCategories(): List<CategoryEntity> = categoryDao.getAllCategoriesSync()
    override suspend fun saveCategory(category: CategoryEntity) = categoryDao.insertCategory(category)

    override fun getBudgetsForMonthFlow(monthYear: String): Flow<List<BudgetEntity>> =
        budgetDao.getBudgetsForMonthFlow(monthYear)
    override suspend fun getBudgetsForMonth(monthYear: String): List<BudgetEntity> =
        budgetDao.getBudgetsForMonthSync(monthYear)
    override suspend fun saveBudget(budget: BudgetEntity) = budgetDao.upsertBudget(budget)
    override suspend fun deleteBudget(id: String) = budgetDao.deleteBudget(id)

    override fun getActiveSubscriptionsFlow(): Flow<List<SubscriptionEntity>> =
        subscriptionDao.getAllActiveSubscriptionsFlow()
    override suspend fun getActiveSubscriptions(): List<SubscriptionEntity> =
        subscriptionDao.getAllActiveSubscriptionsSync()
    override suspend fun saveSubscription(subscription: SubscriptionEntity) =
        subscriptionDao.upsertSubscription(subscription)
    override suspend fun deleteSubscription(id: String) = subscriptionDao.deleteSubscription(id)

    override fun getAllGoalsFlow(): Flow<List<SavingsGoalEntity>> = savingsGoalDao.getAllGoalsFlow()
    override suspend fun getAllGoals(): List<SavingsGoalEntity> = savingsGoalDao.getAllGoalsSync()
    override suspend fun saveGoal(goal: SavingsGoalEntity) = savingsGoalDao.upsertGoal(goal)
    override suspend fun updateGoalAmount(id: String, currentAmount: Double) =
        savingsGoalDao.updateGoalAmount(id, currentAmount)
    override suspend fun deleteGoal(id: String) = savingsGoalDao.deleteGoal(id)

    override fun getAllSplitsFlow(): Flow<List<SplitExpenseEntity>> = splitDao.getAllSplitsFlow()
    override fun getSplitMembersFlow(splitId: String): Flow<List<SplitMemberEntity>> =
        splitDao.getSplitMembersFlow(splitId)
    override suspend fun getUnsettledMembers(): List<SplitMemberEntity> = splitDao.getUnsettledMembersSync()
    override suspend fun saveSplit(split: SplitExpenseEntity, members: List<SplitMemberEntity>) {
        splitDao.upsertSplit(split)
        splitDao.upsertMembers(members)
    }
    override suspend fun setMemberPaid(memberId: String, isPaid: Boolean) =
        splitDao.setMemberPaid(memberId, isPaid)
    override suspend fun setSplitSettled(splitId: String, settled: Boolean) =
        splitDao.setSplitSettled(splitId, settled)
    override suspend fun deleteSplit(splitId: String) {
        splitDao.deleteSplit(splitId)
        splitDao.deleteSplitMembers(splitId)
    }

    override suspend fun exportDataJson(): String {
        val root = JSONObject()
        val accountsArray = JSONArray()
        getAllAccounts().forEach { acc ->
            val obj = JSONObject().apply {
                put("id", acc.id)
                put("name", acc.name)
                put("type", acc.type.name)
                put("accountNumberLast4", acc.accountNumberLast4)
                put("ifscOrVpa", acc.ifscOrVpa)
                put("balance", acc.balance)
                put("currency", acc.currency)
                put("creditLimit", acc.creditLimit)
                put("statementDay", acc.statementDay)
                put("dueDay", acc.dueDay)
                put("colorHex", acc.colorHex)
                put("iconName", acc.iconName)
                put("createdAt", acc.createdAt)
            }
            accountsArray.put(obj)
        }
        root.put("accounts", accountsArray)

        val txnsArray = JSONArray()
        val allTxns = transactionDao.getTransactionsBetweenSync(0L, Long.MAX_VALUE)
        allTxns.forEach { txn ->
            val obj = JSONObject().apply {
                put("id", txn.id)
                put("accountId", txn.accountId)
                put("targetAccountId", txn.targetAccountId)
                put("type", txn.type.name)
                put("amount", txn.amount)
                put("currency", txn.currency)
                put("merchantOriginal", txn.merchantOriginal)
                put("merchantNormalized", txn.merchantNormalized)
                put("categoryId", txn.categoryId)
                put("subcategoryId", txn.subcategoryId)
                put("description", txn.description)
                put("timestamp", txn.timestamp)
                put("paymentMethod", txn.paymentMethod)
                put("upiReference", txn.upiReference)
                put("captureSource", txn.captureSource.name)
                put("confidenceScore", txn.confidenceScore)
                put("isReviewed", txn.isReviewed)
                put("tags", txn.tags)
            }
            txnsArray.put(obj)
        }
        root.put("transactions", txnsArray)
        root.put("exportedAt", System.currentTimeMillis())
        root.put("version", 1)

        return root.toString(2)
    }

    override suspend fun restoreDataJson(jsonString: String): Boolean {
        try {
            val root = JSONObject(jsonString)
            val accountsArray = root.optJSONArray("accounts") ?: JSONArray()
            for (i in 0 until accountsArray.length()) {
                val obj = accountsArray.getJSONObject(i)
                val acc = AccountEntity(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    type = AccountType.valueOf(obj.getString("type")),
                    accountNumberLast4 = obj.optString("accountNumberLast4").takeIf { it.isNotEmpty() },
                    ifscOrVpa = obj.optString("ifscOrVpa").takeIf { it.isNotEmpty() },
                    balance = obj.getDouble("balance"),
                    currency = obj.optString("currency", "INR"),
                    creditLimit = if (obj.has("creditLimit") && !obj.isNull("creditLimit")) obj.getDouble("creditLimit") else null,
                    statementDay = if (obj.has("statementDay") && !obj.isNull("statementDay")) obj.getInt("statementDay") else null,
                    dueDay = if (obj.has("dueDay") && !obj.isNull("dueDay")) obj.getInt("dueDay") else null,
                    colorHex = obj.optString("colorHex", "#10B981"),
                    iconName = obj.optString("iconName", "account_balance"),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                )
                accountDao.insertAccount(acc)
            }

            val txnsArray = root.optJSONArray("transactions") ?: JSONArray()
            for (i in 0 until txnsArray.length()) {
                val obj = txnsArray.getJSONObject(i)
                val txn = TransactionEntity(
                    id = obj.getString("id"),
                    accountId = obj.getString("accountId"),
                    targetAccountId = obj.optString("targetAccountId").takeIf { it.isNotEmpty() },
                    type = TransactionType.valueOf(obj.getString("type")),
                    amount = obj.getDouble("amount"),
                    currency = obj.optString("currency", "INR"),
                    merchantOriginal = obj.getString("merchantOriginal"),
                    merchantNormalized = obj.getString("merchantNormalized"),
                    categoryId = obj.getString("categoryId"),
                    subcategoryId = obj.optString("subcategoryId").takeIf { it.isNotEmpty() },
                    description = obj.optString("description").takeIf { it.isNotEmpty() },
                    timestamp = obj.getLong("timestamp"),
                    paymentMethod = obj.optString("paymentMethod", "UPI"),
                    upiReference = obj.optString("upiReference").takeIf { it.isNotEmpty() },
                    captureSource = com.intelliexpense.app.core.model.CaptureSource.valueOf(obj.optString("captureSource", "MANUAL")),
                    confidenceScore = obj.optDouble("confidenceScore", 1.0).toFloat(),
                    isReviewed = obj.optBoolean("isReviewed", true),
                    tags = obj.optString("tags", "")
                )
                transactionDao.insertTransaction(txn)
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
}
