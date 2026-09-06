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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.intelliexpense.app.analytics.CreditCardManager
import com.intelliexpense.app.analytics.SpendingAnalyticsEngine
import com.intelliexpense.app.core.util.IndianCurrencyFormatter
import com.intelliexpense.app.data.repository.FinancialRepository
import com.intelliexpense.app.ui.theme.LocalAppColors
import java.util.Locale

@Composable
fun AnalyticsScreen(repository: FinancialRepository) {
    val colors = LocalAppColors.current

    val transactions by repository.getAllTransactionsFlow().collectAsState(initial = emptyList())
    val categories by repository.getAllCategoriesFlow().collectAsState(initial = emptyList())
    val accounts by repository.getAllAccountsFlow().collectAsState(initial = emptyList())

    val categoryMap = remember(categories) { categories.associate { it.id to it.name } }
    val analytics = remember(transactions, categoryMap) {
        SpendingAnalyticsEngine.generateMonthlyAnalytics(transactions, categoryMap)
    }
    val cardStatuses = remember(accounts) {
        CreditCardManager.analyzeCards(accounts)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Financial Intelligence",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            Text(
                text = "Spending patterns, cash flow & credit health",
                fontSize = 13.sp,
                color = colors.textSecondary
            )
        }

        // Cash Flow Summary Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = BorderStroke(1.dp, colors.cardBorder)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Cash Flow (Current Month)", fontSize = 13.sp, color = colors.textSecondary, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(colors.incomeGreen.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = colors.incomeGreen, modifier = Modifier.size(17.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Inflow (Income)", fontSize = 11.sp, color = colors.textSecondary)
                                Text(
                                    IndianCurrencyFormatter.format(analytics.totalIncome, false),
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.incomeGreen
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(colors.expenseRed.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = colors.expenseRed, modifier = Modifier.size(17.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Outflow (Spend)", fontSize = 11.sp, color = colors.textSecondary)
                                Text(
                                    IndianCurrencyFormatter.format(analytics.totalExpense, false),
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.surfaceElevated, RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Net Savings Rate", fontSize = 13.sp, color = colors.textPrimary)
                            Text(
                                "${String.format(Locale.ENGLISH, "%.1f", analytics.savingsRate)}%",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (analytics.savingsRate >= 20f) colors.incomeGreen else colors.accentAmber
                            )
                        }
                    }
                }
            }
        }

        // Month-over-Month Comparison
        item {
            val comp = analytics.monthComparison
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = BorderStroke(1.dp, colors.cardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(
                                if (comp.isHigher) colors.expenseRed.copy(alpha = 0.15f) else colors.incomeGreen.copy(alpha = 0.15f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (comp.isHigher) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                            contentDescription = null,
                            tint = if (comp.isHigher) colors.expenseRed else colors.incomeGreen,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = if (comp.isHigher) "Spending is ${String.format(Locale.ENGLISH, "%.1f", comp.percentageChange)}% higher" else "Spending is ${String.format(Locale.ENGLISH, "%.1f", comp.percentageChange)}% lower",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Compared to ${IndianCurrencyFormatter.format(comp.previousMonthSpend, false)} last month",
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                    }
                }
            }
        }

        // Category Breakdown
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = BorderStroke(1.dp, colors.cardBorder)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Category Breakdown", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                        Icon(Icons.Default.PieChart, contentDescription = null, tint = colors.primary, modifier = Modifier.size(20.dp))
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (analytics.categoryBreakdown.isEmpty()) {
                        Text("No expense categories to display.", color = colors.textSecondary, fontSize = 13.sp)
                    } else {
                        analytics.categoryBreakdown.forEach { item ->
                            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(item.categoryName, fontSize = 13.sp, color = colors.textPrimary)
                                    Text(
                                        "${IndianCurrencyFormatter.format(item.totalAmount, false)} (${String.format(Locale.ENGLISH, "%.0f", item.percentage)}%)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.textSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { item.percentage / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = colors.primary,
                                    trackColor = colors.surfaceElevated
                                )
                            }
                        }
                    }
                }
            }
        }

        // Top 5 Merchants
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = BorderStroke(1.dp, colors.cardBorder)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Top Spending Destinations", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                    Spacer(modifier = Modifier.height(14.dp))

                    if (analytics.topMerchants.isEmpty()) {
                        Text("No merchant data yet.", color = colors.textSecondary, fontSize = 13.sp)
                    } else {
                        analytics.topMerchants.forEachIndexed { index, merchant ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "${index + 1}.",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.primary
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(merchant.merchantName, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                                        Text("${merchant.transactionCount} transactions", fontSize = 11.sp, color = colors.textSecondary)
                                    }
                                }
                                Text(
                                    IndianCurrencyFormatter.format(merchant.totalAmount, false),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Credit Card Health Cards
        if (cardStatuses.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    border = BorderStroke(1.dp, colors.cardBorder)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Credit Card Utilization", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                            Icon(Icons.Default.CreditCard, contentDescription = null, tint = colors.secondary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        cardStatuses.forEach { card ->
                            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(card.cardName, fontSize = 13.sp, color = colors.textPrimary)
                                    Text(
                                        "${card.health.label} • ${String.format(Locale.ENGLISH, "%.0f", card.utilizationPercentage)}%",
                                        fontSize = 12.sp,
                                        color = if (card.utilizationPercentage <= 30f) colors.incomeGreen else colors.accentAmber,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(5.dp))
                                Text(
                                    "Outstanding: ${IndianCurrencyFormatter.format(card.outstandingBalance, false)} of ${IndianCurrencyFormatter.format(card.creditLimit, false)}",
                                    fontSize = 12.sp,
                                    color = colors.textSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(70.dp))
        }
    }
}
