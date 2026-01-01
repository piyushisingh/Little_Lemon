package com.littlelemon.menu.ui.menu

import android.os.Bundle
import android.view.*
import android.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.littlelemon.menu.R
import com.littlelemon.menu.data.model.FilterOption
import com.littlelemon.menu.data.model.MenuItem
import com.littlelemon.menu.data.model.SortOrder
import com.littlelemon.menu.databinding.FragmentMenuBinding
import kotlinx.coroutines.launch

/**
 * MenuFragment is the primary screen.
 *
 * Responsibilities:
 *  - Renders a 2-column RecyclerView grid
 *  - Owns the Toolbar / 3-dot overflow menu
 *  - Hosts a SearchView connected to the ViewModel
 *  - Observes [MenuUiState] and drives all UI changes reactively
 *  - Delegates all business logic to [MenuViewModel]
 *  - Navigates to [MenuDetailFragment] on item click
 */
class MenuFragment : Fragment() {

    // ── Binding ───────────────────────────────────────────────────────────────

    private var _binding: FragmentMenuBinding? = null
    private val binding get() = _binding!!

    // ── ViewModel ─────────────────────────────────────────────────────────────

    private val viewModel: MenuViewModel by viewModels {
        MenuViewModel.Factory()
    }

    // ── Adapter ───────────────────────────────────────────────────────────────

    private val menuAdapter = MenuAdapter { item -> navigateToDetail(item) }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupRecyclerView()
        setupSearchView()
        observeUiState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvMenuGrid.adapter = null
        _binding = null
    }

    // ── Setup helpers ─────────────────────────────────────────────────────────

    private fun setupToolbar() {
        binding.toolbar.apply {
            title = getString(R.string.toolbar_title)
            inflateMenu(R.menu.menu_overflow)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_overflow -> {
                        showSortFilterPopup(this)
                        true
                    }
                    else -> false
                }
            }
        }
    }

    private fun setupRecyclerView() {
        binding.rvMenuGrid.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = menuAdapter
        }
    }

    private fun setupSearchView() {
        binding.searchView.apply {
            queryHint = getString(R.string.search_hint)
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    clearFocus()
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    viewModel.onSearchQueryChanged(newText.orEmpty())
                    return true
                }
            })

            setOnCloseListener {
                viewModel.onSearchQueryChanged("")
                false
            }
        }
    }

    // ── State observation ─────────────────────────────────────────────────────

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    renderState(state)
                }
            }
        }
    }

    private fun renderState(state: com.littlelemon.menu.data.model.MenuUiState) {
        binding.progressBar.isVisible = state.isLoading
        binding.tvError.isVisible     = state.error != null
        binding.rvMenuGrid.isVisible  = !state.isLoading && state.error == null

        state.error?.let { binding.tvError.text = it }

        val isEmpty = !state.isLoading && state.displayedItems.isEmpty() && state.error == null
        binding.tvEmptyState.isVisible = isEmpty

        menuAdapter.submitList(state.displayedItems)
    }

    // ── Popup menu ────────────────────────────────────────────────────────────

    private fun showSortFilterPopup(anchor: View) {
        val popup = PopupMenu(requireContext(), anchor)

        // Sort group
        popup.menu.add(SORT_GROUP, ID_SORT_NAME, 0, SortOrder.NAME_ASC.label)
        popup.menu.add(SORT_GROUP, ID_SORT_PRICE_ASC, 1, SortOrder.PRICE_ASC.label)
        popup.menu.add(SORT_GROUP, ID_SORT_PRICE_DESC, 2, SortOrder.PRICE_DESC.label)

        // Filter group
        popup.menu.add(FILTER_GROUP, ID_FILTER_ALL, 3, FilterOption.All.label)
        popup.menu.add(FILTER_GROUP, ID_FILTER_DRINKS, 4, FilterOption.Drinks.label)
        popup.menu.add(FILTER_GROUP, ID_FILTER_FOOD, 5, FilterOption.Food.label)
        popup.menu.add(FILTER_GROUP, ID_FILTER_DESSERT, 6, FilterOption.Dessert.label)

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                ID_SORT_NAME       -> viewModel.onSortSelected(SortOrder.NAME_ASC)
                ID_SORT_PRICE_ASC  -> viewModel.onSortSelected(SortOrder.PRICE_ASC)
                ID_SORT_PRICE_DESC -> viewModel.onSortSelected(SortOrder.PRICE_DESC)
                ID_FILTER_ALL      -> viewModel.onFilterSelected(FilterOption.All)
                ID_FILTER_DRINKS   -> viewModel.onFilterSelected(FilterOption.Drinks)
                ID_FILTER_FOOD     -> viewModel.onFilterSelected(FilterOption.Food)
                ID_FILTER_DESSERT  -> viewModel.onFilterSelected(FilterOption.Dessert)
            }
            true
        }

        popup.show()
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    private fun navigateToDetail(item: MenuItem) {
        val action = MenuFragmentDirections.actionMenuFragmentToMenuDetailFragment(
            itemId       = item.id,
            itemName     = item.name,
            itemPrice    = item.price.toFloat(),
            itemCategory = item.category.displayName,
            itemImageRes = item.imageRes
        )
        findNavController().navigate(action)
    }

    companion object {
        private const val SORT_GROUP   = 1
        private const val FILTER_GROUP = 2

        private const val ID_SORT_NAME       = 100
        private const val ID_SORT_PRICE_ASC  = 101
        private const val ID_SORT_PRICE_DESC = 102
        private const val ID_FILTER_ALL      = 200
        private const val ID_FILTER_DRINKS   = 201
        private const val ID_FILTER_FOOD     = 202
        private const val ID_FILTER_DESSERT  = 203
    }
}
