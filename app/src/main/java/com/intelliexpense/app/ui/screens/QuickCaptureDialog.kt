package com.intelliexpense.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.window.Dialog
import com.intelliexpense.app.capture.NaturalLanguageExpenseParser
import com.intelliexpense.app.core.model.CaptureSource
import com.intelliexpense.app.core.util.IndianCurrencyFormatter
import com.intelliexpense.app.data.model.AccountEntity
import com.intelliexpense.app.data.model.CategoryEntity
import com.intelliexpense.app.data.model.TransactionEntity
import com.intelliexpense.app.data.repository.FinancialRepository
import com.intelliexpense.app.ui.theme.LocalAppColors
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun QuickCaptureDialog(
    accounts: List<AccountEntity>,
    categories: List<CategoryEntity>,
    repository: FinancialRepository,
    onDismiss: () -> Unit
) {
    val colors = LocalAppColors.current
    val coroutineScope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(0) }

    // Natural language state
    var nlInput by remember { mutableStateOf("") }

    // Manual form state
    var manualAmount by remember { mutableStateOf("") }
    var manualMerchant by remember { mutableStateOf("") }
    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id ?: "acc_primary_bank") }
    var selectedCategoryId by remember { mutableStateOf(categories.firstOrNull()?.id ?: "food_dining") }

    val parsedPreview = remember(nlInput) {
        if (nlInput.isNotBlank()) NaturalLanguageExpenseParser.parse(nlInput) else null
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, colors.cardBorder)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Quick Record Expense",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = colors.textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tab Row
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = colors.surfaceElevated,
                    contentColor = colors.primary
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Smart Entry", color = if (selectedTab == 0) colors.primary else colors.textSecondary)
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Manual Form", color = if (selectedTab == 1) colors.primary else colors.textSecondary)
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedTab == 0) {
                    // Smart Natural Language Entry
                    OutlinedTextField(
                        value = nlInput,
                        onValueChange = { nlInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text("e.g. Swiggy 450 yesterday via HDFC, or 60 auto cash", color = colors.textSecondary, fontSize = 13.sp)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.cardBorder,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary,
                            focusedContainerColor = colors.surfaceElevated,
                            unfocusedContainerColor = colors.surfaceElevated
                        ),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    if (parsedPreview != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = colors.surfaceElevated),
                            border = androidx.compose.foundation.BorderStroke(1.dp, colors.primary.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Auto-Detected Preview", fontSize = 11.sp, color = colors.primary, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(parsedPreview.merchantNormalized, color = colors.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                                        Text("${parsedPreview.categoryId.replace('_', ' ')} • ${parsedPreview.paymentMethod}", color = colors.textSecondary, fontSize = 12.sp)
                                    }
                                    Text(
                                        IndianCurrencyFormatter.format(parsedPreview.amount),
                                        color = colors.primary,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 18.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            val parsed = parsedPreview ?: return@Button
                            coroutineScope.launch {
                                val entity = TransactionEntity(
                                    id = UUID.randomUUID().toString(),
                                    accountId = selectedAccountId,
                                    type = parsed.type,
                                    amount = parsed.amount,
                                    currency = "INR",
                                    merchantOriginal = parsed.merchantOriginal,
                                    merchantNormalized = parsed.merchantNormalized,
                                    categoryId = parsed.categoryId,
                                    description = "Natural language entry",
                                    timestamp = parsed.timestamp,
                                    paymentMethod = parsed.paymentMethod,
                                    rawSourceText = parsed.rawText,
                                    captureSource = CaptureSource.MANUAL,
                                    confidenceScore = 1.0f,
                                    isReviewed = true
                                )
                                repository.recordTransaction(entity)
                                onDismiss()
                            }
                        },
                        enabled = parsedPreview != null && parsedPreview.amount > 0,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = if (colors.isDark) Color.Black else Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Transaction", color = if (colors.isDark) Color.Black else Color.White, fontWeight = FontWeight.Bold)
                    }

                } else {
                    // Manual Form
                    OutlinedTextField(
                        value = manualAmount,
                        onValueChange = { manualAmount = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Amount (₹)", color = colors.textSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.cardBorder,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = manualMerchant,
                        onValueChange = { manualMerchant = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Merchant / Description", color = colors.textSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.cardBorder,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            val amt = manualAmount.toDoubleOrNull() ?: return@Button
                            val merchant = manualMerchant.ifBlank { "General Expense" }
                            coroutineScope.launch {
                                val entity = TransactionEntity(
                                    id = UUID.randomUUID().toString(),
                                    accountId = selectedAccountId,
                                    type = com.intelliexpense.app.core.model.TransactionType.EXPENSE,
                                    amount = amt,
                                    currency = "INR",
                                    merchantOriginal = merchant,
                                    merchantNormalized = com.intelliexpense.app.intelligence.IndianMerchantRegistry.normalize(merchant).brandName,
                                    categoryId = selectedCategoryId,
                                    description = "Manual entry",
                                    timestamp = System.currentTimeMillis(),
                                    paymentMethod = "UPI",
                                    captureSource = CaptureSource.MANUAL,
                                    confidenceScore = 1.0f,
                                    isReviewed = true
                                )
                                repository.recordTransaction(entity)
                                onDismiss()
                            }
                        },
                        enabled = manualAmount.toDoubleOrNull() != null && (manualAmount.toDoubleOrNull() ?: 0.0) > 0,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = if (colors.isDark) Color.Black else Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Transaction", color = if (colors.isDark) Color.Black else Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
