package com.intelliexpense.app.analytics

import com.intelliexpense.app.data.model.SplitExpenseEntity
import com.intelliexpense.app.data.model.SplitMemberEntity

data class SplitSummary(
    val totalOwedToUser: Double,
    val totalUserOwes: Double,
    val netOwed: Double,
    val pendingPeople: List<PersonDebt>
)

data class PersonDebt(
    val memberId: String,
    val splitId: String,
    val personName: String,
    val amount: Double,
    val isOwedToUser: Boolean
)

object ExpenseSplitManager {

    fun summarize(
        splits: List<SplitExpenseEntity>,
        members: List<SplitMemberEntity>
    ): SplitSummary {
        val unsettled = members.filter { !it.isPaid }
        val owedToUser = unsettled.filter { !it.isUser }.sumOf { it.amountOwed }
        val userOwes = unsettled.filter { it.isUser }.sumOf { it.amountOwed }

        val pending = unsettled.map { m ->
            PersonDebt(
                memberId = m.id,
                splitId = m.splitId,
                personName = m.personName,
                amount = m.amountOwed,
                isOwedToUser = !m.isUser
            )
        }.sortedByDescending { it.amount }

        return SplitSummary(
            totalOwedToUser = owedToUser,
            totalUserOwes = userOwes,
            netOwed = owedToUser - userOwes,
            pendingPeople = pending
        )
    }
}
