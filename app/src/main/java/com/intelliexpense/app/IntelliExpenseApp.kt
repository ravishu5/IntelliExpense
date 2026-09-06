package com.intelliexpense.app

import android.app.Application
import com.intelliexpense.app.ai.AskYourMoneyEngine
import com.intelliexpense.app.data.local.IntelliExpenseDatabase
import com.intelliexpense.app.data.repository.FinancialRepository
import com.intelliexpense.app.data.repository.FinancialRepositoryImpl
import com.intelliexpense.app.intelligence.TransactionDeduplicator
import com.intelliexpense.app.privacy.PrivacyCenterManager

class IntelliExpenseApp : Application() {

    lateinit var database: IntelliExpenseDatabase
        private set

    lateinit var repository: FinancialRepository
        private set

    lateinit var deduplicator: TransactionDeduplicator
        private set

    lateinit var aiEngine: AskYourMoneyEngine
        private set

    lateinit var privacyManager: PrivacyCenterManager
        private set

    override fun onCreate() {
        super.onCreate()

        database = IntelliExpenseDatabase.getInstance(this, useEncryption = true)

        repository = FinancialRepositoryImpl(
            accountDao = database.accountDao(),
            transactionDao = database.transactionDao(),
            categoryDao = database.categoryDao(),
            budgetDao = database.budgetDao(),
            subscriptionDao = database.subscriptionDao(),
            savingsGoalDao = database.savingsGoalDao(),
            splitDao = database.splitDao()
        )

        deduplicator = TransactionDeduplicator(database.transactionDao())
        aiEngine = AskYourMoneyEngine()
        privacyManager = PrivacyCenterManager(this, repository)
    }
}
