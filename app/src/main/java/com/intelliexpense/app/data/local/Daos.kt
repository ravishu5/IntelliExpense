package com.intelliexpense.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.intelliexpense.app.data.model.AccountEntity
import com.intelliexpense.app.data.model.BudgetEntity
import com.intelliexpense.app.data.model.CategoryEntity
import com.intelliexpense.app.data.model.SavingsGoalEntity
import com.intelliexpense.app.data.model.SplitExpenseEntity
import com.intelliexpense.app.data.model.SplitMemberEntity
import com.intelliexpense.app.data.model.SubscriptionEntity
import com.intelliexpense.app.data.model.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts WHERE isArchived = 0 ORDER BY type ASC, name ASC")
    fun getAllAccountsFlow(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE isArchived = 0")
    suspend fun getAllAccountsSync(): List<AccountEntity>

    @Query("SELECT * FROM accounts WHERE id = :id LIMIT 1")
    suspend fun getAccountById(id: String): AccountEntity?

    @Query("SELECT * FROM accounts WHERE accountNumberLast4 = :last4 LIMIT 1")
    suspend fun findAccountByLast4(last4: String): AccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccounts(accounts: List<AccountEntity>)

    @Update
    suspend fun updateAccount(account: AccountEntity)

    @Query("UPDATE accounts SET balance = balance + :delta, updatedAt = :now WHERE id = :id")
    suspend fun adjustBalance(id: String, delta: Double, now: Long = System.currentTimeMillis())

    @Query("UPDATE accounts SET balance = :balance, updatedAt = :now WHERE id = :id")
    suspend fun setBalance(id: String, balance: Double, now: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteAccount(account: AccountEntity)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactionsFlow(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE timestamp >= :start AND timestamp <= :end ORDER BY timestamp DESC")
    fun getTransactionsBetweenFlow(start: Long, end: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE timestamp >= :start AND timestamp <= :end ORDER BY timestamp DESC")
    suspend fun getTransactionsBetweenSync(start: Long, end: Long): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE isReviewed = 0 ORDER BY timestamp DESC")
    fun getUnreviewedTransactionsFlow(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE merchantNormalized LIKE '%' || :query || '%' OR merchantOriginal LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchTransactionsFlow(query: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: String): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE amount = :amount AND timestamp >= :start AND timestamp <= :end AND (:upiRef IS NULL OR upiReference = :upiRef) LIMIT 1")
    suspend fun findDuplicate(amount: Double, start: Long, end: Long, upiRef: String?): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE amount = :amount AND timestamp >= :start AND timestamp <= :end AND merchantNormalized = :merchantNorm LIMIT 1")
    suspend fun findDuplicateFuzzy(amount: Double, start: Long, end: Long, merchantNorm: String): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE amount = :amount AND type = 'EXPENSE' AND merchantNormalized = :merchantNorm AND timestamp < :beforeTimestamp ORDER BY timestamp DESC LIMIT 1")
    suspend fun findMatchingPriorExpense(merchantNorm: String, amount: Double, beforeTimestamp: Long): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: String)

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'EXPENSE' AND timestamp >= :start AND timestamp <= :end")
    suspend fun getTotalExpenseBetween(start: Long, end: Long): Double?

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'INCOME' AND timestamp >= :start AND timestamp <= :end")
    suspend fun getTotalIncomeBetween(start: Long, end: Long): Double?

    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun getTransactionCount(): Int
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY sortOrder ASC, name ASC")
    fun getAllCategoriesFlow(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY sortOrder ASC, name ASC")
    suspend fun getAllCategoriesSync(): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE id = :id LIMIT 1")
    suspend fun getCategoryById(id: String): CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)
}

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets WHERE monthYear = :monthYear")
    fun getBudgetsForMonthFlow(monthYear: String): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE monthYear = :monthYear")
    suspend fun getBudgetsForMonthSync(monthYear: String): List<BudgetEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBudget(budget: BudgetEntity)

    @Query("DELETE FROM budgets WHERE id = :id")
    suspend fun deleteBudget(id: String)
}

@Dao
interface SubscriptionDao {
    @Query("SELECT * FROM subscriptions WHERE isActive = 1 ORDER BY nextDueDate ASC")
    fun getAllActiveSubscriptionsFlow(): Flow<List<SubscriptionEntity>>

    @Query("SELECT * FROM subscriptions ORDER BY nextDueDate ASC")
    fun getAllSubscriptionsFlow(): Flow<List<SubscriptionEntity>>

    @Query("SELECT * FROM subscriptions WHERE isActive = 1")
    suspend fun getAllActiveSubscriptionsSync(): List<SubscriptionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSubscription(subscription: SubscriptionEntity)

    @Query("DELETE FROM subscriptions WHERE id = :id")
    suspend fun deleteSubscription(id: String)
}

@Dao
interface SavingsGoalDao {
    @Query("SELECT * FROM savings_goals ORDER BY isCompleted ASC, targetAmount DESC")
    fun getAllGoalsFlow(): Flow<List<SavingsGoalEntity>>

    @Query("SELECT * FROM savings_goals")
    suspend fun getAllGoalsSync(): List<SavingsGoalEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGoal(goal: SavingsGoalEntity)

    @Query("UPDATE savings_goals SET currentAmount = :amount WHERE id = :id")
    suspend fun updateGoalAmount(id: String, amount: Double)

    @Query("DELETE FROM savings_goals WHERE id = :id")
    suspend fun deleteGoal(id: String)
}

@Dao
interface SplitDao {
    @Query("SELECT * FROM splits ORDER BY timestamp DESC")
    fun getAllSplitsFlow(): Flow<List<SplitExpenseEntity>>

    @Query("SELECT * FROM splits")
    suspend fun getAllSplitsSync(): List<SplitExpenseEntity>

    @Query("SELECT * FROM split_members WHERE splitId = :splitId")
    fun getSplitMembersFlow(splitId: String): Flow<List<SplitMemberEntity>>

    @Query("SELECT * FROM split_members WHERE isPaid = 0")
    suspend fun getUnsettledMembersSync(): List<SplitMemberEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSplit(split: SplitExpenseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMembers(members: List<SplitMemberEntity>)

    @Query("UPDATE split_members SET isPaid = :isPaid WHERE id = :memberId")
    suspend fun setMemberPaid(memberId: String, isPaid: Boolean)

    @Query("UPDATE splits SET settled = :settled WHERE id = :splitId")
    suspend fun setSplitSettled(splitId: String, settled: Boolean)
}
