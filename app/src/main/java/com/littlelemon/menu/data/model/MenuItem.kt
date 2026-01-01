package com.littlelemon.menu.data.model

import androidx.annotation.DrawableRes

/**
 * Core domain model for a menu item.
 * Kept as a pure data class — no Android/UI dependencies.
 */
data class MenuItem(
    val id: Int,
    val name: String,
    val price: Double,
    val category: Category,
    @DrawableRes val imageRes: Int
) {
    enum class Category(val displayName: String) {
        DRINKS("Drinks"),
        FOOD("Food"),
        DESSERT("Dessert")
    }

    /** Formatted price string, e.g. "$8.0" */
    val formattedPrice: String get() = "${"$"}${"%.1f".format(price)}"
}
