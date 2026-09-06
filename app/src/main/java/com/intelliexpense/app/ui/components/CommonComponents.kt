package com.intelliexpense.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.intelliexpense.app.core.model.TransactionType
import com.intelliexpense.app.core.util.DateUtils
import com.intelliexpense.app.core.util.IndianCurrencyFormatter
import com.intelliexpense.app.data.model.TransactionEntity
import com.intelliexpense.app.ui.theme.LocalAppColors

@Composable
fun StatCard(
    title: String,
    amount: Double,
    subtitle: String? = null,
    icon: ImageVector,
    iconTint: Color? = null,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val colors = LocalAppColors.current
    val tint = iconTint ?: colors.primary

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
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
                Text(
                    text = title,
                    fontSize = 13.sp,
                    color = colors.textSecondary,
                    fontWeight = FontWeight.Medium
                )
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(tint.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(17.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = IndianCurrencyFormatter.format(amount, includeDecimals = false),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )

            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = colors.textMuted
                )
            }
        }
    }
}

@Composable
fun TransactionItemRow(
    transaction: TransactionEntity,
    onClick: () -> Unit = {}
) {
    val colors = LocalAppColors.current
    val isExpense = transaction.type == TransactionType.EXPENSE
    val isIncome = transaction.type == TransactionType.INCOME
    val isRefund = transaction.type == TransactionType.REFUND
    val isTransfer = transaction.type == TransactionType.TRANSFER

    val amountColor = when {
        isIncome || isRefund -> colors.incomeGreen
        isTransfer -> colors.secondary
        else -> colors.textPrimary
    }

    val amountPrefix = when {
        isIncome || isRefund -> "+"
        isTransfer -> ""
        else -> "-"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = BorderStroke(1.dp, colors.cardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        if (isIncome || isRefund) colors.incomeGreen.copy(alpha = 0.15f) else colors.surfaceElevated,
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        isIncome || isRefund -> Icons.Default.ArrowDownward
                        isTransfer -> Icons.Default.Receipt
                        else -> Icons.Default.ArrowUpward
                    },
                    contentDescription = null,
                    tint = if (isIncome || isRefund) colors.incomeGreen else colors.textSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = transaction.merchantNormalized,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = colors.textPrimary
                    )
                    if (!transaction.isReviewed) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(colors.accentAmber.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Verify", fontSize = 10.sp, color = colors.accentAmber, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "${transaction.paymentMethod} • ${DateUtils.formatDate(transaction.timestamp)}",
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )
            }

            // Amount
            Text(
                text = "$amountPrefix${IndianCurrencyFormatter.format(transaction.amount)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = amountColor
            )
        }
    }
}

@Composable
fun ReviewAlertCard(
    unreviewedCount: Int,
    onClick: () -> Unit
) {
    val colors = LocalAppColors.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = colors.accentAmber.copy(alpha = 0.10f)),
        border = BorderStroke(1.dp, colors.accentAmber.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(colors.accentAmber.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PriorityHigh, contentDescription = null, tint = colors.accentAmber, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$unreviewedCount Auto-Detected Transactions",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = colors.textPrimary
                )
                Text(
                    text = "Tap to verify merchant and category",
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )
            }
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = colors.accentAmber),
                shape = RoundedCornerShape(10.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("Verify", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun BudgetProgressBar(
    title: String,
    spent: Double,
    limit: Double,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val fraction = if (limit > 0) (spent / limit).toFloat().coerceIn(0f, 1f) else 0f
    val barColor = when {
        spent >= limit -> colors.expenseRed
        spent >= limit * 0.8 -> colors.accentAmber
        else -> colors.incomeGreen
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, fontSize = 13.sp, color = colors.textPrimary, fontWeight = FontWeight.Medium)
            Text(
                "${IndianCurrencyFormatter.format(spent, false)} / ${IndianCurrencyFormatter.format(limit, false)}",
                fontSize = 12.sp,
                color = colors.textSecondary
            )
        }
        Spacer(modifier = Modifier.height(7.dp))
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = barColor,
            trackColor = colors.surfaceElevated
        )
    }
}
