package com.littlelemon.menu.ui.menu

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.littlelemon.menu.data.model.FilterOption
import com.littlelemon.menu.data.model.MenuItem
import com.littlelemon.menu.data.model.MenuItem.Category
import com.littlelemon.menu.data.model.SortOrder
import com.littlelemon.menu.data.repository.MenuRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * MenuViewModelTest
 * ─────────────────
 * Covers every combination of sort × filter × search.
 *
 * Uses:
 *  • InstantTaskExecutorRule  — forces LiveData/arch components to run synchronously
 *  • UnconfinedTestDispatcher — makes coroutines run eagerly without advance()
 *  • MockK                    — mocks the repository so tests have no I/O
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MenuViewModelTest {

    // ── Rules ─────────────────────────────────────────────────────────────────

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    // ── Test fixtures ─────────────────────────────────────────────────────────

    private val testItems = listOf(
        MenuItem(1, "Latte",             8.0,  Category.DRINKS,  0),
        MenuItem(2, "Mocha",             9.0,  Category.DRINKS,  0),
        MenuItem(3, "Boeuf bourguignon", 22.0, Category.FOOD,    0),
        MenuItem(4, "Bouillabaisse",     18.0, Category.FOOD,    0),
        MenuItem(5, "Lasagna",           15.0, Category.FOOD,    0),
        MenuItem(6, "Onion soup",        10.0, Category.FOOD,    0),
        MenuItem(7, "Crème brûlée",      7.0,  Category.DESSERT, 0),
        MenuItem(8, "Tarte Tatin",       8.5,  Category.DESSERT, 0)
    )

    private lateinit var repository: MenuRepository
    private lateinit var viewModel: MenuViewModel

    // ── Setup / Teardown ──────────────────────────────────────────────────────

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        every { repository.getMenuItems() } returns flowOf(testItems)
        viewModel = MenuViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private suspend fun displayed() =
        viewModel.uiState.first { !it.isLoading }.displayedItems

    // ══════════════════════════════════════════════════════════════════════════
    //  1. Initial state
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    fun `initial state loads all items sorted A-Z`() = runTest {
        val state = viewModel.uiState.first { !it.isLoading }

        assertEquals(testItems.size, state.displayedItems.size)
        // Default sort: A-Z
        assertEquals(
            testItems.sortedBy { it.name },
            state.displayedItems
        )
        assertEquals(SortOrder.NAME_ASC, state.activeSortOrder)
        assertEquals(FilterOption.All,   state.activeFilter)
        assertEquals("",                 state.searchQuery)
        assertNull(state.error)
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  2. Sorting
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    fun `sort NAME_ASC returns items in alphabetical order`() = runTest {
        viewModel.onSortSelected(SortOrder.NAME_ASC)
        val items = displayed()
        assertEquals(items, items.sortedBy { it.name })
    }

    @Test
    fun `sort PRICE_ASC returns items cheapest first`() = runTest {
        viewModel.onSortSelected(SortOrder.PRICE_ASC)
        val items = displayed()
        assertEquals(items, items.sortedBy { it.price })
        // Cheapest item must be first
        assertEquals(7.0, items.first().price, 0.001)
    }

    @Test
    fun `sort PRICE_DESC returns items most expensive first`() = runTest {
        viewModel.onSortSelected(SortOrder.PRICE_DESC)
        val items = displayed()
        assertEquals(items, items.sortedByDescending { it.price })
        assertEquals(22.0, items.first().price, 0.001)
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  3. Filtering
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    fun `filter All shows every item`() = runTest {
        viewModel.onFilterSelected(FilterOption.All)
        assertEquals(testItems.size, displayed().size)
    }

    @Test
    fun `filter Drinks shows only DRINKS category`() = runTest {
        viewModel.onFilterSelected(FilterOption.Drinks)
        val items = displayed()
        assertTrue(items.isNotEmpty())
        assertTrue(items.all { it.category == Category.DRINKS })
        assertEquals(2, items.size) // Latte + Mocha
    }

    @Test
    fun `filter Food shows only FOOD category`() = runTest {
        viewModel.onFilterSelected(FilterOption.Food)
        val items = displayed()
        assertTrue(items.all { it.category == Category.FOOD })
        assertEquals(4, items.size) // Boeuf, Bouillabaisse, Lasagna, Onion soup
    }

    @Test
    fun `filter Dessert shows only DESSERT category`() = runTest {
        viewModel.onFilterSelected(FilterOption.Dessert)
        val items = displayed()
        assertTrue(items.all { it.category == Category.DESSERT })
        assertEquals(2, items.size) // Crème brûlée + Tarte Tatin
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  4. Search
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    fun `search by exact name returns that item`() = runTest {
        viewModel.onSearchQueryChanged("Latte")
        val items = displayed()
        assertEquals(1, items.size)
        assertEquals("Latte", items[0].name)
    }

    @Test
    fun `search is case-insensitive`() = runTest {
        viewModel.onSearchQueryChanged("latte")
        assertEquals(1, displayed().size)

        viewModel.onSearchQueryChanged("LATTE")
        assertEquals(1, displayed().size)

        viewModel.onSearchQueryChanged("LaTtE")
        assertEquals(1, displayed().size)
    }

    @Test
    fun `search by partial name returns all matches`() = runTest {
        // "ou" matches "Boeuf bourguignon", "Bouillabaisse", "Onion soup"
        viewModel.onSearchQueryChanged("ou")
        val items = displayed()
        assertTrue(items.size >= 2)
        assertTrue(items.all { it.name.contains("ou", ignoreCase = true) })
    }

    @Test
    fun `search with no match returns empty list`() = runTest {
        viewModel.onSearchQueryChanged("zzznomatch")
        assertTrue(displayed().isEmpty())
    }

    @Test
    fun `clearing search restores full list`() = runTest {
        viewModel.onSearchQueryChanged("Latte")
        assertEquals(1, displayed().size)

        viewModel.onSearchQueryChanged("")
        assertEquals(testItems.size, displayed().size)
    }

    @Test
    fun `search trims leading and trailing whitespace`() = runTest {
        viewModel.onSearchQueryChanged("  Latte  ")
        // ViewModel trims the query before comparing
        assertEquals(1, displayed().size)
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  5. Combined: filter + sort
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    fun `filter Drinks + sort PRICE_DESC shows drinks cheapest-first descending`() = runTest {
        viewModel.onFilterSelected(FilterOption.Drinks)
        viewModel.onSortSelected(SortOrder.PRICE_DESC)

        val items = displayed()
        assertTrue(items.all { it.category == Category.DRINKS })
        assertEquals(items, items.sortedByDescending { it.price })
        // Mocha ($9) before Latte ($8)
        assertEquals("Mocha", items[0].name)
        assertEquals("Latte", items[1].name)
    }

    @Test
    fun `filter Food + sort PRICE_ASC returns food items price ascending`() = runTest {
        viewModel.onFilterSelected(FilterOption.Food)
        viewModel.onSortSelected(SortOrder.PRICE_ASC)

        val items = displayed()
        assertTrue(items.all { it.category == Category.FOOD })
        assertEquals(items, items.sortedBy { it.price })
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  6. Combined: filter + search
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    fun `filter Drinks + search 'latte' returns only Latte`() = runTest {
        viewModel.onFilterSelected(FilterOption.Drinks)
        viewModel.onSearchQueryChanged("latte")

        val items = displayed()
        assertEquals(1, items.size)
        assertEquals("Latte", items[0].name)
        assertEquals(Category.DRINKS, items[0].category)
    }

    @Test
    fun `filter Drinks + search 'lasagna' returns empty (wrong category)`() = runTest {
        viewModel.onFilterSelected(FilterOption.Drinks)
        viewModel.onSearchQueryChanged("lasagna")
        assertTrue(displayed().isEmpty())
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  7. Combined: filter + search + sort
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    fun `food + search 'b' + sort PRICE_DESC returns matching food items price descending`() = runTest {
        viewModel.onFilterSelected(FilterOption.Food)
        viewModel.onSearchQueryChanged("b")
        viewModel.onSortSelected(SortOrder.PRICE_DESC)

        // Matching food items containing "b": Boeuf bourguignon, Bouillabaisse
        val items = displayed()
        assertTrue(items.all { it.category == Category.FOOD })
        assertTrue(items.all { it.name.contains("b", ignoreCase = true) })
        assertEquals(items, items.sortedByDescending { it.price })
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  8. Reset
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    fun `resetAll restores default state`() = runTest {
        viewModel.onFilterSelected(FilterOption.Drinks)
        viewModel.onSortSelected(SortOrder.PRICE_DESC)
        viewModel.onSearchQueryChanged("latte")

        viewModel.resetAll()

        val state = viewModel.uiState.first { it.searchQuery == "" }
        assertEquals("",              state.searchQuery)
        assertEquals(FilterOption.All, state.activeFilter)
        assertEquals(SortOrder.NAME_ASC, state.activeSortOrder)
        assertEquals(testItems.size,  state.displayedItems.size)
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  9. Edge cases
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    fun `repository emitting empty list results in empty displayed items`() = runTest {
        every { repository.getMenuItems() } returns flowOf(emptyList())
        val vm = MenuViewModel(repository)
        val items = vm.uiState.first { !it.isLoading }.displayedItems
        assertTrue(items.isEmpty())
    }

    @Test
    fun `repository throwing exception sets error state`() = runTest {
        every { repository.getMenuItems() } throws RuntimeException("Network error")
        val vm = MenuViewModel(repository)
        val state = vm.uiState.first { !it.isLoading }
        assertNotNull(state.error)
        assertTrue(state.error!!.contains("Network error"))
        assertTrue(state.displayedItems.isEmpty())
    }

    @Test
    fun `formattedPrice formats correctly`() {
        val item = MenuItem(1, "Test", 8.0, Category.DRINKS, 0)
        assertEquals("$8.0", item.formattedPrice)

        val item2 = MenuItem(2, "Test2", 12.5, Category.FOOD, 0)
        assertEquals("$12.5", item2.formattedPrice)
    }

    @Test
    fun `switching sort order multiple times always reflects latest selection`() = runTest {
        viewModel.onSortSelected(SortOrder.PRICE_ASC)
        viewModel.onSortSelected(SortOrder.PRICE_DESC)
        viewModel.onSortSelected(SortOrder.NAME_ASC)

        val state = viewModel.uiState.first { !it.isLoading }
        assertEquals(SortOrder.NAME_ASC, state.activeSortOrder)
        assertEquals(state.displayedItems, state.displayedItems.sortedBy { it.name })
    }
}
