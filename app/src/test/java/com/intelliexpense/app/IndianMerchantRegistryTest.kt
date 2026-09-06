package com.intelliexpense.app

import com.intelliexpense.app.intelligence.IndianMerchantRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IndianMerchantRegistryTest {

    @Test
    fun testFoodMerchants() {
        val swiggy = IndianMerchantRegistry.normalize("UPI/BUNDL TECHNOLOGIES/SWIGGY")
        assertEquals("Swiggy", swiggy.brandName)
        assertEquals("food_dining", swiggy.defaultCategoryId)

        val zomato = IndianMerchantRegistry.normalize("POS/ZOMATO LIMITED/MUMBAI")
        assertEquals("Zomato", zomato.brandName)
        assertEquals("food_dining", zomato.defaultCategoryId)

        val starbucks = IndianMerchantRegistry.normalize("TATA STARBUCKS PVT LTD")
        assertEquals("Starbucks", starbucks.brandName)
    }

    @Test
    fun testQuickCommerceAndGrocery() {
        val blinkit = IndianMerchantRegistry.normalize("GROFERS INDIA / BLINKIT")
        assertEquals("Blinkit", blinkit.brandName)
        assertEquals("groceries", blinkit.defaultCategoryId)

        val zepto = IndianMerchantRegistry.normalize("KIRANAKART / ZEPTO")
        assertEquals("Zepto", zepto.brandName)
        assertEquals("groceries", zepto.defaultCategoryId)
    }

    @Test
    fun testTravelAndMobility() {
        val uber = IndianMerchantRegistry.normalize("UBER INDIA SYSTEMS PVT")
        assertEquals("Uber", uber.brandName)
        assertEquals("travel_mobility", uber.defaultCategoryId)

        val irctc = IndianMerchantRegistry.normalize("IRCTC APP PAYMENT")
        assertEquals("IRCTC", irctc.brandName)
        assertEquals("travel_mobility", irctc.defaultCategoryId)
    }

    @Test
    fun testSubscriptionsAndOTT() {
        val netflix = IndianMerchantRegistry.normalize("NETFLIX ENTERTAINMENT SERVICES")
        assertEquals("Netflix", netflix.brandName)
        assertEquals("entertainment", netflix.defaultCategoryId)
        assertTrue(netflix.isKnownSubscription)

        val spotify = IndianMerchantRegistry.normalize("SPOTIFY INDIA")
        assertEquals("Spotify", spotify.brandName)
        assertTrue(spotify.isKnownSubscription)
    }

    @Test
    fun testUpiVpaFormatFallback() {
        val vpa = IndianMerchantRegistry.normalize("rahul.sharma@okhdfcbank")
        assertEquals("Rahul Sharma", vpa.brandName)
        assertEquals("transfers", vpa.defaultCategoryId)
    }
}
