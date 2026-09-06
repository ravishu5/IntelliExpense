package com.intelliexpense.app.core.model

enum class AccountType(val displayName: String) {
    BANK("Bank Account"),
    UPI("UPI VPA"),
    CREDIT_CARD("Credit Card"),
    WALLET("Digital Wallet"),
    CASH("Cash in Hand")
}

enum class TransactionType(val displayName: String) {
    EXPENSE("Expense"),
    INCOME("Income"),
    TRANSFER("Transfer"),
    REFUND("Refund")
}

enum class CaptureSource(val displayName: String) {
    SHARE_SHEET("Share Sheet"),
    OCR("Receipt OCR"),
    SMS("Bank SMS"),
    MANUAL("Manual Entry"),
    STATEMENT_IMPORT("Statement Import")
}

enum class SubscriptionFrequency(val displayName: String, val approxDays: Int) {
    WEEKLY("Weekly", 7),
    MONTHLY("Monthly", 30),
    QUARTERLY("Quarterly", 90),
    YEARLY("Yearly", 365)
}

enum class CategoryType {
    EXPENSE,
    INCOME
}
