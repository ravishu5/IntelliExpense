package com.intelliexpense.app.capture

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.intelliexpense.app.IntelliExpenseApp
import com.intelliexpense.app.core.model.AccountType
import com.intelliexpense.app.data.model.TransactionEntity
import com.intelliexpense.app.intelligence.ConfidenceScorer
import com.intelliexpense.app.intelligence.TransactionDeduplicator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class TransactionSmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        val sb = StringBuilder()
        var sender: String? = null
        for (msg in messages) {
            sender = msg.originatingAddress
            sb.append(msg.messageBody)
        }

        val fullBody = sb.toString()
        if (!BankSmsParser.isStrictTransactionSms(sender, fullBody)) {
            return // Ignore non-financial messages
        }

        val parsed = BankSmsParser.parse(sender, fullBody) ?: return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val app = context.applicationContext as? IntelliExpenseApp
                val repository = app?.repository
                val deduplicator = app?.deduplicator

                if (repository != null && deduplicator != null) {
                    // Check duplicate
                    val dupCheck = deduplicator.checkDuplicate(
                        amount = parsed.amount,
                        timestamp = parsed.timestamp,
                        upiReference = parsed.upiReference,
                        merchantNormalized = parsed.merchantNormalized
                    )

                    if (!dupCheck.isDuplicate) {
                        // Find matching account by last4 or default to primary bank
                        val accounts = repository.getAllAccounts()
                        val matchedAccount = parsed.accountLast4?.let { last4 ->
                            accounts.firstOrNull { it.accountNumberLast4 == last4 }
                        } ?: accounts.firstOrNull { it.type == AccountType.BANK } ?: accounts.firstOrNull()

                        val accountId = matchedAccount?.id ?: "acc_primary_bank"

                        val scored = ConfidenceScorer.calculate(
                            amount = parsed.amount,
                            merchantNormalized = parsed.merchantNormalized,
                            upiReference = parsed.upiReference,
                            hasAccountMatch = matchedAccount != null,
                            source = parsed.source
                        )

                        val entity = TransactionEntity(
                            id = UUID.randomUUID().toString(),
                            accountId = accountId,
                            type = parsed.type,
                            amount = parsed.amount,
                            currency = "INR",
                            merchantOriginal = parsed.merchantOriginal,
                            merchantNormalized = parsed.merchantNormalized,
                            categoryId = parsed.categoryId,
                            description = "Detected from SMS: $sender",
                            timestamp = parsed.timestamp,
                            paymentMethod = parsed.paymentMethod,
                            upiReference = parsed.upiReference,
                            rawSourceText = parsed.rawText,
                            captureSource = parsed.source,
                            confidenceScore = scored.confidence,
                            isReviewed = !scored.needsReview
                        )

                        repository.recordTransaction(entity)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
