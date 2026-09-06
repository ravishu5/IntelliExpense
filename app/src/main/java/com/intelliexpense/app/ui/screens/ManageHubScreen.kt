package com.intelliexpense.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.intelliexpense.app.analytics.ExpenseSplitManager
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
import kotlinx.coroutines.launch

@Composable
fun ManageHubScreen(
    repository: FinancialRepository,
    onNavigateToPrivacyCenter: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(0) }

    val accounts by repository.getAllAccountsFlow().collectAsState(initial = emptyList())
    val subscriptions by repository.getActiveSubscriptionsFlow().collectAsState(initial = emptyList())
    val goals by repository.getAllGoalsFlow().collectAsState(initial = emptyList())
    val splits by repository.getAllSplitsFlow().collectAsState(initial = emptyList())

    val tabs = listOf("Accounts", "Subscriptions", "Goals", "Split Bills")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate900)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Manage & Settings",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            // Privacy Center Button
            Card(
                modifier = Modifier.clickable { onNavigateToPrivacyCenter() },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Slate850),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = Emerald500, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Privacy Center", fontSize = 12.sp, color = Emerald500, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tab Navigation
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Slate850,
            contentColor = Emerald500,
            modifier = Modifier.clip(RoundedCornerShape(14.dp))
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontSize = 12.sp, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTab) {
            0 -> {
                // Accounts & Wallets
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text("Bank Accounts & Cards", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                    items(accounts) { acc ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Slate850),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(
                                                if (acc.type == com.intelliexpense.app.core.model.AccountType.CREDIT_CARD) Blue500.copy(alpha = 0.2f) else Emerald500.copy(alpha = 0.2f),
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            if (acc.type == com.intelliexpense.app.core.model.AccountType.CREDIT_CARD) Icons.Default.CreditCard else Icons.Default.AccountBalance,
                                            contentDescription = null,
                                            tint = if (acc.type == com.intelliexpense.app.core.model.AccountType.CREDIT_CARD) Blue500 else Emerald500,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(acc.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                                        Text(
                                            "${acc.type.displayName}${acc.accountNumberLast4?.let { " • Ending $it" }.orEmpty()}",
                                            fontSize = 12.sp,
                                            color = Slate400
                                        )
                                    }
                                }
                                Text(
                                    IndianCurrencyFormatter.format(acc.balance),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (acc.balance >= 0) Emerald500 else Rose500
                                )
                            }
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(70.dp))
                    }
                }
            }
            1 -> {
                // Subscriptions & Recurring Bills
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text("Active Subscriptions & Bills", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                    if (subscriptions.isEmpty()) {
                        item {
                            Text("No subscriptions added yet. Subscriptions are also auto-detected from recurring payments.", color = Slate400, fontSize = 13.sp)
                        }
                    } else {
                        items(subscriptions) { sub ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Slate850),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(Amber500.copy(alpha = 0.2f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.EventRepeat, contentDescription = null, tint = Amber500, modifier = Modifier.size(18.dp))
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(sub.merchantName, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                                            Text(
                                                "Due day ${sub.billingDay} • ${sub.frequency.displayName}",
                                                fontSize = 12.sp,
                                                color = Slate400
                                            )
                                        }
                                    }
                                    Text(
                                        IndianCurrencyFormatter.format(sub.amount),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(70.dp))
                    }
                }
            }
            2 -> {
                // Savings Goals
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text("Savings Goals & Funds", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                    if (goals.isEmpty()) {
                        item {
                            Text("No active savings goals. Create an Emergency Fund or Vacation goal!", color = Slate400, fontSize = 13.sp)
                        }
                    } else {
                        items(goals) { goal ->
                            val fraction = if (goal.targetAmount > 0) (goal.currentAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f) else 0f
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Slate850),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(goal.title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                                        Text(
                                            "${IndianCurrencyFormatter.format(goal.currentAmount, false)} / ${IndianCurrencyFormatter.format(goal.targetAmount, false)}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Emerald500
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    LinearProgressIndicator(
                                        progress = { fraction },
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
                    item {
                        Spacer(modifier = Modifier.height(70.dp))
                    }
                }
            }
            3 -> {
                // Split Bills / Who Owes Me
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text("Who Owes Me / Shared Ledger", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                    if (splits.isEmpty()) {
                        item {
                            Text("No shared group expenses recorded yet.", color = Slate400, fontSize = 13.sp)
                        }
                    } else {
                        items(splits) { split ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Slate850),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(Blue500.copy(alpha = 0.2f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Group, contentDescription = null, tint = Blue500, modifier = Modifier.size(18.dp))
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(split.title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                                            Text(if (split.settled) "Settled" else "Pending Settle", fontSize = 12.sp, color = if (split.settled) Emerald500 else Amber500)
                                        }
                                    }
                                    Text(IndianCurrencyFormatter.format(split.totalAmount), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(70.dp))
                    }
                }
            }
        }
    }
}
