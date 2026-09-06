package com.intelliexpense.app.intelligence

import com.intelliexpense.app.core.model.CaptureSource

data class ScoredTransaction(
    val confidence: Float,
    val needsReview: Boolean,
    val reviewReasons: List<String>
)

object ConfidenceScorer {

    const val CONFIDENCE_THRESHOLD = 0.85f

    fun calculate(
        amount: Double,
        merchantNormalized: String,
        upiReference: String?,
        hasAccountMatch: Boolean,
        source: CaptureSource
    ): ScoredTransaction {
        var score = 0.0f
        val reasons = mutableListOf<String>()

        // Amount validity
        if (amount > 0.0) {
            score += 0.35f
        } else {
            reasons.add("Amount could not be reliably determined")
        }

        // Merchant recognition
        if (merchantNormalized != "General Expense" && merchantNormalized.isNotBlank()) {
            score += 0.25f
        } else {
            score += 0.10f
            reasons.add("Merchant name requires verification")
        }

        // UPI reference / UTR
        if (!upiReference.isNullOrBlank() && upiReference.length >= 12) {
            score += 0.20f
        } else {
            if (source == CaptureSource.SHARE_SHEET || source == CaptureSource.SMS) {
                reasons.add("No 12-digit UPI reference found")
            }
        }

        // Account association
        if (hasAccountMatch) {
            score += 0.10f
        } else {
            reasons.add("Assigned to default account")
        }

        // Source multiplier
        val sourceBonus = when (source) {
            CaptureSource.MANUAL -> 0.10f
            CaptureSource.SHARE_SHEET -> 0.10f
            CaptureSource.SMS -> 0.08f
            CaptureSource.STATEMENT_IMPORT -> 0.08f
            CaptureSource.OCR -> 0.05f
        }
        score += sourceBonus

        val clamped = score.coerceIn(0.0f, 1.0f)
        val needsReview = clamped < CONFIDENCE_THRESHOLD || reasons.isNotEmpty()

        return ScoredTransaction(
            confidence = clamped,
            needsReview = needsReview,
            reviewReasons = reasons
        )
    }
}
