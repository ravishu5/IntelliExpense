package com.intelliexpense.app.analytics

import com.intelliexpense.app.core.model.AccountType
import com.intelliexpense.app.data.model.AccountEntity
import com.intelliexpense.app.data.model.SavingsGoalEntity
import com.intelliexpense.app.data.model.SplitMemberEntity
import kotlin.math.abs

data class AssetBreakdown(
    val bankBalance: Double,
    val cashBalance: Double,
    val walletBalance: Double,
    val savingsGoalsBalance: Double,
    val totalAssets: Double
)

data class LiabilityBreakdown(
    val creditCardOutstanding: Double,
    val payablesOwed: Double,
    val totalLiabilities: Double
)

data class NetWorthReport(
    val totalNetWorth: Double,
    val assets: AssetBreakdown,
    val liabilities: LiabilityBreakdown
)

object NetWorthCalculator {

    fun calculate(
        accounts: List<AccountEntity>,
        savingsGoals: List<SavingsGoalEntity> = emptyList(),
        unsettledMembers: List<SplitMemberEntity> = emptyList()
    ): NetWorthReport {
        var bank = 0.0
        var cash = 0.0
        var wallet = 0.0
        var ccOutstanding = 0.0

        for (acc in accounts) {
            when (acc.type) {
                AccountType.BANK, AccountType.UPI -> {
                    if (acc.balance >= 0) bank += acc.balance else ccOutstanding += abs(acc.balance)
                }
                AccountType.CASH -> {
                    if (acc.balance >= 0) cash += acc.balance
                }
                AccountType.WALLET -> {
                    if (acc.balance >= 0) wallet += acc.balance
                }
                AccountType.CREDIT_CARD -> {
                    ccOutstanding += abs(acc.balance)
                }
            }
        }

        val goalsTotal = savingsGoals.sumOf { it.currentAmount }
        val totalAssets = bank + cash + wallet + goalsTotal

        // Unsettled splits where user owes someone
        val userDebts = unsettledMembers.filter { it.isUser && !it.isPaid }.sumOf { it.amountOwed }
        val totalLiabilities = ccOutstanding + userDebts

        return NetWorthReport(
            totalNetWorth = totalAssets - totalLiabilities,
            assets = AssetBreakdown(bank, cash, wallet, goalsTotal, totalAssets),
            liabilities = LiabilityBreakdown(ccOutstanding, userDebts, totalLiabilities)
        )
    }
}
