package com.intelliexpense.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.intelliexpense.app.analytics.BudgetManager
import com.intelliexpense.app.analytics.NetWorthCalculator
import com.intelliexpense.app.analytics.SpendingAnalyticsEngine
import com.intelliexpense.app.core.util.IndianCurrencyFormatter
import com.intelliexpense.app.data.model.TransactionEntity
import com.intelliexpense.app.data.repository.FinancialRepository
import com.intelliexpense.app.ui.components.BudgetProgressBar
import com.intelliexpense.app.ui.components.ReviewAlertCard
import com.intelliexpense.app.ui.components.StatCard
import com.intelliexpense.app.ui.components.TransactionItemRow
import com.intelliexpense.app.ui.theme.Amber500
import com.intelliexpense.app.ui.theme.Blue500
import com.intelliexpense.app.ui.theme.CardBorder
import com.intelliexpense.app.ui.theme.Emerald500
import com.intelliexpense.app.ui.theme.Slate400
import com.intelliexpense.app.ui.theme.Slate800
import com.intelliexpense.app.ui.theme.Slate850
import com.intelliexpense.app.ui.theme.Slate900

@Composable
fun DashboardScreen(
    repository: FinancialRepository,
    onNavigateToTransactions: () -> Unit,
    onNavigateToAi: () -> Unit,
    onNavigateToReview: () -> Unit
) {
    val accounts by repository.getAllAccountsFlow().collectAsState(initial = emptyList())
    val transactions by repository.getAllTransactionsFlow().collectAsState(initial = emptyList())
    val categories by repository.getAllCategoriesFlow().collectAsState(initial = emptyList())
    val budgets by repository.getBudgetsForMonthFlow(com.intelliexpense.app.core.util.DateUtils.getCurrentMonthKey()).collectAsState(initial = emptyList())
    val subscriptions by repository.getActiveSubscriptionsFlow().collectAsState(initial = emptyList())
    val unreviewed by repository.getUnreviewedTransactionsFlow().collectAsState(initial = emptyList())

    var showQuickCapture by remember { mutableStateOf(false) }

    val categoryMap = remember(categories) { categories.associate { it.id to it.name } }
    val analytics = remember(transactions, categoryMap) {
        SpendingAnalyticsEngine.generateMonthlyAnalytics(transactions, categoryMap)
    }
    val netWorthReport = remember(accounts) {
        NetWorthCalculator.calculate(accounts)
    }
    val budgetStatuses = remember(budgets, categories, transactions) {
        BudgetManager.evaluateBudgets(budgets, categories, transactions)
    }

    val overallBudget = budgetStatuses.firstOrNull { it.categoryId == null }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showQuickCapture = true },
                containerColor = Emerald500,
                contentColor = Color.Black,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Expense", modifier = Modifier.size(28.dp))
            }
        },
        containerColor = Slate900
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                // Top App Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "IntelliExpense",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "Privacy-First • Local Ledger",
                            fontSize = 12.sp,
                            color = Emerald500,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    IconButton(
                        onClick = onNavigateToAi,
                        modifier = Modifier
                            .background(Emerald500.copy(alpha = 0.15f), CircleShape)
                            .size(40.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "Ask AI", tint = Emerald500, modifier = Modifier.size(20.dp))
                    }
                }
            }

            // Unreviewed Transactions Alert Card
            if (unreviewed.isNotEmpty()) {
                item {
                    ReviewAlertCard(
                        unreviewedCount = unreviewed.size,
                        onClick = onNavigateToReview
                    )
                }
            }

            // Net Worth Primary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate850),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total Net Worth", fontSize = 13.sp, color = Slate400, fontWeight = FontWeight.Medium)
                            Box(
                                modifier = Modifier
                                    .background(Emerald500.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text("Local Encrypted", fontSize = 10.sp, color = Emerald500, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = IndianCurrencyFormatter.format(netWorthReport.totalNetWorth, includeDecimals = false),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Assets", fontSize = 11.sp, color = Slate400)
                                Text(
                                    IndianCurrencyFormatter.format(netWorthReport.assets.totalAssets, false),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Emerald500
                                )
                            }
                            Column {
                                Text("Liabilities", fontSize = 11.sp, color = Slate400)
                                Text(
                                    IndianCurrencyFormatter.format(netWorthReport.liabilities.totalLiabilities, false),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Amber500
                                )
                            }
                            Column {
                                Text("Savings Rate", fontSize = 11.sp, color = Slate400)
                                Text(
                                    "${String.format(java.util.Locale.ENGLISH, "%.0f", analytics.savingsRate)}%",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Blue500
                                )
                            }
                        }
                    }
                }
            }

            // Monthly Spend & Burn Rate Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Spent this Month",
                        amount = analytics.totalExpense,
                        subtitle = "${IndianCurrencyFormatter.format(analytics.dailyAverage, false)}/day burn",
                        icon = Icons.Default.TrendingUp,
                        iconTint = Amber500,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Income Received",
                        amount = analytics.totalIncome,
                        subtitle = "Net: ${IndianCurrencyFormatter.format(analytics.netSavings, false)}",
                        icon = Icons.Default.ReceiptLong,
                        iconTint = Emerald500,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Monthly Budget Progress Bar
            if (overallBudget != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Slate850),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            BudgetProgressBar(
                                title = "Monthly Budget Progress",
                                spent = overallBudget.spentAmount,
                                limit = overallBudget.limitAmount
                            )
                        }
                    }
                }
            }

            // Upcoming Subscriptions Card
            if (subscriptions.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Slate850),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Upcoming Subscriptions", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                                Text(
                                    "${subscriptions.size} active",
                                    fontSize = 12.sp,
                                    color = Emerald500
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            subscriptions.take(2).forEach { sub ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(sub.merchantName, color = Color.White, fontSize = 13.sp)
                                    Text(
                                        "${IndianCurrencyFormatter.format(sub.amount)} / ${sub.frequency.displayName.lowercase()}",
                                        color = Slate400,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Recent Transactions Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Transactions",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    TextButton(onClick = onNavigateToTransactions) {
                        Text("View Ledger", color = Emerald500, fontSize = 13.sp)
                    }
                }
            }

            // Recent Transactions List
            val recentTxns = transactions.take(5)
            if (recentTxns.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No transactions recorded yet. Tap + to add!", color = Slate400, fontSize = 14.sp)
                    }
                }
            } else {
                items(recentTxns) { txn ->
                    TransactionItemRow(transaction = txn)
                }
            }

            item {
                Spacer(modifier = Modifier.height(70.dp))
            }
        }
    }

    if (showQuickCapture) {
        QuickCaptureDialog(
            accounts = accounts,
            categories = categories,
            repository = repository,
            onDismiss = { showQuickCapture = false }
        )
    }
}
