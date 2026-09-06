package com.intelliexpense.app

import com.intelliexpense.app.capture.UpiReceiptTextParser
import com.intelliexpense.app.core.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class UpiReceiptParserTest {

    @Test
    fun testGooglePayReceiptText() {
        val text = "You paid ₹1,250.00 to Swiggy. UPI transaction ID: 425678901234."
        val result = UpiReceiptTextParser.parse(text)

        assertNotNull(result)
        assertEquals(1250.0, result!!.amount, 0.01)
        assertEquals("Swiggy", result.merchantNormalized)
        assertEquals("425678901234", result.upiReference)
        assertEquals("food_dining", result.categoryId)
        assertEquals(TransactionType.EXPENSE, result.type)
    }

    @Test
    fun testPhonePeReceiptText() {
        val text = "Payment to Zepto of ₹380.00 was successful. Transaction ID: T240906123456789. UTR: 425098765432."
        val result = UpiReceiptTextParser.parse(text)

        assertNotNull(result)
        assertEquals(380.0, result!!.amount, 0.01)
        assertEquals("Zepto", result.merchantNormalized)
        assertEquals("groceries", result.categoryId)
        assertEquals("425098765432", result.upiReference)
    }

    @Test
    fun testPaytmReceiptText() {
        val text = "Paid ₹85.00 to Chai Point on 06 Sep 2026. UPI Ref: 425112233445."
        val result = UpiReceiptTextParser.parse(text)

        assertNotNull(result)
        assertEquals(85.0, result!!.amount, 0.01)
        assertEquals("Chai Point", result.merchantNormalized)
        assertEquals("food_dining", result.categoryId)
        assertEquals("425112233445", result.upiReference)
    }

    @Test
    fun testCredReceiptText() {
        val text = "Payment of ₹14,500 to HDFC Credit Card successful. Ref: 425998877665."
        val result = UpiReceiptTextParser.parse(text)

        assertNotNull(result)
        assertEquals(14500.0, result!!.amount, 0.01)
        assertEquals("425998877665", result.upiReference)
    }

    @Test
    fun testInvalidTextReturnsNull() {
        assertNull(UpiReceiptTextParser.parse(null))
        assertNull(UpiReceiptTextParser.parse("Just a regular chat message with no payment"))
    }
}
