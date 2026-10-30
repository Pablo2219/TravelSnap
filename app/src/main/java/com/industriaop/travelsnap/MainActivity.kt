package com.industriaop.travelsnap

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.chip.Chip
import com.industriaop.travelsnap.databinding.ActivityMainBinding
import com.industriaop.travelsnap.domain.model.PlaceCategory
import com.industriaop.travelsnap.domain.model.Province
import com.industriaop.travelsnap.domain.model.SortOrder
import com.industriaop.travelsnap.ui.adapter.PlaceAdapter
import com.industriaop.travelsnap.ui.common.BottomNav
import com.industriaop.travelsnap.ui.common.applySystemBarsPadding
import com.industriaop.travelsnap.ui.common.show
import com.industriaop.travelsnap.ui.detail.PlaceDetailActivity
import com.industriaop.travelsnap.ui.dialog.DeleteConfirmDialog
import com.industriaop.travelsnap.ui.form.PlaceFormActivity
import com.industriaop.travelsnap.util.labelRes
import com.industriaop.travelsnap.viewmodel.PlaceListUiState
import com.industriaop.travelsnap.viewmodel.PlaceListViewModel
import com.industriaop.travelsnap.viewmodel.ViewModelFactories
import kotlinx.coroutines.launch

/** "My places" screen (mockup 2): search, filters, list and delete confirmation. */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: PlaceAdapter

    private val viewModel: PlaceListViewModel by viewModels {
        ViewModelFactories.placeList((application as TravelSnapApp).repository)
    }

    /** "All" first, then every province / category. A null item means "no filter". */
    private val provinceOptions: List<Province?> = listOf(null) + Province.entries
    private val categoryOptions: List<PlaceCategory?> = listOf(null) + PlaceCategory.entries
    private val sortOptions: List<SortOrder> = SortOrder.entries

    private val categoryChips = mutableMapOf<PlaceCategory?, Chip>()
    private var deleteDialog: DeleteConfirmDialog? = null

    /** True while the screen is copying the ViewModel state into the views (ignore listeners). */
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
        binding.fabAdd.setOnClickListener { startActivity(PlaceFormActivity.newIntent(this)) }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { render(it) }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // The dialog is created again from the ViewModel state when the screen comes back.
        deleteDialog?.dismiss()
        deleteDialog = null
    }

    // ------------------------------------------------------------------ Setup

    private fun setupList() {
        adapter = PlaceAdapter(
            onClick = { startActivity(PlaceDetailActivity.newIntent(this, it.id)) },
            onFavoriteClick = { viewModel.onToggleFavorite(it.id) },
            onEditClick = { startActivity(PlaceFormActivity.newIntent(this, it.id)) },
            onDeleteClick = { viewModel.onDeleteRequested(it) }
        )
        binding.rvPlaces.adapter = adapter
    }

    private fun setupFilters() {
        binding.etSearch.doOnTextChanged { text, _, _, _ ->
            if (!isRendering) viewModel.onQueryChange(text?.toString().orEmpty())
        }

        binding.chipFavorites.setOnCheckedChangeListener { _, checked ->
            if (!isRendering) viewModel.onFavoritesOnlyChange(checked)
        }

        // Category chips: "All" + one per category.
        val inflater = LayoutInflater.from(this)
        categoryOptions.forEach { category ->
            val chip = inflater.inflate(R.layout.item_filter_chip, binding.chipGroupCategory, false) as Chip
            chip.id = android.view.View.generateViewId()
            chip.text = getString(category?.labelRes() ?: R.string.filter_all)
            chip.setOnClickListener { viewModel.onCategorySelected(category) }
            binding.chipGroupCategory.addView(chip)
            categoryChips[category] = chip
        }

        // Province and sort dropdowns.
        binding.acProvince.setAdapter(
            ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                provinceOptions.map { getString(it?.labelRes() ?: R.string.filter_all) }
            )
        )
        binding.acProvince.setOnItemClickListener { _, _, position, _ ->
            viewModel.onProvinceSelected(provinceOptions[position])
        }
        binding.acSort.setAdapter(
            ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                sortOptions.map { getString(it.labelRes()) }
            )
        )
        binding.acSort.setOnItemClickListener { _, _, position, _ ->
            viewModel.onSortOrderChange(sortOptions[position])
        }

        binding.btnClearFilters.setOnClickListener {
            binding.etSearch.setText("")
            viewModel.onClearFilters()
        }
    }

    // ----------------------------------------------------------------- Render

    private fun render(state: PlaceListUiState) {
        isRendering = true

        adapter.submitList(state.places)

        categoryChips[state.filter.category]?.isChecked = true
        binding.chipFavorites.isChecked = state.filter.onlyFavorites
        binding.acProvince.setText(
            getString(state.filter.province?.labelRes() ?: R.string.filter_all),
            false
        )
        binding.acSort.setText(getString(state.filter.sortOrder.labelRes()), false)
        binding.btnClearFilters.isVisible = state.filter.isActive

        val isEmpty = state.places.isEmpty()
        binding.emptyState.isVisible = isEmpty
        binding.rvPlaces.isVisible = !isEmpty
        binding.tvEmptyTitle.isVisible = !state.filter.isActive
        binding.tvEmptyMessage.setText(
            if (state.filter.isActive) R.string.no_results else R.string.empty_places_message
        )

        renderDeleteDialog(state)

        state.message?.let {
            it.show(this)
            viewModel.onMessageShown()
        }

        isRendering = false
    }

    private fun renderDeleteDialog(state: PlaceListUiState) {
        val pending = state.placePendingDelete
        if (pending == null) {
            deleteDialog?.dismiss()
            deleteDialog = null
        } else if (deleteDialog == null) {
            deleteDialog = DeleteConfirmDialog(
                context = this,
                placeName = pending.name,
                onConfirm = { viewModel.onDeleteConfirmed() },
                onCancel = { viewModel.onDeleteCancelled() }
            ).also { it.show() }
        }
    }
}
