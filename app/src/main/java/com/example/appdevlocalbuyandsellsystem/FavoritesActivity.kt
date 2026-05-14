package com.example.appdevlocalbuyandsellsystem

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FavoritesActivity : AppCompatActivity() {

    private lateinit var rvFavorites: RecyclerView
    private lateinit var emptyState: View
    private var favoriteList = mutableListOf<Product>()
    private lateinit var adapter: FavoritesAdapter
    
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.favoritespage)

        // Adjust for system bars
        val rootLayout = findViewById<View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        rvFavorites = findViewById(R.id.rvFavorites)
        emptyState = findViewById(R.id.emptyState)

        setupRecyclerView()
        checkEmptyState() // Ensure initial state is set
        loadFavoritesFromFirestore()

        // Navigation logic
        findViewById<LinearLayout>(R.id.navHome).setOnClickListener {
            val intent = Intent(this, MainpageActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }

        findViewById<LinearLayout>(R.id.navMessages).setOnClickListener {
            startActivity(Intent(this, InboxActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.navMe).setOnClickListener {
            startActivity(Intent(this, MeActivity::class.java))
        }
    }

    private fun loadFavoritesFromFirestore() {
        val userId = auth.currentUser?.uid ?: return
        
        db.collection("users").document(userId).collection("favorites")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("FirestoreError", "Error fetching favorites: ${e.message}")
                    checkEmptyState()
                    return@addSnapshotListener
                }

                favoriteList.clear()
                snapshots?.forEach { doc ->
                    try {
                        // MANUAL MAPPING: Safely extract fields to avoid serialization crashes
                        val product = Product(
                            price = doc.getString("price") ?: "",
                            name = doc.getString("name") ?: "Unnamed Product",
                            description = doc.getString("description") ?: "",
                            sellerName = doc.getString("sellerName") ?: "Unknown Seller",
                            sellerId = doc.getString("sellerID") ?: doc.getString("sellerId") ?: "",
                            city = doc.getString("city") ?: "",
                            location = doc.getString("location") ?: "",
                            imageUrl = doc.getString("imageUrl") ?: "",
                            documentId = doc.getString("documentId") ?: doc.id,
                            isFavorite = true
                        )
                        favoriteList.add(product)
                    } catch (ex: Exception) {
                        Log.e("ParsingError", "Failed to parse favorite ${doc.id}: ${ex.message}")
                    }
                }
                adapter.notifyDataSetChanged()
                checkEmptyState()
            }
    }

    private fun setupRecyclerView() {
        adapter = FavoritesAdapter(
            favoriteList,
            onRemoveClick = { position ->
                removeFavoriteAt(position)
            },
            onItemClick = { product ->
                val intent = Intent(this, ProductDetailsActivity::class.java)
                intent.putExtra("PRODUCT_DATA", product)
                startActivity(intent)
            }
        )
        rvFavorites.layoutManager = LinearLayoutManager(this)
        rvFavorites.adapter = adapter
    }

    private fun removeFavoriteAt(position: Int) {
        val userId = auth.currentUser?.uid ?: return
        val product = favoriteList[position]
        val productDocId = product.documentId

        if (productDocId.isEmpty()) return

        db.collection("users").document(userId).collection("favorites").document(productDocId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Removed from Favorites", Toast.LENGTH_SHORT).show()
                // SnapshotListener handles UI update
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to remove: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkEmptyState() {
        if (favoriteList.isEmpty()) {
            rvFavorites.visibility = View.GONE
            emptyState.visibility = View.VISIBLE
        } else {
            rvFavorites.visibility = View.VISIBLE
            emptyState.visibility = View.GONE
        }
    }
}
