package com.intelliexpense.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.intelliexpense.app.core.model.AccountType
import com.intelliexpense.app.core.model.CaptureSource
import com.intelliexpense.app.core.model.CategoryType
import com.intelliexpense.app.core.model.SubscriptionFrequency
import com.intelliexpense.app.core.model.TransactionType

@Entity(
    tableName = "accounts",
    indices = [Index(value = ["type"]), Index(value = ["accountNumberLast4"])]
)
data class AccountEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: AccountType,
    val accountNumberLast4: String? = null,
    val ifscOrVpa: String? = null,
    val balance: Double = 0.0,
    val currency: String = "INR",
    val creditLimit: Double? = null,
    val statementDay: Int? = null,
    val dueDay: Int? = null,
    val colorHex: String = "#10B981",
    val iconName: String = "account_balance",
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["timestamp"]),
        Index(value = ["accountId"]),
        Index(value = ["categoryId"]),
        Index(value = ["merchantNormalized"]),
        Index(value = ["upiReference"]),
        Index(value = ["isReviewed"])
    ]
)
data class TransactionEntity(
    @PrimaryKey val id: String,
    val accountId: String,
    val targetAccountId: String? = null,
    val type: TransactionType,
    val amount: Double,
    val currency: String = "INR",
    val merchantOriginal: String,
    val merchantNormalized: String,
    val categoryId: String,
    val subcategoryId: String? = null,
    val description: String? = null,
    val timestamp: Long,
    val paymentMethod: String = "UPI",
    val upiReference: String? = null,
    val rawSourceText: String? = null,
    val captureSource: CaptureSource = CaptureSource.MANUAL,
    val confidenceScore: Float = 1.0f,
    val isReviewed: Boolean = true,
    val isRecurring: Boolean = false,
    val recurringId: String? = null,
    val splitId: String? = null,
    val tags: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: CategoryType = CategoryType.EXPENSE,
    val icon: String = "category",
    val colorHex: String = "#3B82F6",
    val isCustom: Boolean = false,
    val budgetMonthly: Double? = null,
    val sortOrder: Int = 0
)

@Entity(
    tableName = "budgets",
    indices = [Index(value = ["categoryId", "monthYear"], unique = true)]
)
data class BudgetEntity(
    @PrimaryKey val id: String,
    val categoryId: String? = null, // null indicates overall monthly budget
    val monthYear: String, // e.g. "2026-09"
    val limitAmount: Double,
    val alertThresholdPercent: Int = 80,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "subscriptions",
    indices = [Index(value = ["merchantName"]), Index(value = ["nextDueDate"])]
)
data class SubscriptionEntity(
    @PrimaryKey val id: String,
    val merchantName: String,
    val amount: Double,
    val frequency: SubscriptionFrequency = SubscriptionFrequency.MONTHLY,
    val billingDay: Int = 1,
    val nextDueDate: Long,
    val categoryId: String = "entertainment",
    val accountId: String? = null,
    val isActive: Boolean = true,
    val autoDetected: Boolean = false,
    val reminderEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "savings_goals")
data class SavingsGoalEntity(
    @PrimaryKey val id: String,
    val title: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val targetDate: Long? = null,
    val category: String = "Emergency Fund",
    val isCompleted: Boolean = false,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "splits")
data class SplitExpenseEntity(
    @PrimaryKey val id: String,
    val title: String,
    val totalAmount: Double,
    val transactionId: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val settled: Boolean = false,
    val notes: String? = null
)

@Entity(
    tableName = "split_members",
    indices = [Index(value = ["splitId"])]
)
data class SplitMemberEntity(
    @PrimaryKey val id: String,
    val splitId: String,
    val personName: String,
    val amountOwed: Double,
    val isPaid: Boolean = false,
    val isUser: Boolean = false
)
