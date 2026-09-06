package com.intelliexpense.app.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.intelliexpense.app.core.model.AccountType
import com.intelliexpense.app.core.model.SubscriptionFrequency
import com.intelliexpense.app.core.util.DateUtils
import com.intelliexpense.app.core.util.IndianCurrencyFormatter
import com.intelliexpense.app.data.model.AccountEntity
import com.intelliexpense.app.data.model.SavingsGoalEntity
import com.intelliexpense.app.data.model.SplitExpenseEntity
import com.intelliexpense.app.data.model.SplitMemberEntity
import com.intelliexpense.app.data.model.SubscriptionEntity
import com.intelliexpense.app.data.repository.FinancialRepository
import com.intelliexpense.app.ui.theme.LocalAppColors
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID

@Composable
fun ManageHubScreen(
    repository: FinancialRepository,
    onNavigateToPrivacyCenter: () -> Unit
) {
    val colors = LocalAppColors.current
    val coroutineScope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(0) }

    val accounts by repository.getAllAccountsFlow().collectAsState(initial = emptyList())
    val subscriptions by repository.getActiveSubscriptionsFlow().collectAsState(initial = emptyList())
    val goals by repository.getAllGoalsFlow().collectAsState(initial = emptyList())
    val splits by repository.getAllSplitsFlow().collectAsState(initial = emptyList())

    // Dialog States
    var showAccountDialog by remember { mutableStateOf(false) }
    var accountToEdit by remember { mutableStateOf<AccountEntity?>(null) }

    var showSubscriptionDialog by remember { mutableStateOf(false) }
    var subscriptionToEdit by remember { mutableStateOf<SubscriptionEntity?>(null) }

    var showGoalDialog by remember { mutableStateOf(false) }
    var goalToEdit by remember { mutableStateOf<SavingsGoalEntity?>(null) }

    var showDepositGoalDialog by remember { mutableStateOf(false) }
    var goalToDeposit by remember { mutableStateOf<SavingsGoalEntity?>(null) }

    var showAddSplitDialog by remember { mutableStateOf(false) }
    var selectedSplitForDetail by remember { mutableStateOf<SplitExpenseEntity?>(null) }

    val tabs = listOf("Accounts", "Subscriptions", "Goals", "Split Bills")

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Manage & Vault",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Local Encrypted Financial Assets",
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                }

                // Privacy Center & Themes Button
                Card(
                    modifier = Modifier.clickable { onNavigateToPrivacyCenter() },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    border = BorderStroke(1.dp, colors.cardBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Palette,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Theme & Privacy",
                            fontSize = 12.sp,
                            color = colors.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tab Navigation
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = colors.surface,
                contentColor = colors.primary,
                modifier = Modifier.clip(RoundedCornerShape(14.dp))
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                title,
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            when (selectedTab) {
                0 -> {
                    // Accounts & Wallets Tab
                    val totalNetBalance = accounts.sumOf {
                        if (it.type == AccountType.CREDIT_CARD) -kotlin.math.abs(it.balance) else it.balance
                    }
                    val bankAndCash = accounts.filter { it.type != AccountType.CREDIT_CARD }.sumOf { it.balance }
                    val creditLiability = accounts.filter { it.type == AccountType.CREDIT_CARD }.sumOf { kotlin.math.abs(it.balance) }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            // Summary Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = colors.surfaceElevated),
                                border = BorderStroke(1.dp, colors.cardBorderGlow)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("TOTAL NET LIQUIDITY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.primary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        IndianCurrencyFormatter.format(totalNetBalance),
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (totalNetBalance >= 0) colors.textPrimary else colors.expenseRed
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            "Bank & Cash: ${IndianCurrencyFormatter.format(bankAndCash)}",
                                            fontSize = 12.sp,
                                            color = colors.incomeGreen
                                        )
                                        if (creditLiability > 0) {
                                            Text(
                                                "Card Debt: ${IndianCurrencyFormatter.format(creditLiability)}",
                                                fontSize = 12.sp,
                                                color = colors.expenseRed
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Connected Accounts (${accounts.size})",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textPrimary
                                )
                                TextButton(onClick = {
                                    accountToEdit = null
                                    showAccountDialog = true
                                }) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = colors.primary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add Account", color = colors.primary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        if (accounts.isEmpty()) {
                            item {
                                EmptyStateCard(
                                    icon = Icons.Default.AccountBalance,
                                    title = "No Accounts Configured",
                                    subtitle = "Add your Bank Account, UPI VPA, Credit Card, or Cash Wallet to track balances accurately.",
                                    actionText = "+ Add First Account",
                                    onAction = {
                                        accountToEdit = null
                                        showAccountDialog = true
                                    }
                                )
                            }
                        } else {
                            items(accounts) { acc ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            accountToEdit = acc
                                            showAccountDialog = true
                                        },
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                                    border = BorderStroke(1.dp, colors.cardBorder)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(42.dp)
                                                    .background(
                                                        when (acc.type) {
                                                            AccountType.CREDIT_CARD -> colors.secondary.copy(alpha = 0.2f)
                                                            AccountType.CASH -> colors.incomeGreen.copy(alpha = 0.2f)
                                                            AccountType.UPI -> colors.tertiary.copy(alpha = 0.2f)
                                                            else -> colors.primary.copy(alpha = 0.2f)
                                                        },
                                                        CircleShape
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    when (acc.type) {
                                                        AccountType.CREDIT_CARD -> Icons.Default.CreditCard
                                                        AccountType.CASH -> Icons.Default.Payments
                                                        AccountType.UPI -> Icons.Default.Smartphone
                                                        AccountType.WALLET -> Icons.Default.AccountBalanceWallet
                                                        else -> Icons.Default.AccountBalance
                                                    },
                                                    contentDescription = null,
                                                    tint = when (acc.type) {
                                                        AccountType.CREDIT_CARD -> colors.secondary
                                                        AccountType.CASH -> colors.incomeGreen
                                                        AccountType.UPI -> colors.tertiary
                                                        else -> colors.primary
                                                    },
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    acc.name,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = colors.textPrimary
                                                )
                                                Text(
                                                    buildString {
                                                        append(acc.type.displayName)
                                                        acc.accountNumberLast4?.let { append(" • $it") }
                                                        acc.ifscOrVpa?.let { append(" • $it") }
                                                    },
                                                    fontSize = 12.sp,
                                                    color = colors.textSecondary
                                                )
                                                if (acc.type == AccountType.CREDIT_CARD && acc.creditLimit != null) {
                                                    Text(
                                                        "Limit: ${IndianCurrencyFormatter.format(acc.creditLimit, false)}${acc.statementDay?.let { " • Bill day $it" }.orEmpty()}",
                                                        fontSize = 11.sp,
                                                        color = colors.textMuted
                                                    )
                                                }
                                            }
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                IndianCurrencyFormatter.format(acc.balance),
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (acc.balance >= 0) colors.incomeGreen else colors.expenseRed
                                            )
                                            Text(
                                                "Tap to edit",
                                                fontSize = 11.sp,
                                                color = colors.textMuted
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }

                1 -> {
                    // Subscriptions Tab
                    val totalMonthlyRecurring = subscriptions.sumOf {
                        when (it.frequency) {
                            SubscriptionFrequency.MONTHLY -> it.amount
                            SubscriptionFrequency.YEARLY -> it.amount / 12.0
                            SubscriptionFrequency.QUARTERLY -> it.amount / 3.0
                            SubscriptionFrequency.WEEKLY -> it.amount * 4.33
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            // Summary Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = colors.surfaceElevated),
                                border = BorderStroke(1.dp, colors.cardBorderGlow)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("MONTHLY RECURRING BILLS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.accentAmber)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "${IndianCurrencyFormatter.format(totalMonthlyRecurring)}/mo",
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = colors.textPrimary
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        "${subscriptions.size} active subscriptions tracked locally",
                                        fontSize = 12.sp,
                                        color = colors.textSecondary
                                    )
                                }
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Recurring Subscriptions (${subscriptions.size})",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textPrimary
                                )
                                TextButton(onClick = {
                                    subscriptionToEdit = null
                                    showSubscriptionDialog = true
                                }) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = colors.primary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add Subscription", color = colors.primary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        if (subscriptions.isEmpty()) {
                            item {
                                EmptyStateCard(
                                    icon = Icons.Default.EventRepeat,
                                    title = "No Subscriptions Added",
                                    subtitle = "Track Netflix, Spotify, Amazon Prime, Gym, Wifi, or iCloud renewals to prevent surprise auto-debits.",
                                    actionText = "+ Add Subscription",
                                    onAction = {
                                        subscriptionToEdit = null
                                        showSubscriptionDialog = true
                                    }
                                )
                            }
                        } else {
                            items(subscriptions) { sub ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            subscriptionToEdit = sub
                                            showSubscriptionDialog = true
                                        },
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                                    border = BorderStroke(1.dp, colors.cardBorder)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(42.dp)
                                                    .background(colors.accentAmber.copy(alpha = 0.2f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    Icons.Default.EventRepeat,
                                                    contentDescription = null,
                                                    tint = colors.accentAmber,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    sub.merchantName,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = colors.textPrimary
                                                )
                                                Text(
                                                    "Due day ${sub.billingDay} • ${sub.frequency.displayName}",
                                                    fontSize = 12.sp,
                                                    color = colors.textSecondary
                                                )
                                                val linkedAcc = accounts.firstOrNull { it.id == sub.accountId }
                                                if (linkedAcc != null) {
                                                    Text(
                                                        "Debited from ${linkedAcc.name}",
                                                        fontSize = 11.sp,
                                                        color = colors.primary
                                                    )
                                                }
                                            }
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                IndianCurrencyFormatter.format(sub.amount),
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = colors.textPrimary
                                            )
                                            Text(
                                                "Tap to edit",
                                                fontSize = 11.sp,
                                                color = colors.textMuted
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }

                2 -> {
                    // Savings Goals Tab
                    val totalSaved = goals.sumOf { it.currentAmount }
                    val totalTarget = goals.sumOf { it.targetAmount }
                    val overallProgress = if (totalTarget > 0) (totalSaved / totalTarget).toFloat().coerceIn(0f, 1f) else 0f

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            // Summary Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = colors.surfaceElevated),
                                border = BorderStroke(1.dp, colors.cardBorderGlow)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("TOTAL SAVED IN VAULT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.primary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        IndianCurrencyFormatter.format(totalSaved),
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = colors.primary
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        "Target: ${IndianCurrencyFormatter.format(totalTarget)} (${(overallProgress * 100).toInt()}% funded)",
                                        fontSize = 12.sp,
                                        color = colors.textSecondary
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    LinearProgressIndicator(
                                        progress = { overallProgress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        color = colors.primary,
                                        trackColor = colors.surface
                                    )
                                }
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Savings Goals & Funds (${goals.size})",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textPrimary
                                )
                                TextButton(onClick = {
                                    goalToEdit = null
                                    showGoalDialog = true
                                }) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = colors.primary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("New Goal", color = colors.primary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        if (goals.isEmpty()) {
                            item {
                                EmptyStateCard(
                                    icon = Icons.Default.Savings,
                                    title = "No Savings Goals Set",
                                    subtitle = "Set up an Emergency Fund, Travel Fund, or Gadget Goal to visualize and track your savings.",
                                    actionText = "+ Create First Goal",
                                    onAction = {
                                        goalToEdit = null
                                        showGoalDialog = true
                                    }
                                )
                            }
                        } else {
                            items(goals) { goal ->
                                val fraction = if (goal.targetAmount > 0) (goal.currentAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f) else 0f
                                val isComplete = goal.currentAmount >= goal.targetAmount

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            goalToEdit = goal
                                            showGoalDialog = true
                                        },
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                                    border = BorderStroke(1.dp, if (isComplete) colors.incomeGreen.copy(alpha = 0.4f) else colors.cardBorder)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .background(
                                                            if (isComplete) colors.incomeGreen.copy(alpha = 0.2f) else colors.primary.copy(alpha = 0.2f),
                                                            CircleShape
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        if (isComplete) Icons.Default.CheckCircle else Icons.Default.Savings,
                                                        contentDescription = null,
                                                        tint = if (isComplete) colors.incomeGreen else colors.primary,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column {
                                                    Text(goal.title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                                                    Text(goal.category, fontSize = 11.sp, color = colors.textSecondary)
                                                }
                                            }

                                            Text(
                                                "${IndianCurrencyFormatter.format(goal.currentAmount, false)} / ${IndianCurrencyFormatter.format(goal.targetAmount, false)}",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isComplete) colors.incomeGreen else colors.primary
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        LinearProgressIndicator(
                                            progress = { fraction },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(3.dp)),
                                            color = if (isComplete) colors.incomeGreen else colors.primary,
                                            trackColor = colors.surfaceElevated
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                if (isComplete) "🎉 Goal Achieved!" else "${(fraction * 100).toInt()}% completed",
                                                fontSize = 12.sp,
                                                color = if (isComplete) colors.incomeGreen else colors.textSecondary,
                                                fontWeight = if (isComplete) FontWeight.Bold else FontWeight.Normal
                                            )

                                            Row {
                                                Button(
                                                    onClick = {
                                                        goalToDeposit = goal
                                                        showDepositGoalDialog = true
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary.copy(alpha = 0.15f)),
                                                    shape = RoundedCornerShape(10.dp),
                                                    modifier = Modifier.height(34.dp)
                                                ) {
                                                    Icon(Icons.Default.Add, contentDescription = null, tint = colors.primary, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Deposit Funds", color = colors.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }

                3 -> {
                    // Split Bills / Who Owes Me Tab
                    val totalSplitAmount = splits.sumOf { it.totalAmount }
                    val pendingSplits = splits.count { !it.settled }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            // Summary Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = colors.surfaceElevated),
                                border = BorderStroke(1.dp, colors.cardBorderGlow)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("SHARED EXPENSE LEDGER", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.secondary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        IndianCurrencyFormatter.format(totalSplitAmount),
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = colors.textPrimary
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        "$pendingSplits pending group splits to collect",
                                        fontSize = 12.sp,
                                        color = if (pendingSplits > 0) colors.accentAmber else colors.incomeGreen
                                    )
                                }
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Shared Bills & Splits (${splits.size})",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textPrimary
                                )
                                TextButton(onClick = { showAddSplitDialog = true }) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = colors.primary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("New Split Bill", color = colors.primary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        if (splits.isEmpty()) {
                            item {
                                EmptyStateCard(
                                    icon = Icons.Default.Group,
                                    title = "No Shared Group Expenses",
                                    subtitle = "Split dinner bills, trip expenses, or rent with friends. Track who paid and who still owes you.",
                                    actionText = "+ Create First Split",
                                    onAction = { showAddSplitDialog = true }
                                )
                            }
                        } else {
                            items(splits) { split ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedSplitForDetail = split
                                        },
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                                    border = BorderStroke(1.dp, if (split.settled) colors.incomeGreen.copy(alpha = 0.3f) else colors.cardBorder)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(42.dp)
                                                    .background(
                                                        if (split.settled) colors.incomeGreen.copy(alpha = 0.2f) else colors.secondary.copy(alpha = 0.2f),
                                                        CircleShape
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    Icons.Default.Group,
                                                    contentDescription = null,
                                                    tint = if (split.settled) colors.incomeGreen else colors.secondary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    split.title,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = colors.textPrimary
                                                )
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        if (split.settled) "Settled" else "Pending Settle",
                                                        fontSize = 12.sp,
                                                        color = if (split.settled) colors.incomeGreen else colors.accentAmber,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                    Text(" • ${DateUtils.formatDate(split.timestamp)}", fontSize = 11.sp, color = colors.textMuted)
                                                }
                                            }
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                IndianCurrencyFormatter.format(split.totalAmount),
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = colors.textPrimary
                                            )
                                            Text("View Ledger", fontSize = 11.sp, color = colors.primary)
                                        }
                                    }
                                }
                            }
                        }

                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = {
                when (selectedTab) {
                    0 -> {
                        accountToEdit = null
                        showAccountDialog = true
                    }
                    1 -> {
                        subscriptionToEdit = null
                        showSubscriptionDialog = true
                    }
                    2 -> {
                        goalToEdit = null
                        showGoalDialog = true
                    }
                    3 -> {
                        showAddSplitDialog = true
                    }
                }
            },
            containerColor = colors.primary,
            contentColor = if (colors.isDark) Color.Black else Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 20.dp, end = 20.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Item", modifier = Modifier.size(26.dp))
        }
    }

    // ==========================================
    // DIALOG 1: ADD / EDIT ACCOUNT
    // ==========================================
    if (showAccountDialog) {
        AccountEditDialog(
            initialAccount = accountToEdit,
            onDismiss = { showAccountDialog = false },
            onSave = { acc ->
                coroutineScope.launch {
                    repository.saveAccount(acc)
                    showAccountDialog = false
                }
            },
            onDelete = { acc ->
                coroutineScope.launch {
                    repository.deleteAccount(acc)
                    showAccountDialog = false
                }
            }
        )
    }

    // ==========================================
    // DIALOG 2: ADD / EDIT SUBSCRIPTION
    // ==========================================
    if (showSubscriptionDialog) {
        SubscriptionEditDialog(
            initialSubscription = subscriptionToEdit,
            accounts = accounts,
            onDismiss = { showSubscriptionDialog = false },
            onSave = { sub ->
                coroutineScope.launch {
                    repository.saveSubscription(sub)
                    showSubscriptionDialog = false
                }
            },
            onDelete = { subId ->
                coroutineScope.launch {
                    repository.deleteSubscription(subId)
                    showSubscriptionDialog = false
                }
            }
        )
    }

    // ==========================================
    // DIALOG 3: ADD / EDIT SAVINGS GOAL
    // ==========================================
    if (showGoalDialog) {
        GoalEditDialog(
            initialGoal = goalToEdit,
            onDismiss = { showGoalDialog = false },
            onSave = { goal ->
                coroutineScope.launch {
                    repository.saveGoal(goal)
                    showGoalDialog = false
                }
            },
            onDelete = { goalId ->
                coroutineScope.launch {
                    repository.deleteGoal(goalId)
                    showGoalDialog = false
                }
            }
        )
    }

    // ==========================================
    // DIALOG 4: DEPOSIT FUNDS TO GOAL
    // ==========================================
    if (showDepositGoalDialog && goalToDeposit != null) {
        DepositFundsDialog(
            goal = goalToDeposit!!,
            onDismiss = {
                showDepositGoalDialog = false
                goalToDeposit = null
            },
            onDeposit = { depositAmount ->
                val target = goalToDeposit ?: return@DepositFundsDialog
                coroutineScope.launch {
                    repository.updateGoalAmount(target.id, target.currentAmount + depositAmount)
                    showDepositGoalDialog = false
                    goalToDeposit = null
                }
            }
        )
    }

    // ==========================================
    // DIALOG 5: ADD NEW SPLIT BILL
    // ==========================================
    if (showAddSplitDialog) {
        AddSplitBillDialog(
            onDismiss = { showAddSplitDialog = false },
            onSave = { split, members ->
                coroutineScope.launch {
                    repository.saveSplit(split, members)
                    showAddSplitDialog = false
                }
            }
        )
    }

    // ==========================================
    // DIALOG 6: SPLIT DETAIL & MEMBER LEDGER
    // ==========================================
    selectedSplitForDetail?.let { split ->
        SplitDetailDialog(
            split = split,
            repository = repository,
            onDismiss = { selectedSplitForDetail = null },
            onToggleMemberPaid = { memberId, isPaid ->
                coroutineScope.launch {
                    repository.setMemberPaid(memberId, isPaid)
                }
            },
            onSetSettled = { settled ->
                coroutineScope.launch {
                    repository.setSplitSettled(split.id, settled)
                    selectedSplitForDetail = split.copy(settled = settled)
                }
            },
            onDelete = {
                coroutineScope.launch {
                    repository.deleteSplit(split.id)
                    selectedSplitForDetail = null
                }
            }
        )
    }
}

// ==========================================
// COMPONENT: EMPTY STATE CARD
// ==========================================
@Composable
private fun EmptyStateCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    actionText: String,
    onAction: () -> Unit
) {
    val colors = LocalAppColors.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = BorderStroke(1.dp, colors.cardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(colors.surfaceElevated, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = colors.primary, modifier = Modifier.size(26.dp))
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                subtitle,
                fontSize = 13.sp,
                color = colors.textSecondary,
                lineHeight = 18.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    actionText,
                    color = if (colors.isDark) Color.Black else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

// ==========================================
// DIALOG 1: ACCOUNT EDIT DIALOG
// ==========================================
@Composable
private fun AccountEditDialog(
    initialAccount: AccountEntity?,
    onDismiss: () -> Unit,
    onSave: (AccountEntity) -> Unit,
    onDelete: (AccountEntity) -> Unit
) {
    val colors = LocalAppColors.current
    var name by remember { mutableStateOf(initialAccount?.name ?: "") }
    var selectedType by remember { mutableStateOf(initialAccount?.type ?: AccountType.BANK) }
    var balanceText by remember { mutableStateOf(initialAccount?.balance?.toString() ?: "0.0") }
    var last4 by remember { mutableStateOf(initialAccount?.accountNumberLast4 ?: "") }
    var ifscOrVpa by remember { mutableStateOf(initialAccount?.ifscOrVpa ?: "") }
    var creditLimitText by remember { mutableStateOf(initialAccount?.creditLimit?.toString() ?: "100000.0") }
    var statementDayText by remember { mutableStateOf(initialAccount?.statementDay?.toString() ?: "15") }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            border = BorderStroke(1.dp, colors.cardBorder)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialAccount == null) "Add Account / Card" else "Edit Account",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = colors.textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Account Type Chips
                Text("Account Type", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.textSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(AccountType.values()) { type ->
                        val isSelected = selectedType == type
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) colors.primary else colors.surfaceElevated)
                                .clickable { selectedType = type }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Text(
                                type.displayName,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) (if (colors.isDark) Color.Black else Color.White) else colors.textPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Account Name", color = colors.textSecondary) },
                    placeholder = { Text("e.g. HDFC Salary, ICICI Amazon Pay, GPay UPI", color = colors.textMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.cardBorder,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = balanceText,
                    onValueChange = { balanceText = it },
                    label = { Text(if (selectedType == AccountType.CREDIT_CARD) "Current Outstanding Balance (₹)" else "Current Balance (₹)", color = colors.textSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.cardBorder,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (selectedType == AccountType.BANK || selectedType == AccountType.CREDIT_CARD) {
                    OutlinedTextField(
                        value = last4,
                        onValueChange = { if (it.length <= 4) last4 = it },
                        label = { Text("Last 4 Digits of Account / Card", color = colors.textSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.cardBorder,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                if (selectedType == AccountType.CREDIT_CARD) {
                    OutlinedTextField(
                        value = creditLimitText,
                        onValueChange = { creditLimitText = it },
                        label = { Text("Total Credit Limit (₹)", color = colors.textSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.cardBorder,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = statementDayText,
                        onValueChange = { statementDayText = it },
                        label = { Text("Statement Billing Day of Month (1-31)", color = colors.textSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.cardBorder,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                if (selectedType == AccountType.UPI) {
                    OutlinedTextField(
                        value = ifscOrVpa,
                        onValueChange = { ifscOrVpa = it },
                        label = { Text("UPI VPA / Handle (e.g. user@okhdfcbank)", color = colors.textSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.cardBorder,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val parsedBalance = balanceText.toDoubleOrNull() ?: 0.0
                        val entity = AccountEntity(
                            id = initialAccount?.id ?: "acc_${UUID.randomUUID().toString().take(8)}",
                            name = name.ifBlank { selectedType.displayName },
                            type = selectedType,
                            accountNumberLast4 = last4.ifBlank { null },
                            ifscOrVpa = ifscOrVpa.ifBlank { null },
                            balance = parsedBalance,
                            currency = "INR",
                            creditLimit = if (selectedType == AccountType.CREDIT_CARD) creditLimitText.toDoubleOrNull() else null,
                            statementDay = if (selectedType == AccountType.CREDIT_CARD) statementDayText.toIntOrNull()?.coerceIn(1, 31) else null,
                            colorHex = when (selectedType) {
                                AccountType.CREDIT_CARD -> "#8B5CF6"
                                AccountType.CASH -> "#10B981"
                                AccountType.UPI -> "#06B6D4"
                                else -> "#3B82F6"
                            },
                            iconName = when (selectedType) {
                                AccountType.CREDIT_CARD -> "credit_card"
                                AccountType.CASH -> "payments"
                                AccountType.UPI -> "smartphone"
                                AccountType.WALLET -> "account_balance_wallet"
                                else -> "account_balance"
                            }
                        )
                        onSave(entity)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = if (colors.isDark) Color.Black else Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        if (initialAccount == null) "Create Account" else "Save Changes",
                        color = if (colors.isDark) Color.Black else Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (initialAccount != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.expenseRed),
                        border = BorderStroke(1.dp, colors.expenseRed.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = colors.expenseRed)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Delete Account", color = colors.expenseRed, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showDeleteConfirm && initialAccount != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Account?", color = colors.textPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to remove '${initialAccount.name}'? Existing transaction logs linked to this account will remain intact.", color = colors.textSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete(initialAccount)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.expenseRed)
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = colors.textSecondary)
                }
            },
            containerColor = colors.surface,
            shape = RoundedCornerShape(18.dp)
        )
    }
}

// ==========================================
// DIALOG 2: SUBSCRIPTION EDIT DIALOG
// ==========================================
@Composable
private fun SubscriptionEditDialog(
    initialSubscription: SubscriptionEntity?,
    accounts: List<AccountEntity>,
    onDismiss: () -> Unit,
    onSave: (SubscriptionEntity) -> Unit,
    onDelete: (String) -> Unit
) {
    val colors = LocalAppColors.current
    var merchantName by remember { mutableStateOf(initialSubscription?.merchantName ?: "") }
    var amountText by remember { mutableStateOf(initialSubscription?.amount?.toString() ?: "") }
    var selectedFrequency by remember { mutableStateOf(initialSubscription?.frequency ?: SubscriptionFrequency.MONTHLY) }
    var billingDayText by remember { mutableStateOf(initialSubscription?.billingDay?.toString() ?: "1") }
    var selectedAccountId by remember { mutableStateOf(initialSubscription?.accountId ?: accounts.firstOrNull()?.id) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            border = BorderStroke(1.dp, colors.cardBorder)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialSubscription == null) "New Subscription" else "Edit Subscription",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = colors.textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = merchantName,
                    onValueChange = { merchantName = it },
                    label = { Text("Service / Merchant Name", color = colors.textSecondary) },
                    placeholder = { Text("e.g. Netflix Premium, Spotify, Gym, JioFiber", color = colors.textMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.cardBorder,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Billing Amount (₹)", color = colors.textSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.cardBorder,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text("Billing Cycle", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.textSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(SubscriptionFrequency.values()) { freq ->
                        val isSelected = selectedFrequency == freq
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) colors.primary else colors.surfaceElevated)
                                .clickable { selectedFrequency = freq }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Text(
                                freq.displayName,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) (if (colors.isDark) Color.Black else Color.White) else colors.textPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = billingDayText,
                    onValueChange = { billingDayText = it },
                    label = { Text("Renewal Billing Day of Month (1-31)", color = colors.textSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.cardBorder,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (accounts.isNotEmpty()) {
                    Text("Auto-Debited From", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.textSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(accounts) { acc ->
                            val isSelected = selectedAccountId == acc.id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) colors.primary else colors.surfaceElevated)
                                    .clickable { selectedAccountId = acc.id }
                                    .padding(horizontal = 12.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    acc.name,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) (if (colors.isDark) Color.Black else Color.White) else colors.textPrimary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val parsedAmount = amountText.toDoubleOrNull() ?: 0.0
                        val bDay = billingDayText.toIntOrNull()?.coerceIn(1, 31) ?: 1

                        val cal = Calendar.getInstance()
                        cal.set(Calendar.DAY_OF_MONTH, bDay)
                        if (cal.timeInMillis < System.currentTimeMillis()) {
                            cal.add(Calendar.MONTH, 1)
                        }

                        val entity = SubscriptionEntity(
                            id = initialSubscription?.id ?: "sub_${UUID.randomUUID().toString().take(8)}",
                            merchantName = merchantName.ifBlank { "Subscription" },
                            amount = parsedAmount,
                            frequency = selectedFrequency,
                            billingDay = bDay,
                            nextDueDate = cal.timeInMillis,
                            categoryId = "entertainment",
                            accountId = selectedAccountId,
                            isActive = true
                        )
                        onSave(entity)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                    shape = RoundedCornerShape(12.dp),
                    enabled = merchantName.isNotBlank() && (amountText.toDoubleOrNull() ?: 0.0) > 0
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = if (colors.isDark) Color.Black else Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        if (initialSubscription == null) "Save Subscription" else "Update Subscription",
                        color = if (colors.isDark) Color.Black else Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (initialSubscription != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.expenseRed),
                        border = BorderStroke(1.dp, colors.expenseRed.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = colors.expenseRed)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Cancel & Delete", color = colors.expenseRed, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showDeleteConfirm && initialSubscription != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Subscription?", color = colors.textPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to stop tracking '${initialSubscription.merchantName}'? Future renewal reminders will be canceled.", color = colors.textSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete(initialSubscription.id)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.expenseRed)
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = colors.textSecondary)
                }
            },
            containerColor = colors.surface,
            shape = RoundedCornerShape(18.dp)
        )
    }
}

// ==========================================
// DIALOG 3: SAVINGS GOAL EDIT DIALOG
// ==========================================
@Composable
private fun GoalEditDialog(
    initialGoal: SavingsGoalEntity?,
    onDismiss: () -> Unit,
    onSave: (SavingsGoalEntity) -> Unit,
    onDelete: (String) -> Unit
) {
    val colors = LocalAppColors.current
    var title by remember { mutableStateOf(initialGoal?.title ?: "") }
    var targetAmountText by remember { mutableStateOf(initialGoal?.targetAmount?.toString() ?: "") }
    var currentAmountText by remember { mutableStateOf(initialGoal?.currentAmount?.toString() ?: "0.0") }
    var category by remember { mutableStateOf(initialGoal?.category ?: "Emergency Fund") }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val popularCategories = listOf("Emergency Fund", "Vacation / Travel", "MacBook / Gadget", "Gold / Investment", "Vehicle", "Home Renovations")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            border = BorderStroke(1.dp, colors.cardBorder)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialGoal == null) "New Savings Goal" else "Edit Goal",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = colors.textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Goal Title", color = colors.textSecondary) },
                    placeholder = { Text("e.g. 6 Months Emergency Fund, Japan Trip", color = colors.textMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.cardBorder,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = targetAmountText,
                    onValueChange = { targetAmountText = it },
                    label = { Text("Target Goal Amount (₹)", color = colors.textSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.cardBorder,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = currentAmountText,
                    onValueChange = { currentAmountText = it },
                    label = { Text("Current Saved Amount (₹)", color = colors.textSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.cardBorder,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text("Purpose / Category", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.textSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(popularCategories) { cat ->
                        val isSelected = category == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) colors.primary else colors.surfaceElevated)
                                .clickable { category = cat }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Text(
                                cat,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) (if (colors.isDark) Color.Black else Color.White) else colors.textPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val target = targetAmountText.toDoubleOrNull() ?: 0.0
                        val current = currentAmountText.toDoubleOrNull() ?: 0.0
                        val entity = SavingsGoalEntity(
                            id = initialGoal?.id ?: "goal_${UUID.randomUUID().toString().take(8)}",
                            title = title.ifBlank { "Savings Goal" },
                            targetAmount = target,
                            currentAmount = current,
                            category = category,
                            isCompleted = current >= target && target > 0
                        )
                        onSave(entity)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                    shape = RoundedCornerShape(12.dp),
                    enabled = title.isNotBlank() && (targetAmountText.toDoubleOrNull() ?: 0.0) > 0
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = if (colors.isDark) Color.Black else Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        if (initialGoal == null) "Create Goal" else "Save Changes",
                        color = if (colors.isDark) Color.Black else Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (initialGoal != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.expenseRed),
                        border = BorderStroke(1.dp, colors.expenseRed.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = colors.expenseRed)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Delete Goal", color = colors.expenseRed, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showDeleteConfirm && initialGoal != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Savings Goal?", color = colors.textPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to remove '${initialGoal.title}'?", color = colors.textSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete(initialGoal.id)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.expenseRed)
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = colors.textSecondary)
                }
            },
            containerColor = colors.surface,
            shape = RoundedCornerShape(18.dp)
        )
    }
}

// ==========================================
// DIALOG 4: DEPOSIT FUNDS TO GOAL
// ==========================================
@Composable
private fun DepositFundsDialog(
    goal: SavingsGoalEntity,
    onDismiss: () -> Unit,
    onDeposit: (Double) -> Unit
) {
    val colors = LocalAppColors.current
    var depositText by remember { mutableStateOf("") }
    val quickChips = listOf(500.0, 1000.0, 2000.0, 5000.0, 10000.0)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            border = BorderStroke(1.dp, colors.cardBorder)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Deposit to ${goal.title}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = colors.textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    "Currently: ${IndianCurrencyFormatter.format(goal.currentAmount)} of ${IndianCurrencyFormatter.format(goal.targetAmount)}",
                    fontSize = 13.sp,
                    color = colors.textSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Quick Chips
                Text("Quick Deposit Amounts", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.textSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(quickChips) { chipAmt ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.surfaceElevated)
                                .clickable { depositText = chipAmt.toInt().toString() }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Text(
                                "+₹${chipAmt.toInt()}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = depositText,
                    onValueChange = { depositText = it },
                    label = { Text("Deposit Amount (₹)", color = colors.textSecondary) },
                    placeholder = { Text("Enter amount to add", color = colors.textMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.cardBorder,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                val depositVal = depositText.toDoubleOrNull() ?: 0.0
                Button(
                    onClick = {
                        if (depositVal > 0) {
                            onDeposit(depositVal)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                    shape = RoundedCornerShape(12.dp),
                    enabled = depositVal > 0
                ) {
                    Icon(Icons.Default.Savings, contentDescription = null, tint = if (colors.isDark) Color.Black else Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Add ${if (depositVal > 0) IndianCurrencyFormatter.format(depositVal) else ""} to Goal",
                        color = if (colors.isDark) Color.Black else Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ==========================================
// DIALOG 5: ADD NEW SPLIT BILL
// ==========================================
data class SplitMemberDraft(
    val name: String,
    val amountText: String
)

@Composable
private fun AddSplitBillDialog(
    onDismiss: () -> Unit,
    onSave: (SplitExpenseEntity, List<SplitMemberEntity>) -> Unit
) {
    val colors = LocalAppColors.current
    var title by remember { mutableStateOf("") }
    var totalAmountText by remember { mutableStateOf("") }
    val members = remember {
        mutableStateListOf(
            SplitMemberDraft("You", ""),
            SplitMemberDraft("Friend 1", "")
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            border = BorderStroke(1.dp, colors.cardBorder)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "New Split Bill",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = colors.textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Bill / Event Title", color = colors.textSecondary) },
                    placeholder = { Text("e.g. Dinner at Social, Goa Villa, Groceries", color = colors.textMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.cardBorder,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = totalAmountText,
                    onValueChange = { totalAmountText = it },
                    label = { Text("Total Bill Amount (₹)", color = colors.textSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.cardBorder,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Participants & Shares", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                    TextButton(
                        onClick = {
                            val total = totalAmountText.toDoubleOrNull() ?: 0.0
                            if (total > 0 && members.isNotEmpty()) {
                                val equalShare = (total / members.size)
                                val formattedShare = String.format("%.2f", equalShare)
                                val updated = members.map { it.copy(amountText = formattedShare) }
                                members.clear()
                                members.addAll(updated)
                            }
                        }
                    ) {
                        Text("Equal Split", color = colors.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                members.forEachIndexed { index, draft ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = draft.name,
                            onValueChange = { newName ->
                                members[index] = draft.copy(name = newName)
                            },
                            placeholder = { Text("Name", color = colors.textMuted) },
                            modifier = Modifier.weight(1.4f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.primary,
                                unfocusedBorderColor = colors.cardBorder,
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        OutlinedTextField(
                            value = draft.amountText,
                            onValueChange = { newAmt ->
                                members[index] = draft.copy(amountText = newAmt)
                            },
                            placeholder = { Text("₹ Amount", color = colors.textMuted) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.primary,
                                unfocusedBorderColor = colors.cardBorder,
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )

                        if (members.size > 2) {
                            IconButton(
                                onClick = { members.removeAt(index) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Remove", tint = colors.expenseRed, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        members.add(SplitMemberDraft("Person ${members.size + 1}", ""))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = colors.primary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Person", color = colors.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(20.dp))

                val total = totalAmountText.toDoubleOrNull() ?: 0.0
                Button(
                    onClick = {
                        val splitId = "split_${UUID.randomUUID().toString().take(8)}"
                        val splitEntity = SplitExpenseEntity(
                            id = splitId,
                            title = title.ifBlank { "Group Split" },
                            totalAmount = total,
                            settled = false,
                            timestamp = System.currentTimeMillis()
                        )

                        val memberEntities = members.mapIndexed { i, draft ->
                            val amt = draft.amountText.toDoubleOrNull() ?: (total / members.size)
                            SplitMemberEntity(
                                id = "mem_${UUID.randomUUID().toString().take(8)}",
                                splitId = splitId,
                                personName = draft.name.ifBlank { "Member ${i + 1}" },
                                amountOwed = amt,
                                isPaid = i == 0, // Assume the creator/user paid their share
                                isUser = i == 0
                            )
                        }

                        onSave(splitEntity, memberEntities)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                    shape = RoundedCornerShape(12.dp),
                    enabled = title.isNotBlank() && total > 0
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = if (colors.isDark) Color.Black else Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Create Split Bill", color = if (colors.isDark) Color.Black else Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==========================================
// DIALOG 6: SPLIT DETAIL & MEMBER LEDGER
// ==========================================
@Composable
private fun SplitDetailDialog(
    split: SplitExpenseEntity,
    repository: FinancialRepository,
    onDismiss: () -> Unit,
    onToggleMemberPaid: (String, Boolean) -> Unit,
    onSetSettled: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val colors = LocalAppColors.current
    val members by repository.getSplitMembersFlow(split.id).collectAsState(initial = emptyList())
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val collectedAmount = members.filter { it.isPaid }.sumOf { it.amountOwed }
    val pendingAmount = members.filter { !it.isPaid }.sumOf { it.amountOwed }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            border = BorderStroke(1.dp, colors.cardBorder)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = split.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Total: ${IndianCurrencyFormatter.format(split.totalAmount)}",
                            fontSize = 14.sp,
                            color = colors.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = colors.textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Settlement Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (split.settled) colors.incomeGreen.copy(alpha = 0.15f) else colors.accentAmber.copy(alpha = 0.15f)
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (split.settled) colors.incomeGreen.copy(alpha = 0.4f) else colors.accentAmber.copy(alpha = 0.4f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                if (split.settled) "Status: Fully Settled" else "Status: Pending Collection",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (split.settled) colors.incomeGreen else colors.accentAmber
                            )
                            Text(
                                "Collected: ${IndianCurrencyFormatter.format(collectedAmount)} • Pending: ${IndianCurrencyFormatter.format(pendingAmount)}",
                                fontSize = 11.sp,
                                color = colors.textSecondary
                            )
                        }
                        Button(
                            onClick = { onSetSettled(!split.settled) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (split.settled) colors.surfaceElevated else colors.incomeGreen
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(
                                if (split.settled) "Reopen" else "Mark Settled",
                                fontSize = 11.sp,
                                color = if (split.settled) colors.textPrimary else Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Member Payment Status (Tap to toggle)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))

                members.forEach { member ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onToggleMemberPaid(member.id, !member.isPaid) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (member.isPaid) colors.surfaceElevated else colors.surface
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (member.isPaid) colors.incomeGreen.copy(alpha = 0.3f) else colors.cardBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    member.personName + if (member.isUser) " (You)" else "",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textPrimary
                                )
                                Text(
                                    IndianCurrencyFormatter.format(member.amountOwed),
                                    fontSize = 12.sp,
                                    color = colors.textSecondary
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (member.isPaid) colors.incomeGreen.copy(alpha = 0.2f) else colors.accentAmber.copy(alpha = 0.2f)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        if (member.isPaid) Icons.Default.Check else Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = if (member.isPaid) colors.incomeGreen else colors.accentAmber,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        if (member.isPaid) "PAID" else "PENDING",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (member.isPaid) colors.incomeGreen else colors.accentAmber
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.expenseRed),
                    border = BorderStroke(1.dp, colors.expenseRed.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = colors.expenseRed)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Delete Split Bill", color = colors.expenseRed, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Split Bill?", color = colors.textPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to permanently delete '${split.title}' and all associated member records?", color = colors.textSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.expenseRed)
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = colors.textSecondary)
                }
            },
            containerColor = colors.surface,
            shape = RoundedCornerShape(18.dp)
        )
    }
}
