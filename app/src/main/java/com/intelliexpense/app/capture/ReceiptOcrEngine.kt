package com.intelliexpense.app.capture

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.intelliexpense.app.core.model.CaptureSource
import com.intelliexpense.app.core.model.TransactionType
import com.intelliexpense.app.intelligence.CategoryTaxonomy
import com.intelliexpense.app.intelligence.IndianMerchantRegistry
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.regex.Pattern
import kotlin.coroutines.resume

object ReceiptOcrEngine {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun recognizeFromUri(context: Context, imageUri: Uri): String? {
        return suspendCancellableCoroutine { continuation ->
            try {
                val inputImage = InputImage.fromFilePath(context, imageUri)
                recognizer.process(inputImage)
                    .addOnSuccessListener { textResult ->
                        continuation.resume(textResult.text)
                    }
                    .addOnFailureListener {
                        continuation.resume(null)
                    }
            } catch (e: Exception) {
                continuation.resume(null)
            }
        }
    }

    suspend fun recognizeFromBitmap(bitmap: Bitmap): String? {
        return suspendCancellableCoroutine { continuation ->
            try {
                val inputImage = InputImage.fromBitmap(bitmap, 0)
                recognizer.process(inputImage)
                    .addOnSuccessListener { textResult ->
                        continuation.resume(textResult.text)
                    }
                    .addOnFailureListener {
                        continuation.resume(null)
                    }
            } catch (e: Exception) {
                continuation.resume(null)
            }
        }
    }

    fun parseOcrText(ocrText: String?): ParsedCaptureResult? {
        if (ocrText.isNullOrBlank()) return null
        val lines = ocrText.lines().map { it.trim() }.filter { it.isNotBlank() }

        // Find amount (e.g. ₹ 450.00, Rs. 1,200)
        var amount = 0.0
        val amountRegex = Pattern.compile("(?:₹|Rs\\.?|INR)\\s*([0-9,]+(?:\\.[0-9]{1,2})?)", Pattern.CASE_INSENSITIVE)
        val standaloneNumberRegex = Pattern.compile("^([0-9,]+(?:\\.[0-9]{1,2})?)$")

        for (line in lines) {
            val matcher = amountRegex.matcher(line)
            if (matcher.find()) {
                val parsed = matcher.group(1)?.replace(",", "")?.toDoubleOrNull() ?: 0.0
                if (parsed > 0.0) {
                    amount = parsed
                    break
                }
            }
        }

        // If not found with symbol, search for large standalone number after "Paid to" or "Amount"
        if (amount <= 0.0) {
            for (i in 0 until lines.size) {
                val line = lines[i].lowercase()
                if (line.contains("amount") || line.contains("paid") || line.contains("total")) {
                    for (j in (i - 1).coerceAtLeast(0)..(i + 2).coerceAtMost(lines.size - 1)) {
                        val numMatcher = standaloneNumberRegex.matcher(lines[j])
                        if (numMatcher.find()) {
                            val parsed = numMatcher.group(1)?.replace(",", "")?.toDoubleOrNull() ?: 0.0
                            if (parsed > 0.0) {
                                amount = parsed
                                break
                            }
                        }
                    }
                    if (amount > 0.0) break
                }
            }
        }

        if (amount <= 0.0) return null

        // UPI Ref (12 digits)
        var upiRef: String? = null
        val utrRegex = Pattern.compile("\\b([0-9]{12})\\b")
        for (line in lines) {
            val utrMatcher = utrRegex.matcher(line)
            if (utrMatcher.find()) {
                upiRef = utrMatcher.group(1)
                break
            }
        }

        // Identify Payee / Merchant
        var merchantRaw = ""
        for (i in 0 until lines.size) {
            val line = lines[i]
            val lower = line.lowercase()
            if (lower.contains("paid to") || lower.contains("to:") || lower.contains("payment to")) {
                val candidate = line.replace(Regex("^(paid to|to:|payment to)\\s*", RegexOption.IGNORE_CASE), "").trim()
                if (candidate.isNotBlank()) {
                    merchantRaw = candidate
                    break
                } else if (i + 1 < lines.size) {
                    merchantRaw = lines[i + 1]
                    break
                }
            }
        }

        if (merchantRaw.isBlank()) {
            merchantRaw = lines.take(3).joinToString(" ")
        }

        val normalized = IndianMerchantRegistry.normalize(merchantRaw)
        val categoryId = if (normalized.defaultCategoryId != "shopping") {
            normalized.defaultCategoryId
        } else {
            CategoryTaxonomy.inferCategory(ocrText, normalized.brandName)
        }

        return ParsedCaptureResult(
            amount = amount,
            merchantOriginal = merchantRaw.ifBlank { normalized.brandName },
            merchantNormalized = normalized.brandName,
            categoryId = categoryId,
            type = TransactionType.EXPENSE,
            upiReference = upiRef,
            paymentMethod = "UPI",
            timestamp = System.currentTimeMillis(),
            rawText = ocrText,
            source = CaptureSource.OCR,
            confidence = 0.88f
        )
    }
}
