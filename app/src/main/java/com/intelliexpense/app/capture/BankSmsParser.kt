package com.intelliexpense.app.capture

import com.intelliexpense.app.core.model.CaptureSource
import com.intelliexpense.app.core.model.TransactionType
import com.intelliexpense.app.intelligence.CategoryTaxonomy
import com.intelliexpense.app.intelligence.IndianMerchantRegistry
import java.util.Locale
import java.util.regex.Pattern

object BankSmsParser {

    private val nonFinancialKeywords = listOf(
        "otp", "one time password", "verification code", "secret code", "do not share",
        "never share", "pre-approved", "loan", "apply now", "win cash", "congratulations",
        "kyc", "update your pan", "claim your", "lottery", "cashback offer", "discount"
    )

    private val debitKeywords = listOf(
        "debited", "debited by", "debit", "paid", "spent", "sent", "withdrawn", "used for", "transfer to"
    )

    private val creditKeywords = listOf(
        "credited", "credited with", "credit", "received", "deposited", "refund", "salary"
    )

    private val amountRegex = Pattern.compile(
        "(?:INR|Rs\\.?|₹)\\s*([0-9,]+(?:\\.[0-9]{1,2})?)",
        Pattern.CASE_INSENSITIVE
    )

    private val accountLast4Regex = Pattern.compile(
        "(?:A/c|Acct|Account|Card)(?:\\s+(?:no|ending|linked))?\\s*(?:with\\s*)?[xX*]*([0-9]{4})\\b",
        Pattern.CASE_INSENSITIVE
    )

    private val utrRegex = Pattern.compile(
        "(?:UPI Ref(?: no)?|UTR|Ref no|Txn Id|Reference)[\\s:]*([0-9]{12})\\b",
        Pattern.CASE_INSENSITIVE
    )

    private val balanceRegex = Pattern.compile(
        "(?:Avail(?:able)?\\s*(?:Bal(?:ance)?|Limit)?|Bal(?:ance)?)[\\s:]*(?:INR|Rs\\.?|₹)?\\s*([0-9,]+(?:\\.[0-9]{1,2})?)",
        Pattern.CASE_INSENSITIVE
    )

    private val merchantExtractionPatterns = listOf(
        Pattern.compile("(?:transfer to VPA|to VPA|VPA)\\s+([A-Za-z0-9._-]+@[a-zA-Z0-9]+)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(?:to|at|towards|in favour of)\\s+(?:VPA\\s+)?([^\\n.,()]+?)(?:\\s*\\(|\\s+on\\b|\\s+ref\\b|\\s+avail|\\s+bal|\\s+via\\b|\\.|,|$)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("Info:\\s*(?:UPI[*/])?([^\\n.,()*/]+?)(?:[*/]|\\s+on\\b|\\s+avail|\\.|,|$)", Pattern.CASE_INSENSITIVE)
    )

    fun isStrictTransactionSms(sender: String?, body: String?): Boolean {
        if (body.isNullOrBlank()) return false
        val lowerBody = body.lowercase(Locale.ENGLISH)

        // Drop OTPs and promo spam immediately
        for (spam in nonFinancialKeywords) {
            if (lowerBody.contains(spam)) {
                return false
            }
        }

        // Must contain debit or credit indicators
        val hasDebit = debitKeywords.any { lowerBody.contains(it) }
        val hasCredit = creditKeywords.any { lowerBody.contains(it) }
        if (!hasDebit && !hasCredit) return false

        // Must contain a monetary amount
        val matcher = amountRegex.matcher(body)
        return matcher.find()
    }

    fun parse(sender: String?, body: String?): ParsedCaptureResult? {
        if (!isStrictTransactionSms(sender, body)) return null
        val text = body!!.trim()
        val lower = text.lowercase(Locale.ENGLISH)

        // 1. Amount
        var amount = 0.0
        val amountMatcher = amountRegex.matcher(text)
        if (amountMatcher.find()) {
            amount = parseNumber(amountMatcher.group(1))
        }
        if (amount <= 0.0) return null

        // 2. Transaction Type
        val isDebit = debitKeywords.any { lower.contains(it) }
        val isCredit = creditKeywords.any { lower.contains(it) }

        val type = when {
            isDebit -> TransactionType.EXPENSE
            isCredit && (lower.contains("refund") || lower.contains("cashback")) -> TransactionType.REFUND
            isCredit -> TransactionType.INCOME
            else -> TransactionType.EXPENSE
        }

        // 3. Account / Card Last 4 digits
        var accountLast4: String? = null
        val accMatcher = accountLast4Regex.matcher(text)
        if (accMatcher.find()) {
            accountLast4 = accMatcher.group(1)
        }

        // 4. UPI Reference (12-digit UTR)
        var upiRef: String? = null
        val utrMatcher = utrRegex.matcher(text)
        if (utrMatcher.find()) {
            upiRef = utrMatcher.group(1)
        }

        // 5. Balance
        var balance: Double? = null
        val balMatcher = balanceRegex.matcher(text)
        if (balMatcher.find()) {
            balance = parseNumber(balMatcher.group(1)).takeIf { it > 0.0 }
        }

        // 6. Merchant / Payee
        var rawMerchant = ""
        for (pattern in merchantExtractionPatterns) {
            val matcher = pattern.matcher(text)
            if (matcher.find()) {
                val candidate = matcher.group(1)?.trim().orEmpty()
                if (candidate.isNotBlank() && !candidate.contains("A/c", ignoreCase = true)) {
                    rawMerchant = candidate
                    break
                }
            }
        }

        val normalized = IndianMerchantRegistry.normalize(rawMerchant)
        val categoryId = if (type == TransactionType.INCOME) {
            "salary_income"
        } else if (normalized.defaultCategoryId != "shopping") {
            normalized.defaultCategoryId
        } else {
            CategoryTaxonomy.inferCategory(text, normalized.brandName)
        }

        val paymentMethod = when {
            upiRef != null || lower.contains("upi") || lower.contains("vpa") -> "UPI"
            lower.contains("card") || lower.contains("credit card") -> "Credit Card"
            lower.contains("debit card") -> "Debit Card"
            lower.contains("atm") -> "Cash (ATM)"
            else -> "Bank Transfer"
        }

        val confidence = if (accountLast4 != null && (upiRef != null || normalized.brandName != "General Expense")) {
            0.96f
        } else {
            0.88f
        }

        return ParsedCaptureResult(
            amount = amount,
            merchantOriginal = rawMerchant.ifBlank { normalized.brandName },
            merchantNormalized = normalized.brandName,
            categoryId = categoryId,
            type = type,
            upiReference = upiRef,
            paymentMethod = paymentMethod,
            accountLast4 = accountLast4,
            balance = balance,
            timestamp = System.currentTimeMillis(),
            rawText = text,
            source = CaptureSource.SMS,
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
