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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.intelliexpense.app.core.model.TransactionType
import com.intelliexpense.app.core.util.DateUtils
import com.intelliexpense.app.core.util.IndianCurrencyFormatter
import com.intelliexpense.app.data.model.TransactionEntity
import com.intelliexpense.app.data.repository.FinancialRepository
import com.intelliexpense.app.ui.components.TransactionItemRow
import com.intelliexpense.app.ui.theme.Amber500
import com.intelliexpense.app.ui.theme.CardBorder
import com.intelliexpense.app.ui.theme.Emerald500
import com.intelliexpense.app.ui.theme.Rose500
import com.intelliexpense.app.ui.theme.Slate400
import com.intelliexpense.app.ui.theme.Slate800
import com.intelliexpense.app.ui.theme.Slate850
import com.intelliexpense.app.ui.theme.Slate900
import kotlinx.coroutines.launch

@Composable
fun TransactionsScreen(
    repository: FinancialRepository,
    initialFilterUnreviewed: Boolean = false
) {
    val coroutineScope = rememberCoroutineScope()
    val allTransactions by repository.getAllTransactionsFlow().collectAsState(initial = emptyList())
    val accounts by repository.getAllAccountsFlow().collectAsState(initial = emptyList())
    val categories by repository.getAllCategoriesFlow().collectAsState(initial = emptyList())

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(if (initialFilterUnreviewed) "Unreviewed" else "All") }
    var showQuickCapture by remember { mutableStateOf(false) }
    var selectedTxnForDetail by remember { mutableStateOf<TransactionEntity?>(null) }

    val filterOptions = listOf("All", "Expense", "Income", "Transfer", "Unreviewed")

    val filteredTransactions = remember(allTransactions, searchQuery, selectedFilter) {
        allTransactions.filter { txn ->
            val matchesFilter = when (selectedFilter) {
                "Expense" -> txn.type == TransactionType.EXPENSE
                "Income" -> txn.type == TransactionType.INCOME
                "Transfer" -> txn.type == TransactionType.TRANSFER
                "Unreviewed" -> !txn.isReviewed
                else -> true
            }
            val matchesQuery = if (searchQuery.isBlank()) {
                true
            } else {
                txn.merchantNormalized.contains(searchQuery, ignoreCase = true) ||
                        txn.merchantOriginal.contains(searchQuery, ignoreCase = true) ||
                        txn.description.orEmpty().contains(searchQuery, ignoreCase = true) ||
                        txn.paymentMethod.contains(searchQuery, ignoreCase = true) ||
                        txn.amount.toString().contains(searchQuery)
            }
            matchesFilter && matchesQuery
        }
    }

    val groupedTransactions = remember(filteredTransactions) {
        filteredTransactions.groupBy { DateUtils.getRelativeDateHeader(it.timestamp) }
    }

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Transaction Ledger",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Search Box
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search merchant, amount, UPI ref...", color = Slate400, fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Slate400) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = Slate400)
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Emerald500,
                    unfocusedBorderColor = Slate800,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Slate850,
                    unfocusedContainerColor = Slate850
                ),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips Row
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filterOptions) { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Emerald500,
                            selectedLabelColor = Color.Black,
                            containerColor = Slate850,
                            labelColor = Slate400
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (selectedFilter == filter) Emerald500 else CardBorder,
                            enabled = true,
                            selected = selectedFilter == filter
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Timeline List
            if (filteredTransactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 80.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Text("No transactions matching your criteria.", color = Slate400, fontSize = 14.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    groupedTransactions.forEach { (dateHeader, txns) ->
                        item {
                            Text(
                                text = dateHeader,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Slate400,
                                modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                            )
                        }
                        items(txns) { txn ->
                            TransactionItemRow(
                                transaction = txn,
                                onClick = { selectedTxnForDetail = txn }
                            )
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(70.dp))
                    }
                }
            }
        }
    }

    // Detail / Review Dialog
    selectedTxnForDetail?.let { txn ->
        AlertDialog(
            onDismissRequest = { selectedTxnForDetail = null },
            title = {
                Text(txn.merchantNormalized, color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Amount: ${IndianCurrencyFormatter.format(txn.amount)}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Emerald500)
                    Text("Type: ${txn.type.displayName}", color = Slate400, fontSize = 13.sp)
                    Text("Category: ${txn.categoryId.replace('_', ' ')}", color = Slate400, fontSize = 13.sp)
                    Text("Payment Method: ${txn.paymentMethod}", color = Slate400, fontSize = 13.sp)
                    Text("Captured From: ${txn.captureSource.displayName}", color = Slate400, fontSize = 13.sp)
                    txn.upiReference?.let { Text("UPI Ref (UTR): $it", color = Slate400, fontSize = 13.sp) }
                    Text("Date: ${DateUtils.formatFullDateTime(txn.timestamp)}", color = Slate400, fontSize = 13.sp)
                    if (!txn.isReviewed) {
                        Text("Status: Needs Review", color = Amber500, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            },
            confirmButton = {
                if (!txn.isReviewed) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                repository.updateTransaction(txn.copy(isReviewed = true))
                                selectedTxnForDetail = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald500)
                    ) {
                        Text("Confirm & Verify", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                } else {
                    TextButton(onClick = { selectedTxnForDetail = null }) {
                        Text("Close", color = Emerald500)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            repository.deleteTransaction(txn.id)
                            selectedTxnForDetail = null
                        }
                    }
                ) {
                    Text("Delete", color = Rose500)
                }
            },
            containerColor = Slate850
        )
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
