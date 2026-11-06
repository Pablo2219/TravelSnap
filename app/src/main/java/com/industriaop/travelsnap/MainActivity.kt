package com.industriaop.travelsnap

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.industriaop.travelsnap.databinding.ActivityMainBinding
import com.industriaop.travelsnap.domain.model.Place
import com.industriaop.travelsnap.domain.model.PlaceCategory
import com.industriaop.travelsnap.domain.model.PlaceFilter
import com.industriaop.travelsnap.domain.model.Province
import com.industriaop.travelsnap.domain.model.SortOrder
import com.industriaop.travelsnap.ui.adapter.PlaceAdapter
import com.industriaop.travelsnap.ui.common.BottomNav
import com.industriaop.travelsnap.ui.common.applySystemBarsPadding
import com.industriaop.travelsnap.ui.detail.PlaceDetailActivity
import com.industriaop.travelsnap.ui.form.PlaceFormActivity
import com.industriaop.travelsnap.util.labelRes

/** Main places screen: search, filters and complete CRUD access. */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: PlaceAdapter
    private val repository get() = (application as TravelSnapApp).repository

    private val provinceOptions: List<Province?> = listOf(null) + Province.entries
    private val categoryOptions: List<PlaceCategory?> = listOf(null) + PlaceCategory.entries
    private val sortOptions: List<SortOrder> = SortOrder.entries
    private val categoryChips = mutableMapOf<PlaceCategory?, Chip>()

    private var searchText = ""
    private var selectedCategory: PlaceCategory? = null
    private var selectedProvince: Province? = null
    private var favoritesOnly = false
    private var selectedSort = SortOrder.NAME_ASC
    private var isRendering = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarsPadding()

        setupList()
        setupFilters()
        BottomNav.setup(this, binding.bottomNav, R.id.nav_places)
        binding.fabAdd.setOnClickListener {
            startActivity(PlaceFormActivity.newIntent(this))
        }
        refreshPlaces()
    }

    override fun onResume() {
        super.onResume()
        if (::binding.isInitialized) refreshPlaces()
    }

    private fun setupList() {
        adapter = PlaceAdapter(
            onClick = { startActivity(PlaceDetailActivity.newIntent(this, it.id)) },
            onFavoriteClick = {
                repository.toggleFavorite(it.id)
                refreshPlaces()
            },
            onEditClick = { startActivity(PlaceFormActivity.newIntent(this, it.id)) },
            onDeleteClick = ::confirmDelete
        )
        binding.rvPlaces.adapter = adapter
    }

    private fun setupFilters() {
        binding.etSearch.doOnTextChanged { text, _, _, _ ->
            if (!isRendering) {
                searchText = text?.toString().orEmpty()
                refreshPlaces()
            }
        }
        binding.chipFavorites.setOnCheckedChangeListener { _, checked ->
            if (!isRendering) {
                favoritesOnly = checked
                refreshPlaces()
            }
        }

        val inflater = LayoutInflater.from(this)
        categoryOptions.forEach { category ->
            val chip = inflater.inflate(
                R.layout.item_filter_chip,
                binding.chipGroupCategory,
                false
            ) as Chip
            chip.id = View.generateViewId()
            chip.text = getString(category?.labelRes() ?: R.string.filter_all)
            chip.setOnClickListener {
                selectedCategory = category
                refreshPlaces()
            }
            binding.chipGroupCategory.addView(chip)
            categoryChips[category] = chip
        }

        binding.acProvince.setAdapter(
            ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                provinceOptions.map { getString(it?.labelRes() ?: R.string.filter_all) }
            )
        )
        binding.acProvince.setOnItemClickListener { _, _, position, _ ->
            selectedProvince = provinceOptions[position]
            refreshPlaces()
        }

        binding.acSort.setAdapter(
            ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                sortOptions.map { getString(it.labelRes()) }
            )
        )
        binding.acSort.setOnItemClickListener { _, _, position, _ ->
            selectedSort = sortOptions[position]
            refreshPlaces()
        }

        binding.btnClearFilters.setOnClickListener {
            searchText = ""
            selectedCategory = null
            selectedProvince = null
            favoritesOnly = false
            selectedSort = SortOrder.NAME_ASC
            binding.etSearch.setText("")
            refreshPlaces()
        }
    }

    private fun refreshPlaces() {
        if (!::binding.isInitialized) return
        val filter = PlaceFilter(
            query = searchText,
            category = selectedCategory,
            province = selectedProvince,
            onlyFavorites = favoritesOnly,
            sortOrder = selectedSort
        )
        val places = repository.getPlaces(filter)
        adapter.submitList(places)

        isRendering = true
        categoryChips.forEach { (category, chip) ->
            chip.isChecked = category == selectedCategory
        }
        binding.chipFavorites.isChecked = favoritesOnly
        binding.acProvince.setText(
            getString(selectedProvince?.labelRes() ?: R.string.filter_all),
            false
        )
        binding.acSort.setText(getString(selectedSort.labelRes()), false)

        val filtersActive = searchText.isNotBlank() ||
            selectedCategory != null || selectedProvince != null || favoritesOnly
        binding.btnClearFilters.isVisible = filtersActive
        binding.emptyState.isVisible = places.isEmpty()
        binding.rvPlaces.isVisible = places.isNotEmpty()
        binding.tvEmptyTitle.isVisible = !filtersActive
        binding.tvEmptyMessage.setText(
            if (filtersActive) R.string.no_results else R.string.empty_places_message
        )
        isRendering = false
    }

    private fun confirmDelete(place: Place) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_delete_title)
            .setMessage(getString(R.string.dialog_delete_message, place.name))
            .setNegativeButton(R.string.button_cancel, null)
            .setPositiveButton(R.string.dialog_confirm) { _, _ ->
                repository.deletePlace(place.id)
                refreshPlaces()
                Toast.makeText(this, R.string.msg_place_deleted, Toast.LENGTH_SHORT).show()
            }
            .show()
    }
}
