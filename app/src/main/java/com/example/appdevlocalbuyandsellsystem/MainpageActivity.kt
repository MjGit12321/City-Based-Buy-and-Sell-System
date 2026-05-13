package com.example.appdevlocalbuyandsellsystem

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import android.text.TextWatcher

class MainpageActivity : AppCompatActivity() {

    private lateinit var spinnerProvince: Spinner
    private lateinit var spinnerCity: Spinner
    private lateinit var spinnerBarangay: Spinner
    private lateinit var rvProducts: RecyclerView
    private lateinit var etSearch: EditText

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private var favoritesSet = mutableSetOf<String>()
    private var allProducts = mutableListOf<Product>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.mainpage)

        spinnerProvince = findViewById(R.id.spinnerProvince)
        spinnerCity = findViewById(R.id.spinnerCity)
        spinnerBarangay = findViewById(R.id.spinnerBarangay)
        rvProducts = findViewById(R.id.rvProducts)
        etSearch = findViewById(R.id.searchBar)

        rvProducts.layoutManager = LinearLayoutManager(this)

        setupHierarchicalFilters()
        
        // 1. Fetch user's favorite IDs first, then fetch products
        fetchUserFavorites {
            fetchProductsFromFirebase()
        }

        // 2. Setup Search Listener
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterProducts(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        val mainView = findViewById<View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

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

    private fun fetchUserFavorites(onComplete: () -> Unit) {
        val userId = auth.currentUser?.uid ?: return onComplete()
        
        db.collection("users").document(userId).collection("favorites")
            .get()
            .addOnSuccessListener { snapshots ->
                favoritesSet.clear()
                for (doc in snapshots) {
                    favoritesSet.add(doc.id)
                }
                onComplete()
            }
            .addOnFailureListener {
                onComplete()
            }
    }

    private fun fetchProductsFromFirebase(cityFilter: String? = null) {
        var query: Query = db.collection("products")

        if (!cityFilter.isNullOrEmpty()) {
            query = query.whereEqualTo("city", cityFilter)
        }

        query.addSnapshotListener { snapshots, e ->
            if (e != null) {
                Log.e("FirestoreError", "Query failed: ${e.message}")
                return@addSnapshotListener
            }

            allProducts.clear()
            snapshots?.forEach { doc ->
                try {
                    val product = Product(
                        price = doc.getString("price") ?: "",
                        name = doc.getString("name") ?: "Unnamed Product",
                        description = doc.getString("description") ?: "",
                        sellerName = doc.getString("sellerName") ?: "Unknown Seller",
                        sellerID = doc.getString("sellerID") ?: doc.getString("sellerId") ?: "",
                        baranggay = doc.getString("baranggay") ?: doc.getString("brgy") ?: "",
                        city = doc.getString("city") ?: "",
                        province = doc.getString("province") ?: doc.getString("prov") ?: "",
                        location = doc.getString("location") ?: "",
                        documentId = doc.id,
                        isFavorite = favoritesSet.contains(doc.id)
                    )
                    allProducts.add(product)
                } catch (ex: Exception) {
                    Log.e("ParsingError", "Failed to parse product ${doc.id}: ${ex.message}")
                }
            }
            
            // Initial render
            filterProducts(etSearch.text.toString())
        }
    }

    private fun filterProducts(query: String) {
        val filtered = if (query.isEmpty()) {
            allProducts
        } else {
            allProducts.filter { it.name.contains(query, ignoreCase = true) }
        }

        rvProducts.adapter = ProductAdapter(
            filtered,
            onFavoriteClick = { product -> toggleFavorite(product) },
            onItemClick = { product ->
                val intent = Intent(this, ProductDetailsActivity::class.java)
                intent.putExtra("PRODUCT_DATA", product)
                startActivity(intent)
            }
        )
    }

    private fun toggleFavorite(product: Product) {
        val userId = auth.currentUser?.uid ?: return
        val productDocId = product.documentId
        
        if (productDocId.isEmpty()) return

        val favoriteRef = db.collection("users").document(userId).collection("favorites").document(productDocId)

        if (product.isFavorite) {
            val favData = hashMapOf(
                "price" to product.price,
                "name" to product.name,
                "description" to product.description,
                "sellerName" to product.sellerName,
                "sellerID" to product.getSafeSellerId(),
                "city" to product.city,
                "location" to product.location,
                "documentId" to productDocId,
                "isFavorite" to true
            )
            
            favoriteRef.set(favData)
                .addOnSuccessListener {
                    favoritesSet.add(productDocId)
                    Toast.makeText(this, "Added to Favorites", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.e("FavoriteError", "Failed to save: ${e.message}")
                }
        } else {
            favoriteRef.delete()
                .addOnSuccessListener {
                    favoritesSet.remove(productDocId)
                    Toast.makeText(this, "Removed from Favorites", Toast.LENGTH_SHORT).show()
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
