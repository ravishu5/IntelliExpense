package com.intelliexpense.app.capture

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.intelliexpense.app.IntelliExpenseApp
import com.intelliexpense.app.core.util.IndianCurrencyFormatter
import com.intelliexpense.app.data.model.AccountEntity
import com.intelliexpense.app.data.model.TransactionEntity
import com.intelliexpense.app.ui.theme.Emerald500
import com.intelliexpense.app.ui.theme.IntelliExpenseTheme
import com.intelliexpense.app.ui.theme.Slate800
import com.intelliexpense.app.ui.theme.Slate900
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class ShareReceiverActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            IntelliExpenseTheme {
                Surface(
                    color = Color.Black.copy(alpha = 0.6f),
                    modifier = Modifier.padding(16.dp)
                ) {
                    ShareReceiverScreen(
                        intent = intent,
                        onDismiss = { finish() },
                        onSaved = {
                            Toast.makeText(this, "Expense recorded successfully!", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ShareReceiverScreen(
    intent: Intent,
    onDismiss: () -> Unit,
    onSaved: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }
    var parsedResult by remember { mutableStateOf<ParsedCaptureResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var accounts by remember { mutableStateOf<List<AccountEntity>>(emptyList()) }
    var selectedAccountId by remember { mutableStateOf<String?>(null) }

    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as? IntelliExpenseApp
    val repository = app?.repository

    LaunchedEffect(intent) {
        withContext(Dispatchers.IO) {
            accounts = repository?.getAllAccounts().orEmpty()
            selectedAccountId = accounts.firstOrNull()?.id ?: "acc_primary_bank"

            val action = intent.action
            val type = intent.type

            if (Intent.ACTION_SEND == action && type != null) {
                if ("text/plain" == type) {
                    val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
                    val result = UpiReceiptTextParser.parse(sharedText)
                    if (result != null) {
                        parsedResult = result
                    } else {
                        errorMessage = "Could not detect transaction in shared text."
                    }
                } else if (type.startsWith("image/")) {
                    val imageUri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                    if (imageUri != null && app != null) {
                        val ocrText = ReceiptOcrEngine.recognizeFromUri(app, imageUri)
                        val result = ReceiptOcrEngine.parseOcrText(ocrText)
                        if (result != null) {
                            parsedResult = result
                        } else {
                            errorMessage = "Could not extract receipt details from image."
                        }
                    } else {
                        errorMessage = "No image found."
                    }
                }
            }
            isLoading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.95f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Slate900)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Emerald500.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Receipt,
                                contentDescription = null,
                                tint = Emerald500,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Auto Capture",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isLoading) {
                    CircularProgressIndicator(color = Emerald500)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Analyzing receipt...", color = Color.LightGray)
                } else if (parsedResult != null) {
                    val res = parsedResult!!

                    Text(
                        text = IndianCurrencyFormatter.format(res.amount),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Emerald500
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = res.merchantNormalized,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Category: ${res.categoryId.replace('_', ' ').replaceFirstChar { it.uppercase() }}",
                        fontSize = 13.sp,
                        color = Color.LightGray
                    )

                    res.upiReference?.let { utr ->
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "UPI Ref: $utr",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Discard", color = Color.LightGray)
                        }

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    val entity = TransactionEntity(
                                        id = UUID.randomUUID().toString(),
                                        accountId = selectedAccountId ?: "acc_primary_bank",
                                        type = res.type,
                                        amount = res.amount,
                                        currency = "INR",
                                        merchantOriginal = res.merchantOriginal,
                                        merchantNormalized = res.merchantNormalized,
                                        categoryId = res.categoryId,
                                        description = "Captured via Share Sheet",
                                        timestamp = res.timestamp,
                                        paymentMethod = res.paymentMethod,
                                        upiReference = res.upiReference,
                                        rawSourceText = res.rawText,
                                        captureSource = res.source,
                                        confidenceScore = res.confidence,
                                        isReviewed = true
                                    )
                                    repository?.recordTransaction(entity)
                                    onSaved()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save Expense", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Text(
                        text = errorMessage ?: "No transaction detected.",
                        color = Color.Red,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onDismiss) {
                        Text("Close")
                    }
                }
            }
        }
    }
}
