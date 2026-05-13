package com.example.appdevlocalbuyandsellsystem

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.*

class ProductDetailsActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.product_details)

        val rootLayout = findViewById<View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Get Product Data safely
        val product = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("PRODUCT_DATA", Product::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("PRODUCT_DATA") as? Product
        }

        product?.let {
            findViewById<TextView>(R.id.tvProductDetailTitle).text = it.name
            findViewById<TextView>(R.id.tvProductDetailPrice).text = "₱${it.price}"
            findViewById<TextView>(R.id.tvProductDetailDesc).text = it.description
            findViewById<TextView>(R.id.tvSellerName).text = it.sellerName
            
            val locationText = if (it.location.isNotBlank()) it.location else "${it.baranggay} ${it.city} ${it.province}"
            findViewById<TextView>(R.id.tvProductLocation).text = "Location: $locationText"
        }

        findViewById<FloatingActionButton>(R.id.fabAddProduct).setOnClickListener {
            Toast.makeText(this, "Product added to wishlist!", Toast.LENGTH_SHORT).show()
        }

        // Messaging Logic
        findViewById<FloatingActionButton>(R.id.fabMessageSeller).setOnClickListener {
            val currentUserId = auth.currentUser?.uid
            val sellerId = product?.getSafeSellerId()
            val sellerName = product?.sellerName ?: "Seller"

            if (currentUserId == null) {
                Toast.makeText(this, "Please login to message the seller", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (sellerId.isNullOrEmpty()) {
                Toast.makeText(this, "Seller ID missing for this item", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (currentUserId == sellerId) {
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
                    "lastMessage" to "Interested in ${product.name}",
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

        findViewById<View>(R.id.sellerSection).setOnClickListener {
            val intent = Intent(this, ViewOtherUserProfileActivity::class.java)
            intent.putExtra("SELLER_ID", product?.getSafeSellerId())
            intent.putExtra("SELLER_NAME", product?.sellerName)
            startActivity(intent)
        }

        setupNavigation()
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
