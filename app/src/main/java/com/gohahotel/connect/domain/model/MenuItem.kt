package com.gohahotel.connect.domain.model

data class MenuItem(
    val id: String = "",
    val name: String = "",
    val nameAmharic: String = "",
    val nameFrench: String = "",
    val description: String = "",
    val descriptionAmharic: String = "",
    val category: MenuCategory = MenuCategory.ETHIOPIAN,
    val price: Double = 0.0,
    val currency: String = "ETB",
    val imageUrl: String = "",
    val isAvailable: Boolean = true,
    val isVegetarian: Boolean = false,
    val isVegan: Boolean = false,
    val isGlutenFree: Boolean = false,
    val isSpicy: Boolean = false,
    val spiceLevel: Int = 0,        // 0–3
    val allergens: List<String> = emptyList(),
    val prepTimeMinutes: Int = 20,
    val customizations: List<String> = emptyList(),
    val isFeatured: Boolean = false,
    val rating: Float = 0f
)

enum class MenuCategory(val displayName: String, val displayNameAmharic: String) {
    ETHIOPIAN("Ethiopian Cuisine", "ኢትዮጵያዊ ምግብ"),
    INTERNATIONAL("International", "ዓለም አቀፍ"),
    BEVERAGES("Beverages", "መጠጦች"),
    DESSERTS("Desserts", "ጣፋጮች"),
    BREAKFAST("Breakfast", "ቁርስ"),
    ROOM_SERVICE("Room Service", "የክፍል አገልግሎት")
}
