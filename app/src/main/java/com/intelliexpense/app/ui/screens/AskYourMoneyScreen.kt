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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.intelliexpense.app.ai.AiResponse
import com.intelliexpense.app.ai.AskYourMoneyEngine
import com.intelliexpense.app.data.repository.FinancialRepository
import com.intelliexpense.app.ui.theme.LocalAppColors

data class ChatMessage(
    val id: String,
    val isUser: Boolean,
    val text: String,
    val aiResponse: AiResponse? = null
)

@Composable
fun AskYourMoneyScreen(
    repository: FinancialRepository,
    aiEngine: AskYourMoneyEngine
) {
    val colors = LocalAppColors.current

    val transactions by repository.getAllTransactionsFlow().collectAsState(initial = emptyList())
    val accounts by repository.getAllAccountsFlow().collectAsState(initial = emptyList())
    val categories by repository.getAllCategoriesFlow().collectAsState(initial = emptyList())
    val budgets by repository.getBudgetsForMonthFlow(com.intelliexpense.app.core.util.DateUtils.getCurrentMonthKey()).collectAsState(initial = emptyList())
    val subscriptions by repository.getActiveSubscriptionsFlow().collectAsState(initial = emptyList())

    var inputText by remember { mutableStateOf("") }
    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                id = "welcome",
                isUser = false,
                text = "Hello! I am your 100% on-device financial assistant. I analyze your local transactions with zero cloud telemetry. Ask me anything about your money."
            )
        )
    }

    val suggestionPrompts = listOf(
        "How much did I spend on food this month?",
        "Why did my spending increase?",
        "Show my biggest expenses",
        "What subscriptions do I have?",
        "Where can I reduce spending?",
        "Can I afford ₹5,000?",
        "Who owes me money?"
    )

    fun sendQuery(query: String) {
        if (query.isBlank()) return
        messages.add(ChatMessage(id = java.util.UUID.randomUUID().toString(), isUser = true, text = query))

        val response = aiEngine.answerQuery(
            query = query,
            transactions = transactions,
            accounts = accounts,
            budgets = budgets,
            subscriptions = subscriptions,
            categories = categories
        )

        messages.add(
            ChatMessage(
                id = java.util.UUID.randomUUID().toString(),
                isUser = false,
                text = response.mainAnswer,
                aiResponse = response
            )
        )
        inputText = ""
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Ask Your Money",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = colors.primary, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "100% Local Intelligence • Zero Cloud Storage",
                        fontSize = 11.sp,
                        color = colors.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(colors.primary.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = colors.primary, modifier = Modifier.size(20.dp))
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Quick Suggestion Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(suggestionPrompts) { prompt ->
                SuggestionChip(
                    onClick = { sendQuery(prompt) },
                    label = { Text(prompt, fontSize = 12.sp) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = colors.surface,
                        labelColor = colors.textSecondary
                    ),
                    border = SuggestionChipDefaults.suggestionChipBorder(
                        borderColor = colors.cardBorder,
                        enabled = true
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Message Thread
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(messages) { msg ->
                if (msg.isUser) {
                    // User Bubble
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = colors.primary)
                        ) {
                            Text(
                                text = msg.text,
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 11.dp)
                            )
                        }
                    }
                } else {
                    // AI Response Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                        border = BorderStroke(1.dp, colors.cardBorder)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            msg.aiResponse?.let { resp ->
                                Text(
                                    text = resp.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.primary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                            }

                            Text(
                                text = msg.text,
                                color = colors.textPrimary,
                                fontSize = 14.sp,
                                lineHeight = 21.sp
                            )

                            // Key Metrics Row
                            msg.aiResponse?.keyMetrics?.takeIf { it.isNotEmpty() }?.let { metrics ->
                                Spacer(modifier = Modifier.height(14.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    metrics.forEach { metric ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .background(colors.surfaceElevated, RoundedCornerShape(12.dp))
                                                .padding(10.dp)
                                        ) {
                                            Column {
                                                Text(metric.label, fontSize = 10.sp, color = colors.textSecondary)
                                                Text(metric.value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                                            }
                                        }
                                    }
                                }
                            }

                            // Breakdown Items
                            msg.aiResponse?.breakdownItems?.takeIf { it.isNotEmpty() }?.let { items ->
                                Spacer(modifier = Modifier.height(14.dp))
                                items.forEach { item ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(item.title, fontSize = 13.sp, color = colors.textPrimary, fontWeight = FontWeight.Medium)
                                            Text(item.subtitle, fontSize = 11.sp, color = colors.textSecondary)
                                        }
                                        Text(item.amount, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                                    }
                                }
                            }

                            // Actionable Tip
                            msg.aiResponse?.actionableTip?.let { tip ->
                                Spacer(modifier = Modifier.height(14.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(colors.surfaceElevated, RoundedCornerShape(12.dp))
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Lightbulb, contentDescription = null, tint = colors.accentAmber, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(tip, fontSize = 12.sp, color = colors.textSecondary, lineHeight = 17.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Bottom Input Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ask about expenses, budgets, savings...", color = colors.textSecondary, fontSize = 13.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primary,
                    unfocusedBorderColor = colors.cardBorder,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary,
                    focusedContainerColor = colors.surface,
                    unfocusedContainerColor = colors.surface
                ),
                shape = RoundedCornerShape(18.dp),
                maxLines = 2
            )
            Spacer(modifier = Modifier.width(10.dp))
            IconButton(
                onClick = { sendQuery(inputText) },
                modifier = Modifier
                    .size(50.dp)
                    .background(colors.primary, CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.Black, modifier = Modifier.size(20.dp))
            }
        }

        Spacer(modifier = Modifier.height(55.dp))
    }
}
