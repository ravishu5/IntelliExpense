package com.intelliexpense.app.capture

import com.intelliexpense.app.core.model.CaptureSource
import com.intelliexpense.app.core.model.TransactionType
import com.intelliexpense.app.intelligence.CategoryTaxonomy
import com.intelliexpense.app.intelligence.IndianMerchantRegistry
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Locale

object StatementImportEngine {

    private val supportedDateFormats = listOf(
        SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH),
        SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH),
        SimpleDateFormat("dd/MM/yy", Locale.ENGLISH),
        SimpleDateFormat("dd-MM-yy", Locale.ENGLISH),
        SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    )

    fun parseCsv(inputStream: InputStream): List<ParsedCaptureResult> {
        val reader = BufferedReader(InputStreamReader(inputStream))
        val results = mutableListOf<ParsedCaptureResult>()

        var dateIdx = -1
        var narrationIdx = -1
        var refIdx = -1
        var debitIdx = -1
        var creditIdx = -1

        var line = reader.readLine()
        var headerFound = false

        while (line != null) {
            val tokens = parseCsvLine(line)
            if (!headerFound) {
                // Look for header row
                for (i in tokens.indices) {
                    val col = tokens[i].lowercase().trim()
                    when {
                        col.contains("date") && !col.contains("value") -> dateIdx = i
                        col.contains("narration") || col.contains("description") || col.contains("particulars") || col.contains("remarks") -> narrationIdx = i
                        col.contains("ref") || col.contains("chq") || col.contains("cheque") -> refIdx = i
                        col.contains("debit") || col.contains("withdrawal") -> debitIdx = i
                        col.contains("credit") || col.contains("deposit") -> creditIdx = i
                    }
                }
                if (dateIdx != -1 && narrationIdx != -1 && (debitIdx != -1 || creditIdx != -1)) {
                    headerFound = true
                }
            } else {
                // Parse data row
                if (tokens.size > maxOf(dateIdx, narrationIdx, debitIdx.coerceAtLeast(0), creditIdx.coerceAtLeast(0))) {
                    val dateStr = tokens[dateIdx].trim()
                    val narration = tokens[narrationIdx].trim()
                    val debitStr = if (debitIdx >= 0 && debitIdx < tokens.size) tokens[debitIdx].trim() else ""
                    val creditStr = if (creditIdx >= 0 && creditIdx < tokens.size) tokens[creditIdx].trim() else ""
                    val refStr = if (refIdx >= 0 && refIdx < tokens.size) tokens[refIdx].trim() else null

                    val debit = parseAmount(debitStr)
                    val credit = parseAmount(creditStr)

                    if (debit > 0.0 || credit > 0.0) {
                        val isDebit = debit > 0.0
                        val amount = if (isDebit) debit else credit
                        val type = if (isDebit) TransactionType.EXPENSE else TransactionType.INCOME

                        val timestamp = parseDate(dateStr)
                        val normalized = IndianMerchantRegistry.normalize(narration)
                        val categoryId = if (type == TransactionType.INCOME) {
                            "salary_income"
                        } else if (normalized.defaultCategoryId != "shopping") {
                            normalized.defaultCategoryId
                        } else {
                            CategoryTaxonomy.inferCategory(narration, normalized.brandName)
                        }

                        results.add(
                            ParsedCaptureResult(
                                amount = amount,
                                merchantOriginal = narration,
                                merchantNormalized = normalized.brandName,
                                categoryId = categoryId,
                                type = type,
                                upiReference = refStr?.takeIf { it.isNotBlank() },
                                paymentMethod = if (narration.contains("UPI", ignoreCase = true)) "UPI" else "Bank Transfer",
                                timestamp = timestamp,
                                rawText = line,
                                source = CaptureSource.STATEMENT_IMPORT,
                                confidence = 0.95f
                            )
                        )
                    }
                }
            }
            line = reader.readLine()
        }
        return results
    }

    private fun parseCsvLine(line: String): List<String> {
        val tokens = mutableListOf<String>()
        var inQuotes = false
        val sb = StringBuilder()
        for (ch in line) {
            when {
                ch == '\"' -> inQuotes = !inQuotes
                ch == ',' && !inQuotes -> {
                    tokens.add(sb.toString().trim())
                    sb.clear()
                }
                else -> sb.append(ch)
            }
        }
        tokens.add(sb.toString().trim())
        return tokens
    }

    private fun parseAmount(str: String): Double {
        val cleaned = str.replace(",", "").replace("₹", "").replace("Rs.", "").trim()
        return cleaned.toDoubleOrNull() ?: 0.0
    }

    private fun parseDate(str: String): Long {
        for (fmt in supportedDateFormats) {
            try {
                val date = fmt.parse(str)
                if (date != null) return date.time
            } catch (e: Exception) {
                // try next
            }
        }
        return System.currentTimeMillis()
    }
}
