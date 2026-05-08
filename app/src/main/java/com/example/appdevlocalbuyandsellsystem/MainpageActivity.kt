package com.example.appdevlocalbuyandsellsystem

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
// 1. ADD FIREBASE IMPORTS
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MainpageActivity : AppCompatActivity() {

    private lateinit var spinnerProvince: Spinner
    private lateinit var spinnerCity: Spinner
    private lateinit var spinnerBarangay: Spinner
    private lateinit var rvProducts: RecyclerView

    // 2. INITIALIZE FIRESTORE
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.mainpage)

        spinnerProvince = findViewById(R.id.spinnerProvince)
        spinnerCity = findViewById(R.id.spinnerCity)
        spinnerBarangay = findViewById(R.id.spinnerBarangay)
        rvProducts = findViewById(R.id.rvProducts)

        rvProducts.layoutManager = LinearLayoutManager(this)

        setupHierarchicalFilters()

        // 3. INITIAL FETCH (Show all products by default)
        fetchProductsFromFirebase()

        val mainView = findViewById<android.view.View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Navigation listeners stay the same...
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

    // 4. THE FIREBASE FETCH FUNCTION
    private fun fetchProductsFromFirebase(cityFilter: String? = null) {
        // 1. SIMPLIFY: Remove .orderBy for now to prevent the crash
        var query: Query = db.collection("products")

        if (!cityFilter.isNullOrEmpty()) {
            query = query.whereEqualTo("city", cityFilter)
        }

        query.addSnapshotListener { snapshots, e ->
            if (e != null) {
                // This is where the error log is sent
                android.util.Log.e("FirestoreError", "Query failed: ${e.message}")
                return@addSnapshotListener
            }

            val productList = mutableListOf<Product>()
            if (snapshots != null) {
                for (doc in snapshots) {
                    val product = doc.toObject(Product::class.java)
                    productList.add(product)
                }
            }

            rvProducts.adapter = ProductAdapter(productList) { product ->
                val intent = Intent(this, ProductDetailsActivity::class.java)
                intent.putExtra("PRODUCT_DATA", product)
                startActivity(intent)
            }
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
                    // Reset to show all if no province selected
                    fetchProductsFromFirebase()
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
                    // 5. FILTER PRODUCTS BY THE SELECTED CITY
                    fetchProductsFromFirebase(selectedCity)
                    updateBarangayList(selectedCity)
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