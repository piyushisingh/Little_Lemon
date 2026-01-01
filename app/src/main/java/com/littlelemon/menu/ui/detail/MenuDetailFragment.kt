package com.littlelemon.menu.ui.detail

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.littlelemon.menu.R
import com.littlelemon.menu.databinding.FragmentMenuDetailBinding

/**
 * MenuDetailFragment displays full details for a single menu item.
 *
 * All data arrives via Safe Args from [MenuFragment] — no ViewModel
 * or repository call needed here since the data is already loaded.
 *
 * Displayed information (matches reference image):
 *  - Large hero image
 *  - Item name
 *  - Price  ("Price: $8.0")
 *  - Category ("Category: Drinks")
 */
class MenuDetailFragment : Fragment() {

    private var _binding: FragmentMenuDetailBinding? = null
    private val binding get() = _binding!!

    // Safe Args — auto-generated from nav_graph.xml
    private val args: MenuDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMenuDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        bindItemData()
    }

    private fun setupToolbar() {
        binding.toolbar.apply {
            title = args.itemName
            setNavigationIcon(R.drawable.ic_arrow_back)
            setNavigationOnClickListener { findNavController().navigateUp() }
        }
    }

    private fun bindItemData() {
        with(binding) {
            tvItemName.text     = args.itemName
            tvItemPrice.text    = getString(R.string.detail_price, args.itemPrice)
            tvItemCategory.text = getString(R.string.detail_category, args.itemCategory)

            Glide.with(ivItemDetail.context)
                .load(args.itemImageRes)
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .centerCrop()
                .into(ivItemDetail)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
