package com.intelliexpense.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.intelliexpense.app.core.model.AccountType
import com.intelliexpense.app.core.model.CategoryType
import com.intelliexpense.app.core.security.SecurityManager
import com.intelliexpense.app.data.model.AccountEntity
import com.intelliexpense.app.data.model.BudgetEntity
import com.intelliexpense.app.data.model.CategoryEntity
import com.intelliexpense.app.data.model.SavingsGoalEntity
import com.intelliexpense.app.data.model.SplitExpenseEntity
import com.intelliexpense.app.data.model.SplitMemberEntity
import com.intelliexpense.app.data.model.SubscriptionEntity
import com.intelliexpense.app.data.model.TransactionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.sqlcipher.database.SupportFactory

@Database(
    entities = [
        AccountEntity::class,
        TransactionEntity::class,
        CategoryEntity::class,
        BudgetEntity::class,
        SubscriptionEntity::class,
        SavingsGoalEntity::class,
        SplitExpenseEntity::class,
        SplitMemberEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class IntelliExpenseDatabase : RoomDatabase() {

    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun savingsGoalDao(): SavingsGoalDao
    abstract fun splitDao(): SplitDao

    companion object {
        private const val DB_NAME = "intelliexpense_encrypted.db"

        @Volatile
        private var INSTANCE: IntelliExpenseDatabase? = null

        fun getInstance(context: Context, useEncryption: Boolean = true): IntelliExpenseDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context.applicationContext, useEncryption).also { INSTANCE = it }
            }
        }

        fun buildInMemory(context: Context): IntelliExpenseDatabase {
            return Room.inMemoryDatabaseBuilder(context, IntelliExpenseDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        }

        private fun buildDatabase(appContext: Context, useEncryption: Boolean): IntelliExpenseDatabase {
            val builder = Room.databaseBuilder(appContext, IntelliExpenseDatabase::class.java, DB_NAME)

            if (useEncryption) {
                try {
                    val passphrase = SecurityManager.getInstance(appContext).getDatabasePassphrase()
                    val factory = SupportFactory(passphrase)
                    builder.openHelperFactory(factory)
                } catch (e: Throwable) {
                    // Fallback to standard helper if SQLCipher binary not present on target arch
                }
            }

            builder.addCallback(object : Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    CoroutineScope(Dispatchers.IO).launch {
                        seedInitialData(getInstance(appContext, useEncryption))
                    }
                }
            })

            return builder.build()
        }

        suspend fun seedInitialData(database: IntelliExpenseDatabase) {
            val categoryDao = database.categoryDao()
            val accountDao = database.accountDao()

            val defaultCategories = listOf(
                CategoryEntity("food_dining", "Food & Dining", CategoryType.EXPENSE, "restaurant", "#EF4444", false, 8000.0, 1),
                CategoryEntity("groceries", "Groceries & Daily", CategoryType.EXPENSE, "shopping_cart", "#10B981", false, 6000.0, 2),
                CategoryEntity("travel_mobility", "Travel & Commute", CategoryType.EXPENSE, "directions_car", "#3B82F6", false, 4000.0, 3),
                CategoryEntity("shopping", "Shopping & Lifestyle", CategoryType.EXPENSE, "shopping_bag", "#EC4899", false, 5000.0, 4),
                CategoryEntity("bills_utilities", "Bills & Utilities", CategoryType.EXPENSE, "receipt_long", "#F59E0B", false, 4000.0, 5),
                CategoryEntity("entertainment", "Entertainment & OTT", CategoryType.EXPENSE, "movie", "#8B5CF6", false, 2000.0, 6),
                CategoryEntity("healthcare", "Health & Medical", CategoryType.EXPENSE, "local_pharmacy", "#06B6D4", false, 3000.0, 7),
                CategoryEntity("investments", "Investments & Wealth", CategoryType.EXPENSE, "trending_up", "#6366F1", false, 15000.0, 8),
                CategoryEntity("home_maintenance", "Home & Rent", CategoryType.EXPENSE, "home", "#64748B", false, 18000.0, 9),
                CategoryEntity("education", "Education & Courses", CategoryType.EXPENSE, "school", "#14B8A6", false, 2000.0, 10),
                CategoryEntity("salary_income", "Salary & Wages", CategoryType.INCOME, "account_balance_wallet", "#10B981", false, null, 11),
                CategoryEntity("rewards_cashback", "Cashback & Refunds", CategoryType.INCOME, "savings", "#F59E0B", false, null, 12),
                CategoryEntity("transfers", "Transfers & P2P", CategoryType.EXPENSE, "swap_horiz", "#94A3B8", false, null, 13)
            )
            categoryDao.insertCategories(defaultCategories)

            val defaultAccounts = listOf(
                AccountEntity(
                    id = "acc_primary_bank",
                    name = "Primary Bank (UPI)",
                    type = AccountType.BANK,
                    accountNumberLast4 = "1234",
                    ifscOrVpa = "user@upi",
                    balance = 25000.0,
                    colorHex = "#3B82F6",
                    iconName = "account_balance"
                ),
                AccountEntity(
                    id = "acc_cash",
                    name = "Cash Wallet",
                    type = AccountType.CASH,
                    balance = 3500.0,
                    colorHex = "#10B981",
                    iconName = "payments"
                ),
                AccountEntity(
                    id = "acc_credit_card",
                    name = "Credit Card",
                    type = AccountType.CREDIT_CARD,
                    accountNumberLast4 = "9876",
                    balance = -8450.0, // negative represents utilized credit
                    creditLimit = 150000.0,
                    statementDay = 15,
                    dueDay = 5,
                    colorHex = "#8B5CF6",
                    iconName = "credit_card"
                )
            )
            accountDao.insertAccounts(defaultAccounts)
        }
    }
}
