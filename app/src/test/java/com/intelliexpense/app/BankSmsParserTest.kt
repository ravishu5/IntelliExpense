package com.intelliexpense.app

import com.intelliexpense.app.capture.BankSmsParser
import com.intelliexpense.app.core.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BankSmsParserTest {

    @Test
    fun testHdfcBankDebitSms() {
        val sms = "HDFC Bank: Rs 450.00 debited from a/c **1234 on 06-09-26 to SWIGGY. UPI Ref 425012345678. Avail bal Rs 24,550.00."
        assertTrue(BankSmsParser.isStrictTransactionSms("AD-HDFCBK", sms))

        val result = BankSmsParser.parse("AD-HDFCBK", sms)
        assertNotNull(result)
        assertEquals(450.0, result!!.amount, 0.01)
        assertEquals("Swiggy", result.merchantNormalized)
        assertEquals("food_dining", result.categoryId)
        assertEquals(TransactionType.EXPENSE, result.type)
        assertEquals("1234", result.accountLast4)
        assertEquals("425012345678", result.upiReference)
        assertEquals(24550.0, result.balance ?: 0.0, 0.01)
    }

    @Test
    fun testSbiDebitSms() {
        val sms = "Dear SBI User, your A/c ending 4321 debited by Rs 1,200.00 on 06Sep26 by transfer to VPA blinkit@icici (UPI Ref no 425098765432). Avail Bal: Rs 15,300.00"
        assertTrue(BankSmsParser.isStrictTransactionSms("VK-SBINB", sms))

        val result = BankSmsParser.parse("VK-SBINB", sms)
        assertNotNull(result)
        assertEquals(1200.0, result!!.amount, 0.01)
        assertEquals("Blinkit", result.merchantNormalized)
        assertEquals("groceries", result.categoryId)
        assertEquals("4321", result.accountLast4)
        assertEquals("425098765432", result.upiReference)
    }

    @Test
    fun testIciciCreditCardSms() {
        val sms = "Dear Customer, ICICI Bank Credit Card XX4567 has been used for INR 2,499.00 at AMAZON INDIA on 05-Sep-26."
        assertTrue(BankSmsParser.isStrictTransactionSms("VM-ICICIB", sms))

        val result = BankSmsParser.parse("VM-ICICIB", sms)
        assertNotNull(result)
        assertEquals(2499.0, result!!.amount, 0.01)
        assertEquals("Amazon", result.merchantNormalized)
        assertEquals("shopping", result.categoryId)
        assertEquals("4567", result.accountLast4)
        assertEquals("Credit Card", result.paymentMethod)
    }

    @Test
    fun testSalaryCreditSms() {
        val sms = "Your A/c *9988 is credited with INR 75,000.00 on 01-Sep-26 by Salary from INFOSYS LTD. Avail Bal: INR 92,000.00."
        assertTrue(BankSmsParser.isStrictTransactionSms("HDFCBK", sms))

        val result = BankSmsParser.parse("HDFCBK", sms)
        assertNotNull(result)
        assertEquals(75000.0, result!!.amount, 0.01)
        assertEquals(TransactionType.INCOME, result.type)
        assertEquals("salary_income", result.categoryId)
    }

    @Test
    fun testRejectsOtpAndSpamMessages() {
        val otpSms = "Your OTP for login to netbanking is 849201. Do not share this secret code with anyone."
        assertFalse(BankSmsParser.isStrictTransactionSms("HDFCBK", otpSms))
        assertNull(BankSmsParser.parse("HDFCBK", otpSms))

        val spamSms = "Congratulations! You are pre-approved for an instant loan of Rs 5,00,000. Apply now."
        assertFalse(BankSmsParser.isStrictTransactionSms("AXISBK", spamSms))
        assertNull(BankSmsParser.parse("AXISBK", spamSms))
    }
}
