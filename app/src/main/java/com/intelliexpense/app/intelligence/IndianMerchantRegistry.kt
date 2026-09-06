package com.intelliexpense.app.intelligence

import java.util.Locale

data class NormalizedMerchant(
    val brandName: String,
    val defaultCategoryId: String,
    val isKnownSubscription: Boolean = false,
    val confidence: Float = 0.95f
)

object IndianMerchantRegistry {

    private val merchantRules = listOf(
        // Food & Dining
        MerchantRule(listOf("swiggy", "bundl technologies"), "Swiggy", "food_dining"),
        MerchantRule(listOf("zomato", "eternal"), "Zomato", "food_dining"),
        MerchantRule(listOf("mcdonald", "hardcastle", "westlife"), "McDonald's", "food_dining"),
        MerchantRule(listOf("starbucks", "tata starbucks"), "Starbucks", "food_dining"),
        MerchantRule(listOf("domino", "jubilant food"), "Domino's Pizza", "food_dining"),
        MerchantRule(listOf("burger king", "restaurant brands asia"), "Burger King", "food_dining"),
        MerchantRule(listOf("pizza hut", "devyani international", "yum"), "Pizza Hut", "food_dining"),
        MerchantRule(listOf("kfc"), "KFC", "food_dining"),
        MerchantRule(listOf("subway"), "Subway", "food_dining"),
        MerchantRule(listOf("chai point", "mountain trail"), "Chai Point", "food_dining"),
        MerchantRule(listOf("chaayos", "sunshine teahouse"), "Chaayos", "food_dining"),
        MerchantRule(listOf("cafe coffee day", "coffee day", "ccd"), "Cafe Coffee Day", "food_dining"),
        MerchantRule(listOf("haldiram"), "Haldiram's", "food_dining"),
        MerchantRule(listOf("bikanervala"), "Bikanervala", "food_dining"),
        MerchantRule(listOf("faasos", "rebel foods", "behrouz", "ovenstory"), "Rebel Foods (Faasos/Behrouz)", "food_dining"),
        MerchantRule(listOf("wow momo"), "Wow! Momo", "food_dining"),
        MerchantRule(listOf("barbeque nation"), "Barbeque Nation", "food_dining"),

        // Quick Commerce & Grocery
        MerchantRule(listOf("blinkit", "grofers"), "Blinkit", "groceries"),
        MerchantRule(listOf("zepto", "kiranakart"), "Zepto", "groceries"),
        MerchantRule(listOf("instamart"), "Swiggy Instamart", "groceries"),
        MerchantRule(listOf("bigbasket", "innovative retail", "supermarket grocery"), "BigBasket", "groceries"),
        MerchantRule(listOf("bb daily"), "BB Daily", "groceries"),
        MerchantRule(listOf("dmart", "d-mart", "avenue supermarts"), "D-Mart", "groceries"),
        MerchantRule(listOf("spencers", "spencer retail"), "Spencer's", "groceries"),
        MerchantRule(listOf("nature basket"), "Nature's Basket", "groceries"),
        MerchantRule(listOf("more retail"), "More Supermarket", "groceries"),
        MerchantRule(listOf("country delight"), "Country Delight", "groceries", isSubscription = true),
        MerchantRule(listOf("milkbasket"), "Milkbasket", "groceries", isSubscription = true),
        MerchantRule(listOf("dunzo"), "Dunzo", "groceries"),

        // E-Commerce & Retail
        MerchantRule(listOf("amazon", "amzn", "amazon pay", "amazon seller"), "Amazon", "shopping"),
        MerchantRule(listOf("flipkart", "fkrt", "internet pvt ltd"), "Flipkart", "shopping"),
        MerchantRule(listOf("myntra", "myntra designs"), "Myntra", "shopping"),
        MerchantRule(listOf("meesho", "fashnear"), "Meesho", "shopping"),
        MerchantRule(listOf("ajio", "reliance retail"), "Ajio", "shopping"),
        MerchantRule(listOf("nykaa", "fsn e-commerce"), "Nykaa", "shopping"),
        MerchantRule(listOf("tata cliq", "tata uni store", "tata neu"), "Tata CLiQ / Neu", "shopping"),
        MerchantRule(listOf("urbanic"), "Urbanic", "shopping"),
        MerchantRule(listOf("lenskart", "valyoo"), "Lenskart", "shopping"),
        MerchantRule(listOf("croma", "infiniti retail"), "Croma", "shopping"),
        MerchantRule(listOf("reliance digital"), "Reliance Digital", "shopping"),
        MerchantRule(listOf("decathlon"), "Decathlon", "shopping"),
        MerchantRule(listOf("ikea"), "IKEA", "shopping"),
        MerchantRule(listOf("zara", "inditex"), "Zara", "shopping"),
        MerchantRule(listOf("h&m", "hennes"), "H&M", "shopping"),

        // Travel & Mobility
        MerchantRule(listOf("uber", "uber india"), "Uber", "travel_mobility"),
        MerchantRule(listOf("ola", "ani tech", "ola cabs"), "Ola", "travel_mobility"),
        MerchantRule(listOf("rapido", "roppen transportation"), "Rapido", "travel_mobility"),
        MerchantRule(listOf("namma yatri"), "Namma Yatri", "travel_mobility"),
        MerchantRule(listOf("irctc", "indian railway"), "IRCTC", "travel_mobility"),
        MerchantRule(listOf("makemytrip", "mmt"), "MakeMyTrip", "travel_mobility"),
        MerchantRule(listOf("cleartrip"), "Cleartrip", "travel_mobility"),
        MerchantRule(listOf("goibibo", "ibibo"), "Goibibo", "travel_mobility"),
        MerchantRule(listOf("easemytrip"), "EaseMyTrip", "travel_mobility"),
        MerchantRule(listOf("redbus"), "redBus", "travel_mobility"),
        MerchantRule(listOf("indigo", "interglobe aviation"), "IndiGo", "travel_mobility"),
        MerchantRule(listOf("air india"), "Air India", "travel_mobility"),
        MerchantRule(listOf("vistara", "tata sia"), "Vistara", "travel_mobility"),
        MerchantRule(listOf("akasa air"), "Akasa Air", "travel_mobility"),
        MerchantRule(listOf("fastag", "netc fastag", "nhai"), "FASTag", "travel_mobility"),
        MerchantRule(listOf("hpcl", "hindustan petroleum"), "HPCL Fuel", "travel_mobility"),
        MerchantRule(listOf("bpcl", "bharat petroleum"), "BPCL Fuel", "travel_mobility"),
        MerchantRule(listOf("ioc", "indian oil"), "Indian Oil Fuel", "travel_mobility"),
        MerchantRule(listOf("shell fuel", "shell india"), "Shell Fuel", "travel_mobility"),

        // Entertainment & OTT
        MerchantRule(listOf("netflix"), "Netflix", "entertainment", isSubscription = true),
        MerchantRule(listOf("spotify"), "Spotify", "entertainment", isSubscription = true),
        MerchantRule(listOf("hotstar", "disney", "novi digital", "jiohotstar"), "JioHotstar", "entertainment", isSubscription = true),
        MerchantRule(listOf("jiocinema"), "JioCinema", "entertainment", isSubscription = true),
        MerchantRule(listOf("prime video", "amazon digital"), "Amazon Prime", "entertainment", isSubscription = true),
        MerchantRule(listOf("youtube", "google play", "google *youtube"), "YouTube Premium", "entertainment", isSubscription = true),
        MerchantRule(listOf("apple.com/bill", "apple services", "itunes"), "Apple Services", "entertainment", isSubscription = true),
        MerchantRule(listOf("sonyliv", "sony pictures"), "SonyLIV", "entertainment", isSubscription = true),
        MerchantRule(listOf("zee5"), "Zee5", "entertainment", isSubscription = true),
        MerchantRule(listOf("bookmyshow", "bigtree entertainment"), "BookMyShow", "entertainment"),
        MerchantRule(listOf("pvr", "inox"), "PVR INOX", "entertainment"),

        // Utilities, Bills & Telecom
        MerchantRule(listOf("airtel", "bharti airtel", "airtel broadband"), "Airtel", "bills_utilities", isSubscription = true),
        MerchantRule(listOf("jio", "reliance jio", "jiofiber"), "Reliance Jio", "bills_utilities", isSubscription = true),
        MerchantRule(listOf("vodafone", "idea", "vodafone idea", "vi prepaid", "vi postpaid"), "Vi (Vodafone Idea)", "bills_utilities", isSubscription = true),
        MerchantRule(listOf("bescom"), "BESCOM Electricity", "bills_utilities"),
        MerchantRule(listOf("tata power"), "Tata Power", "bills_utilities"),
        MerchantRule(listOf("adani electricity"), "Adani Electricity", "bills_utilities"),
        MerchantRule(listOf("bses rajdhani", "bses yamuna"), "BSES Electricity", "bills_utilities"),
        MerchantRule(listOf("torrent power"), "Torrent Power", "bills_utilities"),
        MerchantRule(listOf("mahanagar gas", "mgl"), "Mahanagar Gas", "bills_utilities"),
        MerchantRule(listOf("indraprastha gas", "igl"), "Indraprastha Gas", "bills_utilities"),
        MerchantRule(listOf("act fibernet", "atria convergence"), "ACT Fibernet", "bills_utilities", isSubscription = true),
        MerchantRule(listOf("hathway"), "Hathway Broadband", "bills_utilities", isSubscription = true),

        // Investments & Wealth
        MerchantRule(listOf("zerodha", "rainmatter"), "Zerodha", "investments"),
        MerchantRule(listOf("groww", "nextbillion"), "Groww", "investments"),
        MerchantRule(listOf("angelone", "angel broking"), "Angel One", "investments"),
        MerchantRule(listOf("upstox", "rksv"), "Upstox", "investments"),
        MerchantRule(listOf("kuvera"), "Kuvera", "investments"),
        MerchantRule(listOf("indmoney", "finzoomers"), "INDmoney", "investments"),
        MerchantRule(listOf("cred", "dreamplug"), "CRED", "bills_utilities"),
        MerchantRule(listOf("navi", "navi technologies"), "Navi", "investments"),
        MerchantRule(listOf("paytm money"), "Paytm Money", "investments"),
        MerchantRule(listOf("sbi mutual fund", "sbi mf"), "SBI Mutual Fund", "investments", isSubscription = true),
        MerchantRule(listOf("hdfc mutual fund", "hdfc mf"), "HDFC Mutual Fund", "investments", isSubscription = true),
        MerchantRule(listOf("icici prudential", "icici pru"), "ICICI Prudential MF", "investments", isSubscription = true),
        MerchantRule(listOf("nippon india", "nam-india"), "Nippon India MF", "investments", isSubscription = true),
        MerchantRule(listOf("lic of india", "lic"), "LIC Insurance", "investments", isSubscription = true),

        // Health & Medical
        MerchantRule(listOf("apollo pharmacy", "apollo hospitals"), "Apollo Pharmacy", "healthcare"),
        MerchantRule(listOf("pharmeasy", "api holdings"), "PharmEasy", "healthcare"),
        MerchantRule(listOf("1mg", "tata 1mg"), "Tata 1mg", "healthcare"),
        MerchantRule(listOf("cult.fit", "curefit"), "Cult.fit", "healthcare", isSubscription = true),
        MerchantRule(listOf("practo"), "Practo", "healthcare"),
        MerchantRule(listOf("netmeds"), "Netmeds", "healthcare"),
        MerchantRule(listOf("medplus"), "MedPlus", "healthcare")
    )

    fun normalize(rawText: String?): NormalizedMerchant {
        if (rawText.isNullOrBlank()) {
            return NormalizedMerchant("General Expense", "shopping", false, 0.4f)
        }

        val cleaned = cleanMerchantString(rawText)
        val lower = cleaned.lowercase(Locale.ENGLISH)

        // 1. Direct Rule match
        for (rule in merchantRules) {
            for (kw in rule.keywords) {
                if (lower.contains(kw)) {
                    return NormalizedMerchant(rule.brandName, rule.category, rule.isSubscription, 0.98f)
                }
            }
        }

        // 2. UPI VPA format check: e.g. "rahul@okhdfcbank" or "merchant@paytm"
        if (cleaned.contains("@")) {
            val vpaParts = cleaned.split("@")
            val username = vpaParts[0].replace(Regex("[^a-zA-Z0-9 ]"), " ").trim()
            val formattedName = capitalizeWords(username)
            return NormalizedMerchant(formattedName, "transfers", false, 0.75f)
        }

        // 3. Fallback to cleaned and title-cased name
        val words = cleaned.split(Regex("\\s+")).filter { it.length > 1 }
        val fallbackName = if (words.isNotEmpty()) {
            words.take(3).joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
        } else {
            cleaned.take(20)
        }

        return NormalizedMerchant(fallbackName, "shopping", false, 0.6f)
    }

    private fun cleanMerchantString(raw: String): String {
        var str = raw.trim()
        // Strip common bank/UPI prefixes like UPI/DR/1234/
        str = str.replace(Regex("^(UPI|POS|NEFT|IMPS|RTGS|ACH|CMS)[/-]", RegexOption.IGNORE_CASE), "")
        str = str.replace(Regex("^(PAYMENT TO|PAID TO|TRANSFER TO|SENT TO)\\s+", RegexOption.IGNORE_CASE), "")
        str = str.replace(Regex("\\b(PVT|LTD|LIMITED|INDIA|CORP|LLP|INC|ENTERPRISES)\\b", RegexOption.IGNORE_CASE), "")
        str = str.replace(Regex("[_#*\\-]+"), " ")
        return str.trim()
    }

    private fun capitalizeWords(text: String): String {
        return text.split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
    }

    private data class MerchantRule(
        val keywords: List<String>,
        val brandName: String,
        val category: String,
        val isSubscription: Boolean = false
    )
}
