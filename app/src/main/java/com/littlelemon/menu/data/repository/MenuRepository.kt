package com.littlelemon.menu.data.repository

import com.littlelemon.menu.R
import com.littlelemon.menu.data.model.MenuItem
import com.littlelemon.menu.data.model.MenuItem.Category
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * MenuRepository is the single source of truth for menu data.
 *
 * In production this would call a remote API / Room database.
 * Here it returns a hard-coded list wrapped in a Flow so the
 * ViewModel can observe it exactly the same way.
 */
interface MenuRepository {
    fun getMenuItems(): Flow<List<MenuItem>>
}

class MenuRepositoryImpl : MenuRepository {

    /**
     * Emits the full catalogue once.
     * The small delay simulates a real network/db call so the
     * loading state in the ViewModel is observable during tests.
     */
    override fun getMenuItems(): Flow<List<MenuItem>> = flow {
        delay(300) // simulate I/O
        emit(SEED_DATA)
    }

    companion object {
        /**
         * Seed data — one entry per dish shown in the reference design.
         *
         * Image resources must be placed in res/drawable/ with the exact
         * names referenced below (snake_case).  The names map 1-to-1 to
         * the photos visible in the reference screenshot.
         */
        private val SEED_DATA = listOf(
            MenuItem(
                id = 1, name = "Latte", price = 8.0,
                category = Category.DRINKS,
                imageRes = R.drawable.img_latte
            ),
            MenuItem(
                id = 2, name = "Mocha", price = 9.0,
                category = Category.DRINKS,
                imageRes = R.drawable.img_mocha
            ),
            MenuItem(
                id = 3, name = "Boeuf bourguignon", price = 22.0,
                category = Category.FOOD,
                imageRes = R.drawable.img_boeuf_bourguignon
            ),
            MenuItem(
                id = 4, name = "Bouillabaisse", price = 18.0,
                category = Category.FOOD,
                imageRes = R.drawable.img_bouillabaisse
            ),
            MenuItem(
                id = 5, name = "Lasagna", price = 15.0,
                category = Category.FOOD,
                imageRes = R.drawable.img_lasagna
            ),
            MenuItem(
                id = 6, name = "Onion Soup", price = 10.0,
                category = Category.FOOD,
                imageRes = R.drawable.img_onion_soup
            ),
            MenuItem(
                id = 7, name = "Salmon en papillote", price = 20.0,
                category = Category.FOOD,
                imageRes = R.drawable.img_salmon_en_papillote
            ),
            MenuItem(
                id = 8, name = "Quiche Lorraine", price = 12.0,
                category = Category.FOOD,
                imageRes = R.drawable.img_quiche_lorraine
            ),
            MenuItem(
                id = 9, name = "Croissant", price = 5.0,
                category = Category.DESSERT,
                imageRes = R.drawable.img_croissant
            ),
            MenuItem(
                id = 10, name = "Custard Tart", price = 6.5,
                category = Category.DESSERT,
                imageRes = R.drawable.img_custard_tart
            )
        )
    }
}
