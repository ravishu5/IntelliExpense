package com.intelliexpense.app.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.intelliexpense.app.data.repository.FinancialRepository
import com.intelliexpense.app.ui.components.BudgetProgressBar
import com.intelliexpense.app.ui.components.ReviewAlertCard
import com.intelliexpense.app.ui.components.StatCard
import com.intelliexpense.app.ui.components.TransactionItemRow
import com.intelliexpense.app.ui.theme.LocalAppColors
import java.util.Locale

@Composable
fun DashboardScreen(
    repository: FinancialRepository,
    onNavigateToTransactions: () -> Unit,
    onNavigateToAi: () -> Unit,
    onNavigateToReview: () -> Unit
) {
    val colors = LocalAppColors.current

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
                containerColor = colors.primary,
                contentColor = Color.Black,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Expense", modifier = Modifier.size(28.dp))
            }
        },
        containerColor = colors.background
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
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Privacy-First • Local Vault",
                            fontSize = 12.sp,
                            color = colors.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    IconButton(
                        onClick = onNavigateToAi,
                        modifier = Modifier
                            .background(colors.primary.copy(alpha = 0.15f), CircleShape)
                            .size(42.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "Ask AI", tint = colors.primary, modifier = Modifier.size(20.dp))
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

            // Net Worth Hero Card with Gradient and Hairline Border Glow
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    border = BorderStroke(1.dp, colors.cardBorderGlow)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.heroGradient)
                            .padding(22.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Total Net Worth", fontSize = 13.sp, color = colors.textSecondary, fontWeight = FontWeight.Medium)
                                Box(
                                    modifier = Modifier
                                        .background(colors.primary.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text("Encrypted Vault", fontSize = 10.sp, color = colors.primary, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = IndianCurrencyFormatter.format(netWorthReport.totalNetWorth, includeDecimals = false),
                                fontSize = 34.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = colors.textPrimary
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Assets", fontSize = 11.sp, color = colors.textSecondary)
                                    Text(
                                        IndianCurrencyFormatter.format(netWorthReport.assets.totalAssets, false),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.incomeGreen
                                    )
                                }
                                Column {
                                    Text("Liabilities", fontSize = 11.sp, color = colors.textSecondary)
                                    Text(
                                        IndianCurrencyFormatter.format(netWorthReport.liabilities.totalLiabilities, false),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.accentAmber
                                    )
                                }
                                Column {
                                    Text("Savings Rate", fontSize = 11.sp, color = colors.textSecondary)
                                    Text(
                                        "${String.format(Locale.ENGLISH, "%.0f", analytics.savingsRate)}%",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.secondary
                                    )
                                }
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
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        iconTint = colors.accentAmber,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Income Received",
                        amount = analytics.totalIncome,
                        subtitle = "Net: ${IndianCurrencyFormatter.format(analytics.netSavings, false)}",
                        icon = Icons.AutoMirrored.Filled.ReceiptLong,
                        iconTint = colors.incomeGreen,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Monthly Budget Progress Bar
            if (overallBudget != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                        border = BorderStroke(1.dp, colors.cardBorder)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
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
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                        border = BorderStroke(1.dp, colors.cardBorder)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Upcoming Subscriptions", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                                Text(
                                    "${subscriptions.size} active",
                                    fontSize = 12.sp,
                                    color = colors.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            subscriptions.take(3).forEach { sub ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(sub.merchantName, color = colors.textPrimary, fontSize = 13.sp)
                                    Text(
                                        "${IndianCurrencyFormatter.format(sub.amount)} / ${sub.frequency.displayName.lowercase()}",
                                        color = colors.textSecondary,
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
                        color = colors.textPrimary
                    )
                    TextButton(onClick = onNavigateToTransactions) {
                        Text("View All", color = colors.primary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
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
                            .padding(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No transactions recorded yet. Tap + to add!", color = colors.textSecondary, fontSize = 14.sp)
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
