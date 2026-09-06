package com.intelliexpense.app.intelligence

import com.intelliexpense.app.core.model.CategoryType

data class CategoryTaxonomyItem(
    val id: String,
    val name: String,
    val type: CategoryType,
    val icon: String,
    val colorHex: String,
    val subcategories: List<String>,
    val defaultKeywords: List<String>
)

object CategoryTaxonomy {
    val items = listOf(
        CategoryTaxonomyItem(
            id = "food_dining",
            name = "Food & Dining",
            type = CategoryType.EXPENSE,
            icon = "restaurant",
            colorHex = "#EF4444",
            subcategories = listOf("Restaurants", "Food Delivery", "Cafes & Chai", "Fast Food", "Bars & Pubs"),
            defaultKeywords = listOf("food", "dining", "lunch", "dinner", "breakfast", "swiggy", "zomato", "restaurant", "cafe", "coffee", "chai", "pizza", "burger", "biryani", "snack")
        ),
        CategoryTaxonomyItem(
            id = "groceries",
            name = "Groceries & Daily",
            type = CategoryType.EXPENSE,
            icon = "shopping_cart",
            colorHex = "#10B981",
            subcategories = listOf("Quick Commerce", "Supermarket", "Vegetables & Fruits", "Dairy & Milk"),
            defaultKeywords = listOf("grocery", "groceries", "blinkit", "zepto", "instamart", "bigbasket", "dmart", "supermarket", "milk", "vegetables", "fruits", "ration")
        ),
        CategoryTaxonomyItem(
            id = "travel_mobility",
            name = "Travel & Commute",
            type = CategoryType.EXPENSE,
            icon = "directions_car",
            colorHex = "#3B82F6",
            subcategories = listOf("Cabs & Autos", "Flights", "Trains & IRCTC", "Fuel & Petrol", "Tolls & FASTag"),
            defaultKeywords = listOf("uber", "ola", "rapido", "cab", "auto", "petrol", "diesel", "fuel", "fastag", "toll", "irctc", "flight", "metro", "bus", "train", "parking")
        ),
        CategoryTaxonomyItem(
            id = "shopping",
            name = "Shopping & Lifestyle",
            type = CategoryType.EXPENSE,
            icon = "shopping_bag",
            colorHex = "#EC4899",
            subcategories = listOf("Clothing & Fashion", "Electronics", "Home & Kitchen", "Beauty & Care"),
            defaultKeywords = listOf("amazon", "flipkart", "myntra", "ajio", "meesho", "shopping", "clothes", "shoes", "electronics", "gadget", "croma", "mall")
        ),
        CategoryTaxonomyItem(
            id = "bills_utilities",
            name = "Bills & Utilities",
            type = CategoryType.EXPENSE,
            icon = "receipt_long",
            colorHex = "#F59E0B",
            subcategories = listOf("Electricity", "Mobile Recharge", "Broadband / WiFi", "Piped Gas", "Water Bill"),
            defaultKeywords = listOf("electricity", "bill", "airtel", "jio", "vi", "recharge", "broadband", "wifi", "bescom", "tata power", "gas bill", "utility", "dth")
        ),
        CategoryTaxonomyItem(
            id = "entertainment",
            name = "Entertainment & OTT",
            type = CategoryType.EXPENSE,
            icon = "movie",
            colorHex = "#8B5CF6",
            subcategories = listOf("OTT Streaming", "Movies & Cinema", "Music & Podcasts", "Gaming"),
            defaultKeywords = listOf("netflix", "spotify", "prime", "hotstar", "jiocinema", "youtube", "movie", "bookmyshow", "pvr", "cinema", "gaming", "steam")
        ),
        CategoryTaxonomyItem(
            id = "healthcare",
            name = "Health & Medical",
            type = CategoryType.EXPENSE,
            icon = "local_pharmacy",
            colorHex = "#06B6D4",
            subcategories = listOf("Pharmacy & Medicines", "Doctor & Clinic", "Lab Tests", "Fitness & Gym"),
            defaultKeywords = listOf("pharmacy", "medicine", "doctor", "hospital", "clinic", "apollo", "pharmeasy", "1mg", "cult.fit", "gym", "dentist", "medical")
        ),
        CategoryTaxonomyItem(
            id = "investments",
            name = "Investments & Wealth",
            type = CategoryType.EXPENSE,
            icon = "trending_up",
            colorHex = "#6366F1",
            subcategories = listOf("Mutual Funds / SIP", "Stocks & Equity", "Fixed Deposit", "Insurance Premium"),
            defaultKeywords = listOf("sip", "mutual fund", "zerodha", "groww", "investment", "stocks", "share", "lic", "insurance", "nps", "ppf")
        ),
        CategoryTaxonomyItem(
            id = "home_maintenance",
            name = "Home & Rent",
            type = CategoryType.EXPENSE,
            icon = "home",
            colorHex = "#64748B",
            subcategories = listOf("House Rent", "Society Maintenance", "Repairs & Plumber", "Furniture"),
            defaultKeywords = listOf("rent", "maintenance", "society", "plumber", "electrician", "maid", "cook", "carpenter", "repair", "home")
        ),
        CategoryTaxonomyItem(
            id = "salary_income",
            name = "Salary & Income",
            type = CategoryType.INCOME,
            icon = "account_balance_wallet",
            colorHex = "#10B981",
            subcategories = listOf("Salary", "Freelance / Consulting", "Dividends & Interest", "Business"),
            defaultKeywords = listOf("salary", "payroll", "stipend", "bonus", "dividend", "interest credited", "freelance", "consulting", "credited")
        ),
        CategoryTaxonomyItem(
            id = "transfers",
            name = "Transfers & P2P",
            type = CategoryType.EXPENSE,
            icon = "swap_horiz",
            colorHex = "#94A3B8",
            subcategories = listOf("Self Transfer", "Sent to Friend", "Received from Friend"),
            defaultKeywords = listOf("transfer", "sent to", "received from", "p2p", "upi transfer", "self transfer", "split bill")
        )
    )

    fun findCategoryById(id: String): CategoryTaxonomyItem? {
        return items.firstOrNull { it.id == id }
    }

    fun inferCategory(description: String?, merchantNorm: String): String {
        val text = "${description.orEmpty()} $merchantNorm".lowercase()
        for (item in items) {
            for (kw in item.defaultKeywords) {
                if (text.contains(kw)) {
                    return item.id
                }
            }
        }
        return "shopping"
    }
}
