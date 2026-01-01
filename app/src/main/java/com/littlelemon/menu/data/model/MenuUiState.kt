package com.littlelemon.menu.data.model

/**
 * Represents all valid sort orders for the menu list.
 */
enum class SortOrder(val label: String) {
    NAME_ASC("Sort: A-Z"),
    PRICE_ASC("Sort: Price ascending"),
    PRICE_DESC("Sort: Price descending")
}

/**
 * Represents all valid filter options for the menu list.
 */
sealed class FilterOption(val label: String) {
    object All     : FilterOption("Filter: All")
    object Drinks  : FilterOption("Filter: Drinks")
    object Food    : FilterOption("Filter: Food")
    object Dessert : FilterOption("Filter: Dessert")

    companion object {
        fun all(): List<FilterOption> = listOf(All, Drinks, Food, Dessert)
    }
}

/**
 * Immutable snapshot of the menu screen's UI state.
 * The ViewModel exposes this via StateFlow; the Fragment only reads it.
 */
data class MenuUiState(
    val allItems: List<MenuItem>       = emptyList(),
    val displayedItems: List<MenuItem> = emptyList(),
    val searchQuery: String            = "",
    val activeSortOrder: SortOrder     = SortOrder.NAME_ASC,
    val activeFilter: FilterOption     = FilterOption.All,
    val isLoading: Boolean             = true,
    val error: String?                 = null
)
