package com.intelliexpense.app.analytics

import com.intelliexpense.app.core.model.AccountType
import com.intelliexpense.app.data.model.AccountEntity
import java.util.Calendar
import kotlin.math.abs

enum class CreditUtilizationHealth(val label: String, val colorHex: String) {
    EXCELLENT("Optimal (<30%)", "#10B981"),
    MODERATE("Moderate (30-50%)", "#F59E0B"),
    HIGH_RISK("High Risk (>50%)", "#EF4444")
}

data class CreditCardStatus(
    val accountId: String,
    val cardName: String,
    val last4: String?,
    val outstandingBalance: Double,
    val creditLimit: Double,
    val availableLimit: Double,
    val utilizationPercentage: Float,
    val health: CreditUtilizationHealth,
    val statementDay: Int?,
    val dueDay: Int?,
    val daysUntilDue: Int?
)

object CreditCardManager {

    fun analyzeCards(accounts: List<AccountEntity>): List<CreditCardStatus> {
        val cards = accounts.filter { it.type == AccountType.CREDIT_CARD }
        val now = Calendar.getInstance()
        val currentDay = now.get(Calendar.DAY_OF_MONTH)

        return cards.map { card ->
            val outstanding = abs(card.balance)
            val limit = card.creditLimit ?: 50000.0
            val utilization = if (limit > 0) ((outstanding / limit) * 100).toFloat() else 0f

            val health = when {
                utilization <= 30f -> CreditUtilizationHealth.EXCELLENT
                utilization <= 50f -> CreditUtilizationHealth.MODERATE
                else -> CreditUtilizationHealth.HIGH_RISK
            }

            val daysUntilDue = card.dueDay?.let { due ->
                if (due >= currentDay) {
                    due - currentDay
                } else {
                    val maxDays = now.getActualMaximum(Calendar.DAY_OF_MONTH)
                    (maxDays - currentDay) + due
                }
            }

            CreditCardStatus(
                accountId = card.id,
                cardName = card.name,
                last4 = card.accountNumberLast4,
                outstandingBalance = outstanding,
                creditLimit = limit,
                availableLimit = (limit - outstanding).coerceAtLeast(0.0),
                utilizationPercentage = utilization,
                health = health,
                statementDay = card.statementDay,
                dueDay = card.dueDay,
                daysUntilDue = daysUntilDue
            )
        }
    }
}
