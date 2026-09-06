package com.intelliexpense.app.capture

import com.intelliexpense.app.core.model.CaptureSource
import com.intelliexpense.app.core.model.TransactionType
import com.intelliexpense.app.intelligence.CategoryTaxonomy
import com.intelliexpense.app.intelligence.IndianMerchantRegistry
import java.util.regex.Pattern

data class ParsedCaptureResult(
    val amount: Double,
    val merchantOriginal: String,
    val merchantNormalized: String,
    val categoryId: String,
    val type: TransactionType,
    val upiReference: String?,
    val paymentMethod: String = "UPI",
    val accountLast4: String? = null,
    val balance: Double? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val rawText: String,
    val source: CaptureSource,
    val confidence: Float
)

object UpiReceiptTextParser {

    private val amountPattern = Pattern.compile("(?:₹|Rs\\.?|INR)\\s*([0-9,]+(?:\\.[0-9]{1,2})?)", Pattern.CASE_INSENSITIVE)
    private val altAmountPattern = Pattern.compile("\\b([0-9,]+(?:\\.[0-9]{1,2})?)\\s*(?:₹|Rs\\.?|INR)", Pattern.CASE_INSENSITIVE)
    private val utrPattern = Pattern.compile("\\b(?:UTR|UPI Ref(?:erence)?(?: No)?|Txn(?: ID)?|Reference No|Ref)[\\s:]*([0-9]{12})\\b", Pattern.CASE_INSENSITIVE)
    private val general12DigitPattern = Pattern.compile("\\b([0-9]{12})\\b")

    private val payeePatterns = listOf(
        Pattern.compile("(?:Paid to|Payment to|Sent to|Transfer to|Transferred to)\\s+([^\\n.,]+)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(?:to|at)\\s+([^\\n.,]+?)\\s+(?:was successful|successful|completed|of\\s+(?:₹|Rs|INR))", Pattern.CASE_INSENSITIVE),
        Pattern.compile("([A-Za-z0-9._-]+@[a-zA-Z0-9]+)") // VPA
    )

    fun parse(sharedText: String?): ParsedCaptureResult? {
        if (sharedText.isNullOrBlank()) return null
        val text = sharedText.trim()

        // 1. Amount Extraction
        var amount = 0.0
        val amountMatcher = amountPattern.matcher(text)
        if (amountMatcher.find()) {
            amount = parseNumber(amountMatcher.group(1))
        } else {
            val altMatcher = altAmountPattern.matcher(text)
            if (altMatcher.find()) {
                amount = parseNumber(altMatcher.group(1))
            }
        }

        if (amount <= 0.0) {
            return null // Must have an amount
        }

        // 2. UPI Reference (UTR) Extraction (12 digits)
        var upiRef: String? = null
        val utrMatcher = utrPattern.matcher(text)
        if (utrMatcher.find()) {
            upiRef = utrMatcher.group(1)
        } else {
            val generalMatcher = general12DigitPattern.matcher(text)
            if (generalMatcher.find()) {
                upiRef = generalMatcher.group(1)
            }
        }

        // 3. Merchant / Payee Extraction
        var rawPayee = ""
        for (pattern in payeePatterns) {
            val matcher = pattern.matcher(text)
            if (matcher.find()) {
                rawPayee = matcher.group(1)?.trim().orEmpty()
                if (rawPayee.isNotBlank()) break
            }
        }

        if (rawPayee.isBlank()) {
            // Check first line as payee
            val firstLine = text.lines().firstOrNull()?.take(40).orEmpty()
            rawPayee = firstLine
        }

        val normalized = IndianMerchantRegistry.normalize(rawPayee)
        val categoryId = if (normalized.defaultCategoryId != "shopping") {
            normalized.defaultCategoryId
        } else {
            CategoryTaxonomy.inferCategory(text, normalized.brandName)
        }

        val isCredit = text.contains("received", ignoreCase = true) ||
                text.contains("credited", ignoreCase = true) ||
                text.contains("cashback", ignoreCase = true)

        val txnType = if (isCredit) {
            if (text.contains("cashback", ignoreCase = true) || text.contains("refund", ignoreCase = true)) {
                TransactionType.REFUND
            } else {
                TransactionType.INCOME
            }
        } else {
            TransactionType.EXPENSE
        }

        val confidence = if (upiRef != null && normalized.brandName != "General Expense") 0.98f else 0.85f

        return ParsedCaptureResult(
            amount = amount,
            merchantOriginal = rawPayee.ifBlank { normalized.brandName },
            merchantNormalized = normalized.brandName,
            categoryId = categoryId,
            type = txnType,
            upiReference = upiRef,
            paymentMethod = "UPI",
            timestamp = System.currentTimeMillis(),
            rawText = text,
            source = CaptureSource.SHARE_SHEET,
            confidence = confidence
        )
    }

    private fun parseNumber(str: String?): Double {
        if (str.isNullOrBlank()) return 0.0
        return try {
            str.replace(",", "").toDouble()
        } catch (e: Exception) {
            0.0
        }
    }
}
