package com.industriaop.travelsnap

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import android.widget.TextView
import com.industriaop.travelsnap.util.EXTRA_PLACE_ID
import com.industriaop.travelsnap.util.Util
import com.industriaop.travelsnap.viewmodel.PlaceViewModel

class PlaceDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_place_detail)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
        findViewById<Button>(R.id.btnEdit).setOnClickListener {
            Util.openActivity(this, PlaceActivity::class.java,
                intent.getStringExtra(EXTRA_PLACE_ID) ?: "")
        }
        findViewById<Button>(R.id.btnBack).setOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()
        val place = PlaceViewModel.getPlaceById(intent.getStringExtra(EXTRA_PLACE_ID) ?: "")
        if (place == null) {
            finish()
            return
        }
        findViewById<TextView>(R.id.tvName).text = place.name
        findViewById<TextView>(R.id.tvDescription).text = place.description
        findViewById<TextView>(R.id.tvCategory).text = resources.getStringArray(R.array.categories)[place.category.ordinal]
        findViewById<TextView>(R.id.tvProvince).text = resources.getStringArray(R.array.provinces)[place.province.ordinal]
        findViewById<TextView>(R.id.tvCoordinates).text =
            getString(R.string.coordinates, place.latitude.toString(), place.longitude.toString())
    }
}
