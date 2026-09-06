package com.intelliexpense.app

import com.intelliexpense.app.capture.StatementImportEngine
import com.intelliexpense.app.core.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream

class StatementImportEngineTest {

    @Test
    fun testParseHdfcBankCsvStatement() {
        val csvData = """
            Date,Narration,Chq/Ref No,Value Date,Withdrawal Amt,Deposit Amt,Closing Balance
            01/09/2026,NEFT-INFOSYS-SALARY-SEP,NEFT123456,01/09/2026,,85000.00,105000.00
            02/09/2026,UPI-SWIGGY-123456@icici-HDFC0001234,425123456789,02/09/2026,550.00,,104450.00
            03/09/2026,POS 425987654321 BLINKIT BANGALORE,425987654321,03/09/2026,1250.00,,103200.00
            04/09/2026,ACH-NETFLIX-ENTERTAINMENT,ACH998877,04/09/2026,649.00,,102551.00
        """.trimIndent()

        val stream = ByteArrayInputStream(csvData.toByteArray())
        val parsed = StatementImportEngine.parseCsv(stream)

        assertEquals(4, parsed.size)

        // Salary credit
        val salary = parsed[0]
        assertEquals(85000.0, salary.amount, 0.01)
        assertEquals(TransactionType.INCOME, salary.type)
        assertEquals("salary_income", salary.categoryId)

        // Swiggy debit
        val swiggy = parsed[1]
        assertEquals(550.0, swiggy.amount, 0.01)
        assertEquals("Swiggy", swiggy.merchantNormalized)
        assertEquals("food_dining", swiggy.categoryId)
        assertEquals("425123456789", swiggy.upiReference)
        assertEquals(TransactionType.EXPENSE, swiggy.type)

        // Blinkit debit
        val blinkit = parsed[2]
        assertEquals(1250.0, blinkit.amount, 0.01)
        assertEquals("Blinkit", blinkit.merchantNormalized)
        assertEquals("groceries", blinkit.categoryId)

        // Netflix debit
        val netflix = parsed[3]
        assertEquals(649.0, netflix.amount, 0.01)
        assertEquals("Netflix", netflix.merchantNormalized)
        assertEquals("entertainment", netflix.categoryId)
    }
}
