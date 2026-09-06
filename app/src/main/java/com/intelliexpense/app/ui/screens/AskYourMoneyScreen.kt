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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Send
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
import com.intelliexpense.app.ui.theme.CardBorder
import com.intelliexpense.app.ui.theme.Emerald500
import com.intelliexpense.app.ui.theme.Slate400
import com.intelliexpense.app.ui.theme.Slate800
import com.intelliexpense.app.ui.theme.Slate850
import com.intelliexpense.app.ui.theme.Slate900

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
            .background(Slate900)
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
                    color = Color.White
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = Emerald500, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "100% Local Intelligence • Zero Cloud Storage",
                        fontSize = 11.sp,
                        color = Emerald500,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Emerald500.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Emerald500, modifier = Modifier.size(20.dp))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

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
                        containerColor = Slate850,
                        labelColor = Slate400
                    ),
                    border = SuggestionChipDefaults.suggestionChipBorder(
                        borderColor = CardBorder,
                        enabled = true
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

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
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Emerald500)
                        ) {
                            Text(
                                text = msg.text,
                                color = Color.Black,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                            )
                        }
                    }
                } else {
                    // AI Response Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Slate850),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            msg.aiResponse?.let { resp ->
                                Text(
                                    text = resp.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Emerald500
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                            }

                            Text(
                                text = msg.text,
                                color = Color.White,
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            )

                            // Key Metrics Row
                            msg.aiResponse?.keyMetrics?.takeIf { it.isNotEmpty() }?.let { metrics ->
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    metrics.forEach { metric ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .background(Slate800, RoundedCornerShape(10.dp))
                                                .padding(8.dp)
                                        ) {
                                            Column {
                                                Text(metric.label, fontSize = 10.sp, color = Slate400)
                                                Text(metric.value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            }
                                        }
                                    }
                                }
                            }

                            // Breakdown Items
                            msg.aiResponse?.breakdownItems?.takeIf { it.isNotEmpty() }?.let { items ->
                                Spacer(modifier = Modifier.height(12.dp))
                                items.forEach { item ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 3.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(item.title, fontSize = 13.sp, color = Color.White)
                                            Text(item.subtitle, fontSize = 11.sp, color = Slate400)
                                        }
                                        Text(item.amount, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }

                            // Actionable Tip
                            msg.aiResponse?.actionableTip?.let { tip ->
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Slate800, RoundedCornerShape(10.dp))
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Lightbulb, contentDescription = null, tint = Emerald500, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(tip, fontSize = 12.sp, color = Slate400)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Bottom Input Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ask about expenses, budgets, savings...", color = Slate400, fontSize = 13.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Emerald500,
                    unfocusedBorderColor = Slate800,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Slate850,
                    unfocusedContainerColor = Slate850
                ),
                shape = RoundedCornerShape(16.dp),
                maxLines = 2
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = { sendQuery(inputText) },
                modifier = Modifier
                    .size(48.dp)
                    .background(Emerald500, CircleShape)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.Black, modifier = Modifier.size(20.dp))
            }
        }

        Spacer(modifier = Modifier.height(55.dp))
    }
}
