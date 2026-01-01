package com.littlelemon.menu.ui.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.littlelemon.menu.data.model.FilterOption
import com.littlelemon.menu.data.model.MenuItem
import com.littlelemon.menu.data.model.MenuUiState
import com.littlelemon.menu.data.model.SortOrder
import com.littlelemon.menu.data.repository.MenuRepository
import com.littlelemon.menu.data.repository.MenuRepositoryImpl
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * MenuViewModel owns ALL business logic for the menu screen.
 *
 * It combines three independent user-intent streams:
 *   1. searchQuery  – typed characters in the SearchView
 *   2. activeFilter – chosen FilterOption from the popup menu
 *   3. activeSortOrder – chosen SortOrder from the popup menu
 *
 * Whenever any of these change, [computeDisplayedItems] re-runs and
 * the UI updates atomically via a single [MenuUiState] StateFlow.
 */
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class MenuViewModel(
    private val repository: MenuRepository
) : ViewModel() {

    // ── Internal mutable streams ──────────────────────────────────────────────

    private val _searchQuery   = MutableStateFlow("")
    private val _activeFilter  = MutableStateFlow<FilterOption>(FilterOption.All)
    private val _activeSortOrder = MutableStateFlow(SortOrder.NAME_ASC)
    private val _allItems      = MutableStateFlow<List<MenuItem>>(emptyList())
    private val _isLoading     = MutableStateFlow(true)
    private val _error         = MutableStateFlow<String?>(null)

    // ── Public UI state ───────────────────────────────────────────────────────

    /**
     * Single source of truth for the Fragment.
     * Derived from all internal streams; never holds stale data.
     */
    // Combine loading + error into one flow so we stay within the 5-param limit of combine()
    private val _loadingState = combine(_isLoading, _error) { loading, error ->
        Pair(loading, error)
    }

    val uiState: StateFlow<MenuUiState> = combine(
        _allItems,
        _searchQuery.debounce(150),   // debounce so we don't filter on every keystroke
        _activeFilter,
        _activeSortOrder,
        _loadingState
    ) { allItems, query, filter, sort, (loading, error) ->
        MenuUiState(
            allItems        = allItems,
            displayedItems  = computeDisplayedItems(allItems, query, filter, sort),
            searchQuery     = query,
            activeFilter    = filter,
            activeSortOrder = sort,
            isLoading       = loading,
            error           = error
        )
    }.stateIn(
        scope            = viewModelScope,
        started          = SharingStarted.WhileSubscribed(5_000),
        initialValue     = MenuUiState(isLoading = true)
    )

    // ── Init ──────────────────────────────────────────────────────────────────

    init {
        loadMenuItems()
    }

    // ── Public user-intent handlers ───────────────────────────────────────────

    /** Called by SearchView's OnQueryTextListener on every character change. */
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query.trim()
    }

    /** Called when user picks a sort option from the popup menu. */
    fun onSortSelected(order: SortOrder) {
        _activeSortOrder.value = order
    }

    /** Called when user picks a filter option from the popup menu. */
    fun onFilterSelected(filter: FilterOption) {
        _activeFilter.value = filter
    }

    /** Clears search + filter + sort back to defaults. */
    fun resetAll() {
        _searchQuery.value     = ""
        _activeFilter.value    = FilterOption.All
        _activeSortOrder.value = SortOrder.NAME_ASC
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private fun loadMenuItems() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.getMenuItems().collect { items ->
                    _allItems.value = items
                    _isLoading.value = false
                    _error.value = null
                }
            } catch (e: Exception) {
                _isLoading.value = false
                _error.value = e.message ?: "Unknown error loading menu"
            }
        }
    }

    /**
     * Pure function — no side effects, no I/O.
     *
     * Pipeline:
     *   allItems
     *     → filter by category
     *     → filter by search query (case-insensitive, trimmed)
     *     → sort by selected order
     */
    private fun computeDisplayedItems(
        allItems: List<MenuItem>,
        query: String,
        filter: FilterOption,
        sort: SortOrder
    ): List<MenuItem> {
        return allItems
            .filterByCategory(filter)
            .filterByQuery(query)
            .sortedBy(sort)
    }

    private fun List<MenuItem>.filterByCategory(filter: FilterOption): List<MenuItem> =
        when (filter) {
            is FilterOption.All     -> this
            is FilterOption.Drinks  -> filter { it.category == MenuItem.Category.DRINKS }
            is FilterOption.Food    -> filter { it.category == MenuItem.Category.FOOD }
            is FilterOption.Dessert -> filter { it.category == MenuItem.Category.DESSERT }
            else                    -> this
        }

    private fun List<MenuItem>.filterByQuery(query: String): List<MenuItem> {
        if (query.isBlank()) return this
        val lower = query.lowercase()
        return filter { it.name.lowercase().contains(lower) }
    }

    private fun List<MenuItem>.sortedBy(order: SortOrder): List<MenuItem> =
        when (order) {
            SortOrder.NAME_ASC   -> sortedBy { it.name }
            SortOrder.PRICE_ASC  -> sortedBy { it.price }
            SortOrder.PRICE_DESC -> sortedByDescending { it.price }
        }

    // ── Factory ───────────────────────────────────────────────────────────────

    class Factory(
        private val repository: MenuRepository = MenuRepositoryImpl()
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MenuViewModel::class.java)) {
                return MenuViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}
