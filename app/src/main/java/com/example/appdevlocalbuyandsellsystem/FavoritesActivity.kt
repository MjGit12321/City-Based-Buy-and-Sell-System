package com.example.appdevlocalbuyandsellsystem

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class FavoritesActivity : AppCompatActivity() {

    private lateinit var rvFavorites: RecyclerView
    private lateinit var emptyState: View
    private lateinit var favoriteList: MutableList<Product>
    private lateinit var adapter: FavoritesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.favoritespage)

        // Adjust for system bars
        val rootLayout = findViewById<android.view.View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        rvFavorites = findViewById(R.id.rvFavorites)
        emptyState = findViewById(R.id.emptyState)

        // Load data from the shared repository
        favoriteList = ProductRepository.getFavorites()

        setupRecyclerView()
        checkEmptyState()

        // FAB logic
        findViewById<FloatingActionButton>(R.id.fabAdd2).setOnClickListener {
            finish()
        }

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

    override fun onResume() {
        super.onResume()
        // Refresh the list whenever the user returns to this screen
        // This ensures items unfavorited on the Mainpage disappear here
        favoriteList.clear()
        favoriteList.addAll(ProductRepository.getFavorites())
        adapter.notifyDataSetChanged()
        checkEmptyState()
    }

    private fun setupRecyclerView() {
        adapter = FavoritesAdapter(favoriteList) { position ->
            // Remove from favorites logic
            val removedProduct = favoriteList[position]
            removedProduct.isFavorite = false // Update the shared state
            favoriteList.removeAt(position)
            adapter.notifyItemRemoved(position)
            adapter.notifyItemRangeChanged(position, favoriteList.size)
            checkEmptyState()
        }
        rvFavorites.layoutManager = LinearLayoutManager(this)
        rvFavorites.adapter = adapter
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