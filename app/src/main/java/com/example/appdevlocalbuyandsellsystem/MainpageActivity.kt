package com.example.appdevlocalbuyandsellsystem

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainpageActivity : AppCompatActivity() {

    private lateinit var spinnerProvince: Spinner
    private lateinit var spinnerCity: Spinner
    private lateinit var spinnerBarangay: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.mainpage)

        // Initialize UI Components
        spinnerProvince = findViewById(R.id.spinnerProvince)
        spinnerCity = findViewById(R.id.spinnerCity)
        spinnerBarangay = findViewById(R.id.spinnerBarangay)

        setupHierarchicalFilters()

        val mainView = findViewById<android.view.View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize RecyclerView with sample products
        val rvProducts = findViewById<RecyclerView>(R.id.rvProducts)
        
        rvProducts.layoutManager = LinearLayoutManager(this)
        rvProducts.adapter = ProductAdapter(ProductRepository.products)

        // Bottom Navigation Logic
        findViewById<LinearLayout>(R.id.navMessages).setOnClickListener {
            startActivity(Intent(this, InboxActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.navFavorites).setOnClickListener {
            startActivity(Intent(this, FavoritesActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.navMe).setOnClickListener {
            startActivity(Intent(this, MeActivity::class.java))
        }
    }

    private fun setupHierarchicalFilters() {
        val provinces = listOf("", "Bukidnon", "Camiguin", "Misamis Occidental", "Misamis Oriental", "Lanao del Norte")
        
        val provinceAdapter = ArrayAdapter(this, R.layout.spinner_item, provinces)
        provinceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerProvince.adapter = provinceAdapter

        spinnerProvince.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedProvince = provinces[position]
                if (selectedProvince.isNotEmpty()) {
                    updateCityList(selectedProvince)
                } else {
                    spinnerCity.adapter = ArrayAdapter(this@MainpageActivity, R.layout.spinner_item, listOf(""))
                    spinnerBarangay.adapter = ArrayAdapter(this@MainpageActivity, R.layout.spinner_item, listOf(""))
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun updateCityList(province: String) {
        val cities = when (province) {
            "Bukidnon" -> listOf("", "Malaybalay City", "Valencia City", "Maramag")
            "Camiguin" -> listOf("", "Mambajao", "Mahinog", "Guinsiliban")
            "Misamis Occidental" -> listOf("", "Ozamiz City", "Oroquieta City", "Tangub City")
            "Misamis Oriental" -> listOf("", "Cagayan de Oro", "Gingoog City", "El Salvador")
            "Lanao del Norte" -> listOf("", "Iligan City", "Tubod", "Baroy")
            else -> listOf("")
        }

        val cityAdapter = ArrayAdapter(this, R.layout.spinner_item, cities)
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCity.adapter = cityAdapter

        spinnerCity.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedCity = cities[position]
                if (selectedCity.isNotEmpty()) {
                    updateBarangayList(selectedCity)
                } else {
                    spinnerBarangay.adapter = ArrayAdapter(this@MainpageActivity, R.layout.spinner_item, listOf(""))
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun updateBarangayList(city: String) {
        val barangays = listOf("", "$city Brgy 1", "$city Brgy 2", "$city Brgy 3")
        val barangayAdapter = ArrayAdapter(this, R.layout.spinner_item, barangays)
        barangayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerBarangay.adapter = barangayAdapter
    }
}