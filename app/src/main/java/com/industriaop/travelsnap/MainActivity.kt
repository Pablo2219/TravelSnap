package com.industriaop.travelsnap

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.industriaop.travelsnap.domain.model.Place
import com.industriaop.travelsnap.domain.model.PlaceCategory
import com.industriaop.travelsnap.domain.model.PlaceFilter
import com.industriaop.travelsnap.domain.model.Province
import com.industriaop.travelsnap.domain.model.SortOrder
import com.industriaop.travelsnap.ui.adapter.PlaceAdapter
import com.industriaop.travelsnap.ui.common.applySystemBarsPadding
import com.industriaop.travelsnap.ui.common.BottomNav
import com.industriaop.travelsnap.ui.detail.PlaceDetailActivity
import com.industriaop.travelsnap.ui.form.PlaceFormActivity
import com.industriaop.travelsnap.util.labelRes
import com.industriaop.travelsnap.util.showConfirmation
import com.industriaop.travelsnap.viewmodel.PlaceViewModel

/** Main places screen: search, filters and complete CRUD access. */
class MainActivity : AppCompatActivity() {

    private lateinit var acProvince: MaterialAutoCompleteTextView
    private lateinit var acSort: MaterialAutoCompleteTextView
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var btnClearFilters: MaterialButton
    private lateinit var chipFavorites: Chip
    private lateinit var chipGroupCategory: ChipGroup
    private lateinit var emptyState: LinearLayout
    private lateinit var etSearch: TextInputEditText
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var rvPlaces: RecyclerView
    private lateinit var tvEmptyMessage: TextView
    private lateinit var tvEmptyTitle: TextView
    private lateinit var adapter: PlaceAdapter
    private val viewModel: PlaceViewModel by viewModels()

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
        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.root).applySystemBarsPadding()

        acProvince = findViewById(R.id.acProvince)
        acSort = findViewById(R.id.acSort)
        bottomNav = findViewById(R.id.bottomNav)
        btnClearFilters = findViewById(R.id.btnClearFilters)
        chipFavorites = findViewById(R.id.chipFavorites)
        chipGroupCategory = findViewById(R.id.chipGroupCategory)
        emptyState = findViewById(R.id.emptyState)
        etSearch = findViewById(R.id.etSearch)
        fabAdd = findViewById(R.id.fabAdd)
        rvPlaces = findViewById(R.id.rvPlaces)
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage)
        tvEmptyTitle = findViewById(R.id.tvEmptyTitle)

        if (savedInstanceState != null) {
            searchText = savedInstanceState.getString("query").orEmpty()
            selectedCategory = savedInstanceState.getString("category")?.let { PlaceCategory.valueOf(it) }
            selectedProvince = savedInstanceState.getString("province")?.let { Province.valueOf(it) }
            favoritesOnly = savedInstanceState.getBoolean("favorites")
            selectedSort = SortOrder.valueOf(savedInstanceState.getString("sort", SortOrder.NAME_ASC.name))
        }
        setupList()
        setupFilters()
        BottomNav.setup(this, bottomNav, R.id.nav_places)
        fabAdd.setOnClickListener {
            startActivity(PlaceFormActivity.newIntent(this))
        }
        refreshPlaces()
    }

    override fun onResume() {
        super.onResume()
        if (::acProvince.isInitialized) refreshPlaces()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("query", searchText)
        outState.putString("category", selectedCategory?.name)
        outState.putString("province", selectedProvince?.name)
        outState.putBoolean("favorites", favoritesOnly)
        outState.putString("sort", selectedSort.name)
    }

    private fun setupList() {
        adapter = PlaceAdapter(
            onClick = { startActivity(PlaceDetailActivity.newIntent(this, it.id)) },
            onFavoriteClick = {
                viewModel.toggleFavorite(it.id)
                refreshPlaces()
            },
            onEditClick = { startActivity(PlaceFormActivity.newIntent(this, it.id)) },
            onDeleteClick = ::confirmDelete
        )
        rvPlaces.adapter = adapter
    }

    private fun setupFilters() {
        etSearch.doOnTextChanged { text, _, _, _ ->
            if (!isRendering) {
                searchText = text?.toString().orEmpty()
                refreshPlaces()
            }
        }
        chipFavorites.setOnCheckedChangeListener { _, checked ->
            if (!isRendering) {
                favoritesOnly = checked
                refreshPlaces()
            }
        }

        val inflater = LayoutInflater.from(this)
        categoryOptions.forEach { category ->
            val chip = inflater.inflate(
                R.layout.item_filter_chip,
                chipGroupCategory,
                false
            ) as Chip
            chip.id = View.generateViewId()
            chip.text = getString(category?.labelRes() ?: R.string.filter_all)
            chip.setOnClickListener {
                selectedCategory = category
                refreshPlaces()
            }
            chipGroupCategory.addView(chip)
            categoryChips[category] = chip
        }

        acProvince.setAdapter(
            ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                provinceOptions.map { getString(it?.labelRes() ?: R.string.filter_all) }
            )
        )
        acProvince.setOnItemClickListener { _, _, position, _ ->
            selectedProvince = provinceOptions[position]
            refreshPlaces()
        }

        acSort.setAdapter(
            ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                sortOptions.map { getString(it.labelRes()) }
            )
        )
        acSort.setOnItemClickListener { _, _, position, _ ->
            selectedSort = sortOptions[position]
            refreshPlaces()
        }

        btnClearFilters.setOnClickListener {
            searchText = ""
            selectedCategory = null
            selectedProvince = null
            favoritesOnly = false
            selectedSort = SortOrder.NAME_ASC
            etSearch.setText("")
            refreshPlaces()
        }
    }

    private fun refreshPlaces() {
        if (!::acProvince.isInitialized) return
        val filter = PlaceFilter(
            query = searchText,
            category = selectedCategory,
            province = selectedProvince,
            onlyFavorites = favoritesOnly,
            sortOrder = selectedSort
        )
        val places = viewModel.getPlaces(filter)
        adapter.updatePlaces(places)

        isRendering = true
        categoryChips.forEach { (category, chip) ->
            chip.isChecked = category == selectedCategory
        }
        chipFavorites.isChecked = favoritesOnly
        acProvince.setText(
            getString(selectedProvince?.labelRes() ?: R.string.filter_all),
            false
        )
        acSort.setText(getString(selectedSort.labelRes()), false)

        val filtersActive = filter.isActive
        btnClearFilters.isVisible = filtersActive
        emptyState.isVisible = places.isEmpty()
        rvPlaces.isVisible = places.isNotEmpty()
        tvEmptyTitle.isVisible = !filtersActive
        tvEmptyMessage.setText(
            if (filtersActive) R.string.no_results else R.string.empty_places_message
        )
        isRendering = false
    }

    private fun confirmDelete(place: Place) {
        showConfirmation(
            this, R.string.dialog_delete_title,
            getString(R.string.dialog_delete_message, place.name), R.string.button_delete
        ) {
            viewModel.deletePlace(place.id)
            refreshPlaces()
            Toast.makeText(this, R.string.msg_place_deleted, Toast.LENGTH_SHORT).show()
        }
    }
}
