package com.intelliexpense.app.ai

import com.intelliexpense.app.analytics.CreditCardManager
import com.intelliexpense.app.analytics.ExpenseSplitManager
import com.intelliexpense.app.analytics.SpendingAnalyticsEngine
import com.intelliexpense.app.core.model.TransactionType
import com.intelliexpense.app.core.util.DateUtils
import com.intelliexpense.app.core.util.IndianCurrencyFormatter
import com.intelliexpense.app.data.model.AccountEntity
import com.intelliexpense.app.data.model.BudgetEntity
import com.intelliexpense.app.data.model.CategoryEntity
import com.intelliexpense.app.data.model.SplitMemberEntity
import com.intelliexpense.app.data.model.SubscriptionEntity
import com.intelliexpense.app.data.model.TransactionEntity
import com.intelliexpense.app.intelligence.CategoryTaxonomy
import java.util.Calendar
import java.util.Locale
import java.util.regex.Pattern

data class AiMetricItem(
    val label: String,
    val value: String
)

data class AiBreakdownItem(
    val title: String,
    val subtitle: String,
    val amount: String
)

data class AiResponse(
    val query: String,
    val title: String,
    val mainAnswer: String,
    val keyMetrics: List<AiMetricItem> = emptyList(),
    val breakdownItems: List<AiBreakdownItem> = emptyList(),
    val actionableTip: String? = null
)

class AskYourMoneyEngine(
    private val categoryMap: Map<String, String> = emptyMap()
) {

    fun answerQuery(
        query: String,
        transactions: List<TransactionEntity>,
        accounts: List<AccountEntity>,
        budgets: List<BudgetEntity>,
        subscriptions: List<SubscriptionEntity>,
        categories: List<CategoryEntity>,
        unsettledMembers: List<SplitMemberEntity> = emptyList()
    ): AiResponse {
        val q = query.trim().lowercase(Locale.ENGLISH)

        return when {
            // 1. Specific Category Spending (e.g. "How much did I spend on food this month?")
            q.contains("food") || q.contains("dining") || q.contains("grocery") ||
                    q.contains("travel") || q.contains("shopping") || q.contains("entertainment") ||
                    q.contains("utilities") || q.contains("spend on") || q.contains("spent on") -> {
                handleCategorySpendQuery(query, q, transactions, categories)
            }

            // 2. Spending Anomaly / Increase (e.g. "Why did my spending increase?")
            q.contains("increase") || q.contains("why did my spending") || q.contains("why higher") ||
                    q.contains("more this month") || q.contains("compare month") -> {
                handleSpendingIncreaseQuery(query, transactions)
            }

            // 3. Biggest Expenses (e.g. "Show my biggest expenses")
            q.contains("biggest") || q.contains("largest") || q.contains("highest") || q.contains("top expense") -> {
                handleBiggestExpensesQuery(query, transactions)
            }

            // 4. Subscriptions (e.g. "What subscriptions do I have?")
            q.contains("subscription") || q.contains("recurring") || q.contains("netflix") || q.contains("spotify") -> {
                handleSubscriptionsQuery(query, subscriptions)
            }

            // 5. Reduce Spending (e.g. "Where can I reduce spending?")
            q.contains("reduce") || q.contains("cut") || q.contains("save money") || q.contains("less spend") -> {
                handleReduceSpendingQuery(query, transactions)
            }

            // 6. Affordability (e.g. "Can I afford this purchase?", "Can I afford 5000?")
            q.contains("afford") || q.contains("can i buy") -> {
                handleAffordabilityQuery(query, q, transactions, accounts, subscriptions, budgets)
            }

            // 7. Credit Card Due Dates (e.g. "When is my credit card due?")
            q.contains("credit card") || q.contains("due date") || q.contains("card bill") -> {
                handleCreditCardQuery(query, accounts)
            }

            // 8. Who Owes Me (e.g. "Who owes me money?")
            q.contains("who owes") || q.contains("owe me") || q.contains("pending split") || q.contains("i owe") -> {
                handleWhoOwesQuery(query, unsettledMembers)
            }

            // 9. Default: Monthly Financial Overview
            else -> {
                handleGeneralSummaryQuery(query, transactions, accounts, budgets)
            }
        }
    }

    private fun handleCategorySpendQuery(
        rawQuery: String,
        lowerQuery: String,
        transactions: List<TransactionEntity>,
        categories: List<CategoryEntity>
    ): AiResponse {
        val startOfMonth = DateUtils.getStartOfMonth()
        val endOfMonth = DateUtils.getEndOfMonth()

        // Match category
        val matchedCategory = categories.firstOrNull { cat ->
            lowerQuery.contains(cat.name.lowercase()) || lowerQuery.contains(cat.id.lowercase())
        } ?: categories.firstOrNull { it.id == "food_dining" }

        val catId = matchedCategory?.id ?: "food_dining"
        val catName = matchedCategory?.name ?: "Food & Dining"

        val monthlyTxns = transactions.filter {
            it.type == TransactionType.EXPENSE &&
                    it.categoryId == catId &&
                    it.timestamp in startOfMonth..endOfMonth
        }.sortedByDescending { it.amount }

        val totalSpent = monthlyTxns.sumOf { it.amount }
        val budget = matchedCategory?.budgetMonthly ?: 0.0
        val remaining = (budget - totalSpent).coerceAtLeast(0.0)

        val metrics = mutableListOf(
            AiMetricItem("Total Spent", IndianCurrencyFormatter.format(totalSpent)),
            AiMetricItem("Transactions", "${monthlyTxns.size}")
        )
        if (budget > 0) {
            metrics.add(AiMetricItem("Monthly Budget", IndianCurrencyFormatter.format(budget)))
            metrics.add(AiMetricItem("Remaining", IndianCurrencyFormatter.format(remaining)))
        }

        val breakdowns = monthlyTxns.take(4).map { txn ->
            AiBreakdownItem(
                title = txn.merchantNormalized,
                subtitle = DateUtils.formatDate(txn.timestamp),
                amount = IndianCurrencyFormatter.format(txn.amount)
            )
        }

        val tip = if (budget > 0 && totalSpent > budget) {
            "You have exceeded your $catName budget by ${IndianCurrencyFormatter.format(totalSpent - budget)}. Consider slowing down on discretionary orders."
        } else {
            "Your average spend per transaction in $catName is ${IndianCurrencyFormatter.format(if (monthlyTxns.isNotEmpty()) totalSpent / monthlyTxns.size else 0.0)}."
        }

        return AiResponse(
            query = rawQuery,
            title = "$catName Spending",
            mainAnswer = "You have spent ${IndianCurrencyFormatter.format(totalSpent)} on $catName so far this month.",
            keyMetrics = metrics,
            breakdownItems = breakdowns,
            actionableTip = tip
        )
    }

    private fun handleSpendingIncreaseQuery(
        rawQuery: String,
        transactions: List<TransactionEntity>
    ): AiResponse {
        val now = Calendar.getInstance()
        val summary = SpendingAnalyticsEngine.generateMonthlyAnalytics(transactions, categoryMap, now)

        val comp = summary.monthComparison
        val diff = comp.currentMonthSpend - comp.previousMonthSpend

        val answer = if (comp.isHigher && diff > 0) {
            "Your spending is ${IndianCurrencyFormatter.format(diff)} (+${String.format(Locale.ENGLISH, "%.1f", comp.percentageChange)}%) higher compared to last month."
        } else {
            "Great news! Your spending is currently lower or on par with last month."
        }

        val topCategoryDeltas = summary.categoryBreakdown.take(3).map { cat ->
            AiBreakdownItem(
                title = cat.categoryName,
                subtitle = "${cat.transactionCount} transactions",
                amount = IndianCurrencyFormatter.format(cat.totalAmount)
            )
        }

        val tip = "Top driver of spend: ${summary.categoryBreakdown.firstOrNull()?.categoryName ?: "Shopping"} accounts for ${String.format(Locale.ENGLISH, "%.0f", summary.categoryBreakdown.firstOrNull()?.percentage ?: 0f)}% of this month's expenses."

        return AiResponse(
            query = rawQuery,
            title = "Spending Trend Analysis",
            mainAnswer = answer,
            keyMetrics = listOf(
                AiMetricItem("This Month", IndianCurrencyFormatter.format(comp.currentMonthSpend)),
                AiMetricItem("Last Month", IndianCurrencyFormatter.format(comp.previousMonthSpend)),
                AiMetricItem("Daily Burn Rate", "${IndianCurrencyFormatter.format(summary.dailyAverage)}/day")
            ),
            breakdownItems = topCategoryDeltas,
            actionableTip = tip
        )
    }

    private fun handleBiggestExpensesQuery(
        rawQuery: String,
        transactions: List<TransactionEntity>
    ): AiResponse {
        val startOfMonth = DateUtils.getStartOfMonth()
        val endOfMonth = DateUtils.getEndOfMonth()

        val topTxns = transactions.filter {
            it.type == TransactionType.EXPENSE && it.timestamp in startOfMonth..endOfMonth
        }.sortedByDescending { it.amount }.take(5)

        val breakdowns = topTxns.map { txn ->
            AiBreakdownItem(
                title = txn.merchantNormalized,
                subtitle = "${DateUtils.formatDate(txn.timestamp)} • ${txn.paymentMethod}",
                amount = IndianCurrencyFormatter.format(txn.amount)
            )
        }

        val largest = topTxns.firstOrNull()
        val answer = if (largest != null) {
            "Your single largest purchase this month was ${IndianCurrencyFormatter.format(largest.amount)} at ${largest.merchantNormalized}."
        } else {
            "No expenses recorded yet this month."
        }

        return AiResponse(
            query = rawQuery,
            title = "Top Expenses",
            mainAnswer = answer,
            keyMetrics = listOf(
                AiMetricItem("Top 5 Total", IndianCurrencyFormatter.format(topTxns.sumOf { it.amount })),
                AiMetricItem("Largest Txn", IndianCurrencyFormatter.format(largest?.amount ?: 0.0))
            ),
            breakdownItems = breakdowns,
            actionableTip = "High-ticket items usually make up over 40% of total outflow. Reviewing recurring annual bills or electronics purchases often yields the fastest savings."
        )
    }

    private fun handleSubscriptionsQuery(
        rawQuery: String,
        subscriptions: List<SubscriptionEntity>
    ): AiResponse {
        val active = subscriptions.filter { it.isActive }
        val monthlyTotal = active.sumOf { it.amount }

        val breakdowns = active.map { sub ->
            AiBreakdownItem(
                title = sub.merchantName,
                subtitle = "Due on day ${sub.billingDay} • ${sub.frequency.displayName}",
                amount = IndianCurrencyFormatter.format(sub.amount)
            )
        }

        return AiResponse(
            query = rawQuery,
            title = "Active Subscriptions",
            mainAnswer = "You have ${active.size} active recurring subscriptions totaling ${IndianCurrencyFormatter.format(monthlyTotal)} per month.",
            keyMetrics = listOf(
                AiMetricItem("Monthly Cost", IndianCurrencyFormatter.format(monthlyTotal)),
                AiMetricItem("Annual Run Rate", IndianCurrencyFormatter.format(monthlyTotal * 12)),
                AiMetricItem("Active Services", "${active.size}")
            ),
            breakdownItems = breakdowns,
            actionableTip = "Audit services you haven't used in 30 days. Cancelling just 1-2 unused OTT platforms can save up to ₹6,000 yearly."
        )
    }

    private fun handleReduceSpendingQuery(
        rawQuery: String,
        transactions: List<TransactionEntity>
    ): AiResponse {
        val startOfMonth = DateUtils.getStartOfMonth()
        val endOfMonth = DateUtils.getEndOfMonth()

        val discretionaryCategories = setOf("food_dining", "shopping", "entertainment")
        val discretionaryTxns = transactions.filter {
            it.type == TransactionType.EXPENSE &&
                    it.categoryId in discretionaryCategories &&
                    it.timestamp in startOfMonth..endOfMonth
        }

        val totalDiscretionary = discretionaryTxns.sumOf { it.amount }
        val grouped = discretionaryTxns.groupBy { it.categoryId }

        val breakdowns = grouped.map { (catId, txns) ->
            AiBreakdownItem(
                title = catId.replace('_', ' ').replaceFirstChar { it.uppercase() },
                subtitle = "${txns.size} transactions",
                amount = IndianCurrencyFormatter.format(txns.sumOf { it.amount })
            )
        }.sortedByDescending { it.amount }

        return AiResponse(
            query = rawQuery,
            title = "Savings Opportunities",
            mainAnswer = "You have spent ${IndianCurrencyFormatter.format(totalDiscretionary)} on discretionary categories (Dining, Shopping, Entertainment) this month.",
            keyMetrics = listOf(
                AiMetricItem("Discretionary Total", IndianCurrencyFormatter.format(totalDiscretionary)),
                AiMetricItem("Potential 15% Cut", IndianCurrencyFormatter.format(totalDiscretionary * 0.15))
            ),
            breakdownItems = breakdowns,
            actionableTip = "Tip: Cooking at home twice more a week and grouping quick-commerce deliveries can reduce dining & grocery fees by up to ₹3,500/month."
        )
    }

    private fun handleAffordabilityQuery(
        rawQuery: String,
        lowerQuery: String,
        transactions: List<TransactionEntity>,
        accounts: List<AccountEntity>,
        subscriptions: List<SubscriptionEntity>,
        budgets: List<BudgetEntity>
    ): AiResponse {
        // Extract amount from query (e.g. "Can I afford 15000?")
        val numMatcher = Pattern.compile("([0-9]+(?:\\.[0-9]+)?)").matcher(lowerQuery)
        val requestedPrice = if (numMatcher.find()) {
            numMatcher.group(1)?.toDoubleOrNull() ?: 0.0
        } else {
            0.0
        }

        val liquidBalance = accounts.filter {
            it.type != com.intelliexpense.app.core.model.AccountType.CREDIT_CARD
        }.sumOf { it.balance.coerceAtLeast(0.0) }

        val monthlyBudget = budgets.firstOrNull { it.categoryId == null }?.limitAmount ?: 50000.0
        val monthSpent = transactions.filter {
            it.type == TransactionType.EXPENSE && it.timestamp >= DateUtils.getStartOfMonth()
        }.sumOf { it.amount }

        val remainingBudget = (monthlyBudget - monthSpent).coerceAtLeast(0.0)
        val upcomingBills = subscriptions.filter { it.isActive }.sumOf { it.amount }

        val safeDiscretionary = (remainingBudget - upcomingBills).coerceAtLeast(0.0)

        val verdict = when {
            requestedPrice <= 0.0 -> "Please specify an amount (e.g., 'Can I afford ₹10,000 for a phone?')."
            requestedPrice <= safeDiscretionary && requestedPrice <= liquidBalance * 0.3 -> {
                "Yes, comfortably affordable! After this ₹${requestedPrice.toInt()} purchase, you will still have ${IndianCurrencyFormatter.format(safeDiscretionary - requestedPrice)} in this month's budget."
            }
            requestedPrice <= liquidBalance -> {
                "Proceed with caution. While you have enough bank balance, this purchase will consume ${String.format(Locale.ENGLISH, "%.0f", (requestedPrice / remainingBudget) * 100)}% of your remaining monthly budget."
            }
            else -> {
                "Not recommended right now. This purchase exceeds your available liquid savings (${IndianCurrencyFormatter.format(liquidBalance)})."
            }
        }

        return AiResponse(
            query = rawQuery,
            title = "Affordability Check",
            mainAnswer = verdict,
            keyMetrics = listOf(
                AiMetricItem("Purchase Price", IndianCurrencyFormatter.format(requestedPrice)),
                AiMetricItem("Remaining Budget", IndianCurrencyFormatter.format(remainingBudget)),
                AiMetricItem("Upcoming Bills", IndianCurrencyFormatter.format(upcomingBills))
            ),
            actionableTip = "Aim to maintain a buffer of at least 20% of your monthly income for unforeseen expenses before making large discretionary purchases."
        )
    }

    private fun handleCreditCardQuery(
        rawQuery: String,
        accounts: List<AccountEntity>
    ): AiResponse {
        val cardStatuses = CreditCardManager.analyzeCards(accounts)
        if (cardStatuses.isEmpty()) {
            return AiResponse(
                query = rawQuery,
                title = "Credit Cards",
                mainAnswer = "You do not have any credit cards registered. Add your credit card under Accounts to track due dates and utilization."
            )
        }

        val first = cardStatuses.first()
        val breakdowns = cardStatuses.map { c ->
            AiBreakdownItem(
                title = c.cardName,
                subtitle = "Due in ${c.daysUntilDue ?: "N/A"} days • Utilization: ${String.format(Locale.ENGLISH, "%.1f", c.utilizationPercentage)}%",
                amount = IndianCurrencyFormatter.format(c.outstandingBalance)
            )
        }

        return AiResponse(
            query = rawQuery,
            title = "Credit Card Status",
            mainAnswer = "${first.cardName} has an outstanding of ${IndianCurrencyFormatter.format(first.outstandingBalance)}, due in ${first.daysUntilDue ?: 0} days.",
            keyMetrics = listOf(
                AiMetricItem("Total Outstanding", IndianCurrencyFormatter.format(cardStatuses.sumOf { it.outstandingBalance })),
                AiMetricItem("Utilization", "${String.format(Locale.ENGLISH, "%.1f", first.utilizationPercentage)}%"),
                AiMetricItem("Status", first.health.label)
            ),
            breakdownItems = breakdowns,
            actionableTip = "Keeping your credit utilization under 30% significantly improves your credit score (CIBIL/Experian)."
        )
    }

    private fun handleWhoOwesQuery(
        rawQuery: String,
        unsettledMembers: List<SplitMemberEntity>
    ): AiResponse {
        val summary = ExpenseSplitManager.summarize(emptyList(), unsettledMembers)

        val breakdowns = summary.pendingPeople.map { p ->
            AiBreakdownItem(
                title = p.personName,
                subtitle = if (p.isOwedToUser) "Owes you" else "You owe",
                amount = IndianCurrencyFormatter.format(p.amount)
            )
        }

        val answer = if (summary.totalOwedToUser > 0) {
            "Friends owe you a total of ${IndianCurrencyFormatter.format(summary.totalOwedToUser)} across pending bill splits."
        } else {
            "All split expenses are currently settled! No one owes you money."
        }

        return AiResponse(
            query = rawQuery,
            title = "Split Bills & Balances",
            mainAnswer = answer,
            keyMetrics = listOf(
                AiMetricItem("Owed to You", IndianCurrencyFormatter.format(summary.totalOwedToUser)),
                AiMetricItem("You Owe", IndianCurrencyFormatter.format(summary.totalUserOwes)),
                AiMetricItem("Net Balance", IndianCurrencyFormatter.format(summary.netOwed))
            ),
            breakdownItems = breakdowns,
            actionableTip = "Send gentle UPI payment reminder links directly to friends to settle group dinners faster."
        )
    }

    private fun handleGeneralSummaryQuery(
        rawQuery: String,
        transactions: List<TransactionEntity>,
        accounts: List<AccountEntity>,
        budgets: List<BudgetEntity>
    ): AiResponse {
        val summary = SpendingAnalyticsEngine.generateMonthlyAnalytics(transactions, categoryMap)

        return AiResponse(
            query = rawQuery,
            title = "Financial Overview",
            mainAnswer = "You have spent ${IndianCurrencyFormatter.format(summary.totalExpense)} and earned ${IndianCurrencyFormatter.format(summary.totalIncome)} this month.",
            keyMetrics = listOf(
                AiMetricItem("Net Savings", IndianCurrencyFormatter.format(summary.netSavings)),
                AiMetricItem("Savings Rate", "${String.format(Locale.ENGLISH, "%.1f", summary.savingsRate)}%"),
                AiMetricItem("Daily Average", "${IndianCurrencyFormatter.format(summary.dailyAverage)}/day")
            ),
            breakdownItems = summary.topMerchants.take(3).map {
                AiBreakdownItem(
                    title = it.merchantName,
                    subtitle = "${it.transactionCount} transactions",
                    amount = IndianCurrencyFormatter.format(it.totalAmount)
                )
            },
            actionableTip = "You are on track to spend approximately ${IndianCurrencyFormatter.format(summary.projectedMonthEndSpend)} by the end of this month."
        )
    }
}
