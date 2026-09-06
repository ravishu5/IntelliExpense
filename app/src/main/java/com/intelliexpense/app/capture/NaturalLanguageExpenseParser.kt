package com.intelliexpense.app.capture

import com.intelliexpense.app.core.model.CaptureSource
import com.intelliexpense.app.core.model.TransactionType
import com.intelliexpense.app.core.util.DateUtils
import com.intelliexpense.app.intelligence.CategoryTaxonomy
import com.intelliexpense.app.intelligence.IndianMerchantRegistry
import java.util.Calendar
import java.util.Locale
import java.util.regex.Pattern

object NaturalLanguageExpenseParser {

    private val amountRegex = Pattern.compile("(?:₹|Rs\\.?|INR)?\\s*\\b([0-9]+(?:\\.[0-9]{1,2})?)\\b", Pattern.CASE_INSENSITIVE)

    fun parse(input: String?): ParsedCaptureResult? {
        if (input.isNullOrBlank()) return null
        val raw = input.trim()
        val lower = raw.lowercase(Locale.ENGLISH)

        // 1. Amount Extraction
        var amount = 0.0
        val matcher = amountRegex.matcher(raw)
        var matchedAmountStr = ""
        while (matcher.find()) {
            val candidate = matcher.group(1)
            val parsed = candidate?.toDoubleOrNull() ?: 0.0
            if (parsed > 0.0) {
                amount = parsed
                matchedAmountStr = matcher.group(0) ?: ""
                break
            }
        }

        if (amount <= 0.0) return null

        // Remove the amount from text to process merchant and intent
        val textWithoutAmount = raw.replace(matchedAmountStr, " ").replace(Regex("\\s+"), " ").trim()
        val lowerWithoutAmount = textWithoutAmount.lowercase(Locale.ENGLISH)

        // 2. Date offset extraction
        val timestamp = when {
            lowerWithoutAmount.contains("day before yesterday") -> DateUtils.getDaysAgo(2)
            lowerWithoutAmount.contains("yesterday") -> DateUtils.getDaysAgo(1)
            else -> System.currentTimeMillis()
        }

        // 3. Payment Method detection
        val paymentMethod = when {
            lowerWithoutAmount.contains("cash") -> "Cash"
            lowerWithoutAmount.contains("credit card") || lowerWithoutAmount.contains("cc") -> "Credit Card"
            lowerWithoutAmount.contains("debit card") || lowerWithoutAmount.contains("dc") -> "Debit Card"
            lowerWithoutAmount.contains("gpay") || lowerWithoutAmount.contains("phonepe") ||
                    lowerWithoutAmount.contains("paytm") || lowerWithoutAmount.contains("upi") -> "UPI"
            else -> "UPI"
        }

        // 4. Transaction Type
        val isIncome = lowerWithoutAmount.contains("salary") ||
                lowerWithoutAmount.contains("received") ||
                lowerWithoutAmount.contains("earned") ||
                lowerWithoutAmount.contains("got") ||
                lowerWithoutAmount.contains("cashback")

        val type = if (isIncome) {
            if (lowerWithoutAmount.contains("cashback") || lowerWithoutAmount.contains("refund")) {
                TransactionType.REFUND
            } else {
                TransactionType.INCOME
            }
        } else if (lowerWithoutAmount.contains("transfer") || lowerWithoutAmount.contains("sent to")) {
            TransactionType.TRANSFER
        } else {
            TransactionType.EXPENSE
        }

        // 5. Merchant & Category resolution
        val cleanedDesc = textWithoutAmount
            .replace(Regex("\\b(paid|spent|bought|for|at|via|to|on|yesterday|today|cash|credit card|upi)\\b", RegexOption.IGNORE_CASE), "")
            .trim()

        val normalized = IndianMerchantRegistry.normalize(cleanedDesc)
        val categoryId = if (type == TransactionType.INCOME) {
            "salary_income"
        } else if (normalized.defaultCategoryId != "shopping") {
            normalized.defaultCategoryId
        } else {
            CategoryTaxonomy.inferCategory(raw, normalized.brandName)
        }

        return ParsedCaptureResult(
            amount = amount,
            merchantOriginal = cleanedDesc.ifBlank { normalized.brandName },
            merchantNormalized = normalized.brandName,
            categoryId = categoryId,
            type = type,
            upiReference = null,
            paymentMethod = paymentMethod,
            timestamp = timestamp,
            rawText = raw,
            source = CaptureSource.MANUAL,
            confidence = 0.95f
        )
    }
}
