package com.intelliexpense.app

import com.intelliexpense.app.analytics.CreditCardManager
import com.intelliexpense.app.analytics.CreditUtilizationHealth
import com.intelliexpense.app.core.model.AccountType
import com.intelliexpense.app.data.model.AccountEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class CreditCardManagerTest {

    @Test
    fun testOptimalUtilization() {
        val accounts = listOf(
            AccountEntity(
                id = "cc_1",
                name = "HDFC Regalia",
                type = AccountType.CREDIT_CARD,
                balance = -25000.0, // 25,000 used
                creditLimit = 100000.0,
                dueDay = 20
            )
        )

        val statuses = CreditCardManager.analyzeCards(accounts)
        assertEquals(1, statuses.size)

        val status = statuses.first()
        assertEquals(25000.0, status.outstandingBalance, 0.01)
        assertEquals(25.0f, status.utilizationPercentage, 0.1f)
        assertEquals(CreditUtilizationHealth.EXCELLENT, status.health)
        assertEquals(75000.0, status.availableLimit, 0.01)
    }

    @Test
    fun testHighRiskUtilization() {
        val accounts = listOf(
            AccountEntity(
                id = "cc_2",
                name = "SBI SimplyCLICK",
                type = AccountType.CREDIT_CARD,
                balance = -75000.0, // 75,000 used
                creditLimit = 100000.0,
                dueDay = 5
            )
        )

        val statuses = CreditCardManager.analyzeCards(accounts)
        val status = statuses.first()

        assertEquals(75.0f, status.utilizationPercentage, 0.1f)
        assertEquals(CreditUtilizationHealth.HIGH_RISK, status.health)
    }
}
