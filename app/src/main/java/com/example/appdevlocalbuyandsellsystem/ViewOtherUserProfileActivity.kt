package com.example.appdevlocalbuyandsellsystem

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import java.io.File
// ADD FIREBASE IMPORTS
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ViewOtherUserProfileActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.view_other_user_profile)

        val rootLayout = findViewById<android.view.View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 1. GET THE SELLER ID FROM THE PREVIOUS SCREEN
        // We expect the ProductDetails page to pass the "sellerId"
        val sellerId = intent.getStringExtra("SELLER_ID") ?: ""
        val sellerName = intent.getStringExtra("SELLER_NAME") ?: "User Profile"

        // Set the Name on the UI
        findViewById<TextView>(R.id.tvOtherUserName).text = sellerName

        val rvOtherUserProducts = findViewById<RecyclerView>(R.id.rvOtherUserProducts)
        rvOtherUserProducts.layoutManager = LinearLayoutManager(this)

        // 2. FETCH REAL DATA
        if (sellerId.isNotEmpty()) {
            fetchSellerProfile(sellerId)
            fetchSellerProducts(sellerId, rvOtherUserProducts)
        }

        findViewById<FloatingActionButton>(R.id.fabMessageOtherUser).setOnClickListener {
            val currentUserId = auth.currentUser?.uid
            if (currentUserId == null) {
                Toast.makeText(this, "Please login to message", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (sellerId == currentUserId) {
                Toast.makeText(this, "You cannot message yourself!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val chatId = if (currentUserId < sellerId) "${currentUserId}_${sellerId}" else "${sellerId}_${currentUserId}"

            db.collection("users").document(currentUserId).get().addOnSuccessListener { snapshot ->
                val currentUserName = snapshot.getString("fullName") ?: "User"

                val chatData = hashMapOf(
                    "participants" to listOf(currentUserId, sellerId),
                    "names" to mapOf(
                        currentUserId to currentUserName,
                        sellerId to sellerName
                    ),
                    "lastMessage" to "Hello!",
                    "lastTimestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                    "lastTime" to SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
                )

                db.collection("chats").document(chatId)
                    .set(chatData, SetOptions.merge())
                    .addOnSuccessListener {
                        val intent = Intent(this, MessageActivity::class.java)
                        intent.putExtra("CHAT_ID", chatId)
                        intent.putExtra("USER_NAME", sellerName)
                        intent.putExtra("OTHER_USER_ID", sellerId)
                        startActivity(intent)
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to start chat", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        // Navigation Bar Logic (Stays the same)
        setupNavigation()
    }

    private fun fetchSellerProfile(sellerId: String) {
        db.collection("users").document(sellerId).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val name = doc.getString("fullName") ?: "User Profile"
                    findViewById<TextView>(R.id.tvOtherUserName).text = name
                    
                    val profileImg = doc.getString("profileImageUrl")
                    if (!profileImg.isNullOrEmpty()) {
                        val file = if (profileImg.contains("/")) File(profileImg) else File(filesDir, profileImg)
                        Glide.with(this)
                            .load(file)
                            .circleCrop()
                            .placeholder(R.drawable.ic_user)
                            .into(findViewById<ImageView>(R.id.ivOtherUserProfile))
                    } else {
                        val ivProfile = findViewById<ImageView>(R.id.ivOtherUserProfile)
                        ivProfile.setImageResource(R.drawable.ic_user)
                        // Ensure tint is applied if no image
                        ivProfile.imageTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#1F6F5F"))
                    }
                }
            }
    }

    private fun fetchSellerProducts(sellerId: String, recyclerView: RecyclerView) {
        db.collection("products")
            .whereEqualTo("sellerId", sellerId) // Filter only this seller's items
            .get()
            .addOnSuccessListener { documents ->
                val productList = mutableListOf<Product>()
                for (document in documents) {
                    val product = document.toObject(Product::class.java)
                    product.documentId = document.id
                    productList.add(product)
                }

                recyclerView.adapter = ProductAdapter(
                    productList = productList,
                    onItemClick = { product ->
                        val intent = Intent(this, ProductDetailsActivity::class.java)
                        intent.putExtra("PRODUCT_DATA", product)
                        startActivity(intent)
                    }
                )
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupNavigation() {
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
        findViewById<LinearLayout>(R.id.navFavorites).setOnClickListener {
            startActivity(Intent(this, FavoritesActivity::class.java))
        }
    }
}