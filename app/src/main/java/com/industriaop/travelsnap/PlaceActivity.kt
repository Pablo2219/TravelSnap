package com.industriaop.travelsnap

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import com.industriaop.travelsnap.domain.model.Place
import com.industriaop.travelsnap.domain.model.PlaceCategory
import com.industriaop.travelsnap.domain.model.Province
import com.industriaop.travelsnap.util.EXTRA_PLACE_ID
import com.industriaop.travelsnap.util.Util
import com.industriaop.travelsnap.viewmodel.PlaceViewModel

class PlaceActivity : AppCompatActivity() {
    private lateinit var txtId: EditText
    private lateinit var txtName: EditText
    private lateinit var txtDescription: EditText
    private lateinit var txtLatitude: EditText
    private lateinit var txtLongitude: EditText
    private lateinit var spCategory: Spinner
    private lateinit var spProvince: Spinner
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_place)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
        txtId = findViewById(R.id.txtId)
        txtName = findViewById(R.id.txtName)
        txtDescription = findViewById(R.id.txtDescription)
        txtLatitude = findViewById(R.id.txtLatitude)
        txtLongitude = findViewById(R.id.txtLongitude)
        spCategory = findViewById(R.id.spCategory)
        spProvince = findViewById(R.id.spProvince)
        val categoryAdapter = ArrayAdapter.createFromResource(this, R.array.categories,
            android.R.layout.simple_spinner_item)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spCategory.adapter = categoryAdapter
        val provinceAdapter = ArrayAdapter.createFromResource(this, R.array.provinces,
            android.R.layout.simple_spinner_item)
        provinceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spProvince.adapter = provinceAdapter
        findViewById<Button>(R.id.btnSearch).setOnClickListener {
            searchPlace(txtId.text.toString().trim())
        }
        val id = intent.getStringExtra(EXTRA_PLACE_ID)
        if (!id.isNullOrBlank()) searchPlace(id)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.crud_actions, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.mnuDelete).isVisible = isEditMode
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mnuSave -> {
                if (isEditMode) {
                    Util.showDialogCondition(this, getString(R.string.update_question)) { savePlace() }
                } else savePlace()
            }
            R.id.mnuDelete -> {
                Util.showDialogCondition(this, getString(R.string.delete_question)) {
                    PlaceViewModel.removePlace(txtId.text.toString())
                    cleanScreen()
                    Toast.makeText(this, R.string.deleted, Toast.LENGTH_SHORT).show()
                }
            }
            R.id.mnuCancel -> cleanScreen()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun searchPlace(id: String) {
        val place = PlaceViewModel.getPlaceById(id)
        if (place == null) {
            Toast.makeText(this, R.string.not_found, Toast.LENGTH_SHORT).show()
            return
        }
        isEditMode = true
        txtId.setText(place.id)
        txtId.isEnabled = false
        txtName.setText(place.name)
        txtDescription.setText(place.description)
        txtLatitude.setText(place.latitude.toString())
        txtLongitude.setText(place.longitude.toString())
        spCategory.setSelection(place.category.ordinal)
        spProvince.setSelection(place.province.ordinal)
        invalidateOptionsMenu()
    }

    private fun savePlace() {
        val id = txtId.text.toString().trim()
        val name = txtName.text.toString().trim()
        val description = txtDescription.text.toString().trim()
        if (id.isBlank() || name.isBlank() || description.isBlank()) {
            Toast.makeText(this, R.string.incomplete_data, Toast.LENGTH_SHORT).show()
            return
        }
        val latitude = txtLatitude.text.toString().toDoubleOrNull()
        val longitude = txtLongitude.text.toString().toDoubleOrNull()
        if (latitude == null || longitude == null ||
            !latitude.isFinite() || !longitude.isFinite() ||
            latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
            Toast.makeText(this, R.string.invalid_coordinates, Toast.LENGTH_SHORT).show()
            return
        }
        if (!isEditMode && PlaceViewModel.getPlaceById(id) != null) {
            Toast.makeText(this, R.string.duplicate_id, Toast.LENGTH_SHORT).show()
            return
        }
        val place = Place(id, name, description, PlaceCategory.entries[spCategory.selectedItemPosition],
            Province.entries[spProvince.selectedItemPosition], latitude, longitude)
        if (isEditMode) PlaceViewModel.updatePlace(place) else PlaceViewModel.savePlace(place)
        cleanScreen()
        Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show()
    }

    private fun cleanScreen() {
        isEditMode = false
        txtId.isEnabled = true
        txtId.setText("")
        txtName.setText("")
        txtDescription.setText("")
        txtLatitude.setText("")
        txtLongitude.setText("")
        spCategory.setSelection(0)
        spProvince.setSelection(0)
        invalidateOptionsMenu()
    }
}
