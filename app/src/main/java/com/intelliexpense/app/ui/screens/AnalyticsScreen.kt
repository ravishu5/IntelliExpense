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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
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
import com.intelliexpense.app.ui.theme.Amber500
import com.intelliexpense.app.ui.theme.Blue500
import com.intelliexpense.app.ui.theme.CardBorder
import com.intelliexpense.app.ui.theme.Emerald500
import com.intelliexpense.app.ui.theme.Rose500
import com.intelliexpense.app.ui.theme.Slate400
import com.intelliexpense.app.ui.theme.Slate800
import com.intelliexpense.app.ui.theme.Slate850
import com.intelliexpense.app.ui.theme.Slate900
import java.util.Locale

@Composable
fun AnalyticsScreen(repository: FinancialRepository) {
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
            .background(Slate900)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Financial Intelligence",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Spending patterns, cash flow & credit health",
                fontSize = 13.sp,
                color = Slate400
            )
        }

        // Cash Flow Summary Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Slate850),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Cash Flow (Current Month)", fontSize = 13.sp, color = Slate400, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Emerald500.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = Emerald500, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Inflow (Income)", fontSize = 11.sp, color = Slate400)
                                Text(
                                    IndianCurrencyFormatter.format(analytics.totalIncome, false),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Emerald500
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Rose500.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = Rose500, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Outflow (Spend)", fontSize = 11.sp, color = Slate400)
                                Text(
                                    IndianCurrencyFormatter.format(analytics.totalExpense, false),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Slate800, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Net Savings Rate", fontSize = 13.sp, color = Color.White)
                            Text(
                                "${String.format(Locale.ENGLISH, "%.1f", analytics.savingsRate)}%",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (analytics.savingsRate >= 20f) Emerald500 else Amber500
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
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Slate850),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                if (comp.isHigher) Rose500.copy(alpha = 0.15f) else Emerald500.copy(alpha = 0.15f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (comp.isHigher) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = if (comp.isHigher) Rose500 else Emerald500,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = if (comp.isHigher) "Spending is ${String.format(Locale.ENGLISH, "%.1f", comp.percentageChange)}% higher" else "Spending is ${String.format(Locale.ENGLISH, "%.1f", comp.percentageChange)}% lower",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Compared to ${IndianCurrencyFormatter.format(comp.previousMonthSpend, false)} last month",
                            fontSize = 12.sp,
                            color = Slate400
                        )
                    }
                }
            }
        }

        // Category Breakdown
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Slate850),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Category Breakdown", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Icon(Icons.Default.PieChart, contentDescription = null, tint = Emerald500, modifier = Modifier.size(18.dp))
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (analytics.categoryBreakdown.isEmpty()) {
                        Text("No expense categories to display.", color = Slate400, fontSize = 13.sp)
                    } else {
                        analytics.categoryBreakdown.forEach { item ->
                            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(item.categoryName, fontSize = 13.sp, color = Color.White)
                                    Text(
                                        "${IndianCurrencyFormatter.format(item.totalAmount, false)} (${String.format(Locale.ENGLISH, "%.0f", item.percentage)}%)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Slate400
                                    )
                                }
                                Spacer(modifier = Modifier.height(5.dp))
                                LinearProgressIndicator(
                                    progress = { item.percentage / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = Emerald500,
                                    trackColor = Slate800
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
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Slate850),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Top Spending Destinations", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(12.dp))

                    if (analytics.topMerchants.isEmpty()) {
                        Text("No merchant data yet.", color = Slate400, fontSize = 13.sp)
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
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Emerald500
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(merchant.merchantName, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.White)
                                        Text("${merchant.transactionCount} transactions", fontSize = 11.sp, color = Slate400)
                                    }
                                }
                                Text(
                                    IndianCurrencyFormatter.format(merchant.totalAmount, false),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
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
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate850),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Credit Card Utilization", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Icon(Icons.Default.CreditCard, contentDescription = null, tint = Blue500, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        cardStatuses.forEach { card ->
                            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(card.cardName, fontSize = 13.sp, color = Color.White)
                                    Text(
                                        "${card.health.label} • ${String.format(Locale.ENGLISH, "%.0f", card.utilizationPercentage)}%",
                                        fontSize = 12.sp,
                                        color = if (card.utilizationPercentage <= 30f) Emerald500 else Amber500,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Outstanding: ${IndianCurrencyFormatter.format(card.outstandingBalance, false)} of ${IndianCurrencyFormatter.format(card.creditLimit, false)}",
                                    fontSize = 12.sp,
                                    color = Slate400
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
