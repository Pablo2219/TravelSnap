package com.industriaop.travelsnap

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.industriaop.travelsnap.domain.model.Place
import com.industriaop.travelsnap.util.Util
import com.industriaop.travelsnap.viewmodel.PlaceViewModel

class MainActivity : AppCompatActivity(), OnItemClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
        findViewById<Button>(R.id.btnNewPlace).setOnClickListener {
            Util.openActivity(this, PlaceActivity::class.java)
        }
        findViewById<Button>(R.id.btnMap).setOnClickListener {
            Util.openActivity(this, MapActivity::class.java)
        }
        findViewById<Button>(R.id.btnPhotos).setOnClickListener {
            Util.openActivity(this, PhotoActivity::class.java)
        }
        findViewById<Button>(R.id.btnProfile).setOnClickListener {
            Util.openActivity(this, ProfileActivity::class.java)
        }
    }

    override fun onResume() {
        super.onResume()
        val places = PlaceViewModel.getPlaces()
        val recycler = findViewById<RecyclerView>(R.id.rvPlaces)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = PlaceListAdapter(places, this)
        findViewById<TextView>(R.id.tvEmpty).visibility =
            if (places.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onItemClick(place: Place) {
        Util.openActivity(this, PlaceDetailActivity::class.java, place.id)
    }
}
